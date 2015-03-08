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

import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.Vector;
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
//        Polygon.createRegularPolygon(mapView.createPoint(cir))
    }

    private void drawPolygon(GeoFence polygon) {

    }

    private void drawLine(GeoFence line) {

    }
}
