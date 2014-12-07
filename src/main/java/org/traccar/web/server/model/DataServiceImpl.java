/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;

import org.traccar.web.client.model.DataService;
import org.traccar.web.shared.model.*;

@Singleton
public class DataServiceImpl extends RemoteServiceServlet implements DataService {
    private static final long serialVersionUID = 1;

    @Inject
    private Provider<User> sessionUser;

    @Inject
    private Provider<ApplicationSettings> applicationSettings;

    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    private Provider<HttpServletRequest> request;

    @Override
    public void init() throws ServletException {
        super.init();

        /**
         * Perform database migrations
         */
        try {
            new DBMigrations().migrate(entityManager.get());
        } catch (Exception e) {
            throw new RuntimeException("Unable to perform DB migrations", e);
        }
    }

    EntityManager getSessionEntityManager() {
        return entityManager.get();
    }

    private void setSessionUser(User user) {
        HttpSession session = request.get().getSession();
        if (user != null) {
            session.setAttribute(CurrentUserProvider.ATTRIBUTE_USER_ID, user.getId());
        } else {
            session.removeAttribute(CurrentUserProvider.ATTRIBUTE_USER_ID);
        }
    }

    User getSessionUser() {
        return sessionUser.get();
    }

    @Transactional
    @RequireUser
    @Override
    public User authenticated() throws IllegalStateException {
        return getSessionUser();
    }

    @Transactional
    @Override
    public User login(String login, String password) {
        EntityManager entityManager = getSessionEntityManager();
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT x FROM User x WHERE x.login = :login", User.class);
        query.setParameter("login", login);
        List<User> results = query.getResultList();

        if (results.isEmpty() || password.equals("")) throw new IllegalStateException();

        User user = null;

        // Since switch with strings only came with java 1.7, lets use if...
        if (results.get(0).getPasswordType().equals("sha512")) {
            if (password_hasher(password,"SHA-512").equals(results.get(0).getPassword())) {
                user = results.get(0);
            }
        } else if (results.get(0).getPasswordType().equals("plain")) {
            if (password.equals(results.get(0).getPassword())) {
                user = results.get(0);
            }
        }
        if (user == null) {
            throw new IllegalStateException();
        }

        // Check if hash has changed in application settings
        if (!results.get(0).getPasswordType().equals(getApplicationSettings().getDefaultHashImplementation())) {
            if (getApplicationSettings().getDefaultHashImplementation().equals("sha512")) {
                user.setPassword(password_hasher(password, "SHA-512"));
            } else {
                user.setPassword(password);
            }
            user.setPasswordType(getApplicationSettings().getDefaultHashImplementation());
            getSessionEntityManager().persist(user);
        }
        setSessionUser(user);
        return user;
    }

    @RequireUser
    @Override
    public boolean logout() {
        setSessionUser(null);
        return true;
    }

