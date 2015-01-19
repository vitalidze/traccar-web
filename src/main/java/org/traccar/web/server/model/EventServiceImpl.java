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
import org.traccar.web.client.model.EventService;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.DeviceEvent;
import org.traccar.web.shared.model.DeviceEventType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class EventServiceImpl extends RemoteServiceServlet implements EventService {

    public static class OfflineDetector extends ScheduledTask {
        @Inject
        Provider<EntityManager> entityManager;

        @Override
        @Transactional
        public void doWork() {
            Date currentTime = new Date();

            for (Device device : entityManager.get().createQuery("SELECT d FROM Device d", Device.class).getResultList()) {
                // skip devices without any positions
                if (device.getLatestPosition() == null) {
                    continue;
                }

                // check that device is offline
                if (currentTime.getTime() - device.getLatestPosition().getTime().getTime() >= device.getTimeout() * 1000) {
                    List<DeviceEvent> offlineEvents = entityManager.get().createQuery("SELECT e FROM DeviceEvent e WHERE e.position=:position AND e.type=:offline")
                            .setParameter("position", device.getLatestPosition())
                            .setParameter("offline", DeviceEventType.OFFLINE)
                            .getResultList();
                    if (offlineEvents.isEmpty()) {
                        DeviceEvent offlineEvent = new DeviceEvent();
                        offlineEvent.setTime(currentTime);
                        offlineEvent.setDevice(device);
                        offlineEvent.setType(DeviceEventType.OFFLINE);
                        offlineEvent.setPosition(device.getLatestPosition());
                        entityManager.get().persist(offlineEvent);
                    }
                }
            }
        }
    }

    @Inject
    private OfflineDetector offlineDetector;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void init() throws ServletException {
        super.init();

        scheduler.scheduleAtFixedRate(offlineDetector, 0, 1, TimeUnit.MINUTES);
    }
}
