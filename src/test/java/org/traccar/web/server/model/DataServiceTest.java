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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.After;
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
import java.util.HashSet;
import java.util.List;

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
                    return null;
                }
            });
            bind(User.class).toProvider(TestUserProvider.class);
        }
    }

    static Injector injector = Guice.createInjector(new TestPersistenceModule());
    static DataService dataService;

    @BeforeClass
    public static void init() throws Exception {
        injector.getInstance(PersistService.class).start();
        dataService = injector.getInstance(DataService.class);

        UnitOfWork unitOfWork = injector.getInstance(UnitOfWork.class);
        unitOfWork.begin();
        EntityManager entityManager = injector.getInstance(EntityManager.class);
        entityManager.getTransaction().begin();
        injector.getInstance(DBMigrations.CreateAdmin.class).migrate(entityManager);
        entityManager.getTransaction().commit();
        unitOfWork.end();
    }

    @After
    public void cleanup() {
        currentUserId = null;
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
        geoFence.setTransferDevices(new HashSet<Device>(Collections.singleton(device)));
        dataService.addGeoFence(geoFence);

        dataService.removeDevice(device);

        assertEquals(0, dataService.getDevices().size());
        List<GeoFence> geoFences = dataService.getGeoFences();
        assertEquals(1, geoFences.size());
        assertTrue(geoFences.get(0).getTransferDevices().isEmpty());
        assertTrue(geoFences.get(0).getDevices().isEmpty());
    }

    @Test
    public void testDeleteUserWithNotificationSettings() {
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
    public void testResetPasswordByAdmin() {
        User user = new User("test", "test");
        user = dataService.addUser(user);

        user.setPassword("test1");
        user = dataService.updateUser(user);

        dataService.removeUser(user);

        assertEquals("test1", user.getPassword());
    }

    @Test
    public void testResetPasswordByManager() {
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
}
