/*
 * Copyright 2018 Vitaly Litvak (vitavaque@gmail.com)
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

@Entity
@Table(name = "event_rules")
public class EventRule implements IsSerializable {
    public static final String TIME_REGEX = "((1[0-2]|\\d)(:[0-5]\\d)?[ap]m)";
    public static final String TIME_FRAME_REGEX = TIME_REGEX + "\\-" + TIME_REGEX;
    public static final String DAY_OF_WEEK_REGEX = "([0-7]|Mon|Tue|Wed|Thu|Fri|Sat|Sun)";
    public static final String DAY_OF_WEEK_FRAME_REGEX = DAY_OF_WEEK_REGEX + "(\\-" + DAY_OF_WEEK_REGEX + ")?";
    public static final String COURSE_REGEX = "(\\d{1,3})\\-(\\d{1,3})";

    public EventRule() {
    }

    public EventRule(User user) {
        this.user = user;
    }

    public EventRule(long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @JsonIgnore
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @GwtTransient
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "event_rules_fkey_user_id"))
    @JsonIgnore
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "event_rules_fkey_device_id"), nullable = false)
    @JsonIgnore
    private Device device;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "event_rules_fkey_geofence_id"))
    @JsonIgnore
    private GeoFence geoFence;

    public GeoFence getGeoFence() {
        return geoFence;
    }

    public void setGeoFence(GeoFence geoFence) {
        this.geoFence = geoFence;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceEventType deviceEventType;

    public DeviceEventType getDeviceEventType() {
        return deviceEventType;
    }

    public void setDeviceEventType(DeviceEventType deviceEventType) {
        this.deviceEventType = deviceEventType;
    }

    private String timeFrame;

    public String getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(String timeFrame) {
        this.timeFrame = timeFrame;
    }

    private String dayOfWeek;

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    private String course;

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public EventRule copyFromClient(EventRule eventRule) {
        id = eventRule.id;
        user = eventRule.user == null ? null : new User(eventRule.user);
        device = eventRule.device == null ? null : new Device(eventRule.device);
        geoFence = eventRule.geoFence == null ? null : new GeoFence().copyFrom(eventRule.geoFence);
        deviceEventType = eventRule.deviceEventType;
        timeFrame = eventRule.timeFrame;
        dayOfWeek = eventRule.dayOfWeek;
        course = eventRule.course;
        return this;
    }

    public EventRule copyFromServer(EventRule eventRules) {
        id = eventRules.id;
        deviceEventType = eventRules.deviceEventType;
        timeFrame = eventRules.timeFrame;
        dayOfWeek = eventRules.dayOfWeek;
        course = eventRules.course;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventRule eventRule = (EventRule) o;

        return getId() == eventRule.getId();

    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}
