/*
 * Copyright 2016 Vitaly Litvak (vitavaque@gmail.com)
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

import com.google.inject.Inject;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Singleton
public class MovementDetector extends ScheduledTask {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Map<Long, Date> nonIdlePositions = new ConcurrentHashMap<>();
    private final Map<Long, Double> speedThresholds = new HashMap<>();
    private final Provider<EntityManager> entityManagerProvider;
    /**
     * Scanning is based on assumption that position identifiers are incremented sequentially
     */
    private Long lastScannedPositionId;

    @Inject
    public MovementDetector(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void doWork() throws Exception {
        if (lastScannedPositionId == null) {
            List<Long> latestPositionId = entityManager().createQuery("SELECT MAX(d.latestPosition.id) FROM Device d WHERE d.latestPosition IS NOT NULL", Long.class).getResultList();
            if (latestPositionId.isEmpty() || latestPositionId.get(0) == null) {
                return;
            } else {
                lastScannedPositionId = latestPositionId.get(0);
            }
            // init latest non-idle positions for the first time
            for (Device device : getDevices()) {
                Position position = getNonIdlePosition(device);
                if (position != null) {
                    nonIdlePositions.put(device.getId(), position.getTime());
                }
                speedThresholds.put(device.getId(), device.getIdleSpeedThreshold());
            }
        }
        // check updated threshold values
        for (Device device : getDevices()) {
            Double storedThreshold = speedThresholds.get(device.getId());
            if (storedThreshold == null ||
                    Math.abs(device.getIdleSpeedThreshold() - storedThreshold) > 0.01) {
                Position position = getNonIdlePosition(device);
                if (position != null) {
                    nonIdlePositions.put(device.getId(), position.getTime());
                }
                speedThresholds.put(device.getId(), device.getIdleSpeedThreshold());
            }
        }

        // scan new positions
        List<Position> positions = entityManager().createQuery(
                "SELECT p FROM Position p WHERE p.id > :from ORDER BY p.time ASC", Position.class)
                .setParameter("from", lastScannedPositionId)
                .getResultList();

        for (Position position : positions) {
            Device device = position.getDevice();
            if (position.getSpeed() != null
                    && position.getSpeed() > device.getIdleSpeedThreshold()) {
                nonIdlePositions.put(device.getId(), position.getTime());
            }
            lastScannedPositionId = Math.max(lastScannedPositionId, position.getId());
        }
    }

    private Position getNonIdlePosition(Device device) {
        List<Position> position = entityManager().createQuery("SELECT p FROM Position p WHERE p.device = :device AND p.speed > :threshold ORDER BY time DESC", Position.class)
                .setParameter("device", device)
                .setParameter("threshold", device.getIdleSpeedThreshold())
                .setMaxResults(1)
                .getResultList();

        if (position.isEmpty()) {
            position = entityManager().createQuery("SELECT p FROM Position p WHERE p.device = :device ORDER BY time ASC", Position.class)
                    .setParameter("device", device)
                    .setMaxResults(1)
                    .getResultList();
        }
        return position.isEmpty() ? null : position.get(0);
    }

    private List<Device> getDevices() {
        return entityManager().createQuery(
                "SELECT D FROM Device D LEFT JOIN FETCH D.latestPosition", Device.class)
                .getResultList();
    }

    private EntityManager entityManager() {
        return entityManagerProvider.get();
    }

    public Date getNonIdlePositionTime(Device device) {
        return nonIdlePositions.get(device.getId());
    }
}
