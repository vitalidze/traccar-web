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

import java.util.*;

import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.state.DeviceVisibilityHandler;
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

public class DeviceController implements ContentController, DeviceView.DeviceHandler, GroupsController.GroupRemoveHandler {
    private final MapController mapController;

    private final Application application;

    private final ListStore<Device> deviceStore;

    private final DeviceView deviceView;

    private Messages i18n = GWT.create(Messages.class);

    private final PositionInfoPopup positionInfo;

    private final StoreHandlers<Device> deviceStoreHandler;

    // geo-fences per device
    private final Map<Long, Set<GeoFence>> deviceGeoFences;

    private final GroupStore groupStore;

    private final DeviceVisibilityHandler deviceVisibilityHandler;

    private Device selectedDevice;

    public DeviceController(MapController mapController,
                            DeviceView.GeoFenceHandler geoFenceHandler,
                            DeviceView.CommandHandler commandHandler,
                            DeviceVisibilityHandler deviceVisibilityHandler,
                            final ListStore<Device> deviceStore,
                            StoreHandlers<Device> deviceStoreHandler,
                            ListStore<GeoFence> geoFenceStore,
                            Map<Long, Set<GeoFence>> deviceGeoFences,
                            GroupStore groupStore,
                            Application application) {
        this.application = application;
        this.mapController = mapController;
        this.deviceStoreHandler = deviceStoreHandler;
        this.deviceStore = deviceStore;
        this.positionInfo = new PositionInfoPopup(deviceStore);
        this.deviceGeoFences = deviceGeoFences;
        this.groupStore = groupStore;
        this.deviceVisibilityHandler = deviceVisibilityHandler;

        deviceView = new DeviceView(this, geoFenceHandler, commandHandler, deviceVisibilityHandler, deviceStore, geoFenceStore, groupStore);
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
        onSelected(device, false);
    }

    @Override
    public void onSelected(Device device, boolean zoomIn) {
        mapController.selectDevice(device);
        updateGeoFences(device);
        selectedDevice = device;
        if(zoomIn) {
            mapController.zoomIn(device);
        }
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
                        } else if (caught instanceof MaxDeviceNumberReachedException) {
                            MaxDeviceNumberReachedException e = (MaxDeviceNumberReachedException) caught;
                            msg = new AlertMessageBox(i18n.error(), i18n.errMaxNumberDevicesReached(e.getReachedLimit().getMaxNumOfDevices().toString()));
                        } else {
                            msg = new AlertMessageBox(i18n.error(), i18n.errDeviceExists());
                        }
                        if (msg != null) {
                            msg.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                                @Override
                                public void onDialogHide(DialogHideEvent event) {
                                    new DeviceDialog(device, deviceStore, groupStore, AddHandler.this).show();
                                }
                            });
                            msg.show();
                        }
                    }
                });
            }
        }

        User user = ApplicationContext.getInstance().getUser();
        if (!user.getAdmin() &&
                user.getMaxNumOfDevices() != null &&
                deviceStore.size() >= user.getMaxNumOfDevices()) {
            new AlertMessageBox(i18n.error(), i18n.errMaxNumberDevicesReached(user.getMaxNumOfDevices().toString())).show();
            return;
        }

        Device newDevice = new Device();
        newDevice.setMaintenances(new ArrayList<Maintenance>());
        newDevice.setSensors(new ArrayList<Sensor>());
        new DeviceDialog(newDevice, deviceStore, groupStore, new AddHandler()).show();
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
                        deviceVisibilityHandler.updated(result);
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
                                    new DeviceDialog(device, deviceStore, groupStore, UpdateHandler.this).show();
                                }
                            });
                            msg.show();
                        }
                    }
                });
            }
        }

        new DeviceDialog(new Device(device), deviceStore, groupStore, new UpdateHandler()).show();
    }

    @Override
    public void onShare(final Device device) {
        Application.getDataService().getDeviceShare(device, new BaseAsyncCallback<Map<User, Boolean>>(i18n) {
            @Override
            public void onSuccess(final Map<User, Boolean> share) {
                new UserShareDialog(share, new UserShareDialog.UserShareHandler() {
                    @Override
                    public void onSaveShares(Map<User, Boolean> shares, final Window window) {
                        Application.getDataService().saveDeviceShare(device, shares, new BaseAsyncCallback<Void>(i18n) {
                            @Override
                            public void onSuccess(Void result) {
                                window.hide();
                            }
                        });
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
        Position latestPosition = mapController.getLatestPosition(device);
        if (latestPosition != null) {
            positionInfo.show(mouseX, mouseY, latestPosition);
        }
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

    @Override
    public void groupRemoved(Group group) {
        for (int i = 0; i < deviceStore.size(); i++) {
            Device device = deviceStore.get(i);
            if (Objects.equals(device.getGroup(), group)) {
                device.setGroup(null);
                deviceStore.update(device);
                deviceVisibilityHandler.updated(device);
            }
        }
    }
}
