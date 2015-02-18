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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.traccar.web.shared.model.ApplicationSettings;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

public class UserCheck implements MethodInterceptor {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private Provider<EntityManager> entityManager;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        RequireUser requireUser = methodInvocation.getMethod().getAnnotation(RequireUser.class);
        if (requireUser != null) checkRequireUser(requireUser);
        ManagesDevices managesDevices = methodInvocation.getMethod().getAnnotation(ManagesDevices.class);
        if (managesDevices != null) checkDeviceManagementAccess(managesDevices);
        return methodInvocation.proceed();
    }

    void checkRequireUser(RequireUser requireUser) {
        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }
        if (requireUser.roles().length > 0) {
            StringBuilder roles = new StringBuilder();
            for (Role role : requireUser.roles()) {
                if (roles.length() > 0) {
                    roles.append(" or ");
                }
                roles.append(role.toString());
                if (role.has(user)) {
                    return;
                }
            }
            throw new SecurityException("User must have " + roles + " role");
        }
    }

    void checkDeviceManagementAccess(ManagesDevices managesDevices) throws Throwable {
        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }
        if (!user.getAdmin() && !user.getManager()) {
            ApplicationSettings applicationSettings = entityManager.get().createQuery("SELECT x FROM ApplicationSettings x", ApplicationSettings.class).getSingleResult();
            if (applicationSettings.isDisallowDeviceManagementByUsers()) {
                throw new SecurityException("Users are not allowed to manage devices");
            }
        }
    }
}
