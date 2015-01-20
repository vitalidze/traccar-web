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

import java.util.List;

import com.google.gwt.core.client.GWT;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.event.MapMoveListener;
import org.gwtopenmaps.openlayers.client.event.MapZoomListener;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.shared.model.UserSettings;

public class MapView {

    public interface MapHandler {
        public void onPositionSelected(Position position);
        public void onArchivePositionSelected(Position position);
    }

    private MapHandler mapHandler;

    private final ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    private final MapWidget mapWidget;
    private Map map;
    private final Vector vectorLayer;
    private final Markers markerLayer;

    private final Messages i18n = GWT.create(Messages.class);

    public Map getMap() {
        return map;
    }

    public Vector getVectorLayer() {
        return vectorLayer;
    }

    public Markers getMarkerLayer() {
        return markerLayer;
    }

    public LonLat createLonLat(double longitude, double latitude) {
        LonLat lonLat = new LonLat(longitude, latitude);
        lonLat.transform(new Projection("EPSG:4326").getProjectionCode(), map.getProjection());
        return lonLat;
    }

    public Point createPoint(double x, double y) {
        Point point = new Point(x, y);
        point.transform(new Projection("EPSG:4326"), new Projection(map.getProjection()));
        return point;
    }

    private void initMapLayers(Map map) {
        for (UserSettings.MapType mapType : UserSettings.MapType.values()) {
            map.addLayer(createMap(mapType));
        }
    }

    private Layer createMap(UserSettings.MapType mapType) {
        switch (mapType) {
            case OSM:
                return OSM.Mapnik(mapType.getName());
            case GOOGLE_HYBRID:
                GoogleV3Options gHybridOptions = new GoogleV3Options();
                gHybridOptions.setNumZoomLevels(20);
                gHybridOptions.setType(GoogleV3MapType.G_HYBRID_MAP);
                return new GoogleV3(mapType.getName(), gHybridOptions);
            case GOOGLE_NORMAL:
                GoogleV3Options gNormalOptions = new GoogleV3Options();
                gNormalOptions.setNumZoomLevels(22);
                gNormalOptions.setType(GoogleV3MapType.G_NORMAL_MAP);
                return new GoogleV3(mapType.getName(), gNormalOptions);
            case GOOGLE_SATELLITE:
                GoogleV3Options gSatelliteOptions = new GoogleV3Options();
                gSatelliteOptions.setNumZoomLevels(20);
                gSatelliteOptions.setType(GoogleV3MapType.G_SATELLITE_MAP);
                return new GoogleV3(mapType.getName(), gSatelliteOptions);
            case GOOGLE_TERRAIN:
                GoogleV3Options gTerrainOptions = new GoogleV3Options();
                gTerrainOptions.setNumZoomLevels(16);
                gTerrainOptions.setType(GoogleV3MapType.G_TERRAIN_MAP);
                return new GoogleV3(mapType.getName(), gTerrainOptions);
            case BING_ROAD:
                return new Bing(new BingOptions(mapType.getName(), mapType.getBingKey(), BingType.ROAD));
            case BING_HYBRID:
                return new Bing(new BingOptions(mapType.getName(), mapType.getBingKey(), BingType.HYBRID));
            case BING_AERIAL:
                return new Bing(new BingOptions(mapType.getName(), mapType.getBingKey(), BingType.AERIAL));
        }
        throw new IllegalArgumentException("Unsupported map type: " + mapType);
    }

