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

import java.io.Serializable;
import java.util.Date;

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

import com.google.gson.annotations.Expose;
import com.google.gwt.user.client.rpc.GwtTransient;

import org.traccar.web.client.ArchiveStyle;

@Entity
@Table(name = "positions",
       indexes = { @Index(name="positionsIndex", columnList="device_id,time") })
public class Position implements Serializable, Cloneable {

    private static final long serialVersionUID = 1;

    public enum Status {
        ARCHIVE, OFFLINE, LATEST;
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
        trackColor = position.trackColor;
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "positions_fkey_device_id"))
    private Device device;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Expose
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Expose
    private Boolean valid;

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    @Expose
    private Double latitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Expose
    private Double longitude;

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Expose
    private Double altitude;

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    @Expose
    private Double speed;

    public Double getSpeed() {
        return speed;
    }

    @Expose
    private Double course;

    public Double getCourse() {
        return course;
    }

    @Expose
    private Double power;

    public Double getPower() {
        return power;
    }

    @Expose
    private String address;

    public String getAddress() {
        return address;
    }

    @Expose
    private String other;

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    private String trackColor;

    public String getTrackColor() {
        return trackColor == null ? ArchiveStyle.DEFAULT_COLOR : trackColor;
    }

    public void setTrackColor(String trackColor) {
        this.trackColor = trackColor;
    }

    @GwtTransient
    private transient Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @GwtTransient
    private transient PositionIconType iconType;

    public PositionIconType getIconType() {
        return iconType;
    }

    public void setIconType(PositionIconType iconType) {
        this.iconType = iconType;
    }

    @GwtTransient
    private transient Date idleSince;

    public Date getIdleSince() {
        return idleSince;
    }

    public void setIdleSince(Date idleSince) {
        this.idleSince = idleSince;
    }

    @Transient
    private double distance;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
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
