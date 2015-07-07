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

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.RegExValidator;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
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
import com.sencha.gxt.widget.core.client.form.TextField;

public class DeviceDialog implements Editor<Device> {

    private static DeviceDialogUiBinder uiBinder = GWT.create(DeviceDialogUiBinder.class);

    interface DeviceDialogUiBinder extends UiBinder<Widget, DeviceDialog> {
    }

    private DeviceDriver driver = GWT.create(DeviceDriver.class);

    interface DeviceDriver extends SimpleBeanEditorDriver<Device, DeviceDialog> {
    }

    public interface DeviceHandler {
        void onSave(Device device);
    }

    private DeviceHandler deviceHandler;

    @UiField
    Window window;

    @UiField
    TextField name;

    @UiField
    TextField uniqueId;

    @UiField
    TextField description;

    @UiField
    TextField phoneNumber;

    @UiField
    TextField plateNumber;

    @UiField
    TextField vehicleInfo;

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField
    NumberField<Integer> timeout;

    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();

    @UiField
    NumberField<Double> idleSpeedThreshold;

    @UiField
    FieldLabel labelMarkers;

    @UiField
    HorizontalLayoutContainer panelMarkers;

    @UiField
    Image markerImage;

    @UiField
    ScrollPanel panelPhoto;

    @UiField
    Image photo;

    @UiField
    VerticalLayoutContainer sensorsTab;
    final SensorsEditor sensorsEditor;

    @UiField
    VerticalLayoutContainer maintenanceTab;
    final MaintenanceEditor maintenanceEditor;

    MarkerIcon selectedIcon;

    @UiField
    Messages i18n;

    final Device device;

    public DeviceDialog(Device device, ListStore<Device> deviceStore, DeviceHandler deviceHandler) {
        this.device = device;
        this.deviceHandler = deviceHandler;
        this.selectedIcon = MarkerIcon.create(device);

        uiBinder.createAndBindUi(this);

        timeout.addValidator(new MinNumberValidator<Integer>(1));
        timeout.addValidator(new MaxNumberValidator<Integer>(7 * 24 * 60 * 60));

        driver.initialize(this);
        driver.edit(device);

        idleSpeedThreshold.setValue(device.getIdleSpeedThreshold() * ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getFactor());
        updateIcon();

        updatePhoto();

        sensorsEditor = new SensorsEditor(device, deviceStore);
        sensorsTab.add(sensorsEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        maintenanceEditor = new MaintenanceEditor(device, deviceStore);
        maintenanceTab.add(maintenanceEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        labelMarkers.setText(i18n.overlayType(UserSettings.OverlayType.MARKERS));
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
        device.setIconType(selectedIcon.getBuiltInIcon());
        device.setIcon(selectedIcon.getDatabaseIcon());
        maintenanceEditor.flush();
        sensorsEditor.flush();
        deviceHandler.onSave(device);
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    @UiHandler("selectButton")
    public void onSelectMarkerIconClicked(SelectEvent event) {
        new DeviceMarkersDialog(selectedIcon, new DeviceMarkersDialog.DeviceMarkerHandler() {
            @Override
            public void onSave(MarkerIcon icon) {
                if (icon != null) {
                    selectedIcon = icon;
                    updateIcon();
                }
            }
        }).show();
    }

    private void updateIcon() {
        markerImage.setUrl(selectedIcon.getOfflineURL());
        panelMarkers.forceLayout();
    }

    @UiHandler("editPhotoButton")
    public void onEditPhoto(SelectEvent event) {
        new DevicePhotoDialog(new DevicePhotoDialog.DevicePhotoHandler() {
            @Override
            public void uploaded(Picture photo) {
                device.setPhoto(photo);
                updatePhoto();
            }
        }).show();
    }

    @UiHandler("removePhotoButton")
    public void onRemovePhoto(SelectEvent event) {
        device.setPhoto(null);
        updatePhoto();
    }

    private void updatePhoto() {
        if (device.getPhoto() == null) {
            photo.setVisible(false);
        } else {
            photo.setUrl(Picture.URL_PREFIX + device.getPhoto().getId());
            photo.setVisible(true);
        }
    }
}
