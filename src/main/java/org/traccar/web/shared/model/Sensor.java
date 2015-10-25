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
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.*;

@Entity
@Table(name = "sensors",
       indexes = { @Index(name = "sensors_pkey", columnList = "id") })
public class Sensor implements IsSerializable {

    public Sensor() {
        visible = true;
    }

    public Sensor(Sensor sensor) {
        copyFrom(sensor);
    }

    public Sensor(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "sensors_fkey_device_id"))
    private Device device;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Expose
    private String parameterName;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Expose
    private boolean visible;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Expose
    @Column(length = 2048)
    private String intervals;

    public String getIntervals() {
        return intervals;
    }

    public void setIntervals(String customIntervals) {
        this.intervals = customIntervals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sensor)) return false;

        Sensor that = (Sensor) o;

        if (getId() != that.getId()) return false;
        if (getDevice() != null ? !getDevice().equals(that.getDevice()) : that.getDevice() != null) return false;
        if (getParameterName() != null ? !getParameterName().equals(that.getParameterName()) : that.getParameterName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (getDevice() != null ? getDevice().hashCode() : 0);
        result = 31 * result + (getParameterName() != null ? getParameterName().hashCode() : 0);
        return result;
    }

    public void copyFrom(Sensor sensor) {
        id = sensor.id;
        name = sensor.name;
        device = sensor.device;
        parameterName = sensor.parameterName;
        description = sensor.description;
        visible = sensor.visible;
        intervals = sensor.intervals;
    }
}
