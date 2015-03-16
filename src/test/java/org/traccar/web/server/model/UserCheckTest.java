/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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

import com.google.inject.*;
import com.google.inject.util.Modules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.traccar.web.shared.model.ApplicationSettings;
import org.traccar.web.shared.model.User;

public class UserCheckTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final TestUserProvider userProvider = new TestUserProvider();
    final TestApplicationSettingsProvider settingsProvider = new TestApplicationSettingsProvider();
    final Injector injector = Guice.createInjector(Modules.override(new GuiceServletConfig().getModule()).with(new TestModule(userProvider, settingsProvider)));

    static class TestUserProvider implements Provider<User> {
        User user;

        @Override
        public User get() {
            return user;
        }
    }

    static class TestApplicationSettingsProvider implements Provider<ApplicationSettings> {
        ApplicationSettings settings = new ApplicationSettings();
        @Override
        public ApplicationSettings get() {
            return settings;
        }
    }

    static class TestModule implements Module {
        final Provider<User> userProvider;
        final Provider<ApplicationSettings> settingsProvider;

        TestModule(Provider<User> userProvider, Provider<ApplicationSettings> settingsProvider) {
            this.userProvider = userProvider;
            this.settingsProvider = settingsProvider;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(User.class).toProvider(userProvider);
            binder.bind(ApplicationSettings.class).toProvider(settingsProvider);
        }
    }

    User admin() {
        userProvider.user = new User();
        userProvider.user.setAdmin(true);
        return userProvider.user;
    }

    User manager() {
        userProvider.user = new User();
        userProvider.user.setManager(true);
        return userProvider.user;
    }

    User user() {
        userProvider.user = new User();
        return userProvider.user;
    }

    static class RequireWriteObject {
        @RequireWrite
        public void requireWrite() {
        }
    }

    @Test
    public void testRequireWriteNoUser() {
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireWriteObject.class).requireWrite();
    }

    @Test
    public void testRequireWriteReadOnlyUser() {
        user().setReadOnly(true);
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireWriteObject.class).requireWrite();
    }

    @Test
    public void testRequireWriteReadOnlyAdmin() {
        admin().setReadOnly(true);
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireWriteObject.class).requireWrite();
    }

    @Test
    public void testRequireWriteReadOnlyManager() {
        manager().setReadOnly(true);
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireWriteObject.class).requireWrite();
    }

    @Test
    public void testRequireWriteOrdinaryUser() {
        user();
        injector.getInstance(RequireWriteObject.class).requireWrite();
    }

    static class ManagesDevicesObject {
        @ManagesDevices
        public void manageDevices() {
        }
    }

    @Test
    public void testManagesDevicesNoUser() {
        thrown.expect(SecurityException.class);
        injector.getInstance(ManagesDevicesObject.class).manageDevices();
    }

    @Test
    public void testManagesDevicesUser() {
        user();
        injector.getInstance(ManagesDevicesObject.class).manageDevices();
    }

    @Test
    public void testManagesDevicesRestricted() {
        settingsProvider.settings.setDisallowDeviceManagementByUsers(true);
        user();
        thrown.expect(SecurityException.class);
        injector.getInstance(ManagesDevicesObject.class).manageDevices();
    }

    @Test
    public void testManagesDevicesRestrictedAdmin() {
        settingsProvider.settings.setDisallowDeviceManagementByUsers(true);
        admin();
        injector.getInstance(ManagesDevicesObject.class).manageDevices();
    }

    @Test
    public void testManagesDevicesRestrictedManager() {
        settingsProvider.settings.setDisallowDeviceManagementByUsers(true);
        manager();
        injector.getInstance(ManagesDevicesObject.class).manageDevices();
    }

    static class RequireUserObject {
        @RequireUser
        public void requireUser() {
        }

        @RequireUser(roles = { Role.ADMIN })
        public void requireAdmin() {
        }

        @RequireUser(roles = { Role.MANAGER })
        public void requireManager() {
        }

        @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
        public void requireAdminOrManager() {
        }
    }

    @Test
    public void testRequireUserNoUser() {
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireUserObject.class).requireUser();
    }

    @Test
    public void testRequireUserOrdinaryUser() {
        user();
        injector.getInstance(RequireUserObject.class).requireUser();
    }

    @Test
    public void testRequireUserReadOnlyUser() {
        user().setReadOnly(true);
        injector.getInstance(RequireUserObject.class).requireUser();
    }

    @Test
    public void testRequireAdminOrdinaryUser() {
        user();
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireUserObject.class).requireAdmin();
    }

    @Test
    public void testRequireAdminManager() {
        manager();
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireUserObject.class).requireAdmin();
    }

    @Test
    public void testRequireAdmin() {
        admin();
        injector.getInstance(RequireUserObject.class).requireAdmin();
    }

    @Test
    public void testRequireManagerOrdinaryUser() {
        user();
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireUserObject.class).requireManager();
    }

    @Test
    public void testRequireManagerAdmin() {
        admin();
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireUserObject.class).requireManager();
    }

    @Test
    public void testRequireManager() {
        manager();
        injector.getInstance(RequireUserObject.class).requireManager();
    }

    @Test
    public void testRequireAdminOrManagerOrdinaryUser() {
        user();
        thrown.expect(SecurityException.class);
        injector.getInstance(RequireUserObject.class).requireAdminOrManager();
    }

    @Test
    public void testRequireAdminOrManagerAdmin() {
        admin();
        injector.getInstance(RequireUserObject.class).requireAdminOrManager();
    }

    @Test
    public void testRequireAdminOrManagerManager() {
        manager();
        injector.getInstance(RequireUserObject.class).requireAdminOrManager();
    }

    public static class MultipleAnnotationsObject {
        @RequireUser(roles = { Role.ADMIN })
        @ManagesDevices
        @RequireWrite
        public void someMethod() {
        }

        @RequireUser
        @ManagesDevices
        public void anotherMethod() {
        }
    }

    @Test
    public void testMultipleAnnotations() {
        thrown.expect(SecurityException.class);
        injector.getInstance(MultipleAnnotationsObject.class).someMethod();
    }

    @Test
    public void testMultipleAnnotationsOrdinaryUser() {
        user();
        thrown.expect(SecurityException.class);
        injector.getInstance(MultipleAnnotationsObject.class).someMethod();
    }

    @Test
    public void testMultipleAnnotationsAdmin() {
        admin();
        injector.getInstance(MultipleAnnotationsObject.class).someMethod();
    }

    @Test
    public void testMultipleAnnotationsReadOnlyAdmin() {
        admin().setReadOnly(true);
        thrown.expect(SecurityException.class);
        injector.getInstance(MultipleAnnotationsObject.class).someMethod();
    }

    @Test
    public void testMultipleAnnotationsUserDisallowedDeviceManagement() {
        user();
        settingsProvider.settings.setDisallowDeviceManagementByUsers(true);
        thrown.expect(SecurityException.class);
        injector.getInstance(MultipleAnnotationsObject.class).anotherMethod();
    }
}
