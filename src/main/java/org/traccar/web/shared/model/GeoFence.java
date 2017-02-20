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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "geofences",
       indexes = { @Index(name = "geofences_pkey", columnList = "id") })
public class GeoFence implements IsSerializable {

    public GeoFence() {
        type = GeoFenceType.POLYGON;
        color = "4169E1";
        radius = 30f;
        allDevices = true;
    }

    public GeoFence(long id, String name) {
        this.id = id;
        this.name = name;
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

    @JsonIgnore
    private String description;

    @JsonIgnore
    public String getDescription() {
        return description;
    }

    @JsonProperty
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
    @JsonIgnore
    private GeoFenceType type;

    @JsonIgnore
    public GeoFenceType getType() {
        return type;
    }

    @JsonProperty
    public void setType(GeoFenceType type) {
        this.type = type;
    }

    // will hold list of lon/lat pairs of base points for this geo-fence separated by comma
    // for example: -1.342 1.23423,33.442324 54.3454
    @Column(length = 2048)
    @JsonIgnore
    private String points;

    @JsonIgnore
    public String getPoints() {
        return points;
    }

    @JsonProperty
    public void setPoints(String points) {
        this.points = points;
    }

    // for circular geo-fence contains radius, for line it's width
    @JsonIgnore
    private float radius;

    @JsonIgnore
    public float getRadius() {
        return radius;
    }

    @JsonProperty
    public void setRadius(float radius) {
        this.radius = radius;
    }

    @GwtTransient
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_geofences",
            foreignKey = @ForeignKey(name = "users_geofences_fkey_geofence_id"),
            joinColumns = { @JoinColumn(name = "geofence_id", table = "geofences", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id", table = "users", referencedColumnName = "id") })
    @JsonIgnore
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
    @JsonIgnore
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
    @JsonIgnore
    private Set<Device> devices;

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    // collection used to transfer devices through GWT RPC and JSON
    @Transient
    @JsonIgnore
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

    public List<LonLat> points() {
        if (getPoints() == null || getPoints().isEmpty()) {
            return Collections.emptyList();
        }

        List<LonLat> result = new LinkedList<>();
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

    public GeoFence copyFrom(GeoFence geoFence) {
        id = geoFence.id;
        name = geoFence.name;
        description = geoFence.description;
        color = geoFence.color;
        type = geoFence.type;
        points = geoFence.points;
        radius = geoFence.radius;
        allDevices = geoFence.allDevices;
        if (geoFence.transferDevices != null) {
            transferDevices = new HashSet<>(geoFence.getTransferDevices());
        }
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
