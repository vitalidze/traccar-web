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

import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CurrentUserProvider implements Provider<User> {
    static final String ATTRIBUTE_USER_ID = "traccar.user.id";

    @Inject
    private Provider<HttpServletRequest> request;
    @Inject
    private Provider<EntityManager> entityManager;

    @Override
    public User get() {
        HttpSession session = request.get().getSession();
        Long userId = (Long) session.getAttribute(ATTRIBUTE_USER_ID);
        return userId == null ? null : entityManager.get().find(User.class, userId);
    }
}
