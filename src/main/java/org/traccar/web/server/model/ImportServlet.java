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
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Singleton
public class ImportServlet extends HttpServlet {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private Provider<EntityManager> entityManager;

    @Transactional
    @RequireUser
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String importType = req.getPathInfo().substring(1);

        long deviceId = Long.parseLong(req.getParameter("deviceId"));

        Device device = entityManager.get().find(Device.class, deviceId);
        checkAccess(device);

        if (importType.equalsIgnoreCase("gpx")) {
            gpx(device, req);
        } else {
            throw new ServletException("Unsupported import type: " + importType);
        }
    }

    void checkAccess(Device device) {
        User user = sessionUser.get();
        if (!user.getAdmin() && !user.getAllAvailableDevices().contains(device)) {
            throw new SecurityException("User does not have access to device with id=" + device.getId());
        }
    }

    void gpx(Device device, HttpServletRequest request) {
        // TODO

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(tz);
    }
}
