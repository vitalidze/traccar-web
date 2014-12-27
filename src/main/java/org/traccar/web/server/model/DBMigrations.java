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

import org.traccar.web.shared.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class DBMigrations {
    public void migrate(EntityManager em) throws Exception {
        for (Migration migration : new Migration[] {
                new SetUpdateInterval(),
                new SetTimePrintInterval(),
                new SetDefaultMapViewSettings(),
                new SetManagerFlag(),
                new SetDefaultDeviceTimeout(),
                new SetDefaultIdleSpeedThreshold(),
                new SetDefaultDisallowDeviceManagementByUsers(),
                new SetDefaultMapType(),
                new CreateAdmin(),
                new SetDefaultDeviceIconType(),
                new SetDefaultHashImplementation(),
                new SetDefaultUserSettings()
        }) {
            em.getTransaction().begin();
            try {
                migration.migrate(em);
                em.getTransaction().commit();
            } catch (Exception ex) {
                em.getTransaction().rollback();
                throw ex;
            }
        }
    }

    interface Migration {
        void migrate(EntityManager em) throws Exception;
    }

    /**
     * Create Administrator account
     */
    static class CreateAdmin implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            TypedQuery<User> query = em.createQuery("SELECT x FROM User x WHERE x.login = 'admin'", User.class);
            List<User> results = query.getResultList();
            if (results.isEmpty()) {
                User user = new User();
                user.setLogin("admin");
                user.setPassword("admin");
                user.setAdmin(true);
                user.setManager(false);
                em.persist(user);
            }
        }
    }

    /**
     * Set up update interval in application settings
     */
    static class SetUpdateInterval implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.updateInterval = :ui WHERE S.updateInterval IS NULL")
                    .setParameter("ui", ApplicationSettings.DEFAULT_UPDATE_INTERVAL)
                    .executeUpdate();
        }
    }

    /**
     * set up time print interval in user settings
     */
    static class SetTimePrintInterval implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.timePrintInterval = :tpi WHERE S.timePrintInterval IS NULL")
                    .setParameter("tpi", UserSettings.DEFAULT_TIME_PRINT_INTERVAL)
                    .executeUpdate();
        }
    }

    /**
     * set up showInvalidPositions flag in user settings
     */
    static class SetShowInvalidPositions implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.showInvalidPositions = :pip WHERE S.showInvalidPositions IS NULL")
                    .setParameter("pip", UserSettings.DEFAULT_SHOW_INVALID_POSITIONS)
                    .executeUpdate();
        }
    }

    /**
     * set up default map view settings
     */
    static class SetDefaultMapViewSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.zoomLevel = :zl, S.centerLongitude = :lon, S.centerLatitude = :lat WHERE S.zoomLevel IS NULL")
                    .setParameter("zl", UserSettings.DEFAULT_ZOOM_LEVEL)
                    .setParameter("lon", UserSettings.DEFAULT_CENTER_LONGITUDE)
                    .setParameter("lat", UserSettings.DEFAULT_CENTER_LATITUDE)
                    .executeUpdate();
        }
    }

    /**
     * set up manager flag
     */
    static class SetManagerFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.manager = :mgr WHERE U.manager IS NULL")
                    .setParameter("mgr", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    /**
     * set up default timeout to 5 minutes
     */
    static class SetDefaultDeviceTimeout implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.timeout = :tmout WHERE D.timeout IS NULL OR D.timeout <= 0")
                    .setParameter("tmout", Integer.valueOf(Device.DEFAULT_TIMEOUT))
                    .executeUpdate();
        }
    }

    /**
     * set up default idle speed threshold to 0
     */
    static class SetDefaultIdleSpeedThreshold implements Migration {

        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.idleSpeedThreshold = :idleSpeedThreshold WHERE D.idleSpeedThreshold IS NULL")
                    .setParameter("idleSpeedThreshold", Double.valueOf(0))
                    .executeUpdate();
        }
    }

    static class SetDefaultDisallowDeviceManagementByUsers implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.disallowDeviceManagementByUsers = :ddmbu WHERE S.disallowDeviceManagementByUsers IS NULL")
                    .setParameter("ddmbu", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetDefaultMapType implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getName() + " S SET S.mapType = :mt WHERE S.mapType IS NULL")
                    .setParameter("mt", UserSettings.MapType.OSM)
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceIconType implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconType = :iconType WHERE D.iconType IS NULL")
                    .setParameter("iconType", DeviceIconType.DEFAULT)
                    .executeUpdate();
        }
    }

    /**
     * Set up default hashing in application settings
     */
    static class SetDefaultHashImplementation implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.defaultPasswordHash = :dh WHERE S.defaultPasswordHash IS NULL")
                    .setParameter("dh", PasswordHashMethod.PLAIN)
                    .executeUpdate();
        }
    }

    static class SetDefaultUserSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (User user : em.createQuery("SELECT u FROM " + User.class.getName() + " u WHERE u.userSettings IS NULL", User.class).getResultList()) {
                user.setUserSettings(new UserSettings());
                em.persist(user);
            }
        }
    }
}
