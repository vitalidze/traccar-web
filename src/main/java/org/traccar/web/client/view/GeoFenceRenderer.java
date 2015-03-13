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

import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Collection;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.JSObject;
import org.traccar.web.shared.model.GeoFence;

import java.util.List;

public class GeoFenceRenderer {
    private final MapView mapView;

    public GeoFenceRenderer(MapView mapView) {
        this.mapView = mapView;
    }

    protected Vector getVectorLayer() {
        return mapView.getGeofenceLayer();
    }

    public void showGeoFences(List<GeoFence> geoFences) {
        for (GeoFence geoFence : geoFences) {
            switch (geoFence.getType()) {
                case CIRCLE:
                    drawCircle(geoFence);
                    break;
                case POLYGON:
                    drawPolygon(geoFence);
                    break;
                case LINE:
                    drawLine(geoFence);
                    break;
            }
        }
    }

    private void drawCircle(GeoFence circle) {
        GeoFence.LonLat center = circle.points().get(0);
        Polygon circleShape = Polygon.createRegularPolygon(mapView.createPoint(center.lon, center.lat), circle.getRadius(), 40, 0f);

        Style st = new org.gwtopenmaps.openlayers.client.Style();
        st.setFillOpacity(0.3);
        st.setStrokeWidth(1.5);
        st.setStrokeOpacity(0.8);
        st.setStrokeColor('#' + circle.getColor());
        st.setFillColor('#' + circle.getColor());

        getVectorLayer().addFeature(new VectorFeature(circleShape, st));
        drawName(circle.getName(), mapView.createPoint(center.lon, center.lat));
    }

    private void drawPolygon(GeoFence polygon) {
        List<GeoFence.LonLat> lonLats = polygon.points();
        Point[] points = new Point[lonLats.size()];
        int i = 0;
        for (GeoFence.LonLat lonLat : lonLats) {
            points[i++] = mapView.createPoint(lonLat.lon, lonLat.lat);
        }
        Polygon polygonShape = new Polygon(new LinearRing[] { new LinearRing(points) });

        Style st = new org.gwtopenmaps.openlayers.client.Style();
        st.setFillOpacity(0.3);
        st.setStrokeWidth(1.5);
        st.setStrokeOpacity(0.8);
        st.setStrokeColor('#' + polygon.getColor());
        st.setFillColor('#' + polygon.getColor());

        getVectorLayer().addFeature(new VectorFeature(polygonShape, st));
        Point center = getCollectionCentroid(polygonShape);
        drawName(polygon.getName(), center);
    }

    private void drawLine(GeoFence line) {

    }

    private void drawName(String name, Point point) {
        org.gwtopenmaps.openlayers.client.Style st = new org.gwtopenmaps.openlayers.client.Style();
        st.setLabel(name);
        st.setLabelAlign("cb");
        st.setFontColor("#FF9B30");
        st.setFontSize("14");
        st.setFill(false);
        st.setStroke(false);

        getVectorLayer().addFeature(new VectorFeature(point, st));
    }

    private static Point getCollectionCentroid(Collection collection) {
        JSObject jsPoint = getCollectionCentroid(collection.getJSObject());
        return Point.narrowToPoint(jsPoint);
    }

    public static native JSObject getCollectionCentroid(JSObject collection) /*-{
        return collection.getCentroid(false);
    }-*/;
}
