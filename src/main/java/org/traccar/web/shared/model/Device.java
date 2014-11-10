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

import com.google.gwt.user.client.rpc.GwtTransient;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "devices",
       indexes = { @Index(name = "devices_pkey", columnList = "id") },
       uniqueConstraints = { @UniqueConstraint(name = "devices_ukey_uniqueid", columnNames = "uniqueid") })
public class Device implements Serializable {

    private static final long serialVersionUID = 1;
    public static final short DEFAULT_TIMEOUT = 5 * 60;

    public Device() {
    }

    public Device(Device device) {
        id = device.id;
        uniqueId = device.uniqueId;
        name = device.name;
        timeout = device.timeout;
        idleSpeedThreshold = device.idleSpeedThreshold;
    }

    @Id
    @SequenceGenerator(name = "devices_id_seq", sequenceName = "devices_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "devices_id_seq")
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    public Long getId() {
        return id;
    }

    @GwtTransient
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "devices_fkey_position_id"))
    private Position latestPosition;

    public void setLatestPosition(Position latestPosition) {
        this.latestPosition = latestPosition;
    }

    public Position getLatestPosition() {
        return latestPosition;
    }

    private String uniqueId;

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private transient boolean follow;

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    private transient boolean recordTrace;

    public boolean isRecordTrace() {
        return recordTrace;
    }

    public void setRecordTrace(boolean recordTrace) {
        this.recordTrace = recordTrace;
    }

    /**
     * Consider device offline after 'timeout' seconds spent from last position
     */
    private int timeout = DEFAULT_TIMEOUT;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private double idleSpeedThreshold;

    public double getIdleSpeedThreshold() {
        return idleSpeedThreshold;
    }

    public void setIdleSpeedThreshold(double idleSpeedThreshold) {
        this.idleSpeedThreshold = idleSpeedThreshold;
    }

    // Hibernate bug HHH-8783: (http://hibernate.atlassian.net/browse/HHH-8783)
    //     ForeignKey(name) has no effect in JoinTable (and others).  It is
    //     reported as closed but the comments indicate it is still not fixed
    //     for @JoinTable() and targeted to be fixed in 5.x :-(.
    //                          
    @GwtTransient
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_devices",
               foreignKey = @ForeignKey(name = "users_devices_fkey_devices_id"),
               joinColumns = { @JoinColumn(name = "devices_id", table = "devices", referencedColumnName = "id") },
               inverseJoinColumns = { @JoinColumn(name = "users_id", table = "users", referencedColumnName = "id") })
    private Set<User> users;

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (uniqueId != null ? !uniqueId.equals(device.uniqueId) : device.uniqueId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uniqueId != null ? uniqueId.hashCode() : 0;
    }
}
