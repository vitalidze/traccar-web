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
import java.util.Date;

public class UserCheck implements MethodInterceptor {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private Provider<ApplicationSettings> applicationSettings;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        RequireUser requireUser = methodInvocation.getMethod().getAnnotation(RequireUser.class);
        if (requireUser != null) checkRequireUser(requireUser);
        ManagesDevices managesDevices = methodInvocation.getMethod().getAnnotation(ManagesDevices.class);
        if (managesDevices != null) checkDeviceManagementAccess(managesDevices);
        RequireWrite requireWrite = methodInvocation.getMethod().getAnnotation(RequireWrite.class);
        if (requireWrite != null) checkRequireWrite(requireWrite);
        try {
            return methodInvocation.proceed();
        } finally {
            cleanUp();
        }
    }

    private final ThreadLocal<Role[]> rolesChecked = new ThreadLocal<Role[]>();
    void checkRequireUser(RequireUser requireUser) {
        if (rolesChecked.get() != null) {
            // this method require an logged in user
            // so if rolesChecked thread local returns some value
            // then this check already passed for sure
            if (requireUser.roles().length == 0) {
                return;
            }
            // check role
            Role[] checkedRoles = rolesChecked.get();
            for (Role role : requireUser.roles()) {
                for (int i = 0; i < checkedRoles.length; i++) {
                    if (role == checkedRoles[i]) {
                        return;
                    }
                }
            }
        }

        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }
        if (user.isBlocked()) {
            throw new SecurityException("User account is blocked");
        }
        if (user.isExpired()) {
            throw new SecurityException("User account expired");
        }
        if (requireUser.roles().length > 0) {
            StringBuilder roles = new StringBuilder();
            for (Role role : requireUser.roles()) {
                if (roles.length() > 0) {
                    roles.append(" or ");
                }
                roles.append(role.toString());
                if (role.has(user)) {
                    rolesChecked.set(requireUser.roles());
                    return;
                }
            }
            throw new SecurityException("User must have " + roles + " role");
        } else {
            rolesChecked.set(requireUser.roles());
        }
    }

    final ThreadLocal<Boolean> checkedDeviceManagement = new ThreadLocal<Boolean>();
    void checkDeviceManagementAccess(ManagesDevices managesDevices) throws Throwable {
        if (checkedDeviceManagement.get() != null) {
            return;
        }
        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }
        if (!user.getAdmin() && !user.getManager()) {
            if (applicationSettings.get().isDisallowDeviceManagementByUsers()) {
                throw new SecurityException("Users are not allowed to manage devices");
            }
            checkedDeviceManagement.set(Boolean.TRUE);
        }
    }

    final ThreadLocal<Boolean> checkedRequireWrite = new ThreadLocal<Boolean>();
    void checkRequireWrite(RequireWrite requireWrite) {
        if (checkedRequireWrite.get() != null) {
            return;
        }
        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }
        if (user.getReadOnly()) {
            throw new SecurityException("User is not allowed to make any changes");
        }
        checkedRequireWrite.set(Boolean.TRUE);
    }

    void cleanUp() {
        rolesChecked.remove();
        checkedDeviceManagement.remove();
        checkedRequireWrite.remove();
    }
}
