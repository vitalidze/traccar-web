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

import java.util.List;

public class DeviceDTO implements IsSerializable {
    public static final short DEFAULT_TIMEOUT = 5 * 60;

    private long id;
    @Expose
    private String uniqueId;

    @Expose
    private String name;

    private transient boolean follow;
    private transient boolean recordTrace;

    @Expose
    private int timeout = DEFAULT_TIMEOUT;

    @Expose
    private double idleSpeedThreshold;

    @Expose
    private DeviceIconDTO icon;

    @Expose
    private DeviceIconType iconType;

    private double odometer;

    private boolean autoUpdateOdometer;

    private List<MaintenanceDTO> maintenances;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean isRecordTrace() {
        return recordTrace;
    }

    public void setRecordTrace(boolean recordTrace) {
        this.recordTrace = recordTrace;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public double getIdleSpeedThreshold() {
        return idleSpeedThreshold;
    }

    public void setIdleSpeedThreshold(double idleSpeedThreshold) {
        this.idleSpeedThreshold = idleSpeedThreshold;
    }

    public DeviceIconType getIconType() {
        return iconType;
    }

    public void setIconType(DeviceIconType iconType) {
        this.iconType = iconType;
    }

    public DeviceIconDTO getIcon() {
        return icon;
    }

    public void setIcon(DeviceIconDTO icon) {
        this.icon = icon;
    }

    public double getOdometer() {
        return odometer;
    }

    public void setOdometer(double odometer) {
        this.odometer = odometer;
    }

    public boolean isAutoUpdateOdometer() {
        return autoUpdateOdometer;
    }

    public void setAutoUpdateOdometer(boolean autoUpdateOdometer) {
        this.autoUpdateOdometer = autoUpdateOdometer;
    }

    public List<MaintenanceDTO> getMaintenances() {
        return maintenances;
    }

    public void setMaintenances(List<MaintenanceDTO> maintenances) {
        this.maintenances = maintenances;
    }
}
