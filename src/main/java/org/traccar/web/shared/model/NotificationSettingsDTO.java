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

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.HashMap;
import java.util.Map;

public class NotificationSettingsDTO implements IsSerializable, EMailSettings {
    private long id;
    private String fromAddress;
    private String server;
    private boolean useAuthorization;
    private int port;
    private String username;
    private String password;
    private SecureConnectionType secureConnectionType;
    private String pushbulletAccessToken;
    private Map<DeviceEventType, NotificationTemplateDTO> templates;

    public NotificationSettingsDTO() {
        server = "smtp.gmail.com";
        port = 465;
        useAuthorization = true;
        secureConnectionType = SecureConnectionType.SSL_TLS;
        templates = new HashMap<DeviceEventType, NotificationTemplateDTO>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public String getPushbulletAccessToken() {
        return pushbulletAccessToken;
    }

    public void setPushbulletAccessToken(String pushbulletAccessToken) {
        this.pushbulletAccessToken = pushbulletAccessToken;
    }

    public Map<DeviceEventType, NotificationTemplateDTO> getTemplates() {
        return templates;
    }

    public void setTemplates(Map<DeviceEventType, NotificationTemplateDTO> templates) {
        this.templates = templates;
    }
}
