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
package org.traccar.web.client.view;

import java.util.*;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.gwtopenmaps.openlayers.client.Icon;
import org.gwtopenmaps.openlayers.client.Marker;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.event.EventHandler;
import org.gwtopenmaps.openlayers.client.event.EventObject;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.traccar.web.client.Track;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;

public class MapPositionRenderer {

    public interface SelectHandler {
        void onSelected(Position position);
    }

    public interface MouseHandler {
        void onMouseOver(Position position);
        void onMouseOut(Position position);
    }

    private final MapView mapView;

    protected Vector getVectorLayer() {
        return mapView.getVectorLayer();
    }

    protected Markers getMarkerLayer() {
        return mapView.getMarkerLayer();
    }

    private final SelectHandler selectHandler;
    private final MouseHandler mouseHandler;

    public MapPositionRenderer(MapView mapView, SelectHandler selectHandler, MouseHandler mouseHandler) {
        this.mapView = mapView;
        this.selectHandler = selectHandler;
        this.mouseHandler = mouseHandler;
    }

    private void addSelectEvent(Marker marker, final Position position) {
        if (selectHandler != null) {
            marker.getEvents().register("click", marker, new EventHandler() {
                @Override
                public void onHandle(EventObject eventObject) {
                    selectHandler.onSelected(position);
                }
            });
        }
    }

    private void addMouseEvent(final Marker marker, final Position position) {
        if (mouseHandler != null) {
            marker.getEvents().register("mouseover", marker, new EventHandler() {
                @Override
                public void onHandle(EventObject eventObject) {
                mouseHandler.onMouseOver(position);
                }
            });
            marker.getEvents().register("mouseout", marker, new EventHandler() {
                @Override
                public void onHandle(EventObject eventObject) {
                    mouseHandler.onMouseOut(position);
                }
            });
        }
    }

    private void changeMarkerIcon(Position position, Icon icon) {
        DeviceData deviceData = getDeviceData(position.getDevice());
        Marker oldMarker = deviceData.markerMap.get(position.getId());
        Marker newMarker = new Marker(oldMarker.getLonLat(), icon);
        addSelectEvent(newMarker, position);
        addMouseEvent(newMarker, position);
        deviceData.markerMap.put(position.getId(), newMarker);
        getMarkerLayer().addMarker(newMarker);
        getMarkerLayer().removeMarker(oldMarker);
    }

    private static class DeviceData {
        Map<Long, Marker> markerMap = new HashMap<Long, Marker>(); // Position.id -> Marker
        List<Position> positions;
        VectorFeature title;
        List<VectorFeature> tracks = new ArrayList<VectorFeature>();
        List<VectorFeature> labels = new ArrayList<VectorFeature>();
    }

    private Map<Long, DeviceData> deviceMap = new HashMap<Long, DeviceData>(); // Device.id -> Device Data

