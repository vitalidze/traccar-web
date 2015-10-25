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

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate implements IsSerializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    @GwtTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "ntemplates_fkey_settings_id"))
    private NotificationSettings settings;

    @Enumerated(EnumType.STRING)
    private DeviceEventType type;

    public NotificationTemplate() {
        subject = "[traccar-web] Notification";
        contentType = "text/plain; charset=utf-8";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public NotificationSettings getSettings() {
        return settings;
    }

    public void setSettings(NotificationSettings settings) {
        this.settings = settings;
    }

    public DeviceEventType getType() {
        return type;
    }

    public void setType(DeviceEventType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof NotificationTemplate)) return false;

        NotificationTemplate that = (NotificationTemplate) o;

        if (!getSettings().equals(that.getSettings())) return false;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        int result = settings.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    private String subject;
    @Column(length = 4096)
    private String body;
    private String contentType;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void copyFrom(NotificationTemplate t) {
        setSubject(t.getSubject());
        setBody(t.getBody());
        setContentType(t.getContentType());
    }
}
