/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.client.view;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.Window;

import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.UserSettingsProperties;
import org.traccar.web.shared.model.UserSettings;

public class UserSettingsDialog implements Editor<UserSettings> {

    private static final UserSettingsDialogUiBinder uiBinder = GWT.create(UserSettingsDialogUiBinder.class);

    interface UserSettingsDialogUiBinder extends UiBinder<Widget, UserSettingsDialog> {
    }

    private final UserSettingsDriver driver = GWT.create(UserSettingsDriver.class);

    interface UserSettingsDriver extends SimpleBeanEditorDriver<UserSettings, UserSettingsDialog> {
    }

    public interface UserSettingsHandler {
        public void onSave(UserSettings userSettings);
        public void onTakeCurrentMapState(ComboBox<UserSettings.MapType> mapType,
                                          NumberField<Double> centerLongitude,
                                          NumberField<Double> centerLatitude,
                                          NumberField<Integer> zoomLevel);
    }

    private UserSettingsHandler userSettingsHandler;

    @UiField
    Window window;

    @UiField(provided = true)
    ComboBox<UserSettings.SpeedUnit> speedUnit;

    @UiField
    NumberField<Short> timePrintInterval;

    @UiField(provided = true)
    CheckBox showInvalidPositions;

    @UiField(provided = true)
    NumberPropertyEditor<Short> shortPropertyEditor = new NumberPropertyEditor.ShortPropertyEditor();

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();

    @UiField
    NumberField<Double> centerLongitude;

    @UiField
    NumberField<Double> centerLatitude;

    @UiField
    NumberField<Integer> zoomLevel;

    @UiField(provided = true)
    ComboBox<UserSettings.MapType> mapType;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public UserSettingsDialog(UserSettings userSettings, UserSettingsHandler userSettingsHandler) {
        this.userSettingsHandler = userSettingsHandler;

        ListStore<UserSettings.SpeedUnit> speedUnitStore = new ListStore<UserSettings.SpeedUnit>(
                new EnumKeyProvider<UserSettings.SpeedUnit>());
        speedUnitStore.addAll(Arrays.asList(UserSettings.SpeedUnit.values()));

        speedUnit = new ComboBox<UserSettings.SpeedUnit>(
                speedUnitStore, new UserSettingsProperties.SpeedUnitLabelProvider());
        speedUnit.setForceSelection(true);
        speedUnit.setTriggerAction(TriggerAction.ALL);
 
        showInvalidPositions = new CheckBox();
        showInvalidPositions.setValue(userSettings.getShowInvalidPositions());

        ListStore<UserSettings.MapType> mapTypeStore = new ListStore<UserSettings.MapType>(
                new EnumKeyProvider<UserSettings.MapType>());
        mapTypeStore.addAll(Arrays.asList(UserSettings.MapType.values()));
        mapType = new ComboBox<UserSettings.MapType>(
                mapTypeStore, new UserSettingsProperties.MapTypeLabelProvider());

        mapType.setForceSelection(true);
        mapType.setTriggerAction(TriggerAction.ALL);

        uiBinder.createAndBindUi(this);

        timePrintInterval.addValidator(new MinNumberValidator<Short>(Short.valueOf((short) 1)));
        timePrintInterval.addValidator(new MaxNumberValidator<Short>(Short.valueOf((short) 512)));

        driver.initialize(this);
        driver.edit(userSettings);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onLoginClicked(SelectEvent event) {
        window.hide();
        userSettingsHandler.onSave(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onRegisterClicked(SelectEvent event) {
        window.hide();
    }

    @UiHandler("takeFromMapButton")
    public void onSaveDefaultMapSateClicked(SelectEvent event) {
        userSettingsHandler.onTakeCurrentMapState(mapType, centerLongitude, centerLatitude, zoomLevel);
    }
}
