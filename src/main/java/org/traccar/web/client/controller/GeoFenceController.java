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
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.traccar.web.client.Application;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.GeoFenceWindow;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GeoFence;

import java.util.List;

public class GeoFenceController implements ContentController, DeviceView.GeoFenceHandler {
    private final MapController mapController;
    private final ListStore<GeoFence> geoFenceStore;

    private Messages i18n = GWT.create(Messages.class);

    public GeoFenceController(MapController mapController) {
        this.mapController = mapController;
        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);
        this.geoFenceStore = new ListStore<GeoFence>(geoFenceProperties.id());
        this.geoFenceStore.addSortInfo(new Store.StoreSortInfo<GeoFence>(geoFenceProperties.name(), SortDir.ASC));
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
        final GeoFence geoFence = new GeoFence();
        geoFence.setName(i18n.newGeoFence());
        new GeoFenceWindow(geoFence, null, mapController.getMap(), mapController.getGeoFenceLayer(),
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
            }
        }).show();
    }

    @Override
    public void onEdit(final GeoFence geoFence) {
        GeoFenceDrawing drawing = mapController.getGeoFenceDrawing(geoFence);
        mapController.getGeoFenceLayer().removeFeature(drawing.getTitle());
        new GeoFenceWindow(geoFence, drawing, mapController.getMap(), mapController.getGeoFenceLayer(),
        new BaseGeoFenceHandler(geoFence) {
            @Override
            public void onSave(GeoFence updatedGeoFence) {
                Application.getDataService().updateGeoFence(updatedGeoFence,
                        new BaseAsyncCallback<GeoFence>(i18n) {
                            @Override
                            public void onSuccess(GeoFence geoFence) {
                                mapController.removeGeoFence(geoFence);
                                mapController.drawGeoFence(geoFence, true);
                                geoFenceStore.update(geoFence);
                                geoFenceStore.applySort(false);
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
                mapController.drawGeoFence(geoFence, true);
            }
        }).show();
    }

    @Override
    public void onRemove(final GeoFence geoFence) {
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
        mapController.selectGeoFence(geoFence);
    }
}
