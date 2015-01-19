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

import com.google.gwt.core.client.GWT;
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
import org.traccar.web.client.ArchiveStyle;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;

public class MapPositionRenderer {

    public interface SelectHandler {
        public void onSelected(Position position);
    }

    public interface MouseHandler {
        public void onMouseOver(Position position);
        public void onMouseOut(Position position);
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
        Marker oldMarker = markerMap.get(position.getId());
        Marker newMarker = new Marker(oldMarker.getLonLat(), icon);
        addSelectEvent(newMarker, position);
        addMouseEvent(newMarker, position);
        markerMap.put(position.getId(), newMarker);
        getMarkerLayer().addMarker(newMarker);
        getMarkerLayer().removeMarker(oldMarker);
    }

    private Map<Long, Marker> markerMap = new HashMap<Long, Marker>(); // Position.id -> Marker
    private Map<Long, Long> deviceMap = new HashMap<Long, Long>(); // Device.id -> Position.id
    private Map<Long, Position> positionMap = new HashMap<Long, Position>(); // Position.id -> Position

    private List<VectorFeature> tracks = new ArrayList<VectorFeature>();
    private List<VectorFeature> labels = new ArrayList<VectorFeature>();

    private final DateTimeFormat timeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE);

    private Long selectedPositionId;
    private Long selectedDeviceId;

    public void showPositions(List<Position> positions) {
        for (Marker marker : markerMap.values()) {
            getMarkerLayer().removeMarker(marker);
        }
        markerMap.clear();
        deviceMap.clear();
        positionMap.clear();

        for (Position position : positions) {
            Marker marker = new Marker(
                    mapView.createLonLat(position.getLongitude(), position.getLatitude()),
                    MarkerIconFactory.getIcon(position.getIconType(), false));
            markerMap.put(position.getId(), marker);
            deviceMap.put(position.getDevice().getId(), position.getId());
            positionMap.put(position.getId(), position);
            addSelectEvent(marker, position);
            addMouseEvent(marker, position);
            getMarkerLayer().addMarker(marker);
        }

        if (selectedPositionId != null) {
            if (!selectPosition(null, selectedPositionId, false)) {
                selectedPositionId = null;
            }
        }

        if (selectedDeviceId != null) {
            if (!selectPosition(null, deviceMap.get(selectedDeviceId), false)) {
                selectedDeviceId = null;
            }
        }
    }

    public void showDeviceName(List<Position> positions) {
        for (VectorFeature label : labels) {
            getVectorLayer().removeFeature(label);
            label.destroy();
        }
        labels.clear();

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

            final VectorFeature point = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), st);
            getVectorLayer().addFeature(point);
            labels.add(point);
        }
    }

    public void showTime(List<Position> positions, boolean abovePoint, boolean clearExisting) {
        if (clearExisting) {
            for (VectorFeature label : labels) {
                getVectorLayer().removeFeature(label);
                label.destroy();
            }
            labels.clear();
        }

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
            labels.add(point);
        }
    }

    public void showTrack(List<Position> positions, boolean clearExisting) {
        if (clearExisting) {
            for (VectorFeature track : tracks) {
                getVectorLayer().removeFeature(track);
                track.destroy();
            }
            tracks.clear();
        }

        if (!positions.isEmpty()) {
            Point[] linePoints = new Point[positions.size()];

            int i = 0;
            String color = null;
            for (Position position : positions) {
                color = position.getTrackColor();
                linePoints[i++] = mapView.createPoint(position.getLongitude(), position.getLatitude());
            }
            // Defaults
            if (color == null)
                color = ArchiveStyle.DEFAULT_COLOR;
            // Assigns color to style
            Style style = mapView.getVectorLayer().getStyle();
            style.setStrokeColor("#" + color);

            LineString lineString = new LineString(linePoints);
            VectorFeature track = new VectorFeature(lineString, style);
            getVectorLayer().addFeature(track);
            tracks.add(track);
            //mapView.getMap().zoomToExtent(lineString.getBounds());
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
        Long oldPositionId = (selectedDeviceId != null) ? deviceMap.get(selectedDeviceId) : null;
        Long newPositionId = (device != null) ? deviceMap.get(device.getId()) : null;
        if (selectPosition(oldPositionId, newPositionId, center)) {
            selectedDeviceId = device.getId();
        } else {
            selectedDeviceId = null;
        }
    }

    private boolean selectPosition(Long oldPositionId, Long newPositionId, boolean center) {
        if (oldPositionId != null && markerMap.containsKey(oldPositionId)) {
            Position oldPosition = positionMap.get(oldPositionId);
            changeMarkerIcon(oldPosition, MarkerIconFactory.getIcon(oldPosition.getIconType(), false));
        }
        if (newPositionId != null && markerMap.containsKey(newPositionId)) {
            Position newPosition = positionMap.get(newPositionId);
            changeMarkerIcon(newPosition, MarkerIconFactory.getIcon(newPosition.getIconType(), true));
            if (center) {
                mapView.getMap().panTo(markerMap.get(newPositionId).getLonLat());
            }
            return true;
        }
        return false;
    }

    public void catchPosition(Position position) {
        if (!mapView.getMap().getExtent().containsLonLat(mapView.createLonLat(position.getLongitude(), position.getLatitude()), true)) {
            selectPosition(position, true);
        }
    }

    public void showTrackPositions(List<Position> positions) {
        for (Position position : positions) {
            VectorFeature point = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), getTrackPointStyle());
            getVectorLayer().addFeature(point);
            tracks.add(point);
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
        Long positionId = deviceMap.get(device.getId());
        Position position = positionMap.get(positionId);
        if (position != null) {
            position.setDevice(device);
            position.setIconType(device.getIconType().getPositionIconType(position.getStatus()));
            boolean selected = selectedPositionId != null && selectedPositionId.equals(positionId);
            changeMarkerIcon(position, MarkerIconFactory.getIcon(position.getIconType(), selected));
        }
    }
}
