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
package org.traccar.web.server.model;

import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.Position;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoFenceCalculator {
    private static final double radKoef = Math.PI / 180;
    private static final double earthRadius = 6371.01; // Radius of the earth in km

    static double getDistance(double lonX, double latX, double lonY, double latY) {
        double dLat = (latX - latY) * radKoef;
        double dLon = (lonX - lonY) * radKoef;
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(latX * radKoef) * Math.cos(latY * radKoef) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // Distance in km
    }

    private static class GeoFenceData {
        final List<GeoFence.LonLat> points;
        final Shape shape;

        GeoFenceData(List<GeoFence.LonLat> points, Shape shape) {
            this.points = points;
            this.shape = shape;
        }
    }

    private final Map<GeoFence, GeoFenceData> geoFences;

    public GeoFenceCalculator(Collection<GeoFence> geoFences) {
        this.geoFences = new HashMap<GeoFence, GeoFenceData>(geoFences.size());
        for (GeoFence geoFence : geoFences) {
            List<GeoFence.LonLat> points = geoFence.points();
            Shape shape = null;
            switch (geoFence.getType()) {
                case POLYGON:
                    Path2D polygon = new Path2D.Double();
                    for (GeoFence.LonLat point : geoFence.points()) {
                        if (polygon.getCurrentPoint() == null) {
                            polygon.moveTo(point.lon, point.lat);
                        } else {
                            polygon.lineTo(point.lon, point.lat);
                        }
                    }
                    polygon.closePath();
                    shape = polygon;
                    break;
            }
            this.geoFences.put(geoFence, new GeoFenceData(points, shape));
        }
    }

    public boolean contains(GeoFence geoFence, Position position) {
        // if geo-fence is device specific then check whether position's device matches
        if (!geoFence.isAllDevices() &&
            (position.getDevice() == null || !geoFence.getDevices().contains(position.getDevice()))) {
            return false;
        }

        GeoFenceData data = geoFences.get(geoFence);
        switch (geoFence.getType()) {
            case POLYGON:
                return data.shape.contains(position.getLongitude(), position.getLatitude());
            case CIRCLE:
                GeoFence.LonLat center = data.points.get(0);
                return getDistance(position.getLongitude(), position.getLatitude(), center.lon, center.lat) <= geoFence.getRadius() / 1000;
            case LINE:
                GeoFence.LonLat prevPoint = null;
                for (GeoFence.LonLat point : data.points) {
                    if (prevPoint != null) {
                        // from http://stackoverflow.com/questions/1459368/snap-point-to-a-line
                        double apx = position.getLongitude() - prevPoint.lon;
                        double apy = position.getLatitude() - prevPoint.lat;
                        double abx = point.lon - prevPoint.lon;
                        double aby = point.lat - prevPoint.lat;

                        double ab2 = abx * abx + aby * aby;
                        double ap_ab = apx * abx + apy * aby;
                        double t = ap_ab / ab2;
                        if (t < 0) {
                            t = 0;
                        } else if (t > 1) {
                            t = 1;
                        }

                        double destLon = prevPoint.lon + abx * t;
                        double destLat = prevPoint.lat + aby * t;

                        if (getDistance(destLon, destLat, position.getLongitude(), position.getLatitude()) <= geoFence.getRadius() / 2000) {
                            return true;
                        }
                    }
                    prevPoint = point;
                }
                break;
        }

        return false;
    }
}
