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

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.GroupProperties;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.renderer.GroupSafeHtmlRenderer;
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
    TabPanel tabs;

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
    NumberField<Integer> minIdleTime;

    @UiField
    NumberField<Double> speedLimit;

    @UiField
    VerticalLayoutContainer sendNotificationsCont;

    @UiField
    CheckBox sendNotifications;

    @UiField
    CheckBox showProtocol;

    @UiField
    CheckBox showOdometer;

    @UiField
    ScrollPanel panelPhoto;

    @UiField
    Image photo;

    @UiField
    VerticalLayoutContainer iconTab;
    final DeviceIconEditor iconEditor;

    @UiField
    VerticalLayoutContainer sensorsTab;
    final SensorsEditor sensorsEditor;

    @UiField
    VerticalLayoutContainer maintenanceTab;
    final MaintenanceEditor maintenanceEditor;

    @UiField(provided = true)
    ComboBox<Group> group;

    @UiField
    Messages i18n;

    final Device device;

    public DeviceDialog(Device device, ListStore<Device> deviceStore, GroupStore groupStore, DeviceHandler deviceHandler) {
        this.device = device;
        this.deviceHandler = deviceHandler;

        GroupProperties groupProperties = GWT.create(GroupProperties.class);

        this.group = new ComboBox<>(groupStore.toListStore(), groupProperties.label(), new GroupSafeHtmlRenderer(groupStore));
        this.group.setForceSelection(false);

        uiBinder.createAndBindUi(this);

        timeout.addValidator(new MinNumberValidator<>(1));
        timeout.addValidator(new MaxNumberValidator<>(7 * 24 * 60 * 60));

        driver.initialize(this);
        driver.edit(device);

        idleSpeedThreshold.setValue(device.getIdleSpeedThreshold() * ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getFactor());
        if (device.getSpeedLimit() != null) {
            speedLimit.setValue(device.getSpeedLimit() * ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getFactor());
        }
        if (device.getId() == 0
                || ApplicationContext.getInstance().getUser().getId() == device.getOwnerId()
                || ApplicationContext.getInstance().getUser().getAdmin()) {
            sendNotifications.setEnabled(true);
            sendNotificationsCont.setVisible(true);
        } else {
            sendNotifications.setEnabled(false);
            sendNotificationsCont.setVisible(false);
        }

        updatePhoto();

        sensorsEditor = new SensorsEditor(device, deviceStore);
        sensorsTab.add(sensorsEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        maintenanceEditor = new MaintenanceEditor(device, deviceStore);
        maintenanceTab.add(maintenanceEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        iconEditor = new DeviceIconEditor(device);
        iconTab.add(iconEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        tabs.addSelectionHandler(new SelectionHandler<Widget>() {
            @Override
            public void onSelection(SelectionEvent<Widget> event) {
                if (event.getSelectedItem() == iconTab) {
                    iconEditor.loadIcons();
                }
            }
        });
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
        if (device.getSpeedLimit() != null) {
            device.setSpeedLimit(ApplicationContext.getInstance().getUserSettings().getSpeedUnit().toKnots(device.getSpeedLimit()));
        }

        iconEditor.flush();
        maintenanceEditor.flush();
        sensorsEditor.flush();
        deviceHandler.onSave(device);
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
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
