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

public class ApplicationSettingsDTO implements IsSerializable {
    public static final short DEFAULT_UPDATE_INTERVAL = 15000;

    public ApplicationSettingsDTO() {
        registrationEnabled = true;
        updateInterval = DEFAULT_UPDATE_INTERVAL;
        defaultPasswordHash = PasswordHashMethod.PLAIN;
        eventRecordingEnabled = true;
        language = "default";
    }

    private long id;

    @Expose
    private boolean registrationEnabled;

    @Expose
    private Short updateInterval;

    @Expose
    private PasswordHashMethod defaultPasswordHash;

    @Expose
    private boolean disallowDeviceManagementByUsers;

    private boolean eventRecordingEnabled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public boolean getRegistrationEnabled() {
        return registrationEnabled;
    }

    public Short getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(Short updateInterval) {
        this.updateInterval = updateInterval;
    }

    public boolean isDisallowDeviceManagementByUsers() {
        return disallowDeviceManagementByUsers;
    }

    public void setDisallowDeviceManagementByUsers(boolean disallowDeviceManagementByUsers) {
        this.disallowDeviceManagementByUsers = disallowDeviceManagementByUsers;
    }

    public PasswordHashMethod getDefaultHashImplementation() {
        return defaultPasswordHash;
    }

    public void setDefaultHashImplementation(PasswordHashMethod hash) {
        this.defaultPasswordHash = hash;
    }

    public boolean isEventRecordingEnabled() {
        return eventRecordingEnabled;
    }

    public void setEventRecordingEnabled(boolean eventRecordingEnabled) {
        this.eventRecordingEnabled = eventRecordingEnabled;
    }

    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
