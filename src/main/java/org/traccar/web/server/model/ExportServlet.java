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
package org.traccar.web.server.model;

import com.google.inject.persist.Transactional;
import org.apache.commons.lang3.StringEscapeUtils;
import org.traccar.web.client.model.DataService;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ExportServlet extends HttpServlet {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private Provider<EntityManager> entityManager;
    @Inject
    private DataService dataService;
    @Inject
    protected Logger logger;

    private SimpleDateFormat requestDateFormat = new SimpleDateFormat(RESTApiServlet.REQUEST_DATE_PATTERN);

    @Transactional
    @RequireUser
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String exportType = req.getPathInfo().substring(1);

        try {
            long deviceId = Long.parseLong(req.getParameter("deviceId"));
            Date from = requestDateFormat.parse(req.getParameter("from"));
            Date to = requestDateFormat.parse(req.getParameter("to"));
            String strFilter = req.getParameter("filter");
            boolean filter = strFilter != null && strFilter.equalsIgnoreCase("true");
            String strSnapToRoads = req.getParameter("snapToRoads");
            boolean snapToRoads = strSnapToRoads != null && strSnapToRoads.equalsIgnoreCase("true");

            Device device = entityManager.get().find(Device.class, deviceId);
            checkAccess(device);

            if (exportType.equals("csv")) {
                csv(resp, device, from, to, filter, snapToRoads);
            } else if (exportType.equals("gpx")) {
                gpx(resp, device, from, to, filter, snapToRoads);
            } else {
                throw new ServletException("Unsupported export type: " + exportType);
            }
        } catch (NumberFormatException nfe) {
            throw new ServletException(nfe);
        } catch (ParseException pe) {
            throw new ServletException(pe);
        } catch (XMLStreamException xse) {
            throw new ServletException(xse);
        } catch (IOException ioex) {
            logger.log(Level.WARNING, ioex.getLocalizedMessage(), ioex);
            throw ioex;
        }
    }

    void checkAccess(Device device) {
        User user = sessionUser.get();
        if (!user.getAdmin() && !user.getAllAvailableDevices().contains(device)) {
            throw new SecurityException("User does not have access to device with id=" + device.getId());
        }
    }

    void csv(HttpServletResponse response, Device device, Date from, Date to, boolean filter, boolean snapToRoads) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=traccar-positions.csv");

        final char SEPARATOR = ';';

        PrintWriter writer = response.getWriter();

        writer.println(line(SEPARATOR, "time", "valid", "latitude", "longitude", "altitude", "speed", "distance", "course", "power", "address", "other"));

        for (Position p : dataService.getPositions(device, from, to, filter, snapToRoads)) {
            writer.println(line(SEPARATOR, p.getTime(), p.getValid(), p.getLatitude(), p.getLongitude(), p.getAltitude(), p.getSpeed(), p.getDistance(), p.getCourse(), p.getPower(), p.getAddress(), p.getOther()));
        }
    }

    private static String line(char SEPARATOR, Object... s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            result.append('\"');
            if (s[i] != null) {
                result.append(s[i]);
            }
            result.append('\"').append(SEPARATOR);
        }
        return result.toString();
    }

    void gpx(HttpServletResponse response, Device device, Date from, Date to, boolean filter, boolean snapToRoads) throws IOException, XMLStreamException {
        response.setContentType("application/gpx+xml;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=traccar-positions.gpx");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(tz);

        XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(response.getOutputStream(), "UTF-8");

        xsw.writeStartDocument("UTF-8", "1.0");
        xsw.writeStartElement("gpx");
        xsw.writeAttribute("version", "1.1");
        xsw.writeAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
        xsw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xsw.writeAttribute("xmlns:traccar", "http://www.traccar.org");
        xsw.writeAttribute("creator", "Traccar WEB UI");
        xsw.writeAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");

        // metadata
        xsw.writeStartElement("metadata");
        xsw.writeStartElement("link");
        xsw.writeAttribute("href", "https://github.com/vitalidze/traccar-web");
        xsw.writeStartElement("text");
        xsw.writeCharacters("Traccar WEB UI");
        xsw.writeEndElement();
        xsw.writeEndElement();
        xsw.writeStartElement("time");
        xsw.writeCharacters(dateFormat.format(new Date()));
        xsw.writeEndElement();
        xsw.writeEndElement();

        // track
        xsw.writeStartElement("trk");
        xsw.writeStartElement("name");
        xsw.writeCharacters(device.getName());
        xsw.writeEndElement();
        xsw.writeStartElement("desc");
        xsw.writeCharacters("Archive records for " + device.getName() + " from " + dateFormat.format(from) + " to " + dateFormat.format(to));
        xsw.writeEndElement();
        xsw.writeStartElement("src");
        xsw.writeCharacters("Traccar archive");
        xsw.writeEndElement();
        xsw.writeStartElement("trkseg");
        for (Position p : dataService.getPositions(device, from, to, filter, snapToRoads)) {
            xsw.writeStartElement("trkpt");
            xsw.writeAttribute("lat", p.getLatitude().toString());
            xsw.writeAttribute("lon", p.getLongitude().toString());
            if (p.getAltitude() != null && p.getAltitude() != 0) {
                xsw.writeStartElement("ele");
                xsw.writeCharacters(p.getAltitude().toString());
                xsw.writeEndElement();
            }
            xsw.writeStartElement("time");
            xsw.writeCharacters(dateFormat.format(p.getTime()));
            xsw.writeEndElement();
            // traccar extensions
            xsw.writeStartElement("extensions");
            if (p.getAddress() != null && !p.getAddress().trim().isEmpty()) {
                xsw.writeStartElement("traccar:address");
                xsw.writeCharacters(StringEscapeUtils.escapeXml(p.getAddress()));
                xsw.writeEndElement();
            }
            if (p.getSpeed() != null) {
                xsw.writeStartElement("traccar:speed");
                xsw.writeCharacters(p.getSpeed().toString());
                xsw.writeEndElement();
            }
            if (p.getCourse() != null) {
                xsw.writeStartElement("traccar:course");
                xsw.writeCharacters(p.getCourse().toString());
                xsw.writeEndElement();
            }
            if (p.getPower() != null) {
                xsw.writeStartElement("traccar:power");
                xsw.writeCharacters(p.getPower().toString());
                xsw.writeEndElement();
            }
            if (p.getOther() != null && !p.getOther().trim().isEmpty()) {
                xsw.writeStartElement("traccar:other");
                xsw.writeCharacters(StringEscapeUtils.escapeXml(p.getOther()));
                xsw.writeEndElement();
            }
            xsw.writeEndElement();
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
        xsw.writeEndElement();

        xsw.writeEndElement();
        xsw.writeEndDocument();

        xsw.flush();
    }
}
