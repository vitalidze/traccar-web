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
package org.traccar.web.server.reports;

import org.traccar.web.shared.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportEV extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<DeviceEvent> events = entityManager.createQuery("SELECT e FROM DeviceEvent e" +
                    " INNER JOIN FETCH e.position" +
                    " WHERE e.device=:device AND e.time BETWEEN :from AND :to ORDER BY e.time", DeviceEvent.class)
                    .setParameter("device", device)
                    .setParameter("from", report.getFromDate())
                    .setParameter("to", report.getToDate())
                    .getResultList();
            panelStart();

            // heading
            panelHeadingStart();
            text(device.getName());
            panelHeadingEnd();

            // body
            panelBodyStart();
            // period
            paragraphStart();
            bold(message("timePeriod") + ": ");
            text(formatDate(report.getFromDate()) + " - " + formatDate(report.getToDate()));
            paragraphEnd();
            // device details
            deviceDetails(device);
            // data table
            if (!events.isEmpty()) {
                drawTable(getGeoFences(report, device), events);
            }

            panelBodyEnd();

            panelEnd();

        }
    }

    static class Stats {
        int offline;
        Map<GeoFence, Integer> geoFenceEnter = new HashMap<GeoFence, Integer>();
        Map<GeoFence, Integer> geoFenceExit = new HashMap<GeoFence, Integer>();
        Map<Maintenance, Integer> maintenances = new HashMap<Maintenance, Integer>();

        void update(DeviceEvent event) {
            switch (event.getType()) {
                case GEO_FENCE_ENTER:
                    update(geoFenceEnter, event.getGeoFence());
                    break;
                case GEO_FENCE_EXIT:
                    update(geoFenceExit, event.getGeoFence());
                    break;
                case OFFLINE:
                    offline++;
                    break;
                case MAINTENANCE_REQUIRED:
                    update(maintenances, event.getMaintenance());
                    break;
            }
        }

        <T> void update(Map<T, Integer> map, T entity) {
            if (entity != null) {
                Integer current = map.get(entity);
                map.put(entity, current == null ? 1 : (current + 1));
            }
        }
    }

    void drawTable(List<GeoFence> geoFences, List<DeviceEvent> events) {
        tableStart(hover().condensed());

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"time", "event", "eventPosition"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }

        tableRowEnd();
        tableHeadEnd();

        Stats stats = new Stats();
        // body
        tableBodyStart();

        for (DeviceEvent event : events) {
            if (event.getGeoFence() != null && !geoFences.contains(event.getGeoFence())) {
                continue;
            }

            tableRowStart();
            tableCell(formatDate(event.getTime()));
            String eventText = message("deviceEventType[" + event.getType() + "]");
            if (event.getGeoFence() != null) {
                eventText += " (" + event.getGeoFence().getName() + ")";
            }
            if (event.getMaintenance() != null) {
                eventText += " (" + event.getMaintenance().getName() + ")";
            }
            tableCell(eventText);
            tableCellStart();
            mapLink(event.getPosition().getLatitude(), event.getPosition().getLongitude());
            tableCellEnd();

            stats.update(event);
        }

        tableBodyEnd();
        tableEnd();

        // summary
        tableStart();
        tableBodyStart();

        if (stats.offline > 0) {
            dataRow(message("totalOffline"), Integer.toString(stats.offline));
        }
        for (GeoFence geoFence : geoFences) {
            Integer enterCount = stats.geoFenceEnter.get(geoFence);
            if (enterCount != null) {
                dataRow(message("totalGeoFenceEnters") + " (" + geoFence.getName() + ")", enterCount.toString());
            }
        }
        for (GeoFence geoFence : geoFences) {
            Integer enterCount = stats.geoFenceExit.get(geoFence);
            if (enterCount != null) {
                dataRow(message("totalGeoFenceExits") + " (" + geoFence.getName() + ")", enterCount.toString());
            }
        }
        if (!stats.maintenances.isEmpty()) {
            for (Map.Entry<Maintenance, Integer> entry : stats.maintenances.entrySet()) {
                dataRow(message("totalMaintenanceRequired") + " (" + entry.getKey().getName() + ")", entry.getValue().toString());
            }
        }

        tableBodyEnd();
        tableEnd();
    }
}
