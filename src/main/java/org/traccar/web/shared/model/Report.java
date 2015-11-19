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
import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "reports",
        indexes = { @Index(name = "reports_pkey", columnList = "id") })
public class Report implements IsSerializable {

    public Report() {
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

    @Enumerated(EnumType.STRING)
    private ReportType type;

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    @GwtTransient
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "reports_users",
            foreignKey = @ForeignKey(name = "reports_users_fkey_report_id"),
            joinColumns = { @JoinColumn(name = "report_id", table = "reports", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id", table = "users", referencedColumnName = "id") })
    @JsonIgnore
    private Set<User> users;

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "reports_devices",
            foreignKey = @ForeignKey(name = "reports_devices_fkey_report_id"),
            joinColumns = { @JoinColumn(name = "report_id", table = "reports", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "device_id", table = "devices", referencedColumnName = "id") })
    private Set<Device> devices;

    public Set<Device> getDevices() {
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "reports_geofences",
            foreignKey = @ForeignKey(name = "reports_geofences_fkey_report_id"),
            joinColumns = { @JoinColumn(name = "report_id", table = "reports", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "geofence_id", table = "geofences", referencedColumnName = "id") })
    private Set<GeoFence> geoFences;

    public Set<GeoFence> getGeoFences() {
        return geoFences;
    }

    public void setGeoFences(Set<GeoFence> geoFences) {
        this.geoFences = geoFences;
    }

    @Enumerated(EnumType.STRING)
    private Period period;

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date fromDate;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date toDate;

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    private boolean includeMap;

    public boolean isIncludeMap() {
        return includeMap;
    }

    public void setIncludeMap(boolean displayMap) {
        this.includeMap = displayMap;
    }

    @Column(nullable = true)
    private boolean disableFilter;

    public boolean isDisableFilter() {
        return disableFilter;
    }

    public void setDisableFilter(boolean disableFilter) {
        this.disableFilter = disableFilter;
    }

    @Column(nullable = true)
    private boolean preview = true;

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public Report copyFrom(Report report) {
        this.id = report.id;
        this.name = report.name;
        this.type = report.type;
        this.period = report.period;
        this.fromDate = report.fromDate;
        this.toDate = report.toDate;
        this.includeMap = report.includeMap;
        this.disableFilter = report.disableFilter;
        this.preview = report.preview;
        return this;
    }
}
