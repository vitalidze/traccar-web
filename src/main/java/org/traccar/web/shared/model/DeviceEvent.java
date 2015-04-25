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

    private boolean notificationSent;

    public DeviceEvent() {
    }

    public DeviceEvent(Date time, Device device, Position position, GeoFence geoFence) {
        this.time = time;
        this.device = device;
        this.position = position;
        this.geoFence = geoFence;
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

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceEvent that = (DeviceEvent) o;

        if (device != null ? !device.equals(that.device) : that.device != null) return false;
        if (geoFence != null ? !geoFence.equals(that.geoFence) : that.geoFence != null) return false;
        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        if (!time.equals(that.time)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + (device != null ? device.hashCode() : 0);
        result = 31 * result + type.hashCode();
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (geoFence != null ? geoFence.hashCode() : 0);
        return result;
    }
}
