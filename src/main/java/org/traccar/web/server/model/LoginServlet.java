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

import org.traccar.web.client.model.DataService;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class LoginServlet extends HttpServlet {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private DataService dataService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("user");
        User currentUser = sessionUser.get();
        try {
            boolean passwordHashed = req.getParameter("password_hashed") != null && req.getParameter("password_hashed").equals("1");
            if (currentUser == null) {
                dataService.login(username, req.getParameter("password"), passwordHashed);
            } else if (!currentUser.getLogin().equals(username)) {
                dataService.logout();
                dataService.login(username, req.getParameter("password"), passwordHashed);
            }
            String locale = req.getParameter("locale");
            resp.sendRedirect("/" + (locale == null ? "" : ("?locale=" + locale)));
        } catch (Exception ex) {
            resp.getWriter().println("Authentication failed");
        }
    }
}
