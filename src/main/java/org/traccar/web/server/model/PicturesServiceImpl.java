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
package org.traccar.web.server.model;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.traccar.web.client.model.PicturesService;
import org.traccar.web.server.entity.DeviceIcon;
import org.traccar.web.server.entity.Picture;
import org.traccar.web.shared.model.DeviceIconDTO;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PicturesServiceImpl extends RemoteServiceServlet implements PicturesService {
    @Inject
    private Provider<EntityManager> entityManager;

    @Transactional
    @RequireUser
    @Override
    public List<DeviceIconDTO> getMarkerPictures() {
        List<DeviceIcon> icons = entityManager.get().createQuery("SELECT i FROM DeviceIcon i ORDER BY i.id DESC", DeviceIcon.class)
                .getResultList();
        List<DeviceIconDTO> result = new ArrayList<DeviceIconDTO>(icons.size());
        for (DeviceIcon icon : icons) {
            result.add(icon.dto());
        }
        return result;
    }

    @Transactional
    @RequireUser
    @Override
    public DeviceIconDTO addMarkerPicture(DeviceIconDTO markerDTO) {
        DeviceIcon newMarker = new DeviceIcon();
        if (markerDTO.getDefaultIcon() != null) {
            newMarker.setDefaultIcon(entityManager.get().find(Picture.class, markerDTO.getDefaultIcon().getId()));
        }
        if (markerDTO.getSelectedIcon() != null) {
            newMarker.setSelectedIcon(entityManager.get().find(Picture.class, markerDTO.getSelectedIcon().getId()));
        }
        if (markerDTO.getOfflineIcon() != null) {
            newMarker.setOfflineIcon(entityManager.get().find(Picture.class, markerDTO.getOfflineIcon().getId()));
        }
        entityManager.get().persist(newMarker);
        return newMarker.dto();
    }

    @Transactional
    @RequireUser
    @Override
    public DeviceIconDTO updateMarkerPicture(DeviceIconDTO markerDTO) {
        DeviceIcon marker = entityManager.get().find(DeviceIcon.class, markerDTO.getId());
        marker.setDefaultIcon(markerDTO.getDefaultIcon() == null ? null : entityManager.get().find(Picture.class, markerDTO.getDefaultIcon().getId()));
        marker.setSelectedIcon(markerDTO.getSelectedIcon() == null ? null : entityManager.get().find(Picture.class, markerDTO.getSelectedIcon().getId()));
        marker.setOfflineIcon(markerDTO.getOfflineIcon() == null ? null : entityManager.get().find(Picture.class, markerDTO.getOfflineIcon().getId()));
        return marker.dto();
    }

    @Transactional
    @RequireUser
    @Override
    public void removeMarkerPicture(DeviceIconDTO marker) {
        DeviceIcon icon = entityManager.get().find(DeviceIcon.class, marker.getId());
        entityManager.get().remove(icon);
        for (Picture picture : new Picture[] { icon.getOfflineIcon(), icon.getSelectedIcon(), icon.getOfflineIcon() }) {
            if (picture == null) {
                continue;
            }
            List<DeviceIcon> relatedIcons = entityManager.get().createQuery("SELECT d FROM DeviceIcon d WHERE d.defaultIcon=:p OR d.selectedIcon=:p OR d.offlineIcon=:p", DeviceIcon.class)
                    .setParameter("p", picture)
                    .getResultList();
            if (relatedIcons.isEmpty()) {
                entityManager.get().remove(picture);
            }
        }
    }
}
