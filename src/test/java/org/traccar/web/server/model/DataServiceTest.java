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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static org.traccar.web.server.model.PasswordUtils.*;
import static org.traccar.web.shared.model.PasswordHashMethod.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.EventService;
import org.traccar.web.client.model.NotificationService;
import org.traccar.web.shared.model.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

public class DataServiceTest {
    static Long currentUserId;

    public static class TestUserProvider implements Provider<User> {
        @Inject
        Provider<EntityManager> entityManager;

        @Transactional
        @Override
        public User get() {
            if (currentUserId == null) {
                return entityManager.get().createQuery("SELECT u FROM User u", User.class).getResultList().get(0);
            } else {
                return entityManager.get().find(User.class, currentUserId);
            }
        }
    }

    public static class TestPersistenceModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new JpaPersistModule("test"));

            bind(DataService.class).to(DataServiceImpl.class);
            bind(NotificationService.class).to(NotificationServiceImpl.class);
            bind(EventService.class).to(EventServiceImpl.class);
            bind(HttpServletRequest.class).toProvider(new com.google.inject.Provider<HttpServletRequest>() {
                @Override
                public HttpServletRequest get() {
                    return mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
                }
            });
            bind(User.class).toProvider(TestUserProvider.class);
            bind(ApplicationSettings.class).toProvider(ApplicationSettingsProvider.class);
        }
    }

    static Injector injector = Guice.createInjector(new TestPersistenceModule());
    static DataService dataService;

    @BeforeClass
    public static void init() throws Exception {
        injector.getInstance(PersistService.class).start();
        dataService = injector.getInstance(DataService.class);

        runInTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                injector.getInstance(DBMigrations.CreateAdmin.class).migrate(injector.getInstance(EntityManager.class));
                ApplicationSettings applicationSettings = dataService.getApplicationSettings();
                applicationSettings.setDefaultHashImplementation(PasswordHashMethod.MD5);
                applicationSettings.setEventRecordingEnabled(false);
                dataService.updateApplicationSettings(applicationSettings);
                return null;
            }
        });
    }

    @After
    public void cleanup() {
        currentUserId = null;
    }

    @AfterClass
    public static void destroy() {
        injector = null;
        dataService = null;
    }

    @Test
    public void testDeleteDeviceWithSpecificGeoFence() throws TraccarException {
        Device device = new Device();
        device.setUniqueId("1");
        device.setName("D1");
        device.setMaintenances(Collections.<Maintenance>emptyList());
        device.setSensors(Collections.<Sensor>emptyList());
        device = dataService.addDevice(device);

        GeoFence geoFence = new GeoFence();
        geoFence.setName("GF1");
        geoFence.setTransferDevices(new HashSet<>(Collections.singleton(device)));
        dataService.addGeoFence(geoFence);

        dataService.removeDevice(device);

        assertEquals(0, dataService.getDevices().size());
        List<GeoFence> geoFences = dataService.getGeoFences();
        assertEquals(1, geoFences.size());
        assertTrue(geoFences.get(0).getTransferDevices().isEmpty());
        assertTrue(geoFences.get(0).getDevices().isEmpty());
    }

    @Test
    public void testDeleteUserWithNotificationSettings() throws TraccarException {
        Long originalUserId = injector.getProvider(User.class).get().getId();

        User user = new User("test", "test");
        user.setManager(true);
        user = dataService.addUser(user);

        NotificationService notificationService = injector.getInstance(NotificationService.class);
        currentUserId = user.getId();
        notificationService.saveSettings(new NotificationSettings());

        currentUserId = originalUserId;
        dataService.removeUser(user);

        assertEquals(1, dataService.getUsers().size());
        assertEquals(originalUserId.longValue(), dataService.getUsers().get(0).getId());
    }

    @Test
    public void testDeleteUserWithNotificationSettingsAndTemplate() throws TraccarException {
        Long originalUserId = injector.getProvider(User.class).get().getId();

        User user = new User("test", "test");
        user.setManager(true);
        user = dataService.addUser(user);

        NotificationService notificationService = injector.getInstance(NotificationService.class);
        currentUserId = user.getId();
        NotificationSettings settings = new NotificationSettings();
        settings.setTransferTemplates(new HashMap<DeviceEventType, NotificationTemplate>());
        settings.getTransferTemplates().put(DeviceEventType.OFFLINE, new NotificationTemplate());
        notificationService.saveSettings(settings);

        currentUserId = originalUserId;
        dataService.removeUser(user);

        assertEquals(1, dataService.getUsers().size());
        assertEquals(originalUserId.longValue(), dataService.getUsers().get(0).getId());
    }

    @Test
    public void testResetPasswordByAdmin() throws TraccarException {
        User user = new User("test", "test");
        user = dataService.addUser(user);

        user.setPassword("test1");
        user = dataService.updateUser(user);

        dataService.removeUser(user);

        assertEquals("test1", user.getPassword());
    }

    @Test
    public void testResetPasswordByManager() throws TraccarException {
        User manager = new User("manager", "manager");
        manager.setManager(Boolean.TRUE);
        manager = dataService.addUser(manager);

        currentUserId = manager.getId();

        User user = new User("test", "test");
        user = dataService.addUser(user);

        user.setPassword("test1");
        user = dataService.updateUser(user);

        currentUserId = null;
        dataService.removeUser(user);
        dataService.removeUser(manager);

        assertEquals("test1", user.getPassword());
    }

    @Test
    public void testLoginPasswordHashAndSalt() throws Exception {
        String salt = dataService.getApplicationSettings().getSalt();
        // ordinary log in
        User admin = dataService.login("admin", "admin");
        assertEquals(hash(MD5, "admin", salt, ""), admin.getPassword());
        // log in with hash
        admin = dataService.login("admin", hash(MD5, "admin", salt, null), true);
        assertEquals(hash(MD5, "admin", salt, ""), admin.getPassword());
        // update user
        runInTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                injector.getInstance(EntityManager.class).createQuery("UPDATE User u SET u.password=:pwd")
                        .setParameter("pwd", hash(MD5, "admin", null, null))
                        .executeUpdate();
                return null;
            }
        });
        // log in with hash will not be possible anymore
        try {
            admin = dataService.login("admin", hash(MD5, "admin", salt, null), true);
            fail("Should be impossible to log in with different hash");
        } catch (IllegalStateException expected) {
            // do nothing since exception is expected in this case
        }
        // check logging in with old hash (for backwards compatibility)
        admin = dataService.login("admin", hash(MD5, "admin", null, null), true);
        assertEquals(hash(MD5, "admin", null, null), admin.getPassword());
        // log in and check if password is updated
        admin = dataService.login("admin", "admin");
        assertEquals(hash(MD5, "admin", salt, null), admin.getPassword());
    }

    @Test
    public void testDeviceOwner() throws Exception {
        Long originalUserId = injector.getProvider(User.class).get().getId();

        User user = new User("test", "test");
        user = dataService.addUser(user);

        Device device = new Device();
        device.setUniqueId("1");
        device.setName("D1");
        device.setMaintenances(Collections.<Maintenance>emptyList());
        device.setSensors(Collections.<Sensor>emptyList());
        currentUserId = user.getId();
        device = dataService.addDevice(device);
        currentUserId = originalUserId;

        assertEquals(user, device.getOwner());
        dataService.removeUser(user);
        runInTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Device device = dataService.getDevices().get(0);
                assertTrue(device.getUsers().isEmpty());
                assertNull(device.getOwner());
                return null;
            }
        });

    }

    @Test
    public void testDeleteManagerUser() throws TraccarException {
        Long originalUserId = injector.getProvider(User.class).get().getId();

        User manager = new User("manager", "manager");
        manager.setManager(true);
        manager = dataService.addUser(manager);

        currentUserId = manager.getId();
        User user = new User("user", "user");
        user = dataService.addUser(user);

        currentUserId = originalUserId;
        dataService.removeUser(manager);

        List<User> users = dataService.getUsers();
        assertEquals(2, users.size());
        user = users.get(users.indexOf(user));
        assertEquals(currentUserId.longValue(), user.getManagedBy().getId());

        dataService.removeUser(user);
    }

    private static <V> V runInTransaction(Callable<V> c) throws Exception {
        UnitOfWork unitOfWork = injector.getInstance(UnitOfWork.class);
        unitOfWork.begin();
        EntityManager entityManager = injector.getInstance(EntityManager.class);
        entityManager.getTransaction().begin();
        try {
            V result = c.call();
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception ex) {
            entityManager.getTransaction().rollback();
            throw ex;
        } finally {
            unitOfWork.end();
        }
    }
}
