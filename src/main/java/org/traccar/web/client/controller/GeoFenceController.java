/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.traccar.web.client.Application;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.GeoFenceWindow;
import org.traccar.web.client.view.UserShareDialog;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.User;

import java.util.*;

public class GeoFenceController implements ContentController, DeviceView.GeoFenceHandler {
    private final MapController mapController;
    private final ListStore<GeoFence> geoFenceStore;
    private final ListStore<Device> deviceStore;
    private final Map<Long, Set<GeoFence>> deviceGeoFences;
    private ListView<GeoFence, String> geoFenceListView;
    private boolean geoFenceManagementInProgress;
    private GeoFence selectedGeoFence;

    private Messages i18n = GWT.create(Messages.class);

    public GeoFenceController(ListStore<Device> deviceStore, MapController mapController) {
        this.deviceStore = deviceStore;
        this.mapController = mapController;
        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);
        this.geoFenceStore = new ListStore<GeoFence>(geoFenceProperties.id());
        this.geoFenceStore.addSortInfo(new Store.StoreSortInfo<GeoFence>(geoFenceProperties.name(), SortDir.ASC));
        this.deviceGeoFences = new HashMap<Long, Set<GeoFence>>();
    }

    abstract class BaseGeoFenceHandler implements GeoFenceWindow.GeoFenceHandler {
        final GeoFence geoFence;

        protected BaseGeoFenceHandler(GeoFence geoFence) {
            this.geoFence = geoFence;
        }

        @Override
        public void onClear() {
            mapController.removeGeoFence(geoFence);
        }

        @Override
        public GeoFenceDrawing repaint(GeoFence geoFence) {
            mapController.removeGeoFence(geoFence);
            mapController.drawGeoFence(geoFence, false);
            return mapController.getGeoFenceDrawing(geoFence);
        }
    }

    @Override
    public void onAdd() {
        if (geoFenceManagementInProgress()) {
            return;
        }
        geoFenceManagementStarted();
        final GeoFence geoFence = new GeoFence();
        geoFence.setName(i18n.newGeoFence());
        geoFence.setTransferDevices(new HashSet<Device>());
        new GeoFenceWindow(geoFence, null, deviceStore, mapController.getMap(), mapController.getGeoFenceLayer(),
        new BaseGeoFenceHandler(geoFence) {
            @Override
            public void onSave(final GeoFence geoFence) {
                Application.getDataService().addGeoFence(geoFence,
                        new BaseAsyncCallback<GeoFence>(i18n) {
                            @Override
                            public void onSuccess(GeoFence addedGeoFence) {
                                mapController.removeGeoFence(geoFence);
                                geoFenceStore.add(addedGeoFence);
                                geoFenceStore.applySort(false);
                                if (!addedGeoFence.isAllDevices()) {
                                    geoFenceListView.getSelectionModel().select(addedGeoFence, false);
                                }
                                geoFenceManagementStopped();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                onCancel();
                                super.onFailure(caught);
                            }
                        });
            }

            @Override
            public void onCancel() {
                onClear();
                geoFenceManagementStopped();
            }
        }).show();
    }

    @Override
    public void onEdit(final GeoFence geoFence) {
        if (geoFenceManagementInProgress()) {
            return;
        }
        geoFenceManagementStarted();
        GeoFenceDrawing drawing = mapController.getGeoFenceDrawing(geoFence);
        mapController.getGeoFenceLayer().removeFeature(drawing.getTitle());
        new GeoFenceWindow(geoFence, drawing, deviceStore, mapController.getMap(), mapController.getGeoFenceLayer(),
        new BaseGeoFenceHandler(geoFence) {
            @Override
            public void onSave(GeoFence updatedGeoFence) {
                Application.getDataService().updateGeoFence(updatedGeoFence,
                        new BaseAsyncCallback<GeoFence>(i18n) {
                            @Override
                            public void onSuccess(GeoFence geoFence) {
                                mapController.removeGeoFence(geoFence);
                                if (geoFence.isAllDevices() || geoFence.equals(selectedGeoFence)) {
                                    mapController.drawGeoFence(geoFence, true);
                                    selectedGeoFence = geoFence;
                                }
                                geoFenceStore.update(geoFence);
                                geoFenceStore.applySort(false);
                                for (Collection<GeoFence> geoFences : deviceGeoFences.values()) {
                                    geoFences.remove(geoFence);
                                }
                                geoFenceAdded(geoFence);
                                geoFenceManagementStopped();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                onCancel();
                                super.onFailure(caught);
                            }
                        });
            }

            @Override
            public void onCancel() {
                mapController.removeGeoFence(geoFence);
                if (geoFence.isAllDevices() || geoFence.equals(selectedGeoFence)) {
                    mapController.drawGeoFence(geoFence, true);
                }
                geoFenceManagementStopped();
            }
        }).show();
    }

    @Override
    public void onRemove(final GeoFence geoFence) {
        if (geoFenceManagementInProgress()) {
            return;
        }
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmGeoFenceRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.YES) {
                    Application.getDataService().removeGeoFence(geoFence, new BaseAsyncCallback<GeoFence>(i18n) {
                        @Override
                        public void onSuccess(GeoFence geoFence) {
                            geoFenceStore.remove(geoFence);
                        }
                    });
                }
            }
        });
        dialog.show();
    }

    @Override
    public ContentPanel getView() {
        return null;
    }

    public void run() {
        Application.getDataService().getGeoFences(new BaseAsyncCallback<List<GeoFence>>(i18n) {
            @Override
            public void onSuccess(List<GeoFence> result) {
                geoFenceStore.addAll(result);
                geoFenceStore.applySort(false);
            }
        });
    }

    public ListStore<GeoFence> getGeoFenceStore() {
        return geoFenceStore;
    }

    @Override
    public void onSelected(GeoFence geoFence) {
        if (selectedGeoFence != null && !selectedGeoFence.isAllDevices()) {
            mapController.removeGeoFence(selectedGeoFence);
        }
        if (geoFence != null) {
            if (!geoFence.isAllDevices()) {
                mapController.drawGeoFence(geoFence, true);
            }
            mapController.selectGeoFence(geoFence);
        }
        selectedGeoFence = geoFence;
    }

    @Override
    public void onShare(final GeoFence geoFence) {
        if (geoFenceManagementInProgress()) {
            return;
        }
        Application.getDataService().getGeoFenceShare(geoFence, new BaseAsyncCallback<Map<User, Boolean>>(i18n) {
            @Override
            public void onSuccess(final Map<User, Boolean> share) {
                new UserShareDialog(share, new UserShareDialog.UserShareHandler() {
                    @Override
                    public void onSaveShares(Map<User, Boolean> shares) {
                        Application.getDataService().saveGeoFenceShare(geoFence, shares, new BaseAsyncCallback<Void>(i18n));
                    }
                }).show();
            }
        });
    }

    private boolean geoFenceManagementInProgress() {
        if (geoFenceManagementInProgress) {
            new AlertMessageBox(i18n.error(), i18n.errSaveChanges()).show();
        }
        return geoFenceManagementInProgress;
    }

    private void geoFenceManagementStarted() {
        geoFenceManagementInProgress = true;
    }

    private void geoFenceManagementStopped() {
        geoFenceManagementInProgress = false;
    }

    @Override
    public void setGeoFenceListView(ListView<GeoFence, String> geoFenceListView) {
        this.geoFenceListView = geoFenceListView;
    }

    public Map<Long, Set<GeoFence>> getDeviceGeoFences() {
        return deviceGeoFences;
    }

    public void geoFenceAdded(GeoFence geoFence) {
        if (geoFence.isAllDevices()) {
            mapController.drawGeoFence(geoFence, true);
        } else {
            for (Device device : geoFence.getTransferDevices()) {
                Set<GeoFence> geoFences = deviceGeoFences.get(device.getId());
                if (geoFences == null) {
                    geoFences = new HashSet<GeoFence>();
                    deviceGeoFences.put(device.getId(), geoFences);
                }
                geoFences.add(geoFence);
            }
        }
    }

    public void geoFenceRemoved(GeoFence geoFence) {
        mapController.removeGeoFence(geoFence);
        for (Map.Entry<Long, Set<GeoFence>> entry : deviceGeoFences.entrySet()) {
            entry.getValue().remove(geoFence);
        }
    }

    public void deviceRemoved(Device device) {
        deviceGeoFences.remove(device.getId());
    }
}
