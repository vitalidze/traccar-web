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
import java.util.List;

public class ReportGI extends ReportGenerator {
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
            // device details
            deviceDetails(device);
            // data table
            dataTable(new Info(positions).calculate());
            panelBodyEnd();

            panelEnd();
        }
    }

    static class Info {
        final List<Position> positions;
        Date start;
        Date end;
        double length;
        long moveDuration;
        long stopDuration;
        double topSpeed;
        double averageSpeed;
        int overspeedCount;

        Info(List<Position> positions) {
            this.positions = positions;
        }

        Info calculate() {
            this.start = positions.isEmpty() ? null : positions.get(0).getTime();
            this.end = positions.isEmpty() ? null : positions.get(positions.size() - 1).getTime();

            Position prevPosition = null;
            double totalSpeed = 0;
            int movingCount = 0;
            for (Position position : positions) {
                Device device = position.getDevice();
                this.length += position.getDistance();

                if (prevPosition != null) {
                    long diffTime = position.getTime().getTime() - prevPosition.getTime().getTime();
                    if (prevPosition.getSpeed() != null
                            && prevPosition.getSpeed() > device.getIdleSpeedThreshold()) {
                        moveDuration += diffTime;
                    } else {
                        stopDuration += diffTime;
                    }
                }
                if (position.getSpeed() != null && position.getSpeed() > device.getIdleSpeedThreshold()) {
                    movingCount++;
                    totalSpeed += position.getSpeed() == null ? 0 : position.getSpeed();
                    topSpeed = Math.max(position.getSpeed(), topSpeed);
                }

                if (device.getSpeedLimit() != null
                        && position.getSpeed() != null && position.getSpeed() > device.getSpeedLimit()
                        && (prevPosition == null
                            || prevPosition.getSpeed() == null
                            || prevPosition.getSpeed() <= device.getSpeedLimit())) {
                    overspeedCount++;
                }

                prevPosition = position;
            }

            this.averageSpeed = movingCount == 0 ? 0 : totalSpeed / movingCount;

            return this;
        }
    }

    void dataTable(Info info) {
        tableStart();
        tableBodyStart();

        dataRow(message("routeStart"), info.start == null ? "n/a" : formatDate(info.start));
        dataRow(message("routeEnd"), info.end == null ? "n/a" : formatDate(info.end));
        dataRow(message("routeLength"), formatDistance(info.length));
        dataRow(message("moveDuration"), formatDuration(info.moveDuration));
        dataRow(message("stopDuration"), formatDuration(info.stopDuration));
        dataRow(message("topSpeed"), formatSpeed(info.topSpeed));
        dataRow(message("averageSpeed"), formatSpeed(info.averageSpeed));
        dataRow(message("overspeedCount"), Integer.toString(info.overspeedCount));

        tableBodyEnd();
        tableEnd();
    }
}