    public MapView(MapHandler mapHandler) {
        this.mapHandler = mapHandler;
        contentPanel = new ContentPanel();
        contentPanel.setHeadingText(i18n.map());

        MapOptions defaultMapOptions = new MapOptions();

        mapWidget = new MapWidget("100%", "100%", defaultMapOptions);
        map = mapWidget.getMap();

        Style style = new Style();
        style.setStrokeColor("blue");
        style.setStrokeWidth(3);
        style.setFillOpacity(1);

        VectorOptions vectorOptions = new VectorOptions();
        vectorOptions.setStyle(style);
        vectorLayer = new Vector("Vector", vectorOptions);

        MarkersOptions markersOptions = new MarkersOptions();
        markerLayer = new Markers("Markers", markersOptions);

        initMapLayers(map);

        map.addLayer(vectorLayer);
        map.addLayer(markerLayer);

        map.addControl(new LayerSwitcher());
        map.addControl(new ScaleLine());

        contentPanel.add(mapWidget);

        // Update map size
        contentPanel.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                Scheduler.get().scheduleDeferred(new Command() {
                    @Override
                    public void execute() {
                        map.updateSize();
                    }
                });
            }
        });

        map.addMapMoveListener(new MapMoveListener() {
            @Override
            public void onMapMove(MapMoveEvent eventObject) {
                hidePopup();
            }
        });

        map.addMapZoomListener(new MapZoomListener() {
            @Override
            public void onMapZoom(MapZoomEvent eventObject) {
                hidePopup();
            }
        });

        latestPositionRenderer = new MapPositionRenderer(this, latestPositionSelectHandler, positionMouseHandler);
        archivePositionRenderer = new MapPositionRenderer(this, archivePositionSelectHandler, positionMouseHandler);
        latestPositionTrackRenderer = new MapPositionRenderer(this, null, null);
    }

    private final MapPositionRenderer latestPositionRenderer;

    private final MapPositionRenderer archivePositionRenderer;

    private final MapPositionRenderer latestPositionTrackRenderer;

    public void showLatestPositions(List<Position> positions) {
        latestPositionRenderer.showPositions(positions);
    }

    public void showDeviceName(List<Position> positions) {
        latestPositionRenderer.showDeviceName(positions);
    }

    public void showLatestTrackPositions(List<Position> positions) {
        latestPositionTrackRenderer.showTrackPositions(positions);
    }

    public void showLatestTime(List<Position> positions) {
        latestPositionTrackRenderer.showTime(positions, true, false);
    }

    public void showLatestTrack(List<Position> positions) {
        latestPositionTrackRenderer.showTrack(positions, false);
    }

    public void showArchiveTrack(List<Position> positions) {
        archivePositionRenderer.showTrack(positions, true);
    }

    public void showArchivePositions(List<Position> positions) {
        archivePositionRenderer.showPositions(positions);
    }

    public void showArchiveTime(List<Position> positions) {
        archivePositionRenderer.showTime(positions, false, true);
    }

    public void selectDevice(Device device) {
        latestPositionRenderer.selectDevice(device, true);
    }

    public void selectArchivePosition(Position position) {
        archivePositionRenderer.selectPosition(position, true);
    }

    private final MapPositionRenderer.SelectHandler latestPositionSelectHandler = new MapPositionRenderer.SelectHandler() {

        @Override
        public void onSelected(Position position) {
            mapHandler.onPositionSelected(position);
        }

    };

    private final MapPositionRenderer.MouseHandler positionMouseHandler = new MapPositionRenderer.MouseHandler() {

        @Override
        public void onMouseOver(Position position) {
            showPopup(position);
        }

        @Override
        public void onMouseOut(Position position) {
            hidePopup();
        }

    };

    private final MapPositionRenderer.SelectHandler archivePositionSelectHandler = new MapPositionRenderer.SelectHandler() {

        @Override
        public void onSelected(Position position) {
            mapHandler.onArchivePositionSelected(position);
        }

    };

    public void catchPosition(Position position) {
        latestPositionRenderer.catchPosition(position);
    }

    private final PositionInfoPopup popup = new PositionInfoPopup();

    private void showPopup(Position position) {
        popup.show(this, position);
    }

    private void hidePopup() {
        popup.hide();
    }

    public void updateIcon(Device device) {
        latestPositionRenderer.updateIcon(device);
    }
}
