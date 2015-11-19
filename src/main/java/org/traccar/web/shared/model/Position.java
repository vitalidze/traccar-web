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
package org.traccar.web.shared.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.user.client.rpc.GwtTransient;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.traccar.web.client.ArchiveStyle;

@Entity
@Table(name = "positions",
       indexes = { @Index(name="positionsIndex", columnList="device_id,time") })
public class Position implements IsSerializable, Cloneable {

    private static final long serialVersionUID = 1;

    public enum Status {
        OFFLINE, LATEST;
    }

    public Position() {
    }

    public Position(Position position) {
        id = position.id;
        device = position.device;
        time = position.time;
        valid = position.valid;
        latitude = position.latitude;
        longitude = position.longitude;
        altitude = position.altitude;
        speed = position.speed;
        course = position.course;
        power = position.power;
        address = position.address;
        other = position.other;
        distance = position.distance;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public long getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "positions_fkey_device_id"))
    private Device device;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    private Boolean valid;

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    private Double latitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    private Double longitude;

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    private Double altitude;

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    private Double speed;

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    private Double course;

    public Double getCourse() {
        return course;
    }

    public void setCourse(Double course) {
        this.course = course;
    }

    /**
     * @deprecated not used anymore by the traccar backend, left for backwards compatibility
     */
    @JsonIgnore
    private Double power;

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String other;

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    private String protocol;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    private Date serverTime;

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    @GwtTransient
    @JsonIgnore
    private transient Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @GwtTransient
    @JsonIgnore
    private transient PositionIcon icon;

    public PositionIcon getIcon() {
        return icon;
    }

    public void setIcon(PositionIcon icon) {
        this.icon = icon;
    }

    @GwtTransient
    @JsonIgnore
    private transient Date idleSince;

    public Date getIdleSince() {
        return idleSince;
    }

    public void setIdleSince(Date idleSince) {
        this.idleSince = idleSince;
    }

    @Transient
    @JsonIgnore
    private double distance;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Transient
    private List<GeoFence> geoFences;

    public List<GeoFence> getGeoFences() {
        return geoFences;
    }

    public void setGeoFences(List<GeoFence> geoFences) {
        this.geoFences = geoFences;
    }


    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Position)) {
            return false;
        }

        Position p = (Position) object;

        return this.id == p.id;
    }
}
