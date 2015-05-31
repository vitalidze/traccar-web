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

public class DeviceIconDTO implements IsSerializable {
    @Expose
    private long id;

    @Expose
    private PictureDTO defaultIcon;

    @Expose
    private PictureDTO selectedIcon;

    @Expose
    private PictureDTO offlineIcon;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PictureDTO getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(PictureDTO defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    public PictureDTO getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(PictureDTO selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public PictureDTO getOfflineIcon() {
        return offlineIcon;
    }

    public void setOfflineIcon(PictureDTO offlineIcon) {
        this.offlineIcon = offlineIcon;
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

    private String URL(PictureDTO picture) {
        return picture == null ? "" : (PictureDTO.URL_PREFIX + picture.getId());
    }
}
