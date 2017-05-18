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

import org.traccar.web.shared.model.AccessDeniedException;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.Report;

import java.io.IOException;
import java.util.*;

public class ReportMD extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
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

        for (String header : new String[] {"date", "start", "end", "distance"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }

        tableRowEnd();
        tableHeadEnd();

        // body
        tableBodyStart();

        Position start = null;
        Position end = null;
        Position prevPosition = null;
        double totalDistance = 0;
        double dailyDistance = 0;

        for (Iterator<Position> it = positions.iterator(); it.hasNext(); ) {
            Position position = it.next();

            if (!it.hasNext() || (prevPosition != null && isNextDay(prevPosition, position))) {
                if (dailyDistance > 0) {
                    tableRowStart();
                    tableCell(formatDateLong(prevPosition == null ? position.getTime() : prevPosition.getTime()));
                    tableCell(start == null ? "" : formatDate(start.getTime()));
                    tableCell(end == null ? "" : formatDate(end.getTime()));
                    tableCell(formatDistance(dailyDistance));
                    tableRowEnd();
                }

                // reset counters
                start = null;
                end = null;
                dailyDistance = 0;
            }

            if (isMoving(position)) {
                if (start == null) {
                    start = position;
                }
                end = position;
            }
            dailyDistance += position.getDistance();
            totalDistance += position.getDistance();
            prevPosition = position;
        }

        tableBodyEnd();

        tableEnd();

        paragraphStart();
        bold(message("totalMileage") + ": " + formatDistance(totalDistance));
    }

    boolean isNextDay(Position prevPosition, Position position) {
        Calendar prev = Calendar.getInstance(getTimeZone());
        prev.setTime(prevPosition.getTime());
        Calendar curr = Calendar.getInstance(getTimeZone());
        curr.setTime(position.getTime());
        return prev.get(Calendar.DAY_OF_MONTH) != curr.get(Calendar.DAY_OF_MONTH);
    }

    boolean isMoving(Position position) {
        return position.getSpeed() != null && position.getSpeed() > position.getDevice().getIdleSpeedThreshold();
    }
}
