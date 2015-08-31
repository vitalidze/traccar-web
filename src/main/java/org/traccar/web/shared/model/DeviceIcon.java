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

import javax.persistence.*;

@Entity
@Table(name = "device_icons")
public class DeviceIcon implements IsSerializable {
    @Expose
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Expose
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "device_icons_fkey_def_icon_id"))
    private Picture defaultIcon;

    public Picture getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(Picture defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    @Expose
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "device_icons_fkey_sel_icon_id"))
    private Picture selectedIcon;

    public Picture getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(Picture selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    @Expose
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "device_icons_fkey_off_icon_id"))
    private Picture offlineIcon;

    public Picture getOfflineIcon() {
        return offlineIcon;
    }

    public void setOfflineIcon(Picture offlineIcon) {
        this.offlineIcon = offlineIcon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceIcon)) return false;

        DeviceIcon that = (DeviceIcon) o;
        return that.getId() == getId();
    }

    @Override
    public int hashCode() {
        return (int)(getId() ^ (getId() >>> 32));
    }

    public String defaultURL() {
        return URL(getDefaultIcon());
    }

    public String selectedURL() {
        return URL(getSelectedIcon());
    }

    public String offlineURL() {
        return URL(getOfflineIcon());
    }

    private String URL(Picture picture) {
        return picture == null ? "" : (Picture.URL_PREFIX + picture.getId());
    }
}