    @Transactional
    @Override
    public User register(String login, String password) {
        if (getApplicationSettings().getRegistrationEnabled()) {
            TypedQuery<User> query = getSessionEntityManager().createQuery(
                    "SELECT x FROM User x WHERE x.login = :login", User.class);
            query.setParameter("login", login);
            List<User> results = query.getResultList();
            if (results.isEmpty()) {
                    User user = new User();
                    user.setLogin(login);
                    user.setPassword(password);
                    // TODO <apf>: password hash mechanism
                    user.setManager(Boolean.TRUE); // registered users are always managers
                    getSessionEntityManager().persist(user);
                    setSessionUser(user);
                    return user;
            }
            else
            {
                throw new IllegalStateException();
            }
        } else {
            throw new SecurityException();
        }
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public List<User> getUsers() {
        User currentUser = getSessionUser();
        List<User> users = new LinkedList<User>();
        if (currentUser.getAdmin()) {
            users.addAll(getSessionEntityManager().createQuery("SELECT x FROM User x", User.class).getResultList());
        } else {
            users.addAll(currentUser.getAllManagedUsers());
        }
        return users;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public User addUser(User user) {
        User currentUser = getSessionUser();
        if (user.getLogin().isEmpty() || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException();
        }
        String login = user.getLogin();
        TypedQuery<User> query = getSessionEntityManager().createQuery("SELECT x FROM User x WHERE x.login = :login", User.class);
        query.setParameter("login", login);
        List<User> results = query.getResultList();

        if (results.isEmpty()) {
            if (!currentUser.getAdmin()) {
                user.setAdmin(false);
            }
            user.setManagedBy(currentUser);
            // TODO <apf>: password hash mechanism
            getSessionEntityManager().persist(user);
            return user;
        } else {
            throw new IllegalStateException();
        }
    }

    @Transactional
    @RequireUser
    @Override
    public User updateUser(User user) {
        User currentUser = getSessionUser();
        if (user.getLogin().isEmpty() || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (currentUser.getAdmin() || (currentUser.getId() == user.getId() && !user.getAdmin())) {
            EntityManager entityManager = getSessionEntityManager();
            // TODO: better solution?
            if (currentUser.getId() == user.getId()) {
                currentUser.setLogin(user.getLogin());
                currentUser.setPassword(user.getPassword());
                currentUser.setUserSettings(user.getUserSettings());
                currentUser.setAdmin(user.getAdmin());
                currentUser.setManager(user.getManager());
                // TODO <apf>: password hash mechanism
                entityManager.merge(currentUser);
                user = currentUser;
            } else {
                // update password
                if (currentUser.getAdmin() || currentUser.getManager()) {
                    User existingUser = entityManager.find(User.class, user.getId());
                    existingUser.setPassword(user.getPassword());
                    // TODO <apf>: password hash mechanism
                    entityManager.merge(existingUser);
                } else {
                    throw new SecurityException();
                }
            }

            return user;
        } else {
            throw new SecurityException();
        }
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public User removeUser(User user) {
        EntityManager entityManager = getSessionEntityManager();
        user = entityManager.merge(user);
        for (Device device : user.getDevices()) {
            device.getUsers().remove(user);
        }
        entityManager.remove(user);
        return user;
    }

    @Transactional
    @RequireUser
    @Override
    public List<Device> getDevices() {
        User user = getSessionUser();
        if (user.getAdmin()) {
            return getSessionEntityManager().createQuery("SELECT x FROM Device x").getResultList();
        }
        return user.getAllAvailableDevices();
    }

    @Transactional
    @RequireUser
    @ManagesDevices
    @Override
    public Device addDevice(Device device) {
        EntityManager entityManager = getSessionEntityManager();
        TypedQuery<Device> query = entityManager.createQuery("SELECT x FROM Device x WHERE x.uniqueId = :id", Device.class);
        query.setParameter("id", device.getUniqueId());
        List<Device> results = query.getResultList();

        User user = getSessionUser();

        if (results.isEmpty()) {
            device.setUsers(new HashSet<User>(1));
            device.getUsers().add(user);
            entityManager.persist(device);
            return device;
        } else {
            throw new IllegalStateException();
        }
    }

    @Transactional
    @RequireUser
    @ManagesDevices
    @Override
    public Device updateDevice(Device device) {
        EntityManager entityManager = getSessionEntityManager();
        TypedQuery<Device> query = entityManager.createQuery("SELECT x FROM Device x WHERE x.uniqueId = :id AND x.id <> :primary_id", Device.class);
        query.setParameter("primary_id", device.getId());
        query.setParameter("id", device.getUniqueId());
        List<Device> results = query.getResultList();

        if (results.isEmpty()) {
            Device tmp_device = entityManager.find(Device.class, device.getId());
            tmp_device.setName(device.getName());
            tmp_device.setUniqueId(device.getUniqueId());
            tmp_device.setTimeout(device.getTimeout());
            tmp_device.setIdleSpeedThreshold(device.getIdleSpeedThreshold());
            tmp_device.setIconType(device.getIconType());
            return tmp_device;
        } else {
            throw new IllegalStateException();
        }
    }

    @Transactional
    @RequireUser
    @ManagesDevices
    @Override
    public Device removeDevice(Device device) {
        EntityManager entityManager = getSessionEntityManager();
        User user = getSessionUser();
        device = entityManager.find(Device.class, device.getId());
        if (user.getAdmin() || user.getManager()) {
            device.getUsers().removeAll(getUsers());
        }
        device.getUsers().remove(user);
        /**
         * Remove device only if there is no more associated users in DB
         */
        if (device.getUsers().isEmpty()) {
            device.setLatestPosition(null);
            entityManager.flush();
            Query query = entityManager.createQuery("DELETE FROM Position x WHERE x.device = :device");
            query.setParameter("device", device);
            query.executeUpdate();
            entityManager.remove(device);
        }
        return device;
    }

    @Transactional
    @RequireUser
    @Override
    public List<Position> getPositions(Device device, Date from, Date to, String speedModifier, Double speed) {
        EntityManager entityManager = getSessionEntityManager();
        List<Position> positions = new LinkedList<Position>();
        TypedQuery<Position> query = entityManager.createQuery(
                "SELECT x FROM Position x WHERE x.device = :device AND x.time BETWEEN :from AND :to" + (speed == null ? "" : " AND x.speed " + speedModifier + " :speed"), Position.class);
        query.setParameter("device", device);
        query.setParameter("from", from);
        query.setParameter("to", to);
        if (speed != null) {
            query.setParameter("speed", getSessionUser().getUserSettings().getSpeedUnit().toKnots(speed));
        }
        positions.addAll(query.getResultList());

        final double radKoef = Math.PI / 180;
        final double earthRadius = 6371.01; // Radius of the earth in km

        for (int i = 0; i < positions.size(); i++) {
            if (i > 0) {
                Position positionA = positions.get(i - 1);
                Position positionB = positions.get(i);

                double dLat = (positionA.getLatitude() - positionB.getLatitude()) * radKoef;
                double dLon = (positionA.getLongitude() - positionB.getLongitude()) * radKoef;
                double a =
                        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                Math.cos(positionA.getLatitude() * radKoef) * Math.cos(positionB.getLatitude() * radKoef) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2)
                        ;
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                positionB.setDistance(earthRadius * c); // Distance in km
            }
        }
        return positions;
    }

    @RequireUser
    @Transactional
    @Override
    public List<Position> getLatestPositions() {
        List<Position> positions = new LinkedList<Position>();
        List<Device> devices = getDevices();
        if (devices != null && !devices.isEmpty()) {
            for (Device device : devices) {
                if (device.getLatestPosition() != null) {
                    positions.add(device.getLatestPosition());
                }
            }
        }
        return positions;
    }

    @RequireUser
    @Transactional
    @Override
    public List<Position> getLatestNonIdlePositions() {
        List<Position> positions = new LinkedList<Position>();
        List<Device> devices = getDevices();
        if (devices != null && !devices.isEmpty()) {
            EntityManager entityManager = getSessionEntityManager();

            for (Device device : devices) {
                List<Position> position = entityManager.createQuery("SELECT p FROM Position p WHERE p.device = :device AND p.speed > 0 ORDER BY time DESC", Position.class)
                        .setParameter("device", device)
                        .setMaxResults(1)
                        .getResultList();

                if (position.isEmpty()) {
                    position = entityManager.createQuery("SELECT p FROM Position p WHERE p.device = :device ORDER BY time ASC", Position.class)
                        .setParameter("device", device)
                        .setMaxResults(1)
                        .getResultList();
                }

                if (!position.isEmpty()) {
                    positions.add(position.get(0));
                }
            }
        }
        return positions;
    }

    @Transactional
    @Override
    public ApplicationSettings getApplicationSettings() {
        ApplicationSettings appSettings = applicationSettings.get();
        if (appSettings == null) {
            appSettings = new ApplicationSettings();
            entityManager.get().persist(appSettings);
        }
        return appSettings;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @Override
    public void updateApplicationSettings(ApplicationSettings applicationSettings) {
        getSessionEntityManager().merge(applicationSettings);
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @Override
    public String getTrackerServerLog(short sizeKB) {
        File workingFolder = new File(System.getProperty("user.dir"));
        File logFile1 = new File(workingFolder, "logs" + File.separatorChar + "tracker-server.log");
        File logFile2 = new File(workingFolder.getParentFile(), "logs" + File.separatorChar + "tracker-server.log");
        File logFile3 = new File(workingFolder, "tracker-server.log");

        File logFile = logFile1.exists() ? logFile1 :
                logFile2.exists() ? logFile2 :
                        logFile3.exists() ? logFile3 : null;

        if (logFile != null) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(logFile, "r");
                int length = 0;
                if (raf.length() > Integer.MAX_VALUE) {
                    length = Integer.MAX_VALUE;
                } else {
                    length = (int) raf.length();
                }
                /**
                 * Read last 5 megabytes from file
                 */
                raf.seek(Math.max(0, raf.length() - sizeKB * 1024));
                byte[] result = new byte[Math.min(length, sizeKB * 1024)];
                raf.read(result);
                return new String(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    raf.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return ("Tracker server log is not available. Looked at " + logFile1.getAbsolutePath() +
                ", " + logFile2.getAbsolutePath() +
                ", " + logFile3.getAbsolutePath());
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public void saveRoles(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }

        EntityManager entityManager = getSessionEntityManager();
        User currentUser = getSessionUser();
        for (User _user : users) {
            User user = entityManager.find(User.class, _user.getId());
            if (currentUser.getAdmin()) {
                user.setAdmin(_user.getAdmin());
            }
            user.setManager(_user.getManager());
        }
    }

    @Transactional
    @RequireUser
    @Override
    public Map<User, Boolean> getDeviceShare(Device device) {
        device = getSessionEntityManager().find(Device.class, device.getId());
        List<User> users = getUsers();
        Map<User, Boolean> result = new HashMap<User, Boolean>(users.size());
        for (User user : users) {
            result.put(user, device.getUsers().contains(user));
        }
        return result;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public void saveDeviceShare(Device device, Map<User, Boolean> share) {
        EntityManager entityManager = getSessionEntityManager();
        device = entityManager.find(Device.class, device.getId());

        for (User user : getUsers()) {
            Boolean shared = share.get(user);
            if (shared == null) continue;
            if (shared.booleanValue()) {
                device.getUsers().add(user);
            } else {
                device.getUsers().remove(user);
            }
            entityManager.merge(user);
        }
    }

    /**
     * password_hasher
     *
     * @param target
     * @param hash_type
     * @return [encrypted string]
     */
    private final static String password_hasher(String target, String hash_type) {
        //TODO implement other hashing methods
        if (!hash_type.equals("SHA-512")) throw new RuntimeException("Hash type not yet implemented");
        //Resume
        try {
            final MessageDigest sha512 = MessageDigest.getInstance(hash_type);
            sha512.update(target.getBytes());
            byte data[] = sha512.digest();
            StringBuffer hexData = new StringBuffer();
            for (int byteIndex = 0; byteIndex < data.length; byteIndex++)
                hexData.append(Integer.toString((data[byteIndex] & 0xff) + 0x100, 16).substring(1));
            return hexData.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
