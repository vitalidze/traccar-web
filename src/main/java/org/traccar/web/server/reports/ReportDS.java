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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ReportDS extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<Position> positions;
            try {
                positions = dataService.getPositions(device, report.getFromDate(), report.getToDate(), true);
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

            // data table
            if (!positions.isEmpty()) {
                drawTable(positions);
            } else {
                drawSummary(0d, 0, 0, 0d, 0d);
            }

            panelBodyEnd();

            panelEnd();
            entityManager.clear();
        }
    }

    void drawTable(List<Position> positions) {
        tableStart(hover().condensed());

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"status", "start", "end", "duration"}) {
            tableHeadCellStart(rowspan(2));
            text(message(header));
            tableHeadCellEnd();
        }

        tableHeadCellStart(colspan(3));
        text(message("stopPosition"));
        tableHeadCellEnd();

        tableRowEnd();

        tableRowStart();
        for (String header : new String[] {"distance", "topSpeed", "averageSpeed"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }
        tableRowEnd();

        tableHeadEnd();

        // body
        tableBodyStart();

        Position prevPosition = null;
        Position start = null;
        double topSpeed = 0;
        double speedSUM = 0;
        double distance = 0;
        int movingPositionCount = 0;

        double totalDistance = 0;
        long totalMoveDuration = 0;
        long totalStopDuration = 0;
        int totalMovingPositionCount = 0;
        double totalSpeedSUM = 0;
        double totalTopSpeed = 0;
        for (Iterator<Position> it = positions.iterator(); it.hasNext(); ) {
            Position position = it.next();
            totalDistance += position.getDistance();

            if (!it.hasNext()) {
                if (start == null) {
                    start = position;
                }
                if (prevPosition == null) {
                    prevPosition = position;
                }
            }

            if (!it.hasNext() || (prevPosition != null && isIdle(position) != isIdle(prevPosition))) {
                tableRowStart();
                tableCell(message(isIdle(start) ? "stopped" : "moving"));
                tableCell(formatDate(start.getTime()));
                tableCell(formatDate(position.getTime()));
                long duration = position.getTime().getTime() - start.getTime().getTime();
                if (isIdle(start)) {
                    totalStopDuration += duration;
                } else {
                    totalMoveDuration += duration;
                }
                tableCell(formatDuration(duration));

                if (isIdle(start)) {
                    tableCellStart(colspan(3));
                    mapLink(start.getLatitude(), start.getLongitude());
                    if (start.getAddress() != null && !start.getAddress().isEmpty()) {
                        text(" - " + start.getAddress());
                    }
                    tableCellEnd();
                } else {
                    tableCell(formatDistance(distance));
                    tableCell(formatSpeed(topSpeed));
                    tableCell(formatSpeed(speedSUM / movingPositionCount));
                }
                tableRowEnd();
                // reset counters
                start = null;
                topSpeed = 0;
                speedSUM = 0;
                movingPositionCount = 0;
                distance = 0;
            }

            if (start == null) {
                start = position;
            }

            if (!isIdle(position)) {
                topSpeed = Math.max(topSpeed, position.getSpeed());
                speedSUM += position.getSpeed();
                movingPositionCount++;
                distance += position.getDistance();

                totalMovingPositionCount++;
                totalSpeedSUM += position.getSpeed();
                totalTopSpeed = Math.max(totalTopSpeed, position.getSpeed());
            }

            prevPosition = position;
        }

        tableBodyEnd();

        tableEnd();

        drawSummary(totalDistance,
                totalMoveDuration,
                totalStopDuration,
                totalTopSpeed,
                totalMovingPositionCount == 0 ? 0d : totalSpeedSUM / totalMovingPositionCount);
    }

    private void drawSummary(double routeLength,
                             long moveDuration,
                             long stopDuration,
                             double topSpeed,
                             double averageSpeed) {
        tableStart();
        tableBodyStart();

        dataRow(message("routeLength"), formatDistance(routeLength));
        dataRow(message("moveDuration"), formatDuration(moveDuration));
        dataRow(message("stopDuration"), formatDuration(stopDuration));
        dataRow(message("topSpeed"), formatSpeed(topSpeed));
        dataRow(message("averageSpeed"), formatSpeed(averageSpeed));

        tableBodyEnd();
        tableEnd();
    }

    void dataRow(String title, String text) {
        tableRowStart();
        tableCellStart();
        bold(title + ":");
        tableCellEnd();
        tableCellStart();
        text(text);
        tableCellEnd();
        tableRowEnd();
    }

    private boolean isIdle(Position position) {
        return position.getSpeed() == null || position.getSpeed() <= position.getDevice().getIdleSpeedThreshold();
    }
}
