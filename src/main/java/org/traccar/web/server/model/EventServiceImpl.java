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
import java.util.*;
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
                if (currentTime.getTime() - device.getLatestPosition().getTime().getTime() >= device.getTimeout() * 1000
                        && (device.getLatestPosition().getServerTime() == null || device.getLatestPosition().getServerTime().getTime() >= device.getTimeout() * 1000)) {
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

    public static class GeoFenceDetector extends EventProducer {
        @Inject
        Provider<EntityManager> entityManager;

        Set<GeoFence> geoFences = new HashSet<GeoFence>();
        GeoFenceCalculator geoFenceCalculator;

        Date currentDate = new Date();

        @Override
        @Transactional
        void before() {
            currentDate = new Date();
            geoFences.addAll(entityManager.get().createQuery("SELECT g FROM GeoFence g LEFT JOIN FETCH g.devices", GeoFence.class).getResultList());
            if (geoFences.isEmpty()) {
                return;
            }
            geoFenceCalculator = new GeoFenceCalculator(geoFences);
        }

        @Transactional
        @Override
        void positionScanned(Position prevPosition, Position position) {
            if (geoFences.isEmpty()) {
                return;
            }

            Device device = position.getDevice();
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
        }

        @Override
        void after() {
            geoFences.clear();
            geoFenceCalculator = null;
        }
    }

    public static class OdometerUpdater extends EventProducer {
        Date currentDate = new Date();
        Map<Device, List<Maintenance>> maintenances = new HashMap<Device, List<Maintenance>>();

        @Transactional
        @Override
        void before() {
            currentDate = new Date();
            List<Device> devices = entityManager().createQuery("SELECT d FROM Device d WHERE d.autoUpdateOdometer=:b", Device.class)
                    .setParameter("b", Boolean.TRUE).getResultList();
            if (devices.isEmpty()) {
                return;
            }

            // load maintenances
            for (Maintenance maintenance : entityManager().createQuery("SELECT m FROM Maintenance m WHERE m.device IN :devices", Maintenance.class)
                    .setParameter("devices", devices).getResultList()) {
                List<Maintenance> deviceMaintenances = maintenances.get(maintenance.getDevice());
                if (deviceMaintenances == null) {
                    deviceMaintenances = new LinkedList<Maintenance>();
                    maintenances.put(maintenance.getDevice(), deviceMaintenances);
                }
                deviceMaintenances.add(maintenance);
            }
        }

        @Transactional
        @Override
        void positionScanned(Position prevPosition, Position position) {
            Device device = position.getDevice();
            if (device.isAutoUpdateOdometer() && prevPosition != null) {
                double distance = GeoFenceCalculator.getDistance(
                        prevPosition.getLongitude(), prevPosition.getLatitude(),
                        position.getLongitude(), position.getLatitude());

                if (distance > 0.003) {
                    double prevOdometer = device.getOdometer();
                    device.setOdometer(prevOdometer + distance);
                    // post maintenance overdue events
                    List<Maintenance> deviceMaintenances = maintenances.get(device);
                    if (deviceMaintenances != null) {
                        for (Maintenance maintenance : deviceMaintenances) {
                            double serviceThreshold = maintenance.getLastService() + maintenance.getServiceInterval();
                            if (prevOdometer < serviceThreshold && device.getOdometer() >= serviceThreshold) {
                                DeviceEvent event = new DeviceEvent();
                                event.setTime(currentDate);
                                event.setDevice(device);
                                event.setType(DeviceEventType.MAINTENANCE_REQUIRED);
                                event.setPosition(position);
                                event.setMaintenance(maintenance);
                                entityManager().persist(event);
                            }
                        }
                    }
                }
            }
        }

        @Override
        void after() {
            maintenances.clear();
        }
    }

    static abstract class EventProducer {
        @Inject
        Provider<EntityManager> entityManager;

        void before() {
        }

        abstract void positionScanned(Position prevPosition, Position position);

        void after() {
        }

        EntityManager entityManager() {
            return entityManager.get();
        }
    }

    static class PositionScanner extends ScheduledTask {
        @Inject
        Provider<EntityManager> entityManager;

        Map<Long, DeviceState> deviceState = new HashMap<Long, DeviceState>();

        /**
         * Scanning is based on assumption that position identifiers are incremented sequentially
         */
        Long lastScannedPositionId;

        List<EventProducer> eventProducers = new ArrayList<EventProducer>();

        @Transactional
        @Override
        public void doWork() throws Exception {
            // find latest position id for the first scan
            if (lastScannedPositionId == null) {
                List<Long> latestPositionId = entityManager.get().createQuery("SELECT MAX(d.latestPosition.id) FROM Device d WHERE d.latestPosition IS NOT NULL", Long.class).getResultList();
                if (latestPositionId.isEmpty() || latestPositionId.get(0) == null) {
                    return;
                } else {
                    lastScannedPositionId = latestPositionId.get(0);
                }
            }

            // load all positions since latest
            List<Position> positions = entityManager.get().createQuery(
                    "SELECT p FROM Position p INNER JOIN p.device d WHERE p.id >= :from ORDER BY d.id, p.time ASC", Position.class)
                    .setParameter("from", lastScannedPositionId)
                    .getResultList();

            // init event producers
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.before();
            }

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
                for (int i = 0; i < eventProducers.size(); i++) {
                    eventProducers.get(i).positionScanned(prevPosition, position);
                }

                // update prev position and state
                state.latestPositionId = position.getId();
                prevPosition = position;
                // update latest position id
                lastScannedPositionId = Math.max(lastScannedPositionId, position.getId());
            }

            // destroy event producers
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.after();
            }
        }
    }

    @Inject
    private OfflineDetector offlineDetector;
    @Inject
    private GeoFenceDetector geoFenceDetector;
    @Inject
    private OdometerUpdater odometerUpdater;
    @Inject
    private PositionScanner positionScanner;

    private Map<Class<?>, ScheduledFuture<?>> futures = new HashMap<Class<?>, ScheduledFuture<?>>();

    @Inject
    private Provider<ApplicationSettings> applicationSettings;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void init() throws ServletException {
        super.init();

        positionScanner.eventProducers.add(geoFenceDetector);
        positionScanner.eventProducers.add(odometerUpdater);

        if (applicationSettings.get().isEventRecordingEnabled()) {
            startTasks();
        }
    }

    private synchronized void startTasks() {
        for (ScheduledTask task : new ScheduledTask[] { offlineDetector, positionScanner }) {
            futures.put(task.getClass(), scheduler.scheduleWithFixedDelay(task, 0, 1, TimeUnit.MINUTES));
        }
    }

    private synchronized void stopTasks() {
        for (ScheduledFuture<?> future : futures.values()) {
            future.cancel(true);
        }
        futures.clear();
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @Override
    public void applicationSettingsChanged() {
        if (applicationSettings.get().isEventRecordingEnabled()) {
            startTasks();
        } else {
            stopTasks();
        }
    }
}
