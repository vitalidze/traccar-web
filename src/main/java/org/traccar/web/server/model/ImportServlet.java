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
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ImportServlet extends HttpServlet {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private Provider<ApplicationSettings> applicationSettings;
    @Inject
    private Provider<EntityManager> entityManager;
    @Inject
    protected Logger logger;

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
                logger.log(Level.WARNING, fue.getLocalizedMessage(), fue);
                throw new IOException(fue);
            } catch (IOException ioex) {
                logger.log(Level.WARNING, ioex.getLocalizedMessage(), ioex);
                throw ioex;
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

        dateFormat.setTimeZone(tz);
        dateFormatWithMS.setTimeZone(tz);


        try {
            GPXParser.Result parsed = new GPXParser().parse(inputStream, device);

            response.getWriter().println("<pre>");

            int imported = 0;

            for (Position position : parsed.positions) {
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

            if (parsed.latestPosition != null && device.getLatestPosition() == null || device.getLatestPosition().getTime().compareTo(parsed.latestPosition.getTime()) < 0) {
                device.setLatestPosition(parsed.latestPosition);
            }

            response.getWriter().println("Already exist: " + (parsed.positions.size() - imported));
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
