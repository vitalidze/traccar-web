package org.traccar.web.shared.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="application_settings")
public class ApplicationSettings implements Serializable {

    private static final long serialVersionUID = 1;
    public static final short DEFAULT_UPDATE_INTERVAL = 15000;
    public static final String DEFAULT_PASSWORD_HASH = "plain";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public ApplicationSettings() {
        registrationEnabled = true;
        updateInterval = Short.valueOf(DEFAULT_UPDATE_INTERVAL);
        defaultPasswordHash = DEFAULT_PASSWORD_HASH;
    }

    @Expose
    private boolean registrationEnabled;

    @Expose
    private Short updateInterval;

    @Expose
    private String defaultPasswordHash;

    @Expose
    @Column(nullable = true)
    private boolean disallowDeviceManagementByUsers;

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

    public String getDefaultHashImplementation() {
        return defaultPasswordHash;
    }

    public void setDefaultHashImplementation(String hash) {
        if (hash.equals("sha512")) {
            this.defaultPasswordHash = hash;
        } else {
            this.defaultPasswordHash = DEFAULT_PASSWORD_HASH;
        }
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
