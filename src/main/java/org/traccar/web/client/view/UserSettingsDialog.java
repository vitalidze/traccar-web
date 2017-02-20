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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.i18n.client.TimeZoneInfo;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ToStringValueProvider;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.grid.*;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.UserSettingsProperties;
import org.traccar.web.client.widget.TimeZoneComboBox;
import org.traccar.web.shared.model.UserSettings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;

public class UserSettingsDialog implements Editor<UserSettings> {

    private static UserSettingsDialogUiBinder uiBinder = GWT.create(UserSettingsDialogUiBinder.class);

    interface UserSettingsDialogUiBinder extends UiBinder<Widget, UserSettingsDialog> {
    }

    private UserSettingsDriver driver = GWT.create(UserSettingsDriver.class);

    interface UserSettingsDriver extends SimpleBeanEditorDriver<UserSettings, UserSettingsDialog> {
    }

    public interface UserSettingsHandler {
        void onSave(UserSettings userSettings);
        void onTakeCurrentMapState(ComboBox<UserSettings.MapType> mapType,
                                          NumberField<Double> centerLongitude,
                                          NumberField<Double> centerLatitude,
                                          NumberField<Integer> zoomLevel,
                                          CheckBox maximizeOverviewMap,
                                          GridSelectionModel<UserSettings.OverlayType> overlays);
        void onSetZoomLevelToCurrent(NumberField<Short> followedDeviceZoomLevel);
    }

    private UserSettingsHandler userSettingsHandler;

    @UiField
    Window window;

    @UiField(provided = true)
    ComboBox<UserSettings.SpeedUnit> speedUnit;

    @Ignore
    @UiField(provided = true)
    ComboBox<TimeZoneInfo> timeZone;

    @UiField
    NumberField<Short> timePrintInterval;

    @UiField
    NumberField<Short> traceInterval;

    @UiField
    NumberField<Short> followedDeviceZoomLevel;

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

    @UiField
    CheckBox maximizeOverviewMap;

    @UiField(provided = true)
    ComboBox<UserSettings.MapType> mapType;

    @UiField
    Grid<UserSettings.OverlayType> grid;

    @UiField(provided = true)
    GridView<UserSettings.OverlayType> view;

    @UiField(provided = true)
    ColumnModel<UserSettings.OverlayType> columnModel;

    @UiField(provided = true)
    ListStore<UserSettings.OverlayType> overlayTypeStore;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public UserSettingsDialog(UserSettings userSettings, UserSettingsHandler userSettingsHandler) {
        this.userSettingsHandler = userSettingsHandler;

        ListStore<UserSettings.SpeedUnit> speedUnitStore = new ListStore<>(
                new EnumKeyProvider<UserSettings.SpeedUnit>());
        speedUnitStore.addAll(Arrays.asList(UserSettings.SpeedUnit.values()));

        speedUnit = new ComboBox<>(speedUnitStore, new UserSettingsProperties.SpeedUnitLabelProvider());
        speedUnit.setForceSelection(true);
        speedUnit.setTriggerAction(TriggerAction.ALL);

        ListStore<UserSettings.MapType> mapTypeStore = new ListStore<>(new EnumKeyProvider<UserSettings.MapType>());
        mapTypeStore.addAll(Arrays.asList(UserSettings.MapType.values()));
        mapType = new ComboBox<>(mapTypeStore, new UserSettingsProperties.MapTypeLabelProvider());

        mapType.setForceSelection(true);
        mapType.setTriggerAction(TriggerAction.ALL);

        // overlay types grid
        IdentityValueProvider<UserSettings.OverlayType> identity = new IdentityValueProvider<>();
        final CheckBoxSelectionModel<UserSettings.OverlayType> selectionModel = new CheckBoxSelectionModel<>(identity);

        ColumnConfig<UserSettings.OverlayType, String> nameCol = new ColumnConfig<>(new ToStringValueProvider<UserSettings.OverlayType>() {
            @Override
            public String getValue(UserSettings.OverlayType object) {
                return i18n.overlayType(object);
            }
        }, 200, i18n.overlay());
        List<ColumnConfig<UserSettings.OverlayType, ?>> columns = new ArrayList<>();
        columns.add(selectionModel.getColumn());
        columns.add(nameCol);

        columnModel = new ColumnModel<>(columns);

        view = new NoScrollbarGridView<>();
        view.setAutoFill(true);
        view.setStripeRows(true);

        overlayTypeStore = new ListStore<>(new EnumKeyProvider<UserSettings.OverlayType>());
        overlayTypeStore.addAll(Arrays.asList(UserSettings.OverlayType.values()));

        timeZone = new TimeZoneComboBox();

        uiBinder.createAndBindUi(this);

        grid.setSelectionModel(selectionModel);
        grid.getView().setForceFit(true);
        grid.getView().setAutoFill(true);

        for (UserSettings.OverlayType overlayType : ApplicationContext.getInstance().getUserSettings().overlays()) {
            grid.getSelectionModel().select(overlayType, true);
        }

        timePrintInterval.addValidator(new MinNumberValidator<>((short) 1));
        timePrintInterval.addValidator(new MaxNumberValidator<>((short) 512));

        traceInterval.addValidator(new MinNumberValidator<>((short) 1));
        traceInterval.addValidator(new MaxNumberValidator<>((short) (60 * 48)));

        followedDeviceZoomLevel.addValidator(new MinNumberValidator<>((short) 1));
        followedDeviceZoomLevel.addValidator(new MaxNumberValidator<>((short) 16));

        driver.initialize(this);
        driver.edit(userSettings);
        timeZone.setValue(TimeZoneComboBox.getByID(userSettings.getTimeZoneId()));
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        window.hide();
        UserSettings settings = driver.flush();
        String overlayTypes = "";
        for (UserSettings.OverlayType overlayType : grid.getSelectionModel().getSelectedItems()) {
            overlayTypes += (overlayTypes.isEmpty() ? "" : ",") + overlayType.name();
        }
        settings.setOverlays(overlayTypes);
        settings.setTimeZoneId(timeZone.getValue() == null ? null : timeZone.getValue().getID());
        userSettingsHandler.onSave(settings);
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    @UiHandler("takeFromMapButton")
    public void onSaveDefaultMapSateClicked(SelectEvent event) {
        userSettingsHandler.onTakeCurrentMapState(mapType, centerLongitude, centerLatitude, zoomLevel, maximizeOverviewMap, grid.getSelectionModel());
    }

    @UiHandler("setZoomLevelToCurrentButton")
    public void onSetZoomLevelToCurrentClicked(SelectEvent event) {
        userSettingsHandler.onSetZoomLevelToCurrent(followedDeviceZoomLevel);
    }
}
