/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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

import com.google.gwt.event.logical.shared.*;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.model.ApplicationSettingsProperties;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.widget.LanguageComboBox;
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

import java.util.Arrays;

public class ApplicationSettingsDialog implements Editor<ApplicationSettings> {

    private static ApplicationSettingsDialogUiBinder uiBinder = GWT.create(ApplicationSettingsDialogUiBinder.class);

    interface ApplicationSettingsDialogUiBinder extends UiBinder<Widget, ApplicationSettingsDialog> {
    }

    private ApplicationSettingsDriver driver = GWT.create(ApplicationSettingsDriver.class);

    interface ApplicationSettingsDriver extends SimpleBeanEditorDriver<ApplicationSettings, ApplicationSettingsDialog> {
    }

    public interface ApplicationSettingsHandler {
        void onSave(ApplicationSettings applicationSettings);
    }

    private ApplicationSettingsHandler applicationSettingsHandler;

    @UiField
    Window window;

    @UiField
    CheckBox registrationEnabled;

    @UiField
    CheckBox disallowDeviceManagementByUsers;

    @UiField
    CheckBox allowCommandsOnlyForAdmins;

    @UiField
    CheckBox eventRecordingEnabled;

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField
    NumberField<Integer> notificationExpirationPeriod;

    @UiField(provided = true)
    NumberPropertyEditor<Short> shortPropertyEditor = new NumberPropertyEditor.ShortPropertyEditor();

    @UiField
    NumberField<Short> updateInterval;

    @UiField(provided = true)
    ComboBox<PasswordHashMethod> defaultHashImplementation;

    @UiField(provided = true)
    ComboBox<String> language;

    @UiField
    TextField bingMapsKey;

    @UiField(provided = true)
    ComboBox<MatchServiceType> matchServiceType;

    @UiField
    TextField matchServiceURL;

    public ApplicationSettingsDialog(ApplicationSettings applicationSettings, ApplicationSettingsHandler applicationSettingsHandler) {
        this.applicationSettingsHandler = applicationSettingsHandler;

        ListStore<PasswordHashMethod> dhmStore = new ListStore<>(
                new EnumKeyProvider<PasswordHashMethod>());
        dhmStore.addAll(Arrays.asList(PasswordHashMethod.values()));
        defaultHashImplementation = new ComboBox<>(
                dhmStore, new ApplicationSettingsProperties.PasswordHashMethodLabelProvider());

        defaultHashImplementation.setForceSelection(true);
        defaultHashImplementation.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        ListStore<MatchServiceType> mstStore = new ListStore<>(new EnumKeyProvider<MatchServiceType>());
        mstStore.addAll(Arrays.asList(MatchServiceType.values()));
        matchServiceType = new ComboBox<>(mstStore, new ApplicationSettingsProperties.MatchServiceTypeLabelProvider());
        matchServiceType.setForceSelection(true);
        matchServiceType.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        matchServiceType.addSelectionHandler(new SelectionHandler<MatchServiceType>() {
            @Override
            public void onSelection(SelectionEvent<MatchServiceType> event) {
                matchServiceURL.setValue(event.getSelectedItem().getDefaultURL());
            }
        });

        language = new LanguageComboBox();

        uiBinder.createAndBindUi(this);

        updateInterval.addValidator(new MinNumberValidator<>((short) 100));
        updateInterval.addValidator(new MaxNumberValidator<>((short) 30000));

        driver.initialize(this);
        driver.edit(applicationSettings);
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
        applicationSettingsHandler.onSave(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onRegisterClicked(SelectEvent event) {
        window.hide();
    }

}
