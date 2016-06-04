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

import com.google.gwt.i18n.client.TimeZoneInfo;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.traccar.web.client.controller.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.view.ApplicationView;
import org.traccar.web.client.view.UserSettingsDialog;
import org.traccar.web.client.widget.TimeZoneComboBox;
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

    private final SettingsController settingsController;
    private final NavController navController;
    private final ImportController importController;
    private final DeviceController deviceController;
    private final CommandController commandController;
    private final GeoFenceController geoFenceController;
    private final MapController mapController;
    private final ArchiveController archiveController;
    private final ReportsController reportsController;
    private final LogController logController;
    private final GroupsController groupsController;
    private final VisibilityController visibilityController;

    private ApplicationView view;

    public Application() {
        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        final ListStore<Device> deviceStore = new ListStore<>(deviceProperties.id());
        deviceStore.clearSortInfo();
        final GroupStore groupStore = new GroupStore();
        ReportProperties reportProperties = GWT.create(ReportProperties.class);
        final ListStore<Report> reportStore = new ListStore<>(reportProperties.id());

        settingsController = new SettingsController(userSettingsHandler, new DefaultUserSettingsHandlerImpl());
        visibilityController = new VisibilityController();
        mapController = new MapController(mapHandler, deviceStore, visibilityController);
        geoFenceController = new GeoFenceController(deviceStore, mapController);
        geoFenceController.getGeoFenceStore().addStoreHandlers(geoFenceStoreHandler);
        commandController = new CommandController();
        reportsController = new ReportsController(reportStore, deviceStore, geoFenceController.getGeoFenceStore());
        deviceController = new DeviceController(mapController,
                geoFenceController,
                commandController,
                visibilityController,
                deviceStore,
                deviceStoreHandler,
                geoFenceController.getGeoFenceStore(),
                geoFenceController.getDeviceGeoFences(),
                groupStore,
                reportStore,
                reportsController,
                this);
        groupsController = new GroupsController(groupStore, deviceController);
        importController = new ImportController(deviceController.getDeviceStore());
        logController = new LogController();
        navController = new NavController(settingsController, reportStore, reportsController, importController, logController, groupsController);
        archiveController = new ArchiveController(archiveHandler, userSettingsHandler, deviceController.getDeviceStore(), reportStore, reportsController);

        view = new ApplicationView(
                navController.getView(), deviceController.getView(), mapController.getView(), archiveController.getView());
    }

    public void run() {
        RootPanel.get().add(view);

        navController.run();
        deviceController.run();
        mapController.run();
        archiveController.run();
        geoFenceController.run();
        commandController.run();
        groupsController.run();
        visibilityController.run();
        reportsController.run();
        setupTimeZone();
    }

    private void setupTimeZone() {
        UserSettings userSettings = ApplicationContext.getInstance().getUserSettings();
        if (userSettings.getTimeZoneId() == null) {
            String timeZoneID = getTimeZoneFromIntlApi();
            if (timeZoneID == null) {
                TimeZoneInfo detectedZone = TimeZoneComboBox.getByOffset(-getClientOffsetTimeZone());
                timeZoneID = detectedZone == null ? null : detectedZone.getID();
            }
            if (timeZoneID != null) {
                userSettings.setTimeZoneId(timeZoneID);
                userSettingsHandler.onSave(userSettings);
            }
        }
    }

    private native String getTimeZoneFromIntlApi() /*-{
        if (typeof Intl === "undefined" || typeof Intl.DateTimeFormat === "undefined") {
            return null;
        }

        format = Intl.DateTimeFormat();

        if (typeof format === "undefined" || typeof format.resolvedOptions === "undefined") {
            return null;
        }

        timezone = format.resolvedOptions().timeZone;

        if (timezone && (timezone.indexOf("/") > -1 || timezone === 'UTC')) {
            return timezone;
        }

        return null;
    }-*/;

    private native int getClientOffsetTimeZone() /*-{
        return new Date().getTimezoneOffset();
    }-*/;

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

        @Override
        public void onClear(Device device) {
            mapController.clearArchive(device);
        }

        @Override
        public void onDrawTrack(Track track) {
            mapController.showArchivePositions(track);
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
            geoFenceController.deviceRemoved(event.getItem());
        }

    };

    private StoreHandlers<GeoFence> geoFenceStoreHandler = new BaseStoreHandlers<GeoFence>() {
        @Override
        public void onAdd(StoreAddEvent<GeoFence> event) {
            for (GeoFence geoFence : event.getItems()) {
                geoFenceController.geoFenceAdded(geoFence);
            }
        }

        @Override
        public void onRemove(StoreRemoveEvent<GeoFence> event) {
            geoFenceController.geoFenceRemoved(event.getItem());
        }
    };

    private abstract class AbstractUserSettingsHandlerImpl implements UserSettingsDialog.UserSettingsHandler {
        @Override
        public final void onTakeCurrentMapState(ComboBox<UserSettings.MapType> mapType,
                                          NumberField<Double> centerLongitude,
                                          NumberField<Double> centerLatitude,
                                          NumberField<Integer> zoomLevel,
                                          CheckBox maximizeOverviewMap,
                                          GridSelectionModel<UserSettings.OverlayType> overlays) {
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
            maximizeOverviewMap.setValue(mapController.getOverviewMap().getJSObject()
                    .getProperty("maximizeDiv").getProperty("style").getPropertyAsString("display").equals("none"));

            overlays.deselectAll();
            for (UserSettings.OverlayType overlayType : UserSettings.OverlayType.values()) {
                Layer[] mapLayer = mapController.getMap().getLayersByName(i18n.overlayType(overlayType));
                if (mapLayer != null && mapLayer.length == 1 && mapLayer[0].isVisible()) {
                    overlays.select(overlayType, true);
                }
            }
        }

        @Override
        public final void onSetZoomLevelToCurrent(NumberField<Short> field) {
            field.setValue((short) mapController.getMap().getZoom());
        }
    }

    private class UserSettingsHandlerImpl extends AbstractUserSettingsHandlerImpl {
        @Override
        public void onSave(UserSettings userSettings) {
            Application.getDataService().updateUserSettings(userSettings, new BaseAsyncCallback<UserSettings>(i18n) {
                @Override
                public void onSuccess(UserSettings result) {
                    ApplicationContext.getInstance().setUserSettings(result);
                }
            });
        }
    }

    private class DefaultUserSettingsHandlerImpl extends AbstractUserSettingsHandlerImpl {
        @Override
        public void onSave(UserSettings userSettings) {
            getDataService().saveDefaultUserSettigs(userSettings, new BaseAsyncCallback<Void>(i18n));
        }
    }

    private UserSettingsDialog.UserSettingsHandler userSettingsHandler = new UserSettingsHandlerImpl();
}
