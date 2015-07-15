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

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.OpenLayersStyle;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.StyleMap;
import org.gwtopenmaps.openlayers.client.StyleOptions;
import org.gwtopenmaps.openlayers.client.StyleRules;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.event.MapMoveListener;
import org.gwtopenmaps.openlayers.client.event.MapZoomListener;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.*;
import org.gwtopenmaps.openlayers.client.util.JSObject;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.Track;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.Position;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.shared.model.UserSettings;

public class MapView {

    public interface MapHandler {
        void onPositionSelected(Position position);
        void onArchivePositionSelected(Position position);
    }

    private MapHandler mapHandler;

    private ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    private MapWidget mapWidget;
    private Map map;
    private Vector vectorLayer;
    private Markers markerLayer;
    private Vector geofenceLayer;
    private TMS seamarkLayer;

    private Messages i18n = GWT.create(Messages.class);

    public Map getMap() {
        return map;
    }

    public Vector getVectorLayer() {
        return vectorLayer;
    }

    public Vector getGeofenceLayer() {
        return geofenceLayer;
    }

    public Markers getMarkerLayer() {
        return markerLayer;
    }

    public LonLat createLonLat(double longitude, double latitude) {
        LonLat lonLat = new LonLat(longitude, latitude);
        lonLat.transform("EPSG:4326", map.getProjection());
        return lonLat;
    }

