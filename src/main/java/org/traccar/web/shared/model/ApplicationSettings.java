package org.traccar.web.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.*;

@Entity
@Table(name="application_settings")
public class ApplicationSettings implements IsSerializable {

    private static final long serialVersionUID = 1;
    public static final short DEFAULT_UPDATE_INTERVAL = 15000;
    public static final short DEFAULT_NOTIFICATION_EXPIRATION_PERIOD = 12 * 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    @JsonIgnore
    private long id;

    public ApplicationSettings() {
        registrationEnabled = true;
        updateInterval = DEFAULT_UPDATE_INTERVAL;
        defaultPasswordHash = PasswordHashMethod.MD5;
        eventRecordingEnabled = true;
        language = "default";
        notificationExpirationPeriod = DEFAULT_NOTIFICATION_EXPIRATION_PERIOD;
        matchServiceType = MatchServiceType.OSRM_V4;
        matchServiceURL = matchServiceType.getDefaultURL();
    }

    private boolean registrationEnabled;

    private Short updateInterval;

    @Enumerated(EnumType.STRING)
    private PasswordHashMethod defaultPasswordHash;

    @Column(nullable = true)
    private boolean disallowDeviceManagementByUsers;

    @Column(nullable = true)
    @JsonIgnore
    private boolean eventRecordingEnabled;

    @Column(nullable = true)
    private int notificationExpirationPeriod;

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

    @JsonIgnore
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @JsonIgnore
    private String salt;

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @JsonIgnore
    private String bingMapsKey;

    public String getBingMapsKey() {
        return bingMapsKey;
    }

    public void setBingMapsKey(String bingMapsKey) {
        this.bingMapsKey = bingMapsKey;
    }

    public int getNotificationExpirationPeriod() {
        return notificationExpirationPeriod;
    }

    public void setNotificationExpirationPeriod(int notificationExpirationPeriod) {
        this.notificationExpirationPeriod = notificationExpirationPeriod;
    }

    @Enumerated(EnumType.STRING)
    private MatchServiceType matchServiceType;

    public MatchServiceType getMatchServiceType() {
        return matchServiceType;
    }

    public void setMatchServiceType(MatchServiceType matchServiceType) {
        this.matchServiceType = matchServiceType;
    }

    private String matchServiceURL;

    public String getMatchServiceURL() {
        return matchServiceURL;
    }

    public void setMatchServiceURL(String matchServiceURL) {
        this.matchServiceURL = matchServiceURL;
    }

    @Column(nullable = true)
    private boolean allowCommandsOnlyForAdmins;

    public boolean isAllowCommandsOnlyForAdmins() {
        return allowCommandsOnlyForAdmins;
    }

    public void setAllowCommandsOnlyForAdmins(boolean allowCommandsOnlyForAdmins) {
        this.allowCommandsOnlyForAdmins = allowCommandsOnlyForAdmins;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "appsettings_fkey_usersettings_id"))
    @GwtTransient
    @JsonIgnore
    private UserSettings userSettings;

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
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
