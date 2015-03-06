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
package org.traccar.web.shared.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "geofences",
       indexes = { @Index(name = "geofences_pkey", columnList = "id") })
public class GeoFence implements Serializable {

    public GeoFence() {
        type = GeoFenceType.LINE;
        color = "4169E1";
        radius = 30d;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public long getId() {
        return id;
    }

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Enumerated(EnumType.STRING)
    private GeoFenceType type;

    public GeoFenceType getType() {
        return type;
    }

    public void setType(GeoFenceType type) {
        this.type = type;
    }

    // will hold list of lon/lat pairs of base points for this geo-fence separated by comma
    // for example: [-1.342,1.23423],[33.442324,54.3454]
    private String points;

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    // for circular geo-fence contains radius, for line it's width
    private double radius;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoFence geoFence = (GeoFence) o;

        if (id != geoFence.id) return false;
        if (name != null ? !name.equals(geoFence.name) : geoFence.name != null) return false;
        if (type != geoFence.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public static class LonLat {
        public final double lon;
        public final double lat;

        public LonLat(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }
    }

    public List<LonLat> points() {
        if (getPoints() == null || getPoints().isEmpty()) {
            return Collections.emptyList();
        }

        List<LonLat> result = new LinkedList<LonLat>();
        for (String strPoint : getPoints().split(",")) {
            int space = strPoint.indexOf(' ');
            double lon = Double.parseDouble(strPoint.substring(0, space));
            double lat = Double.parseDouble(strPoint.substring(space + 1));
            result.add(new LonLat(lon, lat));
        }
        return result;
    }
}
