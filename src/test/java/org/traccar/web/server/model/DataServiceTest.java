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
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.EventService;
import org.traccar.web.shared.model.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class DataServiceTest {
    public static class TestUserProvider implements Provider<User> {
        @Inject
        Provider<EntityManager> entityManager;

        @Override
        public User get() {
            return entityManager.get().createQuery("SELECT u FROM User u", User.class).getResultList().get(0);
        }
    }

    public static class TestPersistenceModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new JpaPersistModule("test"));
            bind(DataService.class).to(DataServiceImpl.class);
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
        new File("target/testdatabase.mv.db").delete();
        new File("target/testdatabase.trace.db").delete();
        injector.getInstance(PersistService.class).start();
        dataService = injector.getInstance(DataService.class);

        EntityManager entityManager = injector.getInstance(EntityManager.class);
        entityManager.getTransaction().begin();
        injector.getInstance(DBMigrations.CreateAdmin.class).migrate(entityManager);
        entityManager.getTransaction().commit();
    }

    @Test
    public void testDeleteDeviceWithSpecificGeoFence() throws TraccarException {
        Device device = new Device();
        device.setUniqueId("1");
        device.setName("D1");
        device.setMaintenances(Collections.<Maintenance>emptyList());
        device = dataService.addDevice(device);

        GeoFence geoFence = new GeoFence();
        geoFence.setName("GF1");
        geoFence.setTransferDevices(new HashSet<Device>(Arrays.asList(device)));
        dataService.addGeoFence(geoFence);

        dataService.removeDevice(device);

        assertEquals(0, dataService.getDevices().size());
        List<GeoFence> geoFences = dataService.getGeoFences();
        assertEquals(1, geoFences.size());
        assertTrue(geoFences.get(0).getTransferDevices().isEmpty());
        assertTrue(geoFences.get(0).getDevices().isEmpty());
    }
}
