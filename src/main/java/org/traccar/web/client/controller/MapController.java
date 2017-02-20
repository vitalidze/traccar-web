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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.Track;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.state.DeviceVisibilityChangeHandler;
import org.traccar.web.client.state.DeviceVisibilityHandler;
import org.traccar.web.client.view.MapView;
import org.traccar.web.client.view.MarkerIcon;
import org.traccar.web.shared.model.*;

import java.util.*;

public class MapController implements ContentController, MapView.MapHandler, DeviceVisibilityChangeHandler {
    public interface MapHandler {
        void onDeviceSelected(Device device);
        void onArchivePositionSelected(Position position);
    }

    private final MapHandler mapHandler;

    private final MapView mapView;

    private final ListStore<Device> deviceStore;

    private final DeviceVisibilityHandler deviceVisibilityHandler;

    public MapController(MapHandler mapHandler,
                         ListStore<Device> deviceStore,
                         DeviceVisibilityHandler deviceVisibilityHandler) {
        this.mapHandler = mapHandler;
        this.deviceStore = deviceStore;
        this.deviceVisibilityHandler = deviceVisibilityHandler;
        mapView = new MapView(this, deviceStore, deviceVisibilityHandler);
        deviceVisibilityHandler.addVisibilityChangeHandler(this);
        loadMapSettings();
    }

    @Override
    public ContentPanel getView() {
        return mapView.getView();
    }

    public org.gwtopenmaps.openlayers.client.Map getMap() {
        return mapView.getMap();
    }

    public OverviewMap getOverviewMap() {
        return mapView.getOverviewMap();
    }

    public Vector getGeoFenceLayer() {
        return mapView.getGeofenceLayer();
    }

    private Timer updateTimer;

    @Override
    public void run() {
        updateTimer = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
        update();
    }

    private Map<Long, Position> latestPositionMap = new HashMap<>();

    private Set<Long> alerts = new HashSet<>();

    private Map<Long, Position> timestampMap = new HashMap<>();

    private Device selectedDevice;

    private int updateFailureCount = 0;

    private final Messages i18n = GWT.create(Messages.class);

