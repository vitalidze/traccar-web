/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import com.google.gson.annotations.Expose;
import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

@Entity
@Table(name="users",
       uniqueConstraints = { @UniqueConstraint(name = "users_ukey_login", columnNames = "login") })
public class User implements IsSerializable, Cloneable {

    private static final long serialVersionUID = 1;

    public User() {
        admin = false;
        manager = false;
        transferNotificationEvents = new HashSet<DeviceEventType>();
    }

    public User(String login) {
        this.login = login;
    }

    public User(String login, String password) {
        this(login);
        this.password = password;
        this.geoFences = new HashSet<GeoFence>();
        this.managedUsers = new HashSet<User>();
    }

    public User(User user) {
        id = user.id;
        admin = user.admin;
        login = user.login;
        password = user.password;
        password_hash_method = user.password_hash_method;
        manager = user.manager;
        email = user.email;
        userSettings = user.userSettings;
        notifications = user.notifications;
        if (user.notificationEvents != null) {
            transferNotificationEvents = new HashSet<DeviceEventType>(user.notificationEvents);
        }
        maxNumOfDevices = user.maxNumOfDevices;
        readOnly = user.readOnly;
        companyName = user.companyName;
        firstName = user.firstName;
        lastName = user.lastName;
        phoneNumber = user.phoneNumber;
    }

    @Expose
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public long getId() {
        return id;
    }

    @Expose
    private String login;

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Expose
    private PasswordHashMethod password_hash_method;

    public void setPasswordHashMethod(PasswordHashMethod type) {
        this.password_hash_method = type;
    }

    public PasswordHashMethod getPasswordHashMethod() {
        // TODO temporary nullable to migrate from old database
        return (password_hash_method == null) ? PasswordHashMethod.PLAIN : password_hash_method;
    }

    // TODO temporary nullable to migrate from old database
    @Expose
    private Boolean admin;

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean getAdmin() {
        // TODO temporary nullable to migrate from old database
        return (admin == null) ? false : admin;
    }

    @Expose
    private Boolean manager;

    public Boolean getManager() {
        return (manager == null) ? false : manager;
    }

    public void setManager(Boolean manager) {
        this.manager = manager;
    }

    // Hibernate bug HHH-8783: (http://hibernate.atlassian.net/browse/HHH-8783)
    //     ForeignKey(name) has no effect in JoinTable (and others).  It is
    //     reported as closed but the comments indicate it is still not fixed
    //     for @JoinTable() and targeted to be fixed in 5.x :-(.
    //                          
    @GwtTransient
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_devices",
               foreignKey = @ForeignKey(name = "users_devices_fkey_users_id"),
               joinColumns = { @JoinColumn(name = "users_id", table = "users", referencedColumnName = "id") },
               inverseJoinColumns = { @JoinColumn(name = "devices_id", table = "devices", referencedColumnName = "id") })
    private Set<Device> devices = new HashSet<Device>();

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public Set<Device> getAllAvailableDevices() {
        Set<Device> devices = new HashSet<Device>();
        devices.addAll(getDevices());
        if (getManager()) {
            for (User managedUser : getManagedUsers()) {
                devices.addAll(managedUser.getAllAvailableDevices());
            }
        }
        return devices;
    }

    @GwtTransient
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_geofences",
            foreignKey = @ForeignKey(name = "users_geofences_fkey_user_id"),
            joinColumns = { @JoinColumn(name = "user_id", table = "users", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "geofence_id", table = "geofences", referencedColumnName = "id") })
    private Set<GeoFence> geoFences;

    public void setGeoFences(Set<GeoFence> geoFences) {
        this.geoFences = geoFences;
    }

    public Set<GeoFence> getGeoFences() {
        return geoFences;
    }

    public Set<GeoFence> getAllAvailableGeoFences() {
        Set<GeoFence> result = new HashSet<GeoFence>();
        result.addAll(getGeoFences());
        if (getManager()) {
            for (User user : getManagedUsers()) {
                result.addAll(user.getAllAvailableGeoFences());
            }
        }
        User managedBy = getManagedBy();
        while (managedBy != null) {
            result.addAll(managedBy.getGeoFences());
            managedBy = managedBy.getManagedBy();
        }

        return result;
    }

    public boolean hasAccessTo(GeoFence geoFence) {
        if (hasAccessOnLowerLevelTo(geoFence)) {
            return true;
        }
        User managedBy = getManagedBy();
        while (managedBy != null) {
            if (managedBy.getGeoFences().contains(geoFence)) {
                return true;
            }
            managedBy = managedBy.getManagedBy();
        }
        return false;
    }

    private boolean hasAccessOnLowerLevelTo(GeoFence geoFence) {
        if (getGeoFences().contains(geoFence)) {
            return true;
        }
        if (getManager()) {
            for (User user : getManagedUsers()) {
                if (user.hasAccessOnLowerLevelTo(geoFence)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Expose
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "users_fkey_usersettings_id"))
    private UserSettings userSettings;

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    @GwtTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "users_fkey_managedby_id"))
    private User managedBy;

    public User getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(User managedBy) {
        this.managedBy = managedBy;
    }

    @GwtTransient
    @OneToMany(mappedBy = "managedBy", fetch = FetchType.LAZY)
    private Set<User> managedUsers;

    public Set<User> getManagedUsers() {
        return managedUsers;
    }

    public void setManagedUsers(Set<User> managedUsers) {
        this.managedUsers = managedUsers;
    }

    public Set<User> getAllManagedUsers() {
        Set<User> result = new HashSet<User>();
        result.addAll(getManagedUsers());
        for (User managedUser : getManagedUsers()) {
            if (managedUser.getManager()) {
                result.addAll(managedUser.getAllManagedUsers());
            }
        }
        return result;
    }

    private String email;
    /**
     * @deprecated now user can select types of events for notifications
     */
    @Column(nullable = true)
    private boolean notifications;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Deprecated
    public boolean isNotifications() {
        return notifications;
    }

    @Deprecated
    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    @ElementCollection(targetClass = DeviceEventType.class)
    @JoinTable(name = "users_notifications", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @GwtTransient
    private Set<DeviceEventType> notificationEvents;

    public Set<DeviceEventType> getNotificationEvents() {
        return notificationEvents;
    }

    public void setNotificationEvents(Set<DeviceEventType> notificationEvents) {
        this.notificationEvents = notificationEvents;
    }

    @Transient
    private Set<DeviceEventType> transferNotificationEvents;

    public Set<DeviceEventType> getTransferNotificationEvents() {
        return transferNotificationEvents;
    }

    public void setTransferNotificationEvents(Set<DeviceEventType> transferNotificationEvents) {
        this.transferNotificationEvents = transferNotificationEvents;
    }

    @Expose
    @Column(nullable = true)
    private boolean readOnly;

    public boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Column(nullable = true)
    private boolean blocked;

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    private Integer maxNumOfDevices;

    public Integer getMaxNumOfDevices() {
        return maxNumOfDevices;
    }

    public void setMaxNumOfDevices(Integer maxNumOfDevices) {
        this.maxNumOfDevices = maxNumOfDevices;
    }

    private String companyName;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;

        User user = (User) o;

        if (getLogin() != null ? !getLogin().equals(user.getLogin()) : user.getLogin() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getLogin() != null ? getLogin().hashCode() : 0;
    }

    public boolean isExpired() {
        return getExpirationDate() != null && new Date().compareTo(getExpirationDate()) >= 0;
    }
}
