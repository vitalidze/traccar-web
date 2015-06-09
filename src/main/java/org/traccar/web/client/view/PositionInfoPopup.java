/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionInfoPopup {
    private final static Messages i18n = GWT.create(Messages.class);

    final ToolTip toolTip;
    final ListStore<Device> deviceStore;

    public PositionInfoPopup(ListStore<Device> deviceStore) {
        this.deviceStore = deviceStore;
        this.toolTip = new ToolTip(new ToolTipConfig());
    }

    public void show(int x, int y, final Position position) {
        long current = System.currentTimeMillis();

        String body = "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" +
                (position.getDevice().getDescription() != null && !position.getDevice().getDescription().trim().isEmpty() ? "<tr><td style=\"border-width: 1px 0px 0px 0px; border-style: solid; border-color: #000000; padding: 3px 0px 3px 0px;\" width=\"100%\" colspan=\"2\">" + position.getDevice().getDescription() + "</td></tr>" : "") +
                "<tr><td style=\"border-width: 1px 0px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 0px 3px 0px;\" width=\"100%\" colspan=\"2\">" + i18n.ago(formatDateTimeDiff(current - position.getTime().getTime())) + "<br>(" + ApplicationContext.getInstance().getFormatterUtil().getTimeFormat().format(position.getTime()) + ")</td></tr>" +
                (position.getIdleSince() == null ? "" : ("<tr><td style=\"font-size: 11pt; border-width: 0px 1px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 0px;\" valign=\"center\">" + i18n.idle() + "</td><td style=\"border-width: 0px 0px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 10px;\" colspan=\"2\">" + formatDateTimeDiff(current - position.getIdleSince().getTime()) + "<br>(" + i18n.since(ApplicationContext.getInstance().getFormatterUtil().getTimeFormat().format(position.getIdleSince())) + ")</td></tr>")) +
                (position.getAddress() == null || position.getAddress().isEmpty() ? "" : ("<tr><td style=\"border-width: 0px 0px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 0px 3px 0px;\" colspan=\"2\">" + position.getAddress() + "</td></tr>")) +
                "<tr>" +
                (position.getSpeed() == null ? "" : ("<td style=\"font-size: 12pt; border-width: 0px 1px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 0px;\" valign=\"bottom\">" + ApplicationContext.getInstance().getFormatterUtil().getSpeedFormat().format(position.getSpeed()) + "</td>")) +
                (position.getAltitude() == null ? "" : ("<td style=\"font-size: 10pt; border-bottom: 1px solid #000000; padding: 3px 10px 3px 10px;\" valign=\"bottom\"" + (position.getSpeed() == null ? " colspan=\"2\" align=\"right\"" : "") + ">" + position.getAltitude() + " " + i18n.meter() + "</td>")) +
                "</tr>";

        if (position.getDevice().getOdometer() > 0) {
            body += "<tr><td style=\"padding: 3px 0px 3px 0px;\">" + i18n.odometer() + "</td><td>" + ApplicationContext.getInstance().getFormatterUtil().getDistanceFormat().format(position.getDevice().getOdometer()) + "</td></tr>";
        }
        String other = position.getOther();
        if (other != null) {
            Device device = deviceStore.findModelWithKey(Long.toString(position.getDevice().getId()));
            Map<String, Sensor> sensors = new HashMap<String, Sensor>(device.getSensors().size());
            for (Sensor sensor : device.getSensors()) {
                sensors.put(sensor.getParameterName(), sensor);
            }

            try {
                NodeList nodes = XMLParser.parse(other).getFirstChild().getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    String parameterName = node.getNodeName();
                    String valueText = node.getFirstChild().getNodeValue();
                    Sensor sensor = sensors.get(parameterName);
                    if (sensor != null) {
                        if (!sensor.isVisible()) {
                            continue;
                        }
                        parameterName = sensor.getName();
                        if (valueText.matches("^[-+]?\\d+(\\.\\d+)?$")) {
                            double value = Double.parseDouble(valueText);
                            List<SensorInterval> intervals = SensorsEditor.intervals(sensor);
                            if (!intervals.isEmpty()) {
                                valueText = intervalText(value, intervals);
                            }
                        }
                    } else if (parameterName.equals("protocol")) {
                        parameterName = i18n.protocol();
                    }
                    if (!valueText.isEmpty()) {
                        body += "<tr><td style=\"padding: 3px 0px 3px 0px;\">" + parameterName + "</td><td>" + valueText + "</td></tr>";
                    }
                }
            } catch (Exception error) {
            }
        }

        if (position.getGeoFences() != null && !position.getGeoFences().isEmpty()) {
            body += "<tr><td style=\"border-width: 1px 0px 0px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 0px;\" colspan=\"2\">";
            for (GeoFence geoFence : position.getGeoFences()) {
                body += "<p><div style=\"" +
                        "    width: 10px;\n" +
                        "    height: 10px;\n" +
                        "    display: inline-block;\n" +
                        "    background-color: #" + geoFence.getColor() + ";\n" +
                        "    border: 1px solid;\n" +
                        "    left: 5px;\n" +
                        "    top: 5px;\"></div> " + geoFence.getName() + "</p>";
            }
            body += "</td></tr>";
        }

        body += "</table>";

        ToolTipConfig config = new ToolTipConfig();

        PositionIcon icon = position.getIcon() == null ? MarkerIcon.create(position) : position.getIcon();
        String deviceTitle = position.getDevice().getName() + (position.getStatus() == Position.Status.OFFLINE ? " (" + i18n.offline() + ")" : "");

        config.setTitleHtml(
                "<table height=\"100%\"><tr>" +
                "<td>" +"<img src=\"" + icon.getURL() + "\">&nbsp;</td>" +
                "<td valign=\"center\">" + deviceTitle + "</td>" +
                "</tr></table>");

        config.setBodyHtml(body);
        config.setAutoHide(false);
        config.setDismissDelay(0);

        toolTip.update(config);
        toolTip.showAt(x + 15, y + 15);
    }

    private String formatDateTimeDiff(long diff) {
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays > 0 ? diffDays + i18n.day() + " " + diffHours + i18n.hour() :
                diffHours > 0 ? diffHours + i18n.hour() + " " + diffMinutes + i18n.minute() :
                        diffMinutes > 0 ? diffMinutes + i18n.minute() + " " + diffSeconds + i18n.second() :
                                diffSeconds + i18n.second();
    }

    public void show(final MapView mapView, final Position position) {
        Pixel pixel = mapView.getMap().getPixelFromLonLat(mapView.createLonLat(position.getLongitude(), position.getLatitude()));
        show(mapView.getView().getAbsoluteLeft() + pixel.x(), mapView.getView().getAbsoluteTop() + pixel.y(), position);
    }

    public void hide() {
        ToolTipConfig config = toolTip.getToolTipConfig();
        config.setAutoHide(true);
        config.setDismissDelay(10);
        toolTip.update(config);
    }

    public static String intervalText(double value, List<SensorInterval> intervals) {
        String valueText = null;
        for (SensorInterval interval : intervals) {
            if (valueText == null) {
                valueText = interval.getText();
            }
            if (value < interval.getValue()) {
                break;
            }
            valueText = interval.getText();
        }
        return valueText;
    }
}
