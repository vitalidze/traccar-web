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
package org.traccar.web.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.DeviceDialog;
import org.traccar.web.client.view.UserShareDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.PositionInfoPopup;
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;

public class DeviceController implements ContentController, DeviceView.DeviceHandler {
    private final MapController mapController;

    private final Application application;

    private final ListStore<Device> deviceStore;

    private final DeviceView deviceView;

    private Messages i18n = GWT.create(Messages.class);

    private final PositionInfoPopup positionInfo = new PositionInfoPopup();

    private final StoreHandlers<Device> deviceStoreHandler;

    // geo-fences per device
    private final Map<Long, Set<GeoFence>> deviceGeoFences;
    private Device selectedDevice;

    public DeviceController(MapController mapController,
                            DeviceView.GeoFenceHandler geoFenceHandler,
                            DeviceView.SettingsHandler settingsHandler,
                            ListStore<Device> deviceStore,
                            StoreHandlers<Device> deviceStoreHandler,
                            ListStore<GeoFence> geoFenceStore,
                            Map<Long, Set<GeoFence>> deviceGeoFences,
                            Application application) {
        this.application = application;
        this.mapController = mapController;
        this.deviceStoreHandler = deviceStoreHandler;
        this.deviceStore = deviceStore;
        this.deviceGeoFences = deviceGeoFences;

        this.deviceStore.addStoreRecordChangeHandler(new StoreRecordChangeEvent.StoreRecordChangeHandler<Device>() {
            @Override
            public void onRecordChange(StoreRecordChangeEvent<Device> event) {
                if (event.getProperty().getPath().equals("follow")) {
                    boolean follow = (Boolean) event.getRecord().getValue(event.getProperty());
                    Device device = event.getRecord().getModel();
                    if (follow) {
                        ApplicationContext.getInstance().follow(device);
                    } else {
                        ApplicationContext.getInstance().stopFollowing(device);
                    }
                } else if (event.getProperty().getPath().equals("recordTrace")) {
                    boolean recordTrace = (Boolean) event.getRecord().getValue(event.getProperty());
                    Device device = event.getRecord().getModel();
                    if (recordTrace) {
                        ApplicationContext.getInstance().recordTrace(device);
                    } else {
                        ApplicationContext.getInstance().stopRecordingTrace(device);
                    }
                }
            }
        });
        deviceView = new DeviceView(this, geoFenceHandler, settingsHandler, deviceStore, geoFenceStore);
    }

    public ListStore<Device> getDeviceStore() {
        return deviceStore;
    }

    @Override
    public ContentPanel getView() {
        return deviceView.getView();
    }

    @Override
    public void run() {
        Application.getDataService().getDevices(new BaseAsyncCallback<List<Device>>(i18n) {
            @Override
            public void onSuccess(List<Device> result) {
                deviceStore.addAll(result);
                deviceStore.addStoreHandlers(deviceStoreHandler);
            }
        });
    }

    @Override
    public void onSelected(Device device) {
        mapController.selectDevice(device);
        updateGeoFences(device);
        selectedDevice = device;
    }

