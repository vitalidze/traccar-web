/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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

import org.traccar.web.server.model.GeoFenceCalculator;
import org.traccar.web.shared.model.*;

import java.io.IOException;
import java.util.*;

public class ReportGFIO extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<GeoFence> geoFences = getGeoFences(report, device);
            List<Position> positions;
            try {
                positions = dataService.getPositions(device, report.getFromDate(), report.getToDate(), !report.isDisableFilter());
            } catch (AccessDeniedException ade) {
                continue;
            }

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
            if (!positions.isEmpty() && !geoFences.isEmpty()) {
                drawTable(calculate(geoFences, positions));
            }

            panelBodyEnd();

            panelEnd();
        }
    }

    static class Data implements Comparable<Data> {
        final GeoFence geoFence;
        final Position enter;
        Position exit;

        Data(GeoFence geoFence, Position enter) {
            this.enter = enter;
            this.geoFence = geoFence;
        }


        @Override
        public int compareTo(Data o) {
            return enter.getTime().compareTo(o.enter.getTime());
        }
    }

    List<Data> calculate(List<GeoFence> geoFences, List<Position> positions) {
        // calculate
        GeoFenceCalculator calculator = new GeoFenceCalculator(geoFences);
        Map<GeoFence, Data> currentData = new HashMap<>(geoFences.size());
        List<Data> result = new ArrayList<>();

        for (Position position : positions) {
            for (GeoFence geoFence : geoFences) {
                if (calculator.contains(geoFence, position)) {
                    if (!currentData.containsKey(geoFence)) {
                        currentData.put(geoFence, new Data(geoFence, position));
                    }
                } else {
                    Data geoFenceData = currentData.remove(geoFence);
                    if (geoFenceData != null) {
                        geoFenceData.exit = position;
                        result.add(geoFenceData);
                    }
                }
            }
        }
        Collection<Data> lastData = currentData.values();
        for (Data data : lastData) {
            data.exit = positions.get(positions.size() - 1);
        }
        result.addAll(lastData);
        Collections.sort(result);
        return result;
    }

    void drawTable(List<Data> datas) {

        // draw
        tableStart(hover().condensed());

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"geoFenceIn", "geoFenceOut", "duration", "geoFenceName", "geoFencePosition"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }

        tableRowEnd();
        tableHeadEnd();

        // body
        tableBodyStart();

        for (Data data : datas) {
            tableRowStart();
            tableCell(formatDate(data.enter.getTime()));
            tableCell(formatDate(data.exit.getTime()));
            long duration = data.exit.getTime().getTime() - data.enter.getTime().getTime();
            tableCell(formatDuration(duration));
            tableCell(data.geoFence.getName());
            tableCellStart();
            mapLink(data.enter.getLatitude(), data.enter.getLongitude());
            tableCellEnd();
            tableRowEnd();
        }

        tableBodyEnd();

        tableEnd();
    }
}
