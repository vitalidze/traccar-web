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

import static org.traccar.web.shared.model.Device.DEFAULT_MOVING_ARROW_COLOR;
import static org.traccar.web.shared.model.Device.DEFAULT_PAUSED_ARROW_COLOR;
import static org.traccar.web.shared.model.Device.DEFAULT_STOPPED_ARROW_COLOR;
import static org.traccar.web.shared.model.Device.DEFAULT_OFFLINE_ARROW_COLOR;

import org.traccar.web.shared.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBMigrations {
    public void migrate(EntityManager em) throws Exception {
        for (Migration migration : new Migration[] {
                new SetUpdateInterval(),
                new SetTimePrintInterval(),
                new SetDefaultFilteringSettings(),
                new SetDefaultMapViewSettings(),
                new SetDefaultMapOverlays(),
                new SetManagerFlag(),
                new SetNotificationsFlag(),
                new SetReadOnlyFlag(),
                new SetBlockedFlag(),
                new SetArchiveFlag(),
                new AddDefaultNotifications(),
                new SetDefaultDeviceTimeout(),
                new SetDefaultDeviceOdometer(),
                new SetDefaultIdleSpeedThreshold(),
                new SetDefaultMinIdleTime(),
                new SetDefaultDisallowDeviceManagementByUsers(),
                new SetDefaultEventRecordingEnabled(),
                new SetDefaultLanguage(),
                new SetDefaultMapType(),
                new CreateAdmin(),
                new SetDefaultDeviceIconType(),
                new SetDefaultDeviceIconModeAndRotation(),
                new SetDefaultArrowIconSettings(),
                new SetDefaultDeviceShowNameProtocolAndOdometer(),
                new SetDefaultDeviceIconArrowRadius(),
                new SetDefaultHashImplementation(),
                new SetGlobalHashSalt(),
                new SetDefaultAllowCommandsOnlyForAdmins(),
                new SetDefaultNotificationExpirationPeriod(),
                new SetDefaultUserSettings(),
                new SetArchiveDefaultColumns(),
                new SetGeoFenceAllDevicesFlag(),
                new SetReportsFilterAndPreview(),
                new SetDefaultExpiredFlagForEvents(),
                new SetDefaultMatchServiceSettings(),
                new RemoveMapQuest(),
                new SetUserHashSalt()
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
            TypedQuery<User> query = em.createQuery("SELECT x FROM User x WHERE x.admin = :adminValue", User.class);
            List<User> results = query.setParameter("adminValue", true).getResultList();
            if (results.isEmpty()) {
                User user = new User();
                user.setLogin("admin");
                user.setPassword("admin");
                user.setPasswordHashMethod(PasswordHashMethod.PLAIN);
                user.setAdmin(true);
                user.setManager(false);
                em.persist(user);
            } else if (results.size() == 1) {
                User singleAdmin = results.get(0);
                if (singleAdmin.getLogin() == null && singleAdmin.getPassword() == null) {
                    singleAdmin.setLogin("admin");
                    singleAdmin.setPassword("admin");
                    singleAdmin.setPasswordHashMethod(PasswordHashMethod.PLAIN);
                }
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
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.maximizeOverviewMap = :b WHERE S.maximizeOverviewMap IS NULL")
                    .setParameter("b", false)
                    .executeUpdate();
        }
    }

    static class SetDefaultMapOverlays implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.overlays = :overlays WHERE S.overlays IS NULL")
                    .setParameter("overlays", "GEO_FENCES,VECTOR,MARKERS")
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
                    .setParameter("idleSpeedThreshold", 0d)
                    .executeUpdate();
        }
    }

    static class SetDefaultMinIdleTime implements Migration {

        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.minIdleTime = :minIdleTime WHERE D.minIdleTime IS NULL")
                    .setParameter("minIdleTime", Integer.valueOf(Device.DEFAULT_MIN_IDLE_TIME))
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
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconType = :iconType WHERE D.icon IS NULL AND D.iconType IS NULL")
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
                    .setParameter("dh", PasswordHashMethod.MD5)
                    .executeUpdate();
        }
    }

    static class SetGlobalHashSalt implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.salt = :s WHERE S.salt IS NULL")
                    .setParameter("s", PasswordUtils.generateRandomString())
                    .executeUpdate();
        }
    }

    static class SetDefaultUserSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            List<ApplicationSettings> appSettings = em.createQuery("SELECT x FROM ApplicationSettings x", ApplicationSettings.class).getResultList();
            for (User user : em.createQuery("SELECT u FROM " + User.class.getName() + " u WHERE u.userSettings IS NULL", User.class).getResultList()) {
                UserSettings defaultSettings = appSettings.isEmpty() || appSettings.get(0).getUserSettings() == null
                        ? new UserSettings()
                        : appSettings.get(0).getUserSettings().copy();
                user.setUserSettings(defaultSettings);
                em.persist(user);
            }
        }
    }

    static class SetDefaultFilteringSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getName() + " S SET S.hideZeroCoordinates = :false, S.hideInvalidLocations = :false, S.hideDuplicates = :false WHERE S.hideZeroCoordinates IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
        }
    }

    static class SetArchiveDefaultColumns implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (User user : em.createQuery("SELECT u FROM User u WHERE u NOT IN (SELECT user FROM UIStateEntry WHERE name=:archiveGridStateId)", User.class)
                             .setParameter("archiveGridStateId", UIStateEntry.ARCHIVE_GRID_STATE_ID)
                             .getResultList()) {
                em.persist(UIStateEntry.createDefaultArchiveGridStateEntry(user));
            }
        }
    }

    static class SetNotificationsFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.notifications = :n WHERE U.notifications IS NULL")
                    .setParameter("n", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class AddDefaultNotifications implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (User user : em.createQuery("SELECT u FROM User u WHERE u.notifications=:b", User.class).setParameter("b", Boolean.TRUE).getResultList()) {
                user.getNotificationEvents().add(DeviceEventType.OFFLINE);
                user.getNotificationEvents().add(DeviceEventType.GEO_FENCE_ENTER);
                user.getNotificationEvents().add(DeviceEventType.GEO_FENCE_EXIT);
                // reset flag to prevent further migrations
                user.setNotifications(false);
            }
        }
    }

    static class SetDefaultEventRecordingEnabled implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.eventRecordingEnabled = :b WHERE S.eventRecordingEnabled IS NULL")
                    .setParameter("b", Boolean.TRUE)
                    .executeUpdate();
        }
    }

    static class SetReadOnlyFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.readOnly = :ro WHERE U.readOnly IS NULL")
                    .setParameter("ro", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetBlockedFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.blocked = :b WHERE U.blocked IS NULL")
                    .setParameter("b", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetArchiveFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.archive = :b WHERE U.archive IS NULL")
                    .setParameter("b", Boolean.TRUE)
                    .executeUpdate();
        }
    }

    static class SetGeoFenceAllDevicesFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + GeoFence.class.getName() + " G SET G.allDevices = :b WHERE G.allDevices IS NULL")
                    .setParameter("b", Boolean.TRUE)
                    .executeUpdate();
        }
    }

    static class SetDefaultLanguage implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.language = :b WHERE S.language IS NULL")
                    .setParameter("b", "default")
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceOdometer implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.odometer = :o WHERE D.odometer IS NULL OR D.odometer <= 0")
                    .setParameter("o", 0d)
                    .executeUpdate();

            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.autoUpdateOdometer = :b WHERE D.autoUpdateOdometer IS NULL")
                    .setParameter("b", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetReportsFilterAndPreview implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Report.class.getSimpleName() + " R SET R.preview = :f WHERE R.preview IS NULL")
                    .setParameter("f", false)
                    .executeUpdate();
            em.createQuery("UPDATE " + Report.class.getSimpleName() + " R SET R.disableFilter = :f WHERE R.disableFilter IS NULL")
                    .setParameter("f", false)
                    .executeUpdate();
        }
    }

    static class SetDefaultNotificationExpirationPeriod implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.notificationExpirationPeriod = :period WHERE S.notificationExpirationPeriod IS NULL")
                    .setParameter("period", Integer.valueOf(ApplicationSettings.DEFAULT_NOTIFICATION_EXPIRATION_PERIOD))
                    .executeUpdate();
        }
    }

    static class SetDefaultExpiredFlagForEvents implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + DeviceEvent.class.getName() + " E SET E.expired=:false WHERE E.expired IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceIconModeAndRotation implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconRotation=:false WHERE D.iconRotation IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconMode=:icon WHERE D.iconMode IS NULL")
                    .setParameter("icon", DeviceIconMode.ICON)
                    .executeUpdate();
        }
    }

    static class SetDefaultArrowIconSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            Map<String, String> defaultColors = new HashMap<>();
            defaultColors.put("iconArrowMovingColor", DEFAULT_MOVING_ARROW_COLOR);
            defaultColors.put("iconArrowPausedColor", DEFAULT_PAUSED_ARROW_COLOR);
            defaultColors.put("iconArrowStoppedColor", DEFAULT_STOPPED_ARROW_COLOR);
            defaultColors.put("iconArrowOfflineColor", DEFAULT_OFFLINE_ARROW_COLOR);

            for (Map.Entry<String, String> e : defaultColors.entrySet()) {
                em.createQuery("UPDATE " + Device.class.getName() + " D SET D." + e.getKey() + "=:color WHERE D." + e.getKey() + " IS NULL")
                        .setParameter("color", e.getValue())
                        .executeUpdate();
            }
        }
    }

    static class SetDefaultDeviceShowNameProtocolAndOdometer implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (String prop : new String[] { "showName", "showProtocol", "showOdometer" }) {
                em.createQuery("UPDATE " + Device.class.getName() + " D SET D." + prop + "=:true WHERE D." + prop + " IS NULL")
                        .setParameter("true", true)
                        .executeUpdate();
            }
        }
    }

    static class SetDefaultMatchServiceSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            // for existing installations, which are using v4 and has no 'matchServiceType' set yet
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.matchServiceType = :msType WHERE S.matchServiceURL IS NOT NULL")
                    .setParameter("msType", MatchServiceType.OSRM_V4)
                    .executeUpdate();
            // for new installations without match service type set
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.matchServiceType = :msType, S.matchServiceURL = :url WHERE S.matchServiceType IS NULL")
                    .setParameter("msType", MatchServiceType.OSRM_V5)
                    .setParameter("url", MatchServiceType.OSRM_V5.getDefaultURL())
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceIconArrowRadius implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconArrowRadius = :radius WHERE D.iconArrowRadius IS NULL")
                    .setParameter("radius", Device.DEFAULT_ARROW_RADIUS)
                    .executeUpdate();
        }
    }

    static class SetDefaultAllowCommandsOnlyForAdmins implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.allowCommandsOnlyForAdmins = :false WHERE S.allowCommandsOnlyForAdmins IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
        }
    }

    static class RemoveMapQuest implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createNativeQuery("UPDATE user_settings SET mapType=? WHERE mapType=? OR mapType=?")
                    .setParameter(1, UserSettings.MapType.OSM.name())
                    .setParameter(2, "MAPQUEST_ROAD")
                    .setParameter(3, "MAPQUEST_AERIAL")
                    .executeUpdate();
        }
    }

    static class SetUserHashSalt implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (User user : em.createQuery("SELECT x FROM " + User.class.getName() + " x WHERE x.salt IS NULL", User.class)
                    .getResultList()) {
                user.setSalt(PasswordUtils.generateRandomUserSalt());
            }
        }
    }
}
