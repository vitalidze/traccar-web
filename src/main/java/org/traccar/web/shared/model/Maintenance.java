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
import java.io.Serializable;

@Entity
@Table(name = "maintenances",
       indexes = { @Index(name = "maintenances_pkey", columnList = "id") })
public class Maintenance implements Serializable {

    public Maintenance() {}

    public Maintenance(Maintenance maintenance) {
        copyFrom(maintenance);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public long getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = "maintenances_fkey_device_id"))
    private Device device;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    // sequence number of this interval
    private int indexNo;

    public int getIndexNo() {
        return indexNo;
    }

    public void setIndexNo(int indexNo) {
        this.indexNo = indexNo;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // how often to perform service - value in kilometers
    private double serviceInterval;

    public double getServiceInterval() {
        return serviceInterval;
    }

    public void setServiceInterval(double serviceInterval) {
        this.serviceInterval = serviceInterval;
    }

    // odometer value when service was last performed
    private double lastService;

    public double getLastService() {
        return lastService;
    }

    public void setLastService(double lastService) {
        this.lastService = lastService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Maintenance that = (Maintenance) o;

        if (id != that.id) return false;
        if (indexNo != that.indexNo) return false;
        if (device != null ? !device.equals(that.device) : that.device != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (device != null ? device.hashCode() : 0);
        result = 31 * result + indexNo;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public void copyFrom(Maintenance maintenance) {
        id = maintenance.id;
        name = maintenance.name;
        indexNo = maintenance.indexNo;
        device = maintenance.device;
        serviceInterval = maintenance.serviceInterval;
        lastService = maintenance.lastService;
    }
}
