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

import org.traccar.web.shared.model.AccessDeniedException;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.Report;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ReportOS extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<Position> positions;
            if (device.getSpeedLimit() == null) {
                positions = Collections.emptyList();
            } else {
                try {
                    positions = dataService.getPositions(device, report.getFromDate(), report.getToDate(), true);
                } catch (AccessDeniedException ade) {
                    continue;
                }
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
            if (!positions.isEmpty()) {
                drawTable(positions);
            }

            panelBodyEnd();

            panelEnd();
        }
    }

    void drawTable(List<Position> positions) {
        tableStart(hover().condensed());

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"start", "end", "duration", "topSpeed", "averageSpeed", "overspeedPosition"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }

        tableRowEnd();
        tableHeadEnd();

        // body
        tableBodyStart();

        Position start = null;
        double topSpeed = 0;
        double speedSUM = 0;
        int overspeedPositionCount = 0;

        for (Iterator<Position> it = positions.iterator(); it.hasNext(); ) {
            Position position = it.next();

            if (!it.hasNext()) {
                if (start == null && isOverspeed(position)) {
                    start = position;
                }
            }

            if (start != null && (!it.hasNext() || !isOverspeed(position))) {
                tableRowStart();
                tableCell(formatDate(start.getTime()));
                tableCell(formatDate(position.getTime()));
                long duration = position.getTime().getTime() - start.getTime().getTime();
                tableCell(formatDuration(duration));
                tableCell(formatSpeed(topSpeed));
                tableCell(formatSpeed(speedSUM / overspeedPositionCount));
                tableCellStart();
                mapLink(start.getLatitude(), start.getLongitude());
                tableCellEnd();
                tableRowEnd();

                // reset counters
                start = null;
                topSpeed = 0;
                speedSUM = 0;
                overspeedPositionCount = 0;
            }

            if (isOverspeed(position)) {
                if (start == null) {
                    start = position;
                }
                topSpeed = Math.max(topSpeed, position.getSpeed());
                speedSUM += position.getSpeed();
                overspeedPositionCount++;
            }
        }

        tableBodyEnd();

        tableEnd();
    }

    private boolean isOverspeed(Position position) {
        return position.getSpeed() > position.getDevice().getSpeedLimit();
    }
}