    public void update() {
        updateTimer.cancel();
        Application.getDataService().getLatestPositions(new AsyncCallback<List<Position>>() {
            @Override
            public void onSuccess(List<Position> result) {
                updateFailureCount = 0;
                /**
                 * Set up icon, 'idle since' and calculate alerts
                 */
                Set<Long> newAlerts = new HashSet<>();
                long currentTime = System.currentTimeMillis();
                int selectedIndex = -1;
                for (int i = 0; i < result.size(); i++) {
                    Position position = result.get(i);
                    Device device = position.getDevice();
                    if (device.equals(selectedDevice)) {
                        selectedIndex = i;
                    }

                    // update status and icon
                    long timeout = (long) position.getDevice().getTimeout() * 1000;
                    boolean isOffline = currentTime - position.getTime().getTime() > timeout;
                    position.setStatus(isOffline ? Position.Status.OFFLINE : Position.Status.LATEST);
                    position.setIcon(MarkerIcon.create(position).setName(device.isShowName()));
                    deviceVisibilityHandler.offlineStatusChanged(device, isOffline);
                    // check 'idle since'
                    if (position.getIdleStatus() == Position.IdleStatus.MOVING) {
                        deviceVisibilityHandler.moving(device);
                    } else if (position.getIdleStatus() == Position.IdleStatus.IDLE) {
                        deviceVisibilityHandler.idle(device);
                    }
                    Device storedDevice = deviceStore.findModelWithKey(Long.toString(device.getId()));
                    if (storedDevice != null) {
                        storedDevice.setOdometer(position.getDistance());
                        // check maintenances
                        for (Maintenance maintenance : storedDevice.getMaintenances()) {
                            if (storedDevice.getOdometer() >= maintenance.getLastService() + maintenance.getServiceInterval()) {
                                newAlerts.add(device.getId());
                                break;
                            }
                        }
                    }
                }
                /**
                 * Put position of selected device to the end of list so it will be drawn after all other positions
                 */
                if (selectedIndex >= 0) {
                    result.add(result.remove(selectedIndex));
                }
                /**
                 * Follow positions and draw track if necessary
                 */
                Set<Long> devicesWithLatestPositions = new HashSet<>();
                for (Position position : result) {
                    Device device = position.getDevice();
                    Position prevPosition = latestPositionMap.get(device.getId());
                    if (prevPosition == null) {
                        mapView.showLatestPosition(position);
                    } else {
                        if (prevPosition.getId() != position.getId()) {
                            // move latest position
                            mapView.moveLatestPosition(position);
                            // follow
                            if (ApplicationContext.getInstance().isFollowing(device)) {
                                mapView.catchPosition(position);
                                mapView.zoomIn(device);
                            }
                            // draw track
                            if (ApplicationContext.getInstance().isRecordingTrace(device)) {
                                mapView.showLatestTrackPositions(Collections.singletonList(prevPosition));
                                mapView.showLatestTrack(new Track(Arrays.asList(prevPosition, position)));
                                Short traceInterval = ApplicationContext.getInstance().getUserSettings().getTraceInterval();
                                if (traceInterval != null) {
                                    mapView.clearLatestTrackPositions(device, new Date(position.getTime().getTime() - traceInterval * 60 * 1000));
                                }
                            }
                        }
                    }
                    // process alert
                    if (newAlerts.contains(device.getId())) {
                        if (alerts.contains(device.getId())) {
                            mapView.moveAlert(position);
                        } else {
                            mapView.showAlert(position);
                        }
                    }

                    if (ApplicationContext.getInstance().isRecordingTrace(device)) {
                        Position prevTimestampPosition = timestampMap.get(device.getId());

                        if (prevTimestampPosition == null ||
                                (position.getTime().getTime() - prevTimestampPosition.getTime().getTime() >= ApplicationContext.getInstance().getUserSettings().getTimePrintInterval() * 60 * 1000)) {
                            mapView.showLatestTime(Collections.singletonList(position));
                            timestampMap.put(device.getId(), position);
                        }
                    }
                    latestPositionMap.put(device.getId(), position);
                    devicesWithLatestPositions.add(device.getId());
                }
                // clear old alerts
                for (Long deviceId : alerts) {
                    if (!newAlerts.contains(deviceId)) {
                        mapView.clearAlert(deviceId);
                    }
                }
                alerts = newAlerts;

                // remove missing positions
                for (Iterator<Long> it = latestPositionMap.keySet().iterator(); it.hasNext(); ) {
                    Long deviceId = it.next();
                    if (!devicesWithLatestPositions.contains(deviceId)) {
                        mapView.clearLatestPosition(deviceId);
                        it.remove();
                    }
                }

                updateTimer.schedule(ApplicationContext.getInstance().getApplicationSettings().getUpdateInterval());
            }

            @Override
            public void onFailure(Throwable caught) {
                if (++updateFailureCount == 3) {
                    updateTimer.cancel();
                    String msg = i18n.errUserDisconnected();
                    if (caught instanceof StatusCodeException) {
                        StatusCodeException e = (StatusCodeException) caught;
                        if (e.getStatusCode() == 500) {
                            msg = i18n.errUserSessionExpired();
                        }
                    }
                    AlertMessageBox msgBox = new AlertMessageBox(i18n.error(), msg);
                    msgBox.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                        @Override
                        public void onDialogHide(DialogHideEvent event) {
                            Window.Location.reload();
                        }
                    });
                    msgBox.show();
                } else {
                    updateTimer.schedule(ApplicationContext.getInstance().getApplicationSettings().getUpdateInterval());
                }
            }
        });
    }

    public void drawGeoFence(GeoFence geoFence, boolean drawTitle) {
        mapView.drawGeoFence(geoFence, drawTitle);
    }

    public void removeGeoFence(GeoFence geoFence) {
        mapView.removeGeoFence(geoFence);
    }

    public GeoFenceDrawing getGeoFenceDrawing(GeoFence geoFence) {
        return mapView.getGeoFenceDrawing(geoFence);
    }

    public void selectGeoFence(GeoFence geoFence) {
        mapView.selectGeoFence(geoFence);
    }

    public void selectDevice(Device device) {
        mapView.selectDevice(device);
        this.selectedDevice = device;
    }

    public void zoomIn(Device device) {
        mapView.zoomIn(device);
    }

    public void showArchivePositions(Track track) {
        List<Position> positions = track.getPositions();
        PositionIcon icon = new PositionIcon(false, track.getStyle().getIconType() == null ?
                PositionIconType.dotArchive : track.getStyle().getIconType());
        for (Position position : positions) {
            position.setIcon(icon);
        }
        mapView.showArchiveTrack(track);

        if (track.getStyle().getIconType() == null) {
            mapView.setArchiveSnapToTrack(positions);
            mapView.showPauseAndStops(positions);
        } else {
            mapView.showArchivePositions(positions);
        }
        List<Position> withTime = track.getTimePositions(ApplicationContext.getInstance().getUserSettings().getTimePrintInterval());
        mapView.showArchiveTime(withTime);
        mapView.showArchiveArrows(withTime, track.getStyle().getTrackColor());
    }

    public void selectArchivePosition(Position position) {
        mapView.selectArchivePosition(position);
    }

    @Override
    public void onPositionSelected(Position position) {
        mapHandler.onDeviceSelected(position.getDevice());
    }

    @Override
    public void onArchivePositionSelected(Position position) {
        mapHandler.onArchivePositionSelected(position);
    }

    public void loadMapSettings() {
        UserSettings userSettings = ApplicationContext.getInstance().getUserSettings();
        for (Layer map : mapView.getMap().getLayers()) {
            if (map.getName().equals(userSettings.getMapType().getName())) {
                mapView.getMap().setBaseLayer(map);
                break;
            }
        }
        mapView.getMap().setCenter(mapView.createLonLat(userSettings.getCenterLongitude(), userSettings.getCenterLatitude()), userSettings.getZoomLevel());
    }

    public Position getLatestPosition(Device device) {
        return latestPositionMap.get(device.getId());
    }

    public void updateIcon(Device device) {
        mapView.updateIcon(device);
    }

    public void clearArchive(Device device) {
        mapView.clearArchive(device);
    }

    public void updateAlert(Device device, boolean show) {
        mapView.updateAlert(device, show);
    }

    @Override
    public void visibilityChanged(Long deviceId, boolean visible) {
        if (visible) {
            Position latestPosition = latestPositionMap.get(deviceId);
            if (latestPosition != null) {
                mapView.showLatestPosition(latestPosition);
                if (alerts.contains(deviceId)) {
                    mapView.showAlert(latestPosition);
                }
            }
        } else {
            mapView.clearLatestPosition(deviceId);
        }
    }
}