    @Override
    public void onAdd() {
        class AddHandler implements DeviceDialog.DeviceHandler {
            @Override
            public void onSave(final Device device) {
                Application.getDataService().addDevice(device, new BaseAsyncCallback<Device>(i18n) {
                    @Override
                    public void onSuccess(Device result) {
                        deviceStore.add(result);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        MessageBox msg = null;
                        if (caught instanceof ValidationException) {
                            msg = new AlertMessageBox(i18n.error(), i18n.errNoDeviceNameOrId());
                        } else {
                            msg = new AlertMessageBox(i18n.error(), i18n.errDeviceExists());
                        }
                        if (msg != null) {
                            msg.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                                @Override
                                public void onDialogHide(DialogHideEvent event) {
                                    new DeviceDialog(device, AddHandler.this).show();
                                }
                            });
                            msg.show();
                        }
                    }
                });
            }
        }

        Device newDevice = new Device();
        newDevice.setMaintenances(new ArrayList<Maintenance>());
        new DeviceDialog(newDevice, new AddHandler()).show();
    }

    @Override
    public void onEdit(Device device) {
        class UpdateHandler implements DeviceDialog.DeviceHandler {
            @Override
            public void onSave(final Device device) {
                Application.getDataService().updateDevice(device, new BaseAsyncCallback<Device>(i18n) {
                    @Override
                    public void onSuccess(Device result) {
                        deviceStore.update(result);
                        mapController.updateIcon(result);
                        boolean showAlert = false;
                        for (Maintenance maintenance : result.getMaintenances()) {
                            if (result.getOdometer() >= maintenance.getLastService() + maintenance.getServiceInterval()) {
                                showAlert = true;
                                break;
                            }
                        }
                        mapController.updateAlert(result, showAlert);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        MessageBox msg = null;
                        if (caught instanceof ValidationException) {
                            msg = new AlertMessageBox(i18n.error(), i18n.errNoDeviceNameOrId());
                        } else {
                            msg = new AlertMessageBox(i18n.error(), i18n.errDeviceExists());
                        }
                        if (msg != null) {
                            msg.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                                @Override
                                public void onDialogHide(DialogHideEvent event) {
                                    new DeviceDialog(device, UpdateHandler.this).show();
                                }
                            });
                            msg.show();
                        }
                    }
                });
            }
        }

        new DeviceDialog(new Device(device), new UpdateHandler()).show();
    }

    @Override
    public void onShare(final Device device) {
        Application.getDataService().getDeviceShare(device, new BaseAsyncCallback<Map<User, Boolean>>(i18n) {
            @Override
            public void onSuccess(final Map<User, Boolean> share) {
                new UserShareDialog(share, new UserShareDialog.UserShareHandler() {
                    @Override
                    public void onSaveShares(Map<User, Boolean> shares) {
                        Application.getDataService().saveDeviceShare(device, shares, new BaseAsyncCallback<Void>(i18n));
                    }
                }).show();
            }
        });
    }

    @Override
    public void onRemove(final Device device) {
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmDeviceRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
			@Override
			public void onDialogHide(DialogHideEvent event) {
				if (event.getHideButton() == PredefinedButton.YES) {
                    Application.getDataService().removeDevice(device, new BaseAsyncCallback<Device>(i18n) {
                        @Override
                        public void onSuccess(Device result) {
                            deviceStore.remove(device);
                        }
                    });
				}
			}
		});
        dialog.show();
    }

    @Override
    public void onMouseOver(int mouseX, int mouseY, Device device) {
        positionInfo.show(mouseX, mouseY, mapController.getLatestPosition(device));
    }

    @Override
    public void onMouseOut(int mouseX, int mouseY, Device device) {
        positionInfo.hide();
    }

    public void selectDevice(Device device) {
        deviceView.selectDevice(device);
        updateGeoFences(device);
        selectedDevice = device;
    }

    public void doubleClicked(Device device) {
        application.getArchiveController().selectDevice(device);
    }

    private void updateGeoFences(Device device) {
        onClearSelection();
        Set<GeoFence> geoFences = device == null ? null : deviceGeoFences.get(device.getId());
        if (geoFences != null) {
            for (GeoFence geoFence : geoFences) {
                mapController.drawGeoFence(geoFence, true);
            }
        }
    }

    @Override
    public void onClearSelection() {
        // remove old geo-fences
        if (selectedDevice != null) {
            Set<GeoFence> geoFences = deviceGeoFences.get(selectedDevice.getId());
            if (geoFences != null) {
                for (GeoFence geoFence : geoFences) {
                    mapController.removeGeoFence(geoFence);
                }
            }
        }
        selectedDevice = null;
    }
}
