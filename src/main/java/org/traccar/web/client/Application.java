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
package org.traccar.web.client;

import java.util.logging.Logger;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Projection;
import org.traccar.web.client.controller.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.view.ApplicationView;
import org.traccar.web.client.view.FilterDialog;
import org.traccar.web.client.view.UserSettingsDialog;
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;

public class Application {

    private static final DataServiceAsync dataService = GWT.create(DataService.class);
    private final static Messages i18n = GWT.create(Messages.class);

    public static DataServiceAsync getDataService() {
        return dataService;
    }

    private static Logger logger = Logger.getLogger("");

    public static Logger getLogger() {
        return logger;
    }

    private final SettingsController settingsController;
    private final DeviceController deviceController;
    private final GeoFenceController geoFenceController;
    private final MapController mapController;
    private final ArchiveController archiveController;

    private ApplicationView view;

    public Application() {
        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        ListStore<Device> deviceStore = new ListStore<Device>(deviceProperties.id());

        settingsController = new SettingsController(userSettingsHandler);
        mapController = new MapController(mapHandler);
        geoFenceController = new GeoFenceController(deviceStore, mapController);
        geoFenceController.getGeoFenceStore().addStoreHandlers(geoFenceStoreHandler);
        deviceController = new DeviceController(mapController, geoFenceController, settingsController, deviceStore, geoFenceController.getGeoFenceStore(), deviceStoreHandler, this);
        archiveController = new ArchiveController(archiveHandler, userSettingsHandler, deviceController.getDeviceStore());
        archiveController.getPositionStore().addStoreHandlers(archiveStoreHandler);

        view = new ApplicationView(
                deviceController.getView(), mapController.getView(), archiveController.getView());
    }

    public void run() {
        RootPanel.get().add(view);

        deviceController.run();
        mapController.run();
        archiveController.run();
        geoFenceController.run();
    }

    private MapController.MapHandler mapHandler = new MapController.MapHandler() {

        @Override
        public void onDeviceSelected(Device device) {
            deviceController.selectDevice(device);
        }

        @Override
        public void onArchivePositionSelected(Position position) {
            archiveController.selectPosition(position);
        }

    };

    private ArchiveController.ArchiveHandler archiveHandler = new ArchiveController.ArchiveHandler() {

        @Override
        public void onSelected(Position position) {
            mapController.selectArchivePosition(position);
        }

    };

    public ArchiveController getArchiveController() {
        return archiveController;
    }

    private StoreHandlers<Device> deviceStoreHandler = new BaseStoreHandlers<Device>() {

        @Override
        public void onAdd(StoreAddEvent<Device> event) {
            mapController.update();
        }

        @Override
        public void onRemove(StoreRemoveEvent<Device> event) {
            mapController.update();
        }

    };

    private StoreHandlers<Position> archiveStoreHandler = new BaseStoreHandlers<Position>() {

        @Override
        public void onAnything() {
            mapController.showArchivePositions(
                    new Track(
                            archiveController.getPositionStore().getAll(),
                            archiveController.getStyle()
                    )
            );
        }

    };

    private StoreHandlers<GeoFence> geoFenceStoreHandler = new BaseStoreHandlers<GeoFence>() {
        @Override
        public void onAdd(StoreAddEvent<GeoFence> event) {
            for (GeoFence geoFence : event.getItems()) {
                mapController.drawGeoFence(geoFence, true);
            }
        }

        @Override
        public void onRemove(StoreRemoveEvent<GeoFence> event) {
            mapController.removeGeoFence(event.getItem());
        }
    };

    private class UserSettingsHandlerImpl implements UserSettingsDialog.UserSettingsHandler, FilterDialog.FilterSettingsHandler {
        @Override
        public void onSave(UserSettings userSettings) {
            ApplicationContext.getInstance().setUserSettings(userSettings);
            User user = ApplicationContext.getInstance().getUser();
            Application.getDataService().updateUser(user, new BaseAsyncCallback<User>(i18n) {
                @Override
                public void onSuccess(User result) {
                    ApplicationContext.getInstance().setUser(result);
                }
            });
        }

        @Override
        public void onTakeCurrentMapState(ComboBox<UserSettings.MapType> mapType,
                                          NumberField<Double> centerLongitude,
                                          NumberField<Double> centerLatitude,
                                          NumberField<Integer> zoomLevel) {
            String layerName = mapController.getMap().getBaseLayer().getName();
            for (UserSettings.MapType mapTypeXX : UserSettings.MapType.values()) {
                if (layerName.equals(mapTypeXX.getName())) {
                    mapType.setValue(mapTypeXX);
                    break;
                }
            }
            LonLat center = mapController.getMap().getCenter();
            center.transform(mapController.getMap().getProjection(), "EPSG:4326");
            centerLongitude.setValue(center.lon());
            centerLatitude.setValue(center.lat());
            zoomLevel.setValue(mapController.getMap().getZoom());
        }
    }

    private UserSettingsHandlerImpl userSettingsHandler = new UserSettingsHandlerImpl();
}
