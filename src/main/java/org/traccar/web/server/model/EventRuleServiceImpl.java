/*
 * Copyright 2018 Vitaly Litvak (vitavaque@gmail.com)
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
import org.traccar.web.client.model.EventRuleService;
import org.traccar.web.shared.model.AccessDeniedException;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.EventRule;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class EventRuleServiceImpl extends RemoteServiceServlet implements EventRuleService {
    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    private Provider<User> sessionUser;

    void checkAccess(User user) throws AccessDeniedException {
        if (user == null) {
            return;
        }

        User currentUser = sessionUser.get();

        if (!currentUser.getAdmin()
            && !(currentUser.getManager() && currentUser.getAllManagedUsers().contains(user))
            && !(currentUser.getId() == user.getId() && !user.getAdmin())) {
            throw new AccessDeniedException();
        }
    }

    @Transactional
    @RequireUser
    @Override
    public List<EventRule> getEventRules(User user) throws AccessDeniedException {
        if (user.getId() > 0) {
            checkAccess(user);
            return new ArrayList<>(entityManager.get().createQuery("SELECT x FROM EventRule x WHERE x.user = :user", EventRule.class).setParameter("user", user).getResultList());
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public EventRule addEventRule(User originalUser, EventRule eventRule) throws AccessDeniedException {
        EventRule toSave = new EventRule().copyFromServer(eventRule);
        User user = entityManager.get().find(User.class, originalUser.getId());
        checkAccess(user);
        Device device = eventRule.getDevice() == null ? null : entityManager.get().find(Device.class, eventRule.getDevice().getId());
        GeoFence geoFence = eventRule.getGeoFence() == null ? null : entityManager.get().find(GeoFence.class, eventRule.getGeoFence().getId());
        toSave.setUser(user);
        toSave.setDevice(device);
        toSave.setGeoFence(geoFence);

        user.getEventRules().add(toSave);
        entityManager.get().persist(toSave);

        return toSave;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public EventRule updateEventRule(User originalUser, EventRule eventRule) throws AccessDeniedException {
        EventRule toSave = entityManager.get().find(EventRule.class, eventRule.getId());
        User user = entityManager.get().find(User.class, originalUser.getId());
        Device device = eventRule.getDevice() == null ? null : entityManager.get().find(Device.class, eventRule.getDevice().getId());
        GeoFence geoFence = eventRule.getGeoFence() == null ? null : entityManager.get().find(GeoFence.class, eventRule.getGeoFence().getId());
        checkAccess(user);

        toSave.copyFromServer(eventRule);
        toSave.setUser(user);
        toSave.setDevice(device);
        toSave.setGeoFence(geoFence);

        return toSave;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public void removeEventRule(EventRule eventRule) throws AccessDeniedException {
        checkAccess(eventRule.getUser());
        EventRule toRemove = entityManager.get().find(EventRule.class, eventRule.getId());
        entityManager.get().remove(toRemove);
    }

}
