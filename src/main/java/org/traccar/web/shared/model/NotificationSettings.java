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

import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "notification_settings")
public class NotificationSettings implements IsSerializable {
    public enum SecureConnectionType {
        NONE("None"),
        SSL_TLS("SSL/TLS"),
        STARTTLS("STARTTLS");

        final String title;

        SecureConnectionType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public NotificationSettings() {
        server = "smtp.gmail.com";
        port = 465;
        useAuthorization = true;
        secureConnectionType = SecureConnectionType.SSL_TLS;
        templates = new HashSet<NotificationTemplate>();
        transferTemplates = new HashMap<DeviceEventType, NotificationTemplate>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    @GwtTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "nsettings_fkey_user_id"))
    private User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        return 17 * user.hashCode() + 31 * (int) (id ^ (id >>> 32));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NotificationSettings)) {
            return false;
        }

        NotificationSettings other = (NotificationSettings) object;

        return this.user.equals(other.user) && this.id == other.id;
    }

    private String fromAddress;
    private String server;
    private boolean useAuthorization;
    private int port;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private SecureConnectionType secureConnectionType;

    @GwtTransient
    @OneToMany(mappedBy = "settings", fetch = FetchType.LAZY)
    private Set<NotificationTemplate> templates;

    @Transient
    private Map<DeviceEventType, NotificationTemplate> transferTemplates;

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isUseAuthorization() {
        return useAuthorization;
    }

    public void setUseAuthorization(boolean useAuthorization) {
        this.useAuthorization = useAuthorization;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SecureConnectionType getSecureConnectionType() {
        return secureConnectionType;
    }

    public void setSecureConnectionType(SecureConnectionType secureConnectionType) {
        this.secureConnectionType = secureConnectionType;
    }

    private String pushbulletAccessToken;

    public String getPushbulletAccessToken() {
        return pushbulletAccessToken;
    }

    public void setPushbulletAccessToken(String pushbulletApiKey) {
        this.pushbulletAccessToken = pushbulletApiKey;
    }

    public Set<NotificationTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(Set<NotificationTemplate> templates) {
        this.templates = templates;
    }

    public Map<DeviceEventType, NotificationTemplate> getTransferTemplates() {
        return transferTemplates;
    }

    public void setTransferTemplates(Map<DeviceEventType, NotificationTemplate> transferTemplates) {
        this.transferTemplates = transferTemplates;
    }

    public void copyFrom(NotificationSettings s) {
        setFromAddress(s.getFromAddress());
        setServer(s.getServer());
        setUseAuthorization(s.isUseAuthorization());
        setPort(s.getPort());
        setUsername(s.getUsername());
        setPassword(s.getPassword());
        setSecureConnectionType(s.getSecureConnectionType());
        setPushbulletAccessToken(s.getPushbulletAccessToken());
    }

    public NotificationTemplate findTemplate(DeviceEventType type) {
        if (getTransferTemplates() == null) {
            setTransferTemplates(new HashMap<DeviceEventType, NotificationTemplate>());
        }
        if (getTransferTemplates().size() < getTemplates().size()) {
            for (NotificationTemplate template : getTemplates()) {
                getTransferTemplates().put(template.getType(), template);
            }
        }
        return getTransferTemplates().get(type);
    }
}
