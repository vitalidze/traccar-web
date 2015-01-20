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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.shared.model.Device;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.traccar.web.shared.model.DeviceIconType;
import org.traccar.web.shared.model.Position;

public class DeviceDialog implements Editor<Device> {

    private static final DeviceDialogUiBinder uiBinder = GWT.create(DeviceDialogUiBinder.class);

    interface DeviceDialogUiBinder extends UiBinder<Widget, DeviceDialog> {
    }

    private final DeviceDriver driver = GWT.create(DeviceDriver.class);

    interface DeviceDriver extends SimpleBeanEditorDriver<Device, DeviceDialog> {
    }

    public interface DeviceHandler {
        public void onSave(Device device);
    }

    private final DeviceHandler deviceHandler;

    @UiField
    Window window;

    @UiField
    TextField name;

    @UiField
    TextField uniqueId;

    @UiField
    VerticalLayoutContainer devicePictures;

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField
    NumberField<Integer> timeout;

    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();

    @UiField
    NumberField<Double> idleSpeedThreshold;

    ToggleGroup iconRadioGroup = new ToggleGroup();

    public DeviceDialog(Device device, DeviceHandler deviceHandler) {
        this.deviceHandler = deviceHandler;
        uiBinder.createAndBindUi(this);

        timeout.addValidator(new MinNumberValidator<Integer>(1));
        timeout.addValidator(new MaxNumberValidator<Integer>(7 * 24 * 60 * 60));

        driver.initialize(this);
        driver.edit(device);

        idleSpeedThreshold.setValue(device.getIdleSpeedThreshold() * ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getFactor());


        HorizontalPanel nextPanel = null;
        DeviceIconType[] deviceIconTypes = DeviceIconType.values();
        for (int i = 0; i < deviceIconTypes.length; i++) {
            DeviceIconType deviceIconType = deviceIconTypes[i];
            if (nextPanel == null || i % 5 == 0) {
                nextPanel = new HorizontalPanel();
                devicePictures.add(nextPanel, new VerticalLayoutContainer.VerticalLayoutData(-1, -1, new Margins(5, 0, 5, 5)));
            }

            Radio radio = new Radio();
            radio.setBoxLabel("<img src=\"" + deviceIconType.getPositionIconType(Position.Status.OFFLINE).getURL(false) + "\">");
            nextPanel.add(radio);
            iconRadioGroup.add(radio);
            radio.setValue(deviceIconType == device.getIconType());
            radio.setId(deviceIconType.name());
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
        Device device = driver.flush();
        device.setIdleSpeedThreshold(ApplicationContext.getInstance().getUserSettings().getSpeedUnit().toKnots(device.getIdleSpeedThreshold()));
        if (iconRadioGroup.getValue() != null) {
            device.setIconType(DeviceIconType.valueOf(((Radio) iconRadioGroup.getValue()).getId()));
        }
        deviceHandler.onSave(device);
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

}
