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
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.traccar.web.shared.model.ApplicationSettings;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Singleton
public class ImportServlet extends HttpServlet {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private Provider<ApplicationSettings> applicationSettings;
    @Inject
    private Provider<EntityManager> entityManager;

    @Transactional(rollbackOn = { IOException.class, RuntimeException.class })
    @RequireUser
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String importType = req.getPathInfo().substring(1);

        long deviceId = Long.parseLong(req.getParameter("deviceId"));

        Device device = entityManager.get().find(Device.class, deviceId);
        checkAccess(device);

        ServletFileUpload servletFileUpload = new ServletFileUpload();

        if (importType.equalsIgnoreCase("gpx")) {
            try {
                FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(req);
                while (fileItemIterator.hasNext()) {
                    gpx(device, fileItemIterator.next().openStream(), resp);
                }
            } catch (FileUploadException fue) {
                throw new IOException(fue);
            }
        } else {
            throw new ServletException("Unsupported import type: " + importType);
        }
    }

    void checkAccess(Device device) {
        User user = sessionUser.get();
        if (!user.getAdmin() && !user.getAllAvailableDevices().contains(device)) {
            throw new SecurityException("User does not have access to device with id=" + device.getId());
        }
        if (!user.getAdmin() && !user.getManager() && applicationSettings.get().isDisallowDeviceManagementByUsers()) {
            throw new SecurityException("User is restricted from devices management");
        }
    }

    void gpx(Device device, InputStream inputStream, HttpServletResponse response) throws IOException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateFormat dateFormatWithMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        dateFormatWithMS.setTimeZone(tz);


        try {
            XMLStreamReader xsr = XMLInputFactory.newFactory().createXMLStreamReader(inputStream);

            List<Position> parsedPositions = new LinkedList<Position>();
            Position position = null;

            response.getWriter().println("<pre>");

            int imported = 0;

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
                    } else if (xsr.getLocalName().equalsIgnoreCase("ele")) {
                        if (position != null) {
                            position.setAltitude(Double.parseDouble(xsr.getElementText()));
                        }
                    } else if (position != null) {
                        position.setOther((position.getOther() == null ? "" : position.getOther()) +
                        "<" + xsr.getLocalName() + ">" + xsr.getElementText() + "</" + xsr.getLocalName() + ">");
                    }
                } else if (xsr.getEventType() == XMLStreamReader.END_ELEMENT &&
                           xsr.getLocalName().equalsIgnoreCase("trkpt")) {

                    parsedPositions.add(position);
                    position = null;
                }
            }

            for (int i = 0; i < parsedPositions.size(); i++) {
                position = parsedPositions.get(i);
                StringBuilder other = new StringBuilder("<info><protocol>gpx_import</protocol>");
                other.append("<type>");
                if (i == 0) {
                    other.append("import_start");
                } else if (i == parsedPositions.size() - 1) {
                    other.append("import_end");
                } else {
                    other.append("import");
                }
                other.append("</type>");
                if (position.getOther() != null) {
                    other.append(position.getOther());
                }
                other.append("</info>");
                position.setOther(other.toString());

                boolean exist = false;
                for (Position existing : entityManager.get().createQuery("SELECT p FROM Position p WHERE p.device=:device AND p.time=:time", Position.class)
                        .setParameter("device", device)
                        .setParameter("time", position.getTime()).getResultList()) {
                    if (equals(existing.getLongitude(), position.getLongitude(), 0.0000000001d) &&
                            equals(existing.getLatitude(), position.getLatitude(), 0.0000000001d) &&
                            equals(existing.getAltitude(), position.getAltitude(), 0.00001d) &&
                            existing.getOther().equals(position.getOther())) {
                        exist = true;
                        break;
                    }
                }

                if (!exist) {
                    entityManager.get().persist(position);
                    imported++;
                }
            }

            response.getWriter().println("Already exist: " + (parsedPositions.size() - imported));
            response.getWriter().println("Imported: " + imported);

            response.getWriter().println("</pre>");
        } catch (XMLStreamException xse) {
            throw new IOException(xse);
        } catch (ParseException pe) {
            throw new IOException(pe);
        }
    }

    private static boolean equals(Double d1, Double d2, double delta) {
        return d1 != null && d2 != null && Math.abs(d1 - d2) < delta;
    }
}
