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
import org.traccar.web.shared.model.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class EventServiceImpl extends RemoteServiceServlet implements EventService {
    static class DeviceState {
        Long latestPositionId;
    }

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

    public static class GeoFenceDetector extends ScheduledTask {
        @Inject
        Provider<EntityManager> entityManager;

        Map<Long, DeviceState> deviceState = new HashMap<Long, DeviceState>();

        /**
         * Scanning is based on assumption that position identifiers are incremented sequentially
         */
        Long lastScannedPositionId;

        @Override
        @Transactional
        public void doWork() throws Exception {
            Date currentDate = new Date();
            Set<GeoFence> geoFences = new HashSet<GeoFence>(entityManager.get().createQuery("SELECT g FROM GeoFence g LEFT JOIN FETCH g.devices", GeoFence.class).getResultList());
            if (geoFences.isEmpty()) {
                return;
            }

            if (lastScannedPositionId == null) {
                List<Long> latestPositionId = entityManager.get().createQuery("SELECT MAX(d.latestPosition.id) FROM Device d WHERE d.latestPosition IS NOT NULL", Long.class).getResultList();
                if (latestPositionId.isEmpty()) {
                    return;
                } else {
                    lastScannedPositionId = latestPositionId.get(0);
                }
            }

            GeoFenceCalculator geoFenceCalculator = new GeoFenceCalculator(geoFences);

            List<Position> positions = entityManager.get().createQuery("SELECT p FROM Position p WHERE p.id >= :from ORDER BY device.id, time ASC", Position.class)
                    .setParameter("from", lastScannedPositionId)
                    .getResultList();

            Position prevPosition = null;
            Device device = null;
            DeviceState state = null;
            for (Position position : positions) {
                // find current device and it's state
                if (device == null || device.getId() != position.getDevice().getId()) {
                    device = position.getDevice();
                    state = deviceState.get(device.getId());
                    if (state == null) {
                        state = new DeviceState();
                        deviceState.put(device.getId(), state);
                        prevPosition = null;
                    } else {
                        prevPosition = entityManager.get().find(Position.class, state.latestPositionId);
                    }
                }

                // calculate
                for (GeoFence geoFence : geoFences) {
                    if (prevPosition != null) {
                        boolean containsCurrent = geoFenceCalculator.contains(geoFence, position);
                        boolean containsPrevious = geoFenceCalculator.contains(geoFence, prevPosition);

                        DeviceEventType eventType = null;
                        if (containsCurrent && !containsPrevious) {
                            eventType = DeviceEventType.GEO_FENCE_ENTER;
                        } else if (!containsCurrent && containsPrevious) {
                            eventType = DeviceEventType.GEO_FENCE_EXIT;
                        }

                        if (eventType != null) {
                            DeviceEvent event = new DeviceEvent();
                            event.setTime(currentDate);
                            event.setDevice(device);
                            event.setType(eventType);
                            event.setPosition(position);
                            event.setGeoFence(geoFence);
                            entityManager.get().persist(event);
                        }
                    }
                }

                // update prev position and state
                state.latestPositionId = position.getId();
                prevPosition = position;
                // update latest position id
                lastScannedPositionId = Math.max(lastScannedPositionId, position.getId());
            }
        }
    }

    @Inject
    private OfflineDetector offlineDetector;
    private ScheduledFuture<?> offlineDetectorFuture;
    @Inject
    private GeoFenceDetector geoFenceDetector;
    private ScheduledFuture<?> geoFenceDetectorFuture;
    @Inject
    private Provider<ApplicationSettings> applicationSettings;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void init() throws ServletException {
        super.init();

        if (applicationSettings.get().isEventRecordingEnabled()) {
            startOfflineDetector();
            startGeoFenceDetector();
        }
    }

    private synchronized void startOfflineDetector() {
        if (offlineDetectorFuture == null) {
            offlineDetectorFuture = scheduler.scheduleAtFixedRate(offlineDetector, 0, 1, TimeUnit.MINUTES);
        }
    }

    private synchronized void stopOfflineDetector() {
        if (offlineDetectorFuture != null) {
            offlineDetectorFuture.cancel(true);
            offlineDetectorFuture = null;
        }
    }

    private synchronized void startGeoFenceDetector() {
        if (geoFenceDetectorFuture == null) {
            geoFenceDetectorFuture = scheduler.scheduleAtFixedRate(geoFenceDetector, 0, 1, TimeUnit.MINUTES);
        }
    }

    private synchronized void stopGeoFenceDetector() {
        if (geoFenceDetectorFuture != null) {
            geoFenceDetectorFuture.cancel(true);
            geoFenceDetectorFuture = null;
        }
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @Override
    public void applicationSettingsChanged() {
        if (applicationSettings.get().isEventRecordingEnabled()) {
            startOfflineDetector();
            startGeoFenceDetector();
        } else {
            stopOfflineDetector();
            stopGeoFenceDetector();
        }
    }
}
