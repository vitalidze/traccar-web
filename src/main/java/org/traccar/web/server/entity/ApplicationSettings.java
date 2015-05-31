package org.traccar.web.server.entity;

import org.traccar.web.shared.model.ApplicationSettingsDTO;
import org.traccar.web.shared.model.PasswordHashMethod;

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
public class ApplicationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public ApplicationSettings() {
    }

    public static ApplicationSettings defaults() {
        return new ApplicationSettings().from(new ApplicationSettingsDTO());
    }

    private boolean registrationEnabled;

    private Short updateInterval;

    @Enumerated(EnumType.STRING)
    private PasswordHashMethod defaultPasswordHash;

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

    public ApplicationSettingsDTO dto() {
        ApplicationSettingsDTO dto = new ApplicationSettingsDTO();
        dto.setId(id);
        dto.setDefaultHashImplementation(getDefaultHashImplementation());
        dto.setDisallowDeviceManagementByUsers(isDisallowDeviceManagementByUsers());
        dto.setEventRecordingEnabled(isEventRecordingEnabled());
        dto.setLanguage(getLanguage());
        dto.setRegistrationEnabled(getRegistrationEnabled());
        dto.setUpdateInterval(getUpdateInterval());
        return dto;
    }

    public ApplicationSettings from(ApplicationSettingsDTO dto) {
        setDisallowDeviceManagementByUsers(dto.isDisallowDeviceManagementByUsers());
        setUpdateInterval(dto.getUpdateInterval());
        setRegistrationEnabled(dto.getRegistrationEnabled());
        setLanguage(dto.getLanguage());
        setDefaultHashImplementation(dto.getDefaultHashImplementation());
        setEventRecordingEnabled(dto.isEventRecordingEnabled());
        return this;
    }
}
