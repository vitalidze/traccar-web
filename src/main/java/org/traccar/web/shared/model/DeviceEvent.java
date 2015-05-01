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
import java.util.Date;

@Entity
@Table(name = "events",
       indexes = { @Index(name="events_position_event_type", columnList="position_id,type"),
                   @Index(name="events_sent_event_type", columnList="notificationSent,type") })
public class DeviceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "events_fkey_device_id"))
    private Device device;

    @Enumerated(EnumType.STRING)
    private DeviceEventType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "events_fkey_position_id"))
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "events_fkey_geofence_id"))
    private GeoFence geoFence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "events_fkey_maintenance_id"))
    private Maintenance maintenance;

    private boolean notificationSent;

    public DeviceEvent() {
    }

    public DeviceEvent(Date time, Device device, Position position, GeoFence geoFence, Maintenance maintenance) {
        this.time = time;
        this.device = device;
        this.position = position;
        this.geoFence = geoFence;
        this.maintenance = maintenance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public GeoFence getGeoFence() {
        return geoFence;
    }

    public void setGeoFence(GeoFence geoFence) {
        this.geoFence = geoFence;
    }

    public DeviceEventType getType() {
        return type;
    }

    public void setType(DeviceEventType eventType) {
        this.type = eventType;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Maintenance getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Maintenance maintenance) {
        this.maintenance = maintenance;
    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof DeviceEvent)) return false;

        DeviceEvent that = (DeviceEvent) o;

        if (getDevice() != null ? !getDevice().equals(that.getDevice()) : that.getDevice() != null) return false;
        if (getGeoFence() != null ? !getGeoFence().equals(that.getGeoFence()) : that.getGeoFence() != null) return false;
        if (getPosition() != null ? !getPosition().equals(that.getPosition()) : that.getPosition() != null) return false;
        if (getMaintenance() != null ? !getMaintenance().equals(that.getMaintenance()) : that.getMaintenance() != null) return false;
        if (!getTime().equals(that.getTime())) return false;
        if (getType() != that.getType()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getTime().hashCode();
        result = 31 * result + (getDevice() != null ? getDevice().hashCode() : 0);
        result = 31 * result + getType().hashCode();
        result = 31 * result + (getPosition() != null ? getPosition().hashCode() : 0);
        result = 31 * result + (getGeoFence() != null ? getGeoFence().hashCode() : 0);
        result = 31 * result + (getMaintenance() != null ? getMaintenance().hashCode() : 0);
        return result;
    }
}
