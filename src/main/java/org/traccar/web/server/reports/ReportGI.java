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

import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Report;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class ReportGI extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Device device : getDevices(report)) {
            panelStart();

            // heading
            panelHeadingStart();
            text(device.getName());
            panelHeadingEnd();

            // body
            panelBodyStart();
            // period
            paragraphStart();
            bold("Period: ");
            text(dateFormat.format(report.getFromDate()) + " - " + dateFormat.format(report.getToDate()));
            paragraphEnd();
            // data table
            dataTable(device);
            panelBodyEnd();

            panelEnd();
        }
    }

    void dataTable(Device device) {
        tableStart();
        tableBodyStart();

        dataRow("Route start", "");
        dataRow("Route end", "");
        dataRow("Route length", "");
        dataRow("Move duration", "");
        dataRow("Stop duration", "");
        dataRow("Top speed", "");
        dataRow("Average speed", "");
        dataRow("Overspeed count", "");
        dataRow("Odometer", "");

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
}
