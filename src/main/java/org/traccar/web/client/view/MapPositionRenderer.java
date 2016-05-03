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
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.*;
import org.gwtopenmaps.openlayers.client.event.EventObject;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.Attributes;
import org.gwtopenmaps.openlayers.client.util.JSObject;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.Track;
import org.traccar.web.client.TrackSegment;
import org.traccar.web.client.state.DeviceVisibilityProvider;
import org.traccar.web.shared.model.*;

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

    protected Vector getMarkerLayer() {
        return mapView.getMarkerLayer();
    }

    private final SelectHandler selectHandler;
    private final MouseHandler mouseHandler;
    private final DeviceVisibilityProvider visibilityProvider;

    public MapPositionRenderer(MapView mapView,
                               final SelectHandler selectHandler,
                               final MouseHandler mouseHandler,
                               DeviceVisibilityProvider visibilityProvider,
                               SelectFeature selectFeatureHover) {
        this.mapView = mapView;
        this.selectHandler = selectHandler;
        this.mouseHandler = mouseHandler;
        this.visibilityProvider = visibilityProvider;

        if (selectHandler != null) {
            getMarkerLayer().addVectorFeatureSelectedListener(new VectorFeatureSelectedListener() {
                @Override
                public void onFeatureSelected(FeatureSelectedEvent eventObject) {
                    Position position = getMouseEventPosition(eventObject.getVectorFeature());
                    if (position != null) {
                        selectHandler.onSelected(position);
                    }
                }
            });
        }
        if (mouseHandler != null) {
            if (selectFeatureHover == null) {
                getMarkerLayer().getEvents().register("featureover", getMarkerLayer(), new EventHandler() {
                    @Override
                    public void onHandle(EventObject eventObject) {
                        Position position = getMouseEventPosition(eventObject);
                        if (position != null) {
                            mouseHandler.onMouseOver(position);
                        }
                    }
                });
                getMarkerLayer().getEvents().register("featureout", getMarkerLayer(), new EventHandler() {
                    @Override
                    public void onHandle(EventObject eventObject) {
                        Position position = getMouseEventPosition(eventObject);
                        if (position != null) {
                            mouseHandler.onMouseOut(position);
                        }
                    }
                });
            } else {
                selectFeatureHover.addFeatureHighlightedListener(new FeatureHighlightedListener() {
                    @Override
                    public void onFeatureHighlighted(VectorFeature vectorFeature) {
                        Position position = getMouseEventPosition(vectorFeature);
                        if (position != null) {
                            mouseHandler.onMouseOver(position);
                        }
                    }
                });
                selectFeatureHover.addFeatureUnhighlightedListener(new FeatureUnhighlightedListener() {
                    @Override
                    public void onFeatureUnhighlighted(VectorFeature vectorFeature) {
                        Position position = getMouseEventPosition(vectorFeature);
                        if (position != null) {
                            mouseHandler.onMouseOut(position);
                        }
                    }
                });
            }
        }
    }

    private Position getMouseEventPosition(EventObject eventObject) {
        JSObject object = eventObject.getJSObject().getProperty("feature");
        VectorFeature marker = object == null ? null : VectorFeature.narrowToVectorFeature(object);
        return marker == null ? null : getMouseEventPosition(marker);
    }

    private Position getMouseEventPosition(VectorFeature marker) {
        Attributes attributes = marker.getAttributes();
        Long deviceId = Long.valueOf(attributes.getAttributeAsString("d_id"));
        DeviceData deviceData = deviceMap.get(deviceId);
        if (deviceData != null) {
            Long positionId = Long.valueOf(attributes.getAttributeAsString("p_id"));
            DeviceMarker deviceMarker = deviceData.markerMap.get(positionId);
            if (deviceMarker != null) {
                return deviceMarker.position;
            }
        }
        return null;
    }

    private void setUpEvents(VectorFeature marker, Position position) {
        if (selectHandler != null || mouseHandler != null) {
            Attributes attributes = marker.getAttributes();
            attributes.setAttribute("d_id", Long.toString(position.getDevice().getId()));
            attributes.setAttribute("p_id", Long.toString(position.getId()));
        }
    }

    private void changeMarkerIcon(Position position, boolean selected) {
        DeviceData deviceData = getDeviceData(position.getDevice());
        DeviceMarker oldMarker = deviceData.markerMap.get(position.getId());
        Point point = Point.narrowToPoint(oldMarker.marker.getJSObject().getProperty("geometry"));
        VectorFeature newMarker = new VectorFeature(point, createStyle(position, selected));
        setUpEvents(newMarker, position);
        deviceData.markerMap.put(position.getId(), new DeviceMarker(oldMarker.position, newMarker));
        getMarkerLayer().removeFeature(oldMarker.marker);
        oldMarker.marker.destroy();
        getMarkerLayer().addFeature(newMarker);
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
                    if (j < deviceData.positions.size()
                            && (closestPosition == null || squaredDistance < closestSquaredDistance)) {
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

    private static class DeviceMarker {
        final Position position;
        final VectorFeature marker;

        private DeviceMarker(Position position, VectorFeature marker) {
            this.position = position;
            this.marker = marker;
        }
    }

    private static class DeviceData {
        Map<Long, DeviceMarker> markerMap = new HashMap<>(); // Position.id -> Marker
        List<Position> positions;
        VectorFeature title;
        VectorFeature track;
        VectorFeature alert;
        LineString trackLine;
        List<VectorFeature> trackPoints = new ArrayList<>();
        List<VectorFeature> labels = new ArrayList<>();
        Map<Position, VectorFeature> pauseAndStops = new HashMap<>();
        Map<Position, VectorFeature> timeLabels = new HashMap<>();
        Map<Position, VectorFeature> arrows = new HashMap<>();

        SnappingHandler snappingHandler;

        Position getLatestPosition() {
            return positions == null || positions.isEmpty() ? null : positions.get(positions.size() - 1);
        }
    }

    private Map<Long, DeviceData> deviceMap = new HashMap<>(); // Device.id -> Device Data

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

    public void clear(Long deviceId) {
        clear(getDeviceData(deviceId));
    }

    private void clearMarkersAndTitleAndAlert(DeviceData deviceData) {
        for (DeviceMarker marker : deviceData.markerMap.values()) {
            getMarkerLayer().removeFeature(marker.marker);
            marker.marker.destroy();
        }
        deviceData.markerMap.clear();
        if (deviceData.title != null) {
            getMarkerLayer().removeFeature(deviceData.title);
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
        // clear time labels
        for (VectorFeature label : deviceData.timeLabels.values()) {
            getVectorLayer().removeFeature(label);
            label.destroy();
        }
        deviceData.timeLabels.clear();
        // clear arrows
        for (VectorFeature arrow : deviceData.arrows.values()) {
            getVectorLayer().removeFeature(arrow);
            arrow.destroy();
        }
        deviceData.arrows.clear();
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
        // clear pause and stop icons
        for (VectorFeature pauseOrStop : deviceData.pauseAndStops.values()) {
            getVectorLayer().removeFeature(pauseOrStop);
            pauseOrStop.destroy();
        }
        deviceData.pauseAndStops.clear();

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
            if (visibilityProvider.isVisible(position.getDevice())) {
                VectorFeature marker = new VectorFeature(
                        mapView.createPoint(position.getLongitude(), position.getLatitude()),
                        createStyle(position, false));
                deviceData.markerMap.put(position.getId(), new DeviceMarker(position, marker));

                setUpEvents(marker, position);
                getMarkerLayer().addFeature(marker);
            }
        }

        if (!selectPosition(null, selectedPosition, false)) {
            this.selectedPosition = null;
        }

        if (positions.size() == 1 && selectedDeviceId != null && selectedDeviceId.equals(positions.get(0).getDevice().getId())
                && !selectPosition(null, positions.get(0), false)) {
            selectedDeviceId = null;
        }
    }

    public void showDeviceName(List<Position> positions) {
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())) {
                DeviceData deviceData = getDeviceData(position.getDevice());
                org.gwtopenmaps.openlayers.client.Style st = new org.gwtopenmaps.openlayers.client.Style();
                st.setLabel(position.getDevice().getName());
                st.setLabelXOffset(0);
                st.setLabelYOffset(position.getIcon().isArrow() ? -20 : -12);
                st.setLabelAlign("cb");
                st.setFontColor("#0000FF");
                st.setFontSize("12");
                st.setFill(false);
                st.setStroke(false);

                final VectorFeature deviceName = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), st);
                getMarkerLayer().addFeature(deviceName);
                deviceData.title = deviceName;
            }
        }
    }

    public void showTime(List<Position> positions, boolean abovePoint) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())) {
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
                deviceData.timeLabels.put(position, point);
            }
        }
    }

    public void showTrack(Track track) {
        List<TrackSegment> segments = track.getSegments();
        if (!segments.isEmpty()
                && visibilityProvider.isVisible(segments.get(0).getPositions().get(0).getDevice())) {
            DeviceData deviceData = getDeviceData(segments.get(0).getPositions());

            List<Point> linePoints = new ArrayList<>();

            for (TrackSegment segment : segments) {
                if (segment.getGeometry() == null) {
                    for (Position position : segment.getPositions()) {
                        linePoints.add(mapView.createPoint(position.getLongitude(), position.getLatitude()));
                    }
                } else {
                    for (VectorFeature feature : segment.getGeometry()) {
                        LineString lineString = LineString.narrowToLineString(feature.getJSObject().getProperty("geometry"));
                        for (int i = 0; i < lineString.getNumberOfComponents(); i++) {
                            Point point = Point.narrowToPoint(lineString.getComponent(i));
                            linePoints.add(mapView.createPoint(point.getX() / 10, point.getY() / 10));
                        }
                    }
                }
            }

            LineString lineString;

            if (deviceData.track == null) {
                lineString = new LineString(linePoints.toArray(new Point[linePoints.size()]));
                deviceData.positions = track.getPositions();
            } else {
                lineString = deviceData.trackLine;
                getVectorLayer().removeFeature(deviceData.track);
                deviceData.track.destroy();
                deviceData.positions = new ArrayList<>(deviceData.positions);
                List<Position> trackPositions = track.getPositions();
                if (deviceData.positions.get(deviceData.positions.size() - 1).equals(trackPositions.get(0))) {
                    for (int i = 1; i < linePoints.size(); i++) {
                        lineString.addPoint(linePoints.get(i), lineString.getNumberOfComponents());
                        deviceData.positions.add(trackPositions.get(i));
                    }
                } else {
                    for (Point point : linePoints) {
                        lineString.addPoint(point, lineString.getNumberOfComponents());
                    }
                    deviceData.positions.addAll(trackPositions);
                }
            }

            // Assigns color to style
            Style style = new Style();
            Style defaultStyle = mapView.getVectorLayer().getStyle();
            style.setStrokeColor("#" + track.getStyle().getTrackColor());
            style.setStrokeOpacity(defaultStyle.getStrokeOpacity());
            style.setStrokeWidth(defaultStyle.getStrokeWidth());
            style.setStrokeDashstyle(defaultStyle.getStrokeDashstyle());
            style.setStrokeLinecap(defaultStyle.getStrokeLinecap());

            VectorFeature mapTrack = new VectorFeature(lineString, style);
            getVectorLayer().addFeature(mapTrack);
            deviceData.track = mapTrack;
            deviceData.trackLine = lineString;

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
        if (!visibilityProvider.isVisible(device)) {
            return;
        }
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

    public void zoomIn(Device device) {
        if (!visibilityProvider.isVisible(device)) {
            return;
        }

        DeviceData deviceData = getDeviceData(device);
        if (deviceData.positions.size() > 0) {
            UserSettings userSettings = ApplicationContext.getInstance().getUserSettings();
            Short zoomLevel = userSettings.getFollowedDeviceZoomLevel();
            if (zoomLevel != null) {
                mapView.getMap().zoomTo(zoomLevel);
            }
        }
    }

    private boolean selectPosition(Position oldPosition, Position newPosition, boolean center) {
        if (oldPosition != null) {
            DeviceData deviceData = getDeviceData(oldPosition.getDevice());
            if (deviceData.markerMap.containsKey(oldPosition.getId())) {
                changeMarkerIcon(oldPosition, false);
            }
        }
        if (newPosition != null) {
            DeviceData deviceData = getDeviceData(newPosition.getDevice());
            if (deviceData.markerMap.containsKey(newPosition.getId())) {
                changeMarkerIcon(newPosition, true);
                if (center) {
                    DeviceMarker marker = deviceData.markerMap.get(newPosition.getId());
                    Point point = Point.narrowToPoint(marker.marker.getJSObject().getProperty("geometry"));
                    mapView.getMap().panTo(new LonLat(point.getX(), point.getY()));
                }
                return true;
            }
        }
        return false;
    }

    public void catchPosition(Position position) {
        if (visibilityProvider.isVisible(position.getDevice())
            && !mapView.getMap().getExtent().containsLonLat(mapView.createLonLat(position.getLongitude(), position.getLatitude()), true)) {
            selectPosition(position, true);
        }
    }

    public void clearTrackPositions(Device device, Date before) {
        DeviceData deviceData = getDeviceData(device);
        if (deviceData.track != null) {
            boolean updated = false;
            LineString trackLine = deviceData.trackLine;
            for (Iterator<VectorFeature> it = deviceData.trackPoints.iterator(); it.hasNext(); ) {
                Position position = deviceData.positions.get(0);
                if (position.getTime().after(before)) {
                    break;
                }
                updated = true;
                trackLine.removePoint(Point.narrowToPoint(trackLine.getComponent(0)));
                getVectorLayer().removeFeature(it.next());
                it.remove();
                deviceData.positions.remove(0);

                VectorFeature timeLabel = deviceData.timeLabels.remove(position);
                if (timeLabel != null) {
                    getVectorLayer().removeFeature(timeLabel);
                    timeLabel.destroy();
                }

                VectorFeature arrow = deviceData.arrows.remove(position);
                if (arrow != null) {
                    getVectorLayer().removeFeature(arrow);
                    arrow.destroy();
                }

                VectorFeature pauseOrStop = deviceData.pauseAndStops.remove(position);
                if (pauseOrStop != null) {
                    getVectorLayer().removeFeature(pauseOrStop);
                    pauseOrStop.destroy();
                }
            }
            if (updated) {
                getVectorLayer().removeFeature(deviceData.track);
                VectorFeature track = new VectorFeature(trackLine, deviceData.track.getStyle());
                deviceData.track.destroy();
                getVectorLayer().addFeature(track);
                deviceData.track = track;
            }
        }
    }

    public void showTrackPositions(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())) {
                VectorFeature point = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), getTrackPointStyle());
                getVectorLayer().addFeature(point);
                deviceData.trackPoints.add(point);
            }
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
        if (visibilityProvider.isVisible(device)) {
            DeviceData deviceData = getDeviceData(device);
            Position position = deviceData.positions == null || deviceData.positions.size() != 1 ? null : deviceData.positions.get(0);
            if (position != null) {
                position.setDevice(device);
                position.setIcon(MarkerIcon.create(position));
                boolean selected = selectedPosition != null && selectedPosition.getId() == position.getId();
                changeMarkerIcon(position, selected);
            }
        }
    }

    public void showAlerts(Collection<Position> positions) {
        if (positions != null) {
            for (Position position : positions) {
                if (visibilityProvider.isVisible(position.getDevice())) {
                    drawAlert(position);
                }
            }
        }
    }

    private void drawAlert(Position position) {
        DeviceData deviceData = getDeviceData(position.getDevice());

        int iconWidthHalf = position.getIcon().isArrow()
                ? (DeviceIconType.DEFAULT.getPositionIconType(Position.Status.LATEST).getWidth() / 2)
                : position.getIcon().getWidth() / 2;
        int iconHeight = position.getIcon().isArrow()
                ? (DeviceIconType.DEFAULT.getPositionIconType(Position.Status.LATEST).getHeight() / 2)
                : position.getIcon().getHeight();

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
        if (show && visibilityProvider.isVisible(device)) {
            Position latestPosition = deviceData.getLatestPosition();
            if (latestPosition != null) {
                drawAlert(latestPosition);
            }
        }
    }

    private static final int IDLE_ICON_WIDTH = 10;
    private static final int IDLE_ICON_HEIGHT = 10;

    private Style createStyle(Position position, boolean selected) {
        return position.getIcon().isArrow()
                ? createArrowStyle(position, getBgColor(position))
                : createIconStyle(position, selected);
    }

    private Style createIconStyle(Position position, boolean selected) {
        PositionIcon icon = position.getIcon();

        Style style = new Style();
        int width = selected ? icon.getSelectedWidth() : icon.getWidth();
        int height = selected ? icon.getSelectedHeight() : icon.getHeight();

        style.setExternalGraphic(selected ? icon.getSelectedURL() : icon.getURL());
        style.setGraphicSize(width, height);
        style.setGraphicOffset(-width / 2, -height);
        style.setGraphicOpacity(1.0);
        style.setGraphicZIndex(10);

        String graphic = getIdleIcon(position);
        if (graphic != null) {
            style.setBackgroundGraphic(graphic);
            style.setBackgroundGraphicSize(IDLE_ICON_WIDTH, IDLE_ICON_HEIGHT);
            style.setBackgroundOffset(width / 2 - IDLE_ICON_WIDTH / 2, -height - 2);
            style.setBackgroundGraphicZIndex(11);
        }

        if (position.getDevice().isIconRotation() && position.getCourse() != null) {
            style.setRotation(position.getCourse().toString());
        }

        return style;
    }

    private String getIdleIcon(Position position) {
        if (position.getIdleStatus() != null && position.getIdleStatus() != Position.IdleStatus.MOVING) {
            switch (position.getIdleStatus()) {
                case PAUSED:
                    return "img/paused.svg";
                case IDLE:
                    return "img/stopped.svg";
            }
        }
        return null;
    }

    public void showPauseAndStops(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            String graphic = getIdleIcon(position);
            if (graphic != null) {
                Style style = new Style();
                style.setExternalGraphic(graphic);
                style.setGraphicSize(IDLE_ICON_WIDTH * 3 / 2, IDLE_ICON_HEIGHT * 3 / 2);
                style.setGraphicOpacity(1.0);
                VectorFeature pauseOrStop = new VectorFeature(
                        mapView.createPoint(position.getLongitude(), position.getLatitude()),
                        style);
                getVectorLayer().addFeature(pauseOrStop);
                deviceData.pauseAndStops.put(position, pauseOrStop);
            }
        }
    }

    private String getBgColor(Position position) {
        String bgColor = position.getDevice().getIconArrowStoppedColor();
        if (position.getStatus() == Position.Status.OFFLINE || position.getIdleStatus() == null) {
            bgColor = position.getDevice().getIconArrowOfflineColor();
        } else {
            switch (position.getIdleStatus()) {
                case MOVING:
                    bgColor = position.getDevice().getIconArrowMovingColor();
                    break;
                case PAUSED:
                    bgColor = position.getDevice().getIconArrowPausedColor();
                    break;
                case IDLE:
                    bgColor = position.getDevice().getIconArrowStoppedColor();
                    break;
            }
        }
        return bgColor;
    }

    private Style createArrowStyle(Position position, String bgColor) {
        Style style = new Style();
        style.setGraphicName("arrow");
        style.setStrokeColor("black");
        style.setStrokeWidth(0.5);
        style.setFillOpacity(1.0);

        style.setFillColor("#" + bgColor);
        style.setFill(true);
        style.setPointRadius(10);
        if (position.getCourse() != null) {
            style.setRotation(position.getCourse().toString());
        }
        return style;
    }

    public void showArrows(List<Position> positions, String color) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())) {
                VectorFeature arrow = new VectorFeature(
                        mapView.createPoint(position.getLongitude(), position.getLatitude()),
                        createArrowStyle(position, color));
                deviceData.arrows.put(position, arrow);
                getMarkerLayer().addFeature(arrow);
            }
        }
    }
}