    public Point createPoint(double longitude, double latitude) {
        Point point = new Point(longitude, latitude);
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
            case MAPQUEST_ROAD:
            case MAPQUEST_AERIAL:
                XYZOptions mqOptions = new XYZOptions();
                mqOptions.setTransitionEffect(TransitionEffect.RESIZE);
                mqOptions.setProjection("EPSG:900913");
                String tiles = "map";
                switch (mapType) {
                    case MAPQUEST_ROAD:
                        mqOptions.setAttribution(
                                "Data, imagery and map information provided by <a href='http://www.mapquest.com/'  target='_blank'>MapQuest</a>," +
                                " <a href='http://www.openstreetmap.org/' target='_blank'>Open Street Map</a> and contributors," +
                                " <a href='http://creativecommons.org/licenses/by-sa/2.0/' target='_blank'>CC-BY-SA</a>" +
                                "  <img src='http://developer.mapquest.com/content/osm/mq_logo.png' border='0'>");
                        break;
                    case MAPQUEST_AERIAL:
                        tiles = "sat";
                        mqOptions.setAttribution(
                                "Tiles Courtesy of <a href='http://open.mapquest.co.uk/' target='_blank'>MapQuest</a>." +
                                        " Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency." +
                                        " <img src='http://developer.mapquest.com/content/osm/mq_logo.png' border='0'>");
                        break;
                }

                return new XYZ(mapType.getName(), new String[] {
                        "http://otile1.mqcdn.com/tiles/1.0.0/" + tiles + "/${z}/${x}/${y}.png",
                        "http://otile2.mqcdn.com/tiles/1.0.0/" + tiles + "/${z}/${x}/${y}.png",
                        "http://otile3.mqcdn.com/tiles/1.0.0/" + tiles + "/${z}/${x}/${y}.png",
                        "http://otile4.mqcdn.com/tiles/1.0.0/" + tiles + "/${z}/${x}/${y}.png"}, mqOptions);
            case STAMEN_TONER:
                Layer stamenLayer = Layer.narrowToLayer(createStamenLayer("toner"));
                stamenLayer.setName(mapType.getName());
                return stamenLayer;
        }
        throw new IllegalArgumentException("Unsupported map type: " + mapType);
    }

    public static native JSObject getTileURL() /*-{
        return $wnd.getTileURL;
    }-*/;

    public MapView(MapHandler mapHandler, ListStore<Device> deviceStore) {
        this.mapHandler = mapHandler;
        this.popup = new PositionInfoPopup(deviceStore);
        contentPanel = new ContentPanel();
        contentPanel.setHeadingText(i18n.map());

        MapOptions defaultMapOptions = new MapOptions();
        defaultMapOptions.setMaxExtent(new Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34));

        mapWidget = new MapWidget("100%", "100%", defaultMapOptions);
        map = mapWidget.getMap();

        Style style = new Style();
        style.setStrokeColor("blue");
        style.setStrokeWidth(3);
        style.setFillOpacity(1);

        VectorOptions vectorOptions = new VectorOptions();
        vectorOptions.setStyle(style);
        vectorLayer = new Vector(i18n.overlayType(UserSettings.OverlayType.VECTOR), vectorOptions);

        MarkersOptions markersOptions = new MarkersOptions();
        markerLayer = new Markers(i18n.overlayType(UserSettings.OverlayType.MARKERS), markersOptions);

        vectorOptions = new VectorOptions();
        OpenLayersStyle defaultStyle = new OpenLayersStyle(new StyleRules(), new StyleOptions());
        defaultStyle.setJSObject(getGeoFenceLineStyle(map.getJSObject()));
        vectorOptions.setStyleMap(new StyleMap(defaultStyle, defaultStyle, null));
        geofenceLayer = new Vector(i18n.overlayType(UserSettings.OverlayType.GEO_FENCES), vectorOptions);

        initMapLayers(map);

        List<UserSettings.OverlayType> userOverlays = ApplicationContext.getInstance().getUserSettings().overlays();

        map.addLayer(geofenceLayer);
        map.addLayer(vectorLayer);
        map.addLayer(markerLayer);

        geofenceLayer.setIsVisible(userOverlays.contains(UserSettings.OverlayType.GEO_FENCES));
        geofenceLayer.setIsVisible(userOverlays.contains(UserSettings.OverlayType.VECTOR));
        geofenceLayer.setIsVisible(userOverlays.contains(UserSettings.OverlayType.MARKERS));

        TMSOptions seamarkOptions = new TMSOptions();
        seamarkOptions.setType("png");
        seamarkOptions.setGetURL(getTileURL());
        seamarkOptions.setNumZoomLevels(20);
        seamarkOptions.setIsBaseLayer(false);
        seamarkOptions.setDisplayOutsideMaxExtent(true);
        seamarkLayer = new TMS(i18n.overlayType(UserSettings.OverlayType.SEAMARK), "http://t1.openseamap.org/seamark/", seamarkOptions);
        map.addLayer(seamarkLayer);
        seamarkLayer.setIsVisible(userOverlays.contains(UserSettings.OverlayType.SEAMARK));

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
        geoFenceRenderer = new GeoFenceRenderer(this);
    }

    private final MapPositionRenderer latestPositionRenderer;
    private final MapPositionRenderer archivePositionRenderer;
    private final MapPositionRenderer latestPositionTrackRenderer;
    private final GeoFenceRenderer geoFenceRenderer;

    public void clearLatestPositions() {
        latestPositionRenderer.clearPositionsAndTitlesAndAlerts();
    }

    public void showLatestPositions(List<Position> positions) {
        for (Position position : positions) {
            latestPositionRenderer.showPositions(Arrays.asList(position));
        }
    }

    public void showDeviceName(List<Position> positions) {
        latestPositionRenderer.showDeviceName(positions);
    }

    public void showAlerts(List<Position> positions) {
        latestPositionRenderer.showAlerts(positions);
    }

    public void showLatestTrackPositions(List<Position> positions) {
        latestPositionTrackRenderer.showTrackPositions(positions);
    }

    public void showLatestTime(List<Position> positions) {
        latestPositionTrackRenderer.showTime(positions, true);
    }

    public void showLatestTrack(Track track) {
        latestPositionTrackRenderer.showTrack(track);
    }

    public void clearArchive(Device device) {
        archivePositionRenderer.clear(device);
    }

    public void showArchiveTrack(Track track) {
        archivePositionRenderer.showTrack(track);
    }

    public void showArchivePositions(List<Position> positions) {
        archivePositionRenderer.showPositions(positions);
    }

    public void setArchiveSnapToTrack(List<Position> positions) {
        if (!positions.isEmpty()) {
            archivePositionRenderer.setSnapToTrack(positions.get(0).getDevice(), true);
        }
    }

    public void showArchiveTime(List<Position> positions) {
        archivePositionRenderer.showTime(positions, false);
    }

    public void selectDevice(Device device) {
        latestPositionRenderer.selectDevice(device, true);
    }

    public void selectArchivePosition(Position position) {
        archivePositionRenderer.selectPosition(position, true);
    }

    private MapPositionRenderer.SelectHandler latestPositionSelectHandler = new MapPositionRenderer.SelectHandler() {

        @Override
        public void onSelected(Position position) {
            mapHandler.onPositionSelected(position);
        }

    };

    private MapPositionRenderer.MouseHandler positionMouseHandler = new MapPositionRenderer.MouseHandler() {

        @Override
        public void onMouseOver(Position position) {
            showPopup(position);
        }

        @Override
        public void onMouseOut(Position position) {
            hidePopup();
        }

    };

    private MapPositionRenderer.SelectHandler archivePositionSelectHandler = new MapPositionRenderer.SelectHandler() {

        @Override
        public void onSelected(Position position) {
            mapHandler.onArchivePositionSelected(position);
        }

    };

    public void catchPosition(Position position) {
        latestPositionRenderer.catchPosition(position);
    }

    private final PositionInfoPopup popup;

    private void showPopup(Position position) {
        popup.show(this, position);
    }

    private void hidePopup() {
        popup.hide();
    }

    public void updateIcon(Device device) {
        latestPositionRenderer.updateIcon(device);
    }

    public void updateAlert(Device device, boolean show) {
        latestPositionRenderer.updateAlert(device, show);
    }

    public void drawGeoFence(GeoFence geoFence, boolean drawTitle) {
        geoFenceRenderer.drawGeoFence(geoFence, drawTitle);
    }

    public void removeGeoFence(GeoFence geoFence) {
        geoFenceRenderer.removeGeoFence(geoFence);
    }

    public GeoFenceDrawing getGeoFenceDrawing(GeoFence geoFence) {
        return geoFenceRenderer.getDrawing(geoFence);
    }

    public void selectGeoFence(GeoFence geoFence) {
        geoFenceRenderer.selectGeoFence(geoFence);
    }

    /**
     * This style is used to dynamically calculate width of 'LINE' geo-fence
     *
     * <p>See:
     * <ul>
     * <li>http://gis.stackexchange.com/questions/56754/features-on-a-vector-layer-to-have-a-scalable-stroke</li>
     * <li>http://stackoverflow.com/questions/6037969/get-radius-size-in-meters-of-a-drawn-point</li>
     * <li>http://stackoverflow.com/questions/21672508/gwt-openlayers-set-sum-of-values-of-underlying-vectorfeatures-on-cluster-point</li>
     * </ul>
     * </p>
     */
    public static native JSObject getGeoFenceLineStyle(JSObject map) /*-{
        var context =
        {
            getWidth: function (feature) {
                if (feature.attributes.widthInMeters === undefined) {
                    return 2;
                } else {
                    return feature.attributes.widthInMeters / map.getResolution();
                }
            },
            getLineColor: function (feature)
            {
                if (feature.attributes.lineColor === undefined) {
                    return '#000000';
                } else {
                    return feature.attributes.lineColor;
                }
            }
        };

        return new $wnd.OpenLayers.Style(
        {
            strokeWidth: "${getWidth}",
            strokeColor: "${getLineColor}",
            strokeOpacity: 0.3,
            // for editing
            pointRadius: 5,
            fillColor: '#00ffff',
            fillOpacity: '0.5'
        },
        {
            context: context
        });
    }-*/;

    private static native JSObject createStamenLayer(String type) /*-{
        return new $wnd.OpenLayers.Layer.Stamen(type);
    }-*/;
}
