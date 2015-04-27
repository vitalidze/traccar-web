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

import com.google.gson.annotations.Expose;
import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "geofences",
       indexes = { @Index(name = "geofences_pkey", columnList = "id") })
public class GeoFence implements IsSerializable {

    public GeoFence() {
        type = GeoFenceType.LINE;
        color = "4169E1";
        radius = 30f;
        allDevices = true;
    }

    public GeoFence(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Expose
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public long getId() {
        return id;
    }

    @Expose
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

    @Expose
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
    // for example: -1.342 1.23423,33.442324 54.3454
    @Column(length = 2048)
    private String points;

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    // for circular geo-fence contains radius, for line it's width
    private float radius;

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @GwtTransient
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_geofences",
            foreignKey = @ForeignKey(name = "users_geofences_fkey_geofence_id"),
            joinColumns = { @JoinColumn(name = "geofence_id", table = "geofences", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id", table = "users", referencedColumnName = "id") })
    private Set<User> users;

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    // indicates that this geo-fence is applied to all available devices
    // it is possible to configure geo-fence per device
    @Column(nullable = true)
    private boolean allDevices;

    public boolean isAllDevices() {
        return allDevices;
    }

    public void setAllDevices(boolean allDevices) {
        this.allDevices = allDevices;
    }

    @GwtTransient
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "devices_geofences",
            foreignKey = @ForeignKey(name = "devices_geofences_fkey_geofence_id"),
            joinColumns = { @JoinColumn(name = "geofence_id", table = "geofences", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "device_id", table = "devices", referencedColumnName = "id") })
    private Set<Device> devices;

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    // collection used to transfer devices through GWT RPC and JSON
    @Transient
    private Set<Device> transferDevices;

    public Set<Device> getTransferDevices() {
        return transferDevices;
    }

    public void setTransferDevices(Set<Device> transferDevices) {
        this.transferDevices = transferDevices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GeoFence)) return false;

        GeoFence geoFence = (GeoFence) o;

        if (getId() != geoFence.getId()) return false;
        if (getName() != null ? !getName().equals(geoFence.getName()) : geoFence.getName() != null) return false;
        if (getType() != geoFence.getType()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
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

    public void points(LonLat... points) {
        if (points == null) {
            setPoints(null);
            return;
        }

        String strPoints = "";
        for (LonLat point : points) {
            if (strPoints.length() > 0) {
                strPoints += ',';
            }
            strPoints += point.lon + " " + point.lat;
        }
        setPoints(strPoints);
    }

    public void copyFrom(GeoFence geoFence) {
        id = geoFence.id;
        name = geoFence.name;
        description = geoFence.description;
        color = geoFence.color;
        type = geoFence.type;
        points = geoFence.points;
        radius = geoFence.radius;
        allDevices = geoFence.allDevices;
        transferDevices = new HashSet<Device>(geoFence.getTransferDevices());
    }

    @Override
    public String toString() {
        return name;
    }
}
