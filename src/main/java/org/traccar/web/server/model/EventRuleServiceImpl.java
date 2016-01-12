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
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.EventRuleService;
import org.traccar.web.shared.model.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.logging.Logger;

@Singleton
public class EventRuleServiceImpl extends RemoteServiceServlet implements EventRuleService {
    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    private Provider<User> sessionUser;

    @Inject
    private DataService dataService;

    private Logger logger = Logger.getLogger(EventRuleServiceImpl.class.getName());

    @Transactional
    @RequireUser
    @Override
    public List<EventRule> getEventRules(User user) {
        List<EventRule> eventRules;

        if (user.getId() > 0) {
            eventRules = entityManager.get().createQuery("SELECT x FROM EventRule x WHERE x.user = :user", EventRule.class).setParameter("user", user).getResultList();
            return new ArrayList<>(eventRules);
        } else {
            return new ArrayList<>();
        }

    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public EventRule addEventRule(User originalUser, EventRule eventRule) {
        EventRule toSave = new EventRule().copyFromServer(eventRule);
        logger.info("EventRuleServiceImpl.addEventRule() id:" + eventRule.getId());
        logger.info("EventRuleServiceImpl.addEventRule() course:" + eventRule.getCourse());
        logger.info("EventRuleServiceImpl.addEventRule() TimeFrame:" + eventRule.getTimeFrame());
        logger.info("EventRuleServiceImpl.addEventRule() DeviceEventType:" + eventRule.getDeviceEventType());
//        logger.info("EventRuleServiceImpl.addEventRule() user:" + eventRule.getUser());
        logger.info("EventRuleServiceImpl.addEventRule() originalUser:" + originalUser);
        logger.info("EventRuleServiceImpl.addEventRule() originalUser.id:" + (originalUser != null ? originalUser.getId() : "ISNULL"));
        User user = entityManager.get().find(User.class, originalUser.getId());
        Device device = eventRule.getDevice() == null ? null : entityManager.get().find(Device.class, eventRule.getDevice().getId());
        GeoFence geoFence = eventRule.getGeoFence() == null ? null : entityManager.get().find(GeoFence.class, eventRule.getGeoFence().getId());
        toSave.setUser(user);
        toSave.setDevice(device);
        toSave.setGeoFence(geoFence);

//        toSave.setUsers(new HashSet<User>(1));
        user.getEventRules().add(toSave);
        entityManager.get().persist(toSave);

        user.getDevices().add(toSave.getDevice());
        if (toSave.getGeoFence() != null) {
            user.getGeoFences().add(toSave.getGeoFence());
        }
        entityManager.get().persist(user);

        return toSave;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public EventRule updateEventRule(User originalUser, EventRule eventRule) throws AccessDeniedException {
//        User user = sessionUser.get();
        logger.info("EventRuleServiceImpl.updateEventRule() id:" + eventRule.getId());
        logger.info("EventRuleServiceImpl.updateEventRule() course:" + eventRule.getCourse());
        logger.info("EventRuleServiceImpl.updateEventRule() TimeFrame:" + eventRule.getTimeFrame());
        logger.info("EventRuleServiceImpl.updateEventRule() DeviceEventType:" + eventRule.getDeviceEventType());
        logger.info("EventRuleServiceImpl.updateEventRule() id:" + eventRule.getId());
        EventRule toSave = entityManager.get().find(EventRule.class, eventRule.getId());
        User user = entityManager.get().find(User.class, originalUser.getId());
        Device device = eventRule.getDevice() == null ? null : entityManager.get().find(Device.class, eventRule.getDevice().getId());
        GeoFence geoFence = eventRule.getGeoFence() == null ? null : entityManager.get().find(GeoFence.class, eventRule.getGeoFence().getId());
//        if (!user.hasAccessTo(toSave)) {
//            throw new AccessDeniedException();
//        }

        toSave.copyFromServer(eventRule);
        toSave.setUser(user);
        toSave.setDevice(device);
        toSave.setGeoFence(geoFence);

        user.getDevices().add(toSave.getDevice());
        user.getGeoFences().add(toSave.getGeoFence());
        entityManager.get().persist(user);

        return toSave;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public void removeEventRule(EventRule eventRule) throws AccessDeniedException {
        logger.info("EventRuleServiceImpl.removeEventRule() id:" + eventRule.getId());
        EventRule toRemove = entityManager.get().find(EventRule.class, eventRule.getId());
        entityManager.get().remove(toRemove);
    }

}
