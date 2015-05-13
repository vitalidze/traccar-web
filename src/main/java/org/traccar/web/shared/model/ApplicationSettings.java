package org.traccar.web.shared.model;

import com.google.gson.annotations.Expose;
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;

@Entity
@Table(name="application_settings")
public class ApplicationSettings implements IsSerializable {

    private static final long serialVersionUID = 1;
    public static final short DEFAULT_UPDATE_INTERVAL = 15000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public ApplicationSettings() {
        registrationEnabled = true;
        updateInterval = DEFAULT_UPDATE_INTERVAL;
        defaultPasswordHash = PasswordHashMethod.PLAIN;
        eventRecordingEnabled = true;
        language = "default";
    }

    @Expose
    private boolean registrationEnabled;

    @Expose
    private Short updateInterval;

    @Enumerated(EnumType.STRING)
    @Expose
    private PasswordHashMethod defaultPasswordHash;

    @Expose
    @Column(nullable = true)
    private boolean disallowDeviceManagementByUsers;

    @Column(nullable = true)
    private boolean eventRecordingEnabled;

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

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ApplicationSettings)) {
            return false;
        }

        ApplicationSettings other = (ApplicationSettings) object;
        
        return this.id == other.id;
    }
}
