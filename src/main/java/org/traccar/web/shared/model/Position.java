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

import com.google.gwt.user.client.rpc.GwtTransient;

import org.traccar.web.client.view.MarkerIconFactory;

@Entity
@Table(name = "positions",
       indexes = { @Index(name="positionsIndex", columnList="device_id,time") })
public class Position implements Serializable, Cloneable {

    private static final long serialVersionUID = 1;

    public enum Status {
        ARCHIVE(MarkerIconFactory.IconType.iconArchive),
        OFFLINE(MarkerIconFactory.IconType.iconOffline),
        LATEST(MarkerIconFactory.IconType.iconLatest);

        final MarkerIconFactory.IconType iconType;

        Status(MarkerIconFactory.IconType iconType) {
            this.iconType = iconType;
        }

        public MarkerIconFactory.IconType getIconType() {
            return iconType;
        }
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
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    public Long getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "positions_fkey_device_id"))
    private Device device;

    public Device getDevice() {
        return device;
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    public Date getTime() {
        return time;
    }

    private Boolean valid;

    public Boolean getValid() {
        return valid;
    }

    private Double latitude;

    public Double getLatitude() {
        return latitude;
    }

    private Double longitude;

    public Double getLongitude() {
        return longitude;
    }

    private Double altitude;

    public Double getAltitude() {
        return altitude;
    }

    private Double speed;

    public Double getSpeed() {
        return speed;
    }

    private Double course;

    public Double getCourse() {
        return course;
    }

    private Double power;

    public Double getPower() {
        return power;
    }

    private String address;

    public String getAddress() {
        return address;
    }

    private String other;

    public String getOther() {
        return other;
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
    private transient Date idleSince;

    public Date getIdleSince() {
        return idleSince;
    }

    public void setIdleSince(Date idleSince) {
        this.idleSince = idleSince;
    }

    // Added hashCode() and equals() to conform to JPA.

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Position)) {
            return false;
        }
        Position p = (Position) object;
        if ((this.id == null && p.id != null) || (this.id != null && !this.id.equals(p.id))) {
            return false;
        }
        return true;
    }
}
