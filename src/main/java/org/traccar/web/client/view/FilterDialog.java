/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.shared.model.UserSettings;

public class FilterDialog implements Editor<UserSettings> {

    private static FilterDialogDialogUiBinder uiBinder = GWT.create(FilterDialogDialogUiBinder.class);

    interface FilterDialogDialogUiBinder extends UiBinder<Widget, FilterDialog> {
    }

    private FilterSettingsDriver driver = GWT.create(FilterSettingsDriver.class);

    interface FilterSettingsDriver extends SimpleBeanEditorDriver<UserSettings, FilterDialog> {
    }

    private UserSettingsDialog.UserSettingsHandler userSettingsHandler;

    @UiField
    Window window;

    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();

    @UiField
    CheckBox hideZeroCoordinates;

    @UiField
    CheckBox hideInvalidLocations;

    @UiField
    CheckBox hideDuplicates;

    @UiField
    NumberField<Double> minDistance;

    @UiField
    @Ignore
    Label distanceUnits;

    @UiField(provided = true)
    SimpleComboBox<String> speedModifier;

    @UiField
    NumberField<Double> speedForFilter;

    @UiField
    @Ignore
    Label speedUnits;

    public FilterDialog(UserSettings filterSettings, UserSettingsDialog.UserSettingsHandler userSettingsHandler) {
        this.userSettingsHandler = userSettingsHandler;

        speedModifier = new SimpleComboBox<String>(new StringLabelProvider<String>());
        speedModifier.add("<");
        speedModifier.add("<=");
        speedModifier.add("=");
        speedModifier.add(">=");
        speedModifier.add(">");
        speedModifier.setValue(">=");

        uiBinder.createAndBindUi(this);

        speedUnits.setText(ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getUnit());
        distanceUnits.setText(ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getDistanceUnit().getUnit());

        speedForFilter.addValidator(new MinNumberValidator<Double>(0d));
        speedForFilter.addValidator(new MaxNumberValidator<Double>(30000d));

        minDistance.addValidator(new MinNumberValidator<Double>(0d));
        minDistance.addValidator(new MaxNumberValidator<Double>(30000d));

        driver.initialize(this);
        driver.edit(filterSettings);

        if (filterSettings.getMinDistance() != null) {
            minDistance.setValue(filterSettings.getMinDistance() * filterSettings.getSpeedUnit().getDistanceUnit().getFactor());
        }
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
        UserSettings filterSettings = driver.flush();
        if (filterSettings.getMinDistance() != null) {
            filterSettings.setMinDistance(filterSettings.getMinDistance() * filterSettings.getSpeedUnit().getDistanceUnit().getFactor());
        }
        userSettingsHandler.onSave(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

}
