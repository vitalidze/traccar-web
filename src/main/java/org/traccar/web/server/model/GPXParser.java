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
package org.traccar.web.server.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GPXParser {
    public static class Result {
        Position latestPosition;
        List<Position> positions;
    }

    public Result parse(InputStream inputStream, Device device) throws XMLStreamException, ParseException, IOException {
        Result result = new Result();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateFormat dateFormatWithMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        dateFormat.setTimeZone(tz);
        dateFormatWithMS.setTimeZone(tz);

        XMLStreamReader xsr = XMLInputFactory.newFactory().createXMLStreamReader(inputStream);
        ObjectMapper jsonMapper = new ObjectMapper();

        result.positions = new LinkedList<>();
        Position position = null;
        Stack<String> extensionsElements = new Stack<>();
        boolean extensionsStarted = false;
        Map<String, Object> other = null;

        while (xsr.hasNext()) {
            xsr.next();
            if (xsr.getEventType() == XMLStreamReader.START_ELEMENT) {
                if (xsr.getLocalName().equalsIgnoreCase("trkpt")) {
                    position = new Position();
                    position.setLongitude(Double.parseDouble(xsr.getAttributeValue(null, "lon")));
                    position.setLatitude(Double.parseDouble(xsr.getAttributeValue(null, "lat")));
                    position.setValid(Boolean.TRUE);
                    position.setDevice(device);
                } else if (xsr.getLocalName().equalsIgnoreCase("time")) {
                    if (position != null) {
                        String strTime = xsr.getElementText();
                        if (strTime.length() == 20) {
                            position.setTime(dateFormat.parse(strTime));
                        } else {
                            position.setTime(dateFormatWithMS.parse(strTime));
                        }
                    }
                } else if (xsr.getLocalName().equalsIgnoreCase("ele") && position != null) {
                    position.setAltitude(Double.parseDouble(xsr.getElementText()));
                } else if (xsr.getLocalName().equalsIgnoreCase("address") && position != null) {
                    position.setAddress(StringEscapeUtils.unescapeXml(xsr.getElementText()));
                } else if (xsr.getLocalName().equalsIgnoreCase("protocol") && position != null) {
                    position.setProtocol(xsr.getElementText());
                } else if (xsr.getLocalName().equalsIgnoreCase("speed") && position != null) {
                    position.setSpeed(Double.parseDouble(xsr.getElementText()));
                } else if (xsr.getLocalName().equalsIgnoreCase("power") && position != null) {
                    position.setPower(Double.parseDouble(xsr.getElementText()));
                } else if (xsr.getLocalName().equalsIgnoreCase("course") && position != null) {
                    position.setCourse(Double.parseDouble(xsr.getElementText()));
                } else if (xsr.getLocalName().equalsIgnoreCase("other") && position != null) {
                    position.setOther(StringEscapeUtils.unescapeXml(xsr.getElementText()));
                } else if (xsr.getLocalName().equalsIgnoreCase("extensions")) {
                    other = new LinkedHashMap<>();
                    extensionsStarted = true;
                } else if (position != null && extensionsStarted && other != null) {
                    extensionsElements.push(xsr.getLocalName());
                }
            } else if (xsr.getEventType() == XMLStreamReader.END_ELEMENT) {
                if (xsr.getLocalName().equalsIgnoreCase("trkpt")) {
                    if (other == null) {
                        other = new HashMap<>();
                    }

                    if (position.getOther() != null) {
                        if (position.getOther().startsWith("<")) {
                            XMLStreamReader otherReader = XMLInputFactory.newFactory().createXMLStreamReader(new StringReader(position.getOther()));
                            while (otherReader.hasNext()) {
                                if (otherReader.next() == XMLStreamReader.START_ELEMENT && !otherReader.getLocalName().equals("info")) {
                                    other.put(otherReader.getLocalName(), otherReader.getElementText());
                                }
                            }
                        } else {
                            Map<String, Object> parsedOther = jsonMapper.readValue(position.getOther(), LinkedHashMap.class);
                            other.putAll(parsedOther);
                        }
                    }

                    if (other.containsKey("protocol") && position.getProtocol() == null) {
                        position.setProtocol(other.get("protocol").toString());
                    } else if (!other.containsKey("protocol") && position.getProtocol() == null) {
                        position.setProtocol("gpx_import");
                    }

                    other.put("import_type", (result.positions.isEmpty() ? "import_start" : "import"));

                    position.setOther(jsonMapper.writeValueAsString(other));

                    result.positions.add(position);
                    if (result.latestPosition == null || result.latestPosition.getTime().compareTo(position.getTime()) < 0) {
                        result.latestPosition = position;
                    }
                    position = null;
                    other = null;
                } else if (xsr.getLocalName().equalsIgnoreCase("extensions")) {
                    extensionsStarted = false;
                } else if (extensionsStarted) {
                    extensionsElements.pop();
                }
            } else if (extensionsStarted && other != null
                    && xsr.getEventType() == XMLStreamReader.CHARACTERS && !xsr.getText().trim().isEmpty() && !extensionsElements.empty()) {
                String name = "";
                for (int i = 0; i < extensionsElements.size(); i++) {
                    name += (name.length() > 0 ? "-" : "") + extensionsElements.get(i);
                }

                other.put(name, xsr.getText());
            }
        }

        if (result.positions.size() > 1) {
            Position last = ((LinkedList<Position>) result.positions).getLast();
            Map<String, Object> parsedOther = jsonMapper.readValue(last.getOther(), LinkedHashMap.class);
            parsedOther.put("import_type", "import_end");
            last.setOther(jsonMapper.writeValueAsString(parsedOther));
        }

        return result;
    }
}
