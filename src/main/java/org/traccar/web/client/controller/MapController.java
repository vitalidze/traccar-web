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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.Track;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.view.MapView;
import org.traccar.web.shared.model.*;

import java.util.*;

public class MapController implements ContentController, MapView.MapHandler {
    private final static Messages i18n = GWT.create(Messages.class);

    public interface MapHandler {
        public void onDeviceSelected(Device device);
        public void onArchivePositionSelected(Position position);
    }

    private final MapHandler mapHandler;

    private final MapView mapView;

    private final ListStore<Device> deviceStore;

    public MapController(MapHandler mapHandler, ListStore<Device> deviceStore) {
        this.mapHandler = mapHandler;
        this.deviceStore = deviceStore;
        mapView = new MapView(this);
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

    public void update() {
        updateTimer.cancel();
        Application.getDataService().getLatestPositions(new AsyncCallback<List<Position>>() {
            @Override
            public void onSuccess(List<Position> result) {
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
                    position.setIconType(device.getIconType().getPositionIconType(position.getStatus()));
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
                    if (device.getMaintenances() != null) {
                        for (Maintenance maintenance : device.getMaintenances()) {
                            if (maintenance.getLastService() + maintenance.getServiceInterval() >= device.getOdometer()) {
                                if (alerts == null) alerts = new LinkedList<Position>();
                                alerts.add(position);
                                break;
                            }
                        }
                    }
                }
                /**
                 * Draw positions
                 */
                mapView.showLatestPositions(result);
                mapView.showAlert(alerts);
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
                updateTimer.schedule(ApplicationContext.getInstance().getApplicationSettings().getUpdateInterval());
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
        mapView.showArchiveTrack(track);
        mapView.showArchivePositions(track);
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
}
