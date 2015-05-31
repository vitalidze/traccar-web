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

import java.util.HashSet;
import java.util.Set;

public class UserDTO implements IsSerializable {

    @Expose
    private long id;
    private String email;
    @Expose
    private String login;
    private String password;
    @Expose
    private boolean admin;
    @Expose
    private boolean manager;
    @Expose
    private boolean readOnly;
    @Expose
    private UserSettingsDTO userSettings;
    private Set<DeviceEventType> notificationEvents;

    public UserDTO() {
    }

    public UserDTO(UserDTO user) {
        setId(user.getId());
        setAdmin(user.isAdmin());
        setLogin(user.getLogin());
        setPassword(user.getPassword());
        setManager(user.isManager());
        setEmail(user.getEmail());
        setUserSettings(user.getUserSettings());
        setReadOnly(user.isReadOnly());
        if (user.getNotificationEvents() != null) {
            setNotificationEvents(new HashSet<DeviceEventType>(user.getNotificationEvents()));
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public UserSettingsDTO getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettingsDTO userSettings) {
        this.userSettings = userSettings;
    }

    public Set<DeviceEventType> getNotificationEvents() {
        return notificationEvents;
    }

    public void setNotificationEvents(Set<DeviceEventType> notificationEvents) {
        this.notificationEvents = notificationEvents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof UserDTO)) return false;

        UserDTO user = (UserDTO) o;

        if (getLogin() != null ? !getLogin().equals(user.getLogin()) : user.getLogin() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getLogin() != null ? getLogin().hashCode() : 0;
    }
}
