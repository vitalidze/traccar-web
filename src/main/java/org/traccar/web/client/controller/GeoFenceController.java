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
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.client.Application;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.GeoFenceWindow;
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
    }

    @Override
    public void onAdd() {
        new GeoFenceWindow(new GeoFence(), mapController.getMap(), mapController.getGeoFenceLayer(),
        new GeoFenceWindow.GeoFenceHandler() {
            @Override
            public void onSave(GeoFence device) {
            }
        }).show();
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
            }
        });
    }

    public ListStore<GeoFence> getGeoFenceStore() {
        return geoFenceStore;
    }
}
