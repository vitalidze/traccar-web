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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.*;

import java.util.*;

public class PositionInfoPopup {
    private final Messages i18n = GWT.create(Messages.class);

    final ToolTip toolTip;
    final ListStore<Device> deviceStore;
    final PopupTemplate template;
    final PopupResources popupResources;

    interface PopupCssStyle extends CssResource {
        String rowPaddingLeftRight();
        String rowPadding310310();
        String rowPadding31030();
        String rowBorder();
        String rowBorderTop();
        String rowBorderTopBottom();
        String rowBorderRightBottom();
        String rowBorderBottom();
        String font11pt();
        String font12pt();
        String font10pt();
        String geoFenceSquare();
    }

    interface PopupResources extends ClientBundle {
        @ClientBundle.Source("PositionInfoPopup.css")
        PopupCssStyle popupStyle();
    }

    interface PopupTemplate extends XTemplates {
        @XTemplates.XTemplate(source = "PositionInfoPopup.html")
        SafeHtml body(PopupCssStyle style, Messages i18n, Position position, Device device,
                      String ago,
                      String formattedTime,
                      String idle,
                      String formattedIdleSinceTime,
                      String speed,
                      String odometer,
                      List<SensorData> sensorData);

        @XTemplate("<table height=\"100%\"><tr>" +
                "<td><img src=\"{iconURL}\">&nbsp;</td>" +
                "<td valign=\"center\">{deviceTitle}</td>" +
                "<td valign=\"center\" align=\"right\"><img src=\"{arrowIconURL}\"/></td>" +
                "</tr></table>")
        SafeHtml title(String iconURL, String deviceTitle, String arrowIconURL);
    }

    static class SensorData {
        final String name;
        final String valueText;
        final String color;

        SensorData(String name, String valueText, String color) {
            this.name = name;
            this.valueText = valueText;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getValueText() {
            return valueText;
        }

        public String getColor() {
            return color;
        }
    }

    public PositionInfoPopup(ListStore<Device> deviceStore) {
        this.deviceStore = deviceStore;
        this.toolTip = new ToolTip(new ToolTipConfig());
        this.template = GWT.create(PopupTemplate.class);
        this.popupResources = GWT.create(PopupResources.class);
        this.popupResources.popupStyle().ensureInjected();
    }

    public void show(int x, int y, final Position position) {
        long current = System.currentTimeMillis();

        Device device = deviceStore.findModelWithKey(Long.toString(position.getDevice().getId()));

        List<SensorData> sensorData = Collections.emptyList();
        String other = position.getOther();
        if (other != null) {
            Map<String, Sensor> sensors = new HashMap<>(device.getSensors().size());
            for (Sensor sensor : device.getSensors()) {
                sensors.put(sensor.getParameterName(), sensor);
            }

            Map<String, Object> parsedSensorData = new HashMap<>();

            // XML
            if (other.trim().startsWith("<")) {
                try {
                    NodeList nodes = XMLParser.parse(other).getFirstChild().getChildNodes();
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        String parameterName = node.getNodeName();
                        String valueText = node.getFirstChild().getNodeValue();
                        parsedSensorData.put(parameterName, valueText);
                    }
                } catch (Exception error) {
                }
            } else { // JSON
                try {
                    JSONValue parsed = JSONParser.parseStrict(other);
                    JSONObject object = parsed.isObject();
                    if (object != null) {
                        for (String parameterName : object.keySet()) {
                            JSONValue value = object.get(parameterName);
                            if (value.isNumber() != null) {
                                parsedSensorData.put(parameterName, value.isNumber().doubleValue());
                            } else if (value.isBoolean() != null) {
                                parsedSensorData.put(parameterName, value.isBoolean().booleanValue());
                            } else if (value.isString() != null) {
                                parsedSensorData.put(parameterName, value.isString().stringValue());
                            }
                        }
                    }
                } catch (Exception error) {
                }
            }

            if (!device.isShowProtocol()) {
                parsedSensorData.remove("protocol");
            }

            // write values
            sensorData = new ArrayList<>(parsedSensorData.size());
            for (Map.Entry<String, Object> entry : parsedSensorData.entrySet()) {
                String parameterName = entry.getKey();
                Object value = entry.getValue();
                String valueText = value.toString();
                String color = null;
                Sensor sensor = sensors.get(parameterName);
                if (sensor != null) {
                    if (!sensor.isVisible()) {
                        continue;
                    }
                    parameterName = sensor.getName();
                    if (value instanceof Number || value.toString().matches("^[-+]?\\d+(\\.\\d+)?$")) {
                        double doubleValue;
                        if (value instanceof Number) {
                            doubleValue = ((Number) value).doubleValue();
                        } else {
                            doubleValue = Double.parseDouble(valueText);
                        }
                        List<SensorInterval> intervals = SensorsEditor.intervals(sensor);
                        if (!intervals.isEmpty()) {
                            SensorInterval interval = interval(doubleValue, intervals);
                            valueText = interval.getText();
                            color = interval.getColor();
                        }
                    }
                } else if (parameterName.equals("protocol")) {
                    parameterName = i18n.protocol();
                }
                if (!valueText.isEmpty()) {
                    sensorData.add(new SensorData(parameterName, valueText, color));
                }
            }
        }

        SafeHtml body = template.body(popupResources.popupStyle(), i18n,
                position,
                device,
                i18n.ago(formatDateTimeDiff(current - position.getTime().getTime())),
                ApplicationContext.getInstance().getFormatterUtil().getTimeFormat().format(position.getTime()),
                position.getIdleSince() == null ? null : formatDateTimeDiff(current - position.getIdleSince().getTime()),
                position.getIdleSince() == null ? null : i18n.since(ApplicationContext.getInstance().getFormatterUtil().getTimeFormat().format(position.getIdleSince())),
                position.getSpeed() == null ? null : ApplicationContext.getInstance().getFormatterUtil().getSpeedFormat().format(position.getSpeed()),
                position.getDevice().getOdometer() > 0 && device.isShowOdometer() ? ApplicationContext.getInstance().getFormatterUtil().getDistanceFormat().format(position.getDevice().getOdometer()) : null,
                sensorData);

        ToolTipConfig config = new ToolTipConfig();

        PositionIcon icon = position.getIcon() == null ? MarkerIcon.create(position) : position.getIcon();
        String deviceTitle = position.getDevice().getName() + (position.getStatus() == Position.Status.OFFLINE ? " (" + i18n.offline() + ")" : "");

        config.setTitleHtml(template.title(icon.getURL(), deviceTitle, "img/arrow" + (int) ((position.getCourse() + 45 / 2) % 360) / 45 + ".png"));
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

    public static SensorInterval interval(double value, List<SensorInterval> intervals) {
        SensorInterval result = null;
        for (SensorInterval interval : intervals) {
            if (result == null) {
                result = interval;
            }
            if (value < interval.getValue()) {
                break;
            }
            result = interval;
        }
        return result;
    }

    public static String intervalText(double value, List<SensorInterval> intervals) {
        SensorInterval interval = interval(value, intervals);
        return interval == null ? null : interval.getText();
    }
}