    private final DateTimeFormat timeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE);

    private Long selectedPositionId;
    private Long selectedDeviceId;

    private DeviceData getDeviceData(List<Position> positions) {
        return positions.isEmpty() ? null : getDeviceData(positions.get(0).getDevice());
    }

    private DeviceData getDeviceData(Device device) {
        DeviceData deviceData = deviceMap.get(device.getId());
        if (deviceData == null) {
            deviceData = new DeviceData();
            deviceMap.put(device.getId(), deviceData);
        }
        return deviceData;
    }

    public void clear(Device device) {
        clear(getDeviceData(device));
    }

    private void clearMarkersAndTitle(DeviceData deviceData) {
        for (Marker marker : deviceData.markerMap.values()) {
            getMarkerLayer().removeMarker(marker);
        }
        deviceData.markerMap.clear();
        if (deviceData.title != null) {
            getVectorLayer().removeFeature(deviceData.title);
            deviceData.title = null;
        }
    }

    private void clear(DeviceData deviceData) {
        // clear markers and title
        clearMarkersAndTitle(deviceData);
        // clear labels
        for (VectorFeature label : deviceData.labels) {
            getVectorLayer().removeFeature(label);
            label.destroy();
        }
        deviceData.labels.clear();
        // clear tracks
        for (VectorFeature mapTrack : deviceData.tracks) {
            getVectorLayer().removeFeature(mapTrack);
            mapTrack.destroy();
        }
        deviceData.tracks.clear();
    }

    public void clearPositionsAndTitles() {
        for (DeviceData deviceData : deviceMap.values()) {
            clearMarkersAndTitle(deviceData);
        }
    }

    public void clear() {
        for (DeviceData deviceData : deviceMap.values()) {
            clear(deviceData);
        }
        deviceMap.clear();
    }

    public void showPositions(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        deviceData.positions = positions;
        for (Position position : positions) {
            Marker marker = new Marker(
                    mapView.createLonLat(position.getLongitude(), position.getLatitude()),
                    MarkerIconFactory.getIcon(position.getIconType(), false));
            deviceData.markerMap.put(position.getId(), marker);
            addSelectEvent(marker, position);
            addMouseEvent(marker, position);
            getMarkerLayer().addMarker(marker);
        }

        if (selectedPositionId != null) {
            if (!selectPosition(null, selectedPositionId, false)) {
                selectedPositionId = null;
            }
        }

        if (positions.size() == 1) {
            if (selectedDeviceId != null && selectedDeviceId.equals(positions.get(0).getDevice().getId())) {
                if (!selectPosition(null, positions.get(0).getId(), false)) {
                    selectedDeviceId = null;
                }
            }
        }
    }

    public void showDeviceName(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            org.gwtopenmaps.openlayers.client.Style st = new org.gwtopenmaps.openlayers.client.Style();
            st.setLabel(position.getDevice().getName());
            st.setLabelXOffset(0);
            st.setLabelYOffset(-12);
            st.setLabelAlign("cb");
            st.setFontColor("#0000FF");
            st.setFontSize("12");
            st.setFill(false);
            st.setStroke(false);

            final VectorFeature deviceName = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), st);
            getVectorLayer().addFeature(deviceName);
            deviceData.title = deviceName;
        }
    }

    public void showTime(List<Position> positions, boolean abovePoint) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            org.gwtopenmaps.openlayers.client.Style st = new org.gwtopenmaps.openlayers.client.Style();
            st.setLabel(timeFormat.format(position.getTime()));
            st.setLabelXOffset(0);
            st.setLabelYOffset(abovePoint ? 12 : -12);
            st.setLabelAlign("cb");
            st.setFontColor("#FF4D00");
            st.setFontSize("11");
            st.setFill(false);
            st.setStroke(false);

            final VectorFeature point = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), st);
            getVectorLayer().addFeature(point);
            deviceData.labels.add(point);
        }
    }

    public void showTrack(Track track) {
        List<Position> positions = track.getPositions();
        if (!positions.isEmpty()) {
            DeviceData deviceData = getDeviceData(positions);
            Point[] linePoints = new Point[positions.size()];

            int i = 0;
            for (Position position : positions) {
                linePoints[i++] = mapView.createPoint(position.getLongitude(), position.getLatitude());
            }
            // Assigns color to style
            Style style = mapView.getVectorLayer().getStyle();
            style.setStrokeColor("#" + track.getStyle().getTrackColor());

            LineString lineString = new LineString(linePoints);
            VectorFeature mapTrack = new VectorFeature(lineString, style);
            getVectorLayer().addFeature(mapTrack);
            deviceData.tracks.add(mapTrack);
            if (track.getStyle().getZoomToTrack())
                mapView.getMap().zoomToExtent(lineString.getBounds());
        }
    }

    public void selectPosition(Position position, boolean center) {
        Long oldPositionId = selectedPositionId;
        Long newPositionId = (position != null) ? position.getId() : null;
        if (selectPosition(oldPositionId, newPositionId, center)) {
            selectedPositionId = position.getId();
        } else {
            selectedPositionId = null;
        }
    }

    public void selectDevice(Device device, boolean center) {
//         TODO
//        Long oldPositionId = (selectedDeviceId != null) ? deviceMap.get(selectedDeviceId) : null;
//        Long newPositionId = (device != null) ? deviceMap.get(device.getId()) : null;
//        if (selectPosition(oldPositionId, newPositionId, center)) {
//            selectedDeviceId = device.getId();
//        } else {
//            selectedDeviceId = null;
//        }
    }

    private boolean selectPosition(Long oldPositionId, Long newPositionId, boolean center) {
//        TODO
//        if (oldPositionId != null && markerMap.containsKey(oldPositionId)) {
//            Position oldPosition = positionMap.get(oldPositionId);
//            changeMarkerIcon(oldPosition, MarkerIconFactory.getIcon(oldPosition.getIconType(), false));
//        }
//        if (newPositionId != null && markerMap.containsKey(newPositionId)) {
//            Position newPosition = positionMap.get(newPositionId);
//            changeMarkerIcon(newPosition, MarkerIconFactory.getIcon(newPosition.getIconType(), true));
//            if (center) {
//                mapView.getMap().panTo(markerMap.get(newPositionId).getLonLat());
//            }
//            return true;
//        }
        return false;
    }

    public void catchPosition(Position position) {
        if (!mapView.getMap().getExtent().containsLonLat(mapView.createLonLat(position.getLongitude(), position.getLatitude()), true)) {
            selectPosition(position, true);
        }
    }

    public void showTrackPositions(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            VectorFeature point = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), getTrackPointStyle());
            getVectorLayer().addFeature(point);
            deviceData.tracks.add(point);
        }
    }

    org.gwtopenmaps.openlayers.client.Style trackPointStyle;

    private org.gwtopenmaps.openlayers.client.Style getTrackPointStyle() {
        if (trackPointStyle == null) {
            trackPointStyle = new org.gwtopenmaps.openlayers.client.Style();
            trackPointStyle.setPointRadius(5d);
            trackPointStyle.setFillOpacity(1d);
        }
        return trackPointStyle;
    }

    public void updateIcon(Device device) {
        DeviceData deviceData = getDeviceData(device);
        Position position = deviceData.positions == null || deviceData.positions.size() != 1 ? null : deviceData.positions.get(0);
        if (position != null) {
            position.setDevice(device);
            position.setIconType(device.getIconType().getPositionIconType(position.getStatus()));
            boolean selected = selectedPositionId != null && selectedPositionId.equals(position.getId());
            changeMarkerIcon(position, MarkerIconFactory.getIcon(position.getIconType(), selected));
        }
    }
}
