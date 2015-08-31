/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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

import com.google.gwt.core.client.GWT;
import org.gwtopenmaps.openlayers.client.OpenLayers;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.*;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.JSObject;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.shared.model.GeoFence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoFenceRenderer {
    private final MapView mapView;
    private final Map<Long, GeoFenceDrawing> drawings = new HashMap<Long, GeoFenceDrawing>();

    public GeoFenceRenderer(MapView mapView) {
        this.mapView = mapView;
    }

    protected Vector getVectorLayer() {
        return mapView.getGeofenceLayer();
    }

    public void drawGeoFence(GeoFence geoFence, boolean drawTitle) {
        switch (geoFence.getType()) {
            case CIRCLE:
                drawCircle(geoFence, drawTitle);
                break;
            case POLYGON:
                drawPolygon(geoFence, drawTitle);
                break;
            case LINE:
                drawLine(geoFence, drawTitle);
                break;
        }
    }

    public void removeGeoFence(GeoFence geoFence) {
        GeoFenceDrawing drawing = getDrawing(geoFence);
        if (drawing != null) {
            getVectorLayer().removeFeature(drawing.getShape());
            getVectorLayer().removeFeature(drawing.getTitle());
            drawings.remove(geoFence.getId());
        }
    }

    private void drawCircle(GeoFence circle, boolean drawTitle) {
        GeoFence.LonLat center = circle.points().get(0);
        Polygon circleShape = Polygon.createRegularPolygon(mapView.createPoint(center.lon, center.lat), applyMercatorScale(center.lat, circle.getRadius()), 100, 0f);

        Style st = new Style();
        st.setFillOpacity(0.3);
        st.setStrokeWidth(1.5);
        st.setStrokeOpacity(0.8);
        st.setStrokeColor('#' + circle.getColor());
        st.setFillColor('#' + circle.getColor());

        VectorFeature drawing = new VectorFeature(circleShape, st);
        getVectorLayer().addFeature(drawing);
        VectorFeature title = drawName(circle.getName(), mapView.createPoint(center.lon, center.lat));
        drawings.put(circle.getId(), new GeoFenceDrawing(drawing, title));
        if (drawTitle) {
            getVectorLayer().addFeature(title);
        }
    }

    private void drawPolygon(GeoFence polygon, boolean drawTitle) {
        List<GeoFence.LonLat> lonLats = polygon.points();
        Point[] points = new Point[lonLats.size()];
        int i = 0;
        for (GeoFence.LonLat lonLat : lonLats) {
            points[i++] = mapView.createPoint(lonLat.lon, lonLat.lat);
        }
        Polygon polygonShape = new Polygon(new LinearRing[] { new LinearRing(points) });

        Style st = new Style();
        st.setFillOpacity(0.3);
        st.setStrokeWidth(1.5);
        st.setStrokeOpacity(0.8);
        st.setStrokeColor('#' + polygon.getColor());
        st.setFillColor('#' + polygon.getColor());

        VectorFeature drawing = new VectorFeature(polygonShape, st);
        getVectorLayer().addFeature(drawing);
        Point center = getCollectionCentroid(polygonShape);
        VectorFeature title = drawName(polygon.getName(), center);
        drawings.put(polygon.getId(), new GeoFenceDrawing(drawing, title));
        if (drawTitle) {
            getVectorLayer().addFeature(title);
        }
    }

    private void drawLine(GeoFence line, boolean drawTitle) {
        List<GeoFence.LonLat> lonLats = line.points();
        Point[] linePoints = new Point[lonLats.size()];

        int i = 0;
        for (GeoFence.LonLat lonLat : lonLats) {
            linePoints[i++] = mapView.createPoint(lonLat.lon, lonLat.lat);
        }

        LineString lineString = new LineString(linePoints);
        VectorFeature lineFeature = new VectorFeature(lineString);
        lineFeature.getAttributes().setAttribute("widthInMeters", applyMercatorScale(lonLats.get(0).lat, line.getRadius()));
        lineFeature.getAttributes().setAttribute("lineColor", '#' + line.getColor());

        getVectorLayer().addFeature(lineFeature);
        VectorFeature title = drawName(line.getName(), getCollectionCentroid(lineString));
        drawings.put(line.getId(), new GeoFenceDrawing(lineFeature, title));
        if (drawTitle) {
            getVectorLayer().addFeature(title);
        }
    }

    /**
     * Applies scale to specified radius in meters so it is precisely drawn on map
     *
     * @see <a href="http://osdir.com/ml/openlayers-users-gis/2012-09/msg00034.html">http://osdir.com/ml/openlayers-users-gis/2012-09/msg00034.html</a>
     * @see <a href="http://osdir.com/ml/openlayers-users-gis/2012-09/msg00036.html">http://osdir.com/ml/openlayers-users-gis/2012-09/msg00036.html</a>
     */
    private float applyMercatorScale(double latitude, float radius) {
        return (float) (radius / Math.cos(latitude * Math.PI / 180));
    }

    private VectorFeature drawName(String name, Point point) {
        org.gwtopenmaps.openlayers.client.Style st = new org.gwtopenmaps.openlayers.client.Style();
        st.setLabel(name);
        st.setLabelAlign("cb");
        st.setFontColor("#FF9B30");
        st.setFontSize("14");
        st.setFill(false);
        st.setStroke(false);

        return new VectorFeature(point, st);
    }

    private static Point getCollectionCentroid(Collection collection) {
        JSObject jsPoint = getCollectionCentroid(collection.getJSObject());
        return Point.narrowToPoint(jsPoint);
    }

    public static native JSObject getCollectionCentroid(JSObject collection) /*-{
        return collection.getCentroid(false);
    }-*/;

    public GeoFenceDrawing getDrawing(GeoFence geoFence) {
        return drawings.get(geoFence.getId());
    }

    public void selectGeoFence(GeoFence geoFence) {
        GeoFenceDrawing drawing = getDrawing(geoFence);
        if (drawing != null) {
            mapView.getMap().zoomToExtent(drawing.getShape().getGeometry().getBounds());
        }
    }
}
