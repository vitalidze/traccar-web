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
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.Track;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.view.MapView;
import org.traccar.web.client.view.MarkerIcon;
import org.traccar.web.shared.model.*;

import java.util.*;

public class MapController implements ContentController, MapView.MapHandler {
    public interface MapHandler {
        void onDeviceSelected(Device device);
        void onArchivePositionSelected(Position position);
    }

    private final MapHandler mapHandler;

    private final MapView mapView;

    private final ListStore<Device> deviceStore;

    public MapController(MapHandler mapHandler, ListStore<Device> deviceStore) {
        this.mapHandler = mapHandler;
        this.deviceStore = deviceStore;
        mapView = new MapView(this, deviceStore);
        loadMapSettings();
    }

    @Override
    public ContentPanel getView() {
        return mapView.getView();
    }

    public org.gwtopenmaps.openlayers.client.Map getMap() {
        return mapView.getMap();
    }

    public Vector getGeoFenceLayer() {
        return mapView.getGeofenceLayer();
    }

    private Timer updateTimer;

    @Override
    public void run() {
        latestNonIdlePositionMap.clear();
        updateTimer = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
        Application.getDataService().getLatestNonIdlePositions(new AsyncCallback<List<Position>>() {
            @Override
            public void onFailure(Throwable throwable) {
                update();
            }

            @Override
            public void onSuccess(List<Position> positions) {
                for (Position position : positions) {
                    latestNonIdlePositionMap.put(position.getDevice().getId(), position);
                }
                update();
            }
        });
    }

    private Map<Long, Position> latestPositionMap = new HashMap<Long, Position>();

    private Map<Long, Position> latestNonIdlePositionMap = new HashMap<Long, Position>();

    private Map<Long, Position> timestampMap = new HashMap<Long, Position>();

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
                List<Position> alerts = null;
                long currentTime = System.currentTimeMillis();
                for (Position position : result) {
                    Device device = position.getDevice();
                    // update status and icon
                    boolean isOffline = currentTime - position.getTime().getTime() > position.getDevice().getTimeout() * 1000;
                    position.setStatus(isOffline ? Position.Status.OFFLINE : Position.Status.LATEST);
                    position.setIcon(MarkerIcon.create(position));
                    // check 'idle since'
                    if (position.getSpeed() != null) {
                        if (position.getSpeed() > position.getDevice().getIdleSpeedThreshold()) {
                            latestNonIdlePositionMap.put(device.getId(), position);
                        } else {
                            Position latestNonIdlePosition = latestNonIdlePositionMap.get(device.getId());
                            if (latestNonIdlePosition != null) {
                                position.setIdleSince(latestNonIdlePosition.getTime());
                            }
                        }
                    }
                    device = deviceStore.findModelWithKey(Long.toString(device.getId()));
                    device.setOdometer(position.getDistance());
                    // check maintenances
                    for (Maintenance maintenance : device.getMaintenances()) {
                        if (device.getOdometer() >= maintenance.getLastService() + maintenance.getServiceInterval()) {
                            if (alerts == null) alerts = new LinkedList<Position>();
                            alerts.add(position);
                            break;
                        }
                    }
                }
                /**
                 * Draw positions
                 */
                mapView.clearLatestPositions();
                mapView.showLatestPositions(result);
                mapView.showAlerts(alerts);
                mapView.showDeviceName(result);
                /**
                 * Follow positions and draw track if necessary
                 */
                for (Position position : result) {
                    Device device = position.getDevice();
                    Position prevPosition = latestPositionMap.get(device.getId());
                    if (prevPosition != null && prevPosition.getId() != position.getId()) {
                        if (ApplicationContext.getInstance().isFollowing(device)) {
                            mapView.catchPosition(position);
                        }
                        if (ApplicationContext.getInstance().isRecordingTrace(device)) {
                            mapView.showLatestTrackPositions(Arrays.asList(prevPosition));
                            mapView.showLatestTrack(new Track(Arrays.asList(prevPosition, position)));
                        }
                    }
                    if (ApplicationContext.getInstance().isRecordingTrace(device)) {
                        Position prevTimestampPosition = timestampMap.get(device.getId());

                        if (prevTimestampPosition == null ||
                                (position.getTime().getTime() - prevTimestampPosition.getTime().getTime() >= ApplicationContext.getInstance().getUserSettings().getTimePrintInterval() * 60 * 1000)) {
                            mapView.showLatestTime(Arrays.asList(position));
                            timestampMap.put(device.getId(), position);
                        }
                    }
                    latestPositionMap.put(device.getId(), position);
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
    }

    public void showArchivePositions(Track track) {
        List<Position> positions = track.getPositions();
        PositionIcon icon = new PositionIcon(track.getStyle().getIconType() == null ?
                PositionIconType.dotArchive : track.getStyle().getIconType());
        for (Position position : positions) {
            position.setIcon(icon);
        }
        mapView.showArchiveTrack(track);

        if (track.getStyle().getIconType() == null) {
            mapView.setArchiveSnapToTrack(positions);
        } else {
            mapView.showArchivePositions(positions);
        }
        List<Position> withTime = track.getTimePositions(ApplicationContext.getInstance().getUserSettings().getTimePrintInterval());
        mapView.showArchiveTime(withTime);
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
}
