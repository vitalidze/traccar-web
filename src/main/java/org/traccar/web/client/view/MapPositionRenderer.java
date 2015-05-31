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
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Icon;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Marker;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.event.EventHandler;
import org.gwtopenmaps.openlayers.client.event.EventObject;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.JSObject;
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

    private static class SnappingHandler extends EventHandler {
        // minimum distance in pixels for snapping to occur
        static final int TOLERANCE = 15;
        final DeviceData deviceData;
        final MapView mapView;
        final Vector vectorLayer;
        final MouseHandler mouseHandler;

        VectorFeature feature;
        Style pointStyle;
        Position position;

        double resolution;
        Double cachedTolerance;

        SnappingHandler(DeviceData deviceData, MapView mapView, Vector vectorLayer, MouseHandler mouseHandler) {
            this.deviceData = deviceData;
            this.mapView = mapView;
            this.vectorLayer = vectorLayer;
            this.mouseHandler = mouseHandler;
            mapView.getMap().getEvents().register("mousemove", mapView.getMap(), this);
        }

        @Override
        public void onHandle(EventObject eventObject) {
            JSObject xy = eventObject.getJSObject().getProperty("xy");
            Pixel px = Pixel.narrowToPixel(xy);
            LonLat lonLat = mapView.getMap().getLonLatFromPixel(px);

            Position closestPosition = null;
            double closestSquaredDistance = 0;

            double mouseX = lonLat.lon();
            double mouseY = lonLat.lat();

            LineString lineString = deviceData.trackLine;
            // check bounds
            Bounds bounds = lineString.getBounds();
            if (mouseX >= bounds.getLowerLeftX() - getTolerance() && mouseX <= bounds.getUpperRightX() + getTolerance() &&
                mouseY >= bounds.getLowerLeftY() - getTolerance() && mouseY <= bounds.getUpperRightY() + getTolerance()) {
                // check all points
                for (int j = 0; j < lineString.getNumberOfComponents(); j++) {
                    JSObject jsObject = lineString.getComponent(j);
                    double dX = jsObject.getPropertyAsDouble("x") - mouseX;
                    double dY = jsObject.getPropertyAsDouble("y") - mouseY;

                    double squaredDistance = dX * dX + dY * dY;
                    if (closestPosition == null || squaredDistance < closestSquaredDistance) {
                        closestPosition = deviceData.positions.get(j);
                        closestSquaredDistance = squaredDistance;
                    }
                }
            }

            double distance = Math.sqrt(closestSquaredDistance);
            if (closestPosition != null && distance < getTolerance()) {
                LonLat posLonLat = mapView.createLonLat(closestPosition.getLongitude(), closestPosition.getLatitude());

                if (feature == null) {
                    feature = new VectorFeature(new Point(posLonLat.lon(), posLonLat.lat()), getPointStyle());
                    vectorLayer.addFeature(feature);
                } else {
                    feature.move(new LonLat(posLonLat.lon(), posLonLat.lat()));
                }
            } else {
                if (feature != null) {
                    vectorLayer.removeFeature(feature);
                    feature = null;
                }
            }

            if (position != null &&
                (closestPosition == null || distance > getTolerance() || closestPosition.getId() != position.getId())) {
                mouseHandler.onMouseOut(position);
                position = null;
            }

            if (closestPosition != null && distance < getTolerance()) {
                position = closestPosition;
                mouseHandler.onMouseOver(closestPosition);
            }
        }

        /**
         * @return tolerance in map units
         */
        private double getTolerance() {
            if (cachedTolerance == null || resolution != mapView.getMap().getResolution()) {
                resolution = mapView.getMap().getResolution();
                cachedTolerance = TOLERANCE * resolution;
            }
            return cachedTolerance;
        }

        private Style getPointStyle() {
            if (pointStyle == null) {
                pointStyle = new Style();
                pointStyle.setPointRadius(5d);
                pointStyle.setFillOpacity(1d);
            }
            return pointStyle;
        }

        void destroy() {
            mapView.getMap().getEvents().unregister("mousemove", mapView.getMap(), this);
            if (feature != null) {
                vectorLayer.removeFeature(feature);
                feature.destroy();
                feature = null;
                position = null;
            }
        }
    }

    private static class DeviceData {
        Map<Long, Marker> markerMap = new HashMap<Long, Marker>(); // Position.id -> Marker
        List<Position> positions;
        VectorFeature title;
        VectorFeature track;
        VectorFeature alert;
        LineString trackLine;
        List<VectorFeature> trackPoints = new ArrayList<VectorFeature>();
        List<VectorFeature> labels = new ArrayList<VectorFeature>();

        SnappingHandler snappingHandler;

        Position getLatestPosition() {
            return positions == null || positions.isEmpty() ? null : positions.get(positions.size() - 1);
        }
    }

    private Map<Long, DeviceData> deviceMap = new HashMap<Long, DeviceData>(); // Device.id -> Device Data

    private final DateTimeFormat timeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE);

    private Position selectedPosition;
    private Long selectedDeviceId;

    private DeviceData getDeviceData(List<Position> positions) {
        return positions.isEmpty() ? null : getDeviceData(positions.get(0).getDevice());
    }

    private DeviceData getDeviceData(Device device) {
        return getDeviceData(device.getId());
    }

    private DeviceData getDeviceData(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        DeviceData deviceData = deviceMap.get(deviceId);
        if (deviceData == null) {
            deviceData = new DeviceData();
            deviceMap.put(deviceId, deviceData);
        }
        return deviceData;
    }

    public void clear(Device device) {
        clear(getDeviceData(device));
    }

    private void clearMarkersAndTitleAndAlert(DeviceData deviceData) {
        for (Marker marker : deviceData.markerMap.values()) {
            getMarkerLayer().removeMarker(marker);
        }
        deviceData.markerMap.clear();
        if (deviceData.title != null) {
            getVectorLayer().removeFeature(deviceData.title);
            deviceData.title.destroy();
            deviceData.title = null;
        }
        if (deviceData.alert != null) {
            getVectorLayer().removeFeature(deviceData.alert);
            deviceData.alert.destroy();
            deviceData.alert = null;
        }
    }

    private void clear(DeviceData deviceData) {
        // clear markers and title
        clearMarkersAndTitleAndAlert(deviceData);
        // clear labels
        for (VectorFeature label : deviceData.labels) {
            getVectorLayer().removeFeature(label);
            label.destroy();
        }
        deviceData.labels.clear();
        // clear tracks
        if (deviceData.track != null) {
            getVectorLayer().removeFeature(deviceData.track);
            deviceData.track.destroy();
        }
        deviceData.track = null;
        deviceData.trackLine = null;
        for (VectorFeature trackPoint : deviceData.trackPoints) {
            getVectorLayer().removeFeature(trackPoint);
            trackPoint.destroy();
        }
        deviceData.trackPoints.clear();
        setSnapToTrack(deviceData, false);
    }

    public void clearPositionsAndTitlesAndAlerts() {
        for (DeviceData deviceData : deviceMap.values()) {
            clearMarkersAndTitleAndAlert(deviceData);
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
                    MarkerIconFactory.getIcon(position.getIcon(), false));
            deviceData.markerMap.put(position.getId(), marker);
            addSelectEvent(marker, position);
            addMouseEvent(marker, position);
            getMarkerLayer().addMarker(marker);
        }

        if (!selectPosition(null, selectedPosition, false)) {
            this.selectedPosition = null;
        }

        if (positions.size() == 1) {
            if (selectedDeviceId != null && selectedDeviceId.equals(positions.get(0).getDevice().getId())) {
                if (!selectPosition(null, positions.get(0), false)) {
                    selectedDeviceId = null;
                }
            }
        }
    }

    public void showDeviceName(List<Position> positions) {
        for (Position position : positions) {
            DeviceData deviceData = getDeviceData(position.getDevice());
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

            final LineString lineString = new LineString(linePoints);
            VectorFeature mapTrack = new VectorFeature(lineString, style);
            getVectorLayer().addFeature(mapTrack);
            deviceData.track = mapTrack;
            deviceData.trackLine = lineString;
            deviceData.positions = positions;
            if (track.getStyle().getZoomToTrack())
                mapView.getMap().zoomToExtent(lineString.getBounds());
        }
    }

    public void setSnapToTrack(Device device, boolean snap) {
        setSnapToTrack(getDeviceData(device), snap);
    }

    private void setSnapToTrack(DeviceData deviceData, boolean snap) {
        if (snap) {
            if (deviceData.snappingHandler == null) {
                deviceData.snappingHandler = new SnappingHandler(deviceData, mapView, getVectorLayer(), mouseHandler);
            }
        } else {
            if (deviceData.snappingHandler != null) {
                deviceData.snappingHandler.destroy();
                deviceData.snappingHandler = null;
            }
        }
    }

    public void selectPosition(Position position, boolean center) {
        if (selectPosition(selectedPosition, position, center)) {
            selectedPosition = position;
        } else {
            selectedPosition = null;
        }
    }

    public void selectDevice(Device device, boolean center) {
        DeviceData oldDeviceData = getDeviceData(selectedDeviceId);
        Position oldPosition = oldDeviceData == null ? null : oldDeviceData.getLatestPosition();

        DeviceData newDeviceData = getDeviceData(device);
        Position newPosition = newDeviceData == null ? null : newDeviceData.getLatestPosition();
        if (selectPosition(oldPosition, newPosition, center)) {
            selectedDeviceId = device.getId();
        } else {
            selectedDeviceId = null;
        }
    }

    private boolean selectPosition(Position oldPosition, Position newPosition, boolean center) {
        if (oldPosition != null) {
            DeviceData deviceData = getDeviceData(oldPosition.getDevice());
            if (deviceData.markerMap.containsKey(oldPosition.getId())) {
                changeMarkerIcon(oldPosition, MarkerIconFactory.getIcon(oldPosition.getIcon(), false));
            }
        }
        if (newPosition != null) {
            DeviceData deviceData = getDeviceData(newPosition.getDevice());
            if (deviceData.markerMap.containsKey(newPosition.getId())) {
                changeMarkerIcon(newPosition, MarkerIconFactory.getIcon(newPosition.getIcon(), true));
                if (center) {
                    mapView.getMap().panTo(deviceData.markerMap.get(newPosition.getId()).getLonLat());
                }
                return true;
            }
        }
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
            deviceData.trackPoints.add(point);
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
            position.setIcon(MarkerIcon.create(position));
            boolean selected = selectedPosition != null && selectedPosition.getId() == position.getId();
            changeMarkerIcon(position, MarkerIconFactory.getIcon(position.getIcon(), selected));
        }
    }

    public void showAlerts(List<Position> positions) {
        if (positions != null) {
            for (Position position : positions) {
                drawAlert(position);
            }
        }
    }

    private void drawAlert(Position position) {
        DeviceData deviceData = getDeviceData(position.getDevice());

        int iconWidthHalf = position.getIcon().getWidth() / 2;
        int iconHeight = position.getIcon().getHeight();

        Style alertCircleStyle = new org.gwtopenmaps.openlayers.client.Style();
        alertCircleStyle.setPointRadius(Math.sqrt(iconWidthHalf * iconWidthHalf + iconHeight * iconHeight) + 1);
        alertCircleStyle.setFillOpacity(0d);
        alertCircleStyle.setStrokeWidth(2d);
        alertCircleStyle.setStrokeColor("#ff0000");

        VectorFeature alertCircle = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), alertCircleStyle);
        getVectorLayer().addFeature(alertCircle);
        deviceData.alert = alertCircle;
    }

    public void updateAlert(Device device, boolean show) {
        DeviceData deviceData = getDeviceData(device);
        if (deviceData.alert != null) {
            getVectorLayer().removeFeature(deviceData.alert);
            deviceData.alert.destroy();
        }
        if (show) {
            Position latestPosition = deviceData.getLatestPosition();
            if (latestPosition != null) {
                drawAlert(latestPosition);
            }
        }
    }
}
