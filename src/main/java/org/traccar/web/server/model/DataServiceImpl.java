/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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

import static org.traccar.web.server.model.PasswordUtils.generateRandomUserSalt;
import static org.traccar.web.server.model.PasswordUtils.hash;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;

import org.hibernate.proxy.HibernateProxy;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.EventService;
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

    @Inject
    private EventService eventService;

    @Inject
    private MovementDetector movementDetector;

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

        /**
         * Start movement detector
         */
        movementDetector.start();
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
    @Override
    public User authenticated() {
        return getSessionUser() == null ? null : fillUserSettings(new User(getSessionUser()));
    }

    @Transactional
    @LogCall("Login '{0}'")
    @Override
    public User login(String login, String password, boolean passwordHashed) throws TraccarException {
        EntityManager entityManager = getSessionEntityManager();
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT x FROM User x WHERE x.login = :login", User.class);
        query.setParameter("login", login);
        List<User> results = query.getResultList();

        if (results.isEmpty() || password.equals("")) throw new IllegalStateException();

        User user = results.get(0);

        String storedPassword = user.getPassword();
        // login by password 'hash'
        if (passwordHashed) {
            if (!storedPassword.equals(password)) {
                throw new IllegalStateException();
            }
        } else {
            if (!storedPassword.equals(hash(user.getPasswordHashMethod(), password, getApplicationSettings().getSalt(), user.getSalt()))) {
                // check for the old implementation without salt
                // if it matches then update password with new salt
                if (storedPassword.equals(hash(user.getPasswordHashMethod(), password, "", ""))) {
                    user.setPassword(hash(user.getPasswordHashMethod(), password, getApplicationSettings().getSalt(), user.getSalt()));
                } else {
                    throw new IllegalStateException();
                }
            }
        }

        if (user.isBlocked()) {
            throw new UserBlockedException();
        }

        if (user.isExpired()) {
            throw new UserExpiredException(user.getExpirationDate());
        }

        /*
         * If hash method has changed in application settings and password parameter is not hashed, rehash user password
         */
        if (!user.getPasswordHashMethod().equals(getApplicationSettings().getDefaultHashImplementation()) && !passwordHashed) {
            user.setPasswordHashMethod(getApplicationSettings().getDefaultHashImplementation());
            user.setPassword(hash(user.getPasswordHashMethod(), password, getApplicationSettings().getSalt(), user.getSalt()));
        }

        setSessionUser(user);
        return fillUserSettings(new User(user));
    }

    @Transactional
    @Override
    public User login(String login, String password) throws TraccarException {
        return this.login(login, password, false);
    }

    @RequireUser
    @Override
    public boolean logout() {
        setSessionUser(null);
        return true;
    }

    @Transactional
    @LogCall("Register '{0}'")
    @Override
    public User register(String login, String password) throws AccessDeniedException {
        if (getApplicationSettings().getRegistrationEnabled()) {
            TypedQuery<User> query = getSessionEntityManager().createQuery(
                    "SELECT x FROM User x WHERE x.login = :login", User.class);
            query.setParameter("login", login);
            List<User> results = query.getResultList();
            if (results.isEmpty()) {
                User user = new User();
                user.setLogin(login);
                user.setPasswordHashMethod(getApplicationSettings().getDefaultHashImplementation());
                user.setSalt(generateRandomUserSalt());
                user.setPassword(hash(user.getPasswordHashMethod(), password, getApplicationSettings().getSalt(), user.getSalt()));
                user.setManager(Boolean.TRUE); // registered users are always managers
                user.setUserSettings(getUserSettingsForNewUser());
                getSessionEntityManager().persist(user);
                getSessionEntityManager().persist(UIStateEntry.createDefaultArchiveGridStateEntry(user));
                setSessionUser(user);
                return fillUserSettings(new User(user));
            }
            else
            {
                throw new IllegalStateException();
            }
        } else {
            throw new AccessDeniedException();
        }
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public List<User> getUsers() {
        User currentUser = getSessionUser();
        List<User> users = new LinkedList<>();
        if (currentUser.getAdmin()) {
            users.addAll(getSessionEntityManager().createQuery("SELECT x FROM User x", User.class).getResultList());
        } else {
            users.addAll(currentUser.getAllManagedUsers());
        }
        List<User> result = new ArrayList<>(users.size());
        for (User user : users) {
            result.add(fillUserSettings(unproxy(user)));
        }
        return result;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @RefreshBackendPermissions
    @Override
    public User addUser(User user) throws InvalidMaxDeviceNumberForUserException {
        User currentUser = getSessionUser();
        if (user.getLogin() == null || user.getLogin().isEmpty() ||
            user.getPassword() == null || user.getPassword().isEmpty()) {
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
            validateMaximumNumberOfDevices(user, null, user.getMaxNumOfDevices());
            user.setPasswordHashMethod(getApplicationSettings().getDefaultHashImplementation());
            user.setSalt(generateRandomUserSalt());
            user.setPassword(hash(user.getPasswordHashMethod(), user.getPassword(), getApplicationSettings().getSalt(), user.getSalt()));
            if (user.getUserSettings() == null) {
                user.setUserSettings(getUserSettingsForNewUser());
            }
            user.setNotificationEvents(user.getTransferNotificationEvents());
            getSessionEntityManager().persist(user);
            getSessionEntityManager().persist(UIStateEntry.createDefaultArchiveGridStateEntry(user));
            return fillUserSettings(user);
        } else {
            throw new IllegalStateException();
        }
    }

    private void validateMaximumNumberOfDevices(User user, Integer originalValue, Integer newValue) throws InvalidMaxDeviceNumberForUserException {
        if (user.getManagedBy() != null && newValue != null) {
            int allowed = user.getManagedBy().getNumberOfDevicesToDistribute() + (originalValue == null ? 0 : originalValue);
            if (allowed - newValue < 0) {
                throw new InvalidMaxDeviceNumberForUserException(allowed);
            }
        }
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @RefreshBackendPermissions
    @Override
    public User updateUser(User user) throws AccessDeniedException {
        User currentUser = getSessionUser();
        if (user.getLogin().isEmpty() || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (currentUser.getAdmin()
            || (currentUser.getManager() && currentUser.getAllManagedUsers().contains(user))
            || (currentUser.getId() == user.getId() && !user.getAdmin())) {
            EntityManager entityManager = getSessionEntityManager();
            // TODO: better solution?
            if (currentUser.getId() == user.getId()) {
                currentUser.setLogin(user.getLogin());
                // Password is different or hash method has changed since login
                if (!currentUser.getPassword().equals(user.getPassword())
                        || currentUser.getPasswordHashMethod().equals(PasswordHashMethod.PLAIN)
                        && !getApplicationSettings().getDefaultHashImplementation().equals(PasswordHashMethod.PLAIN)) {
                    currentUser.setPasswordHashMethod(getApplicationSettings().getDefaultHashImplementation());
                    if (currentUser.getSalt() == null) {
                        currentUser.setSalt(generateRandomUserSalt());
                    }
                    currentUser.setPassword(hash(currentUser.getPasswordHashMethod(), user.getPassword(), getApplicationSettings().getSalt(), currentUser.getSalt()));
                }
                if (currentUser.getAdmin() || currentUser.getManager())
                {
                    if (currentUser.getAdmin()) {
                        currentUser.setAdmin(user.getAdmin());
                    }
                    currentUser.setManager(user.getManager());
                }
                currentUser.setUserSettings(user.getUserSettings());
                currentUser.setEmail(user.getEmail());
                currentUser.setNotificationEvents(user.getTransferNotificationEvents());
                currentUser.setCompanyName(user.getCompanyName());
                currentUser.setFirstName(user.getFirstName());
                currentUser.setLastName(user.getLastName());
                currentUser.setPhoneNumber(user.getPhoneNumber());

                entityManager.merge(currentUser);
                user = currentUser;
            } else {
                // update password
                if (currentUser.getAdmin() || currentUser.getManager()) {
                    User existingUser = entityManager.find(User.class, user.getId());
                    if (existingUser.getSalt() == null) {
                        existingUser.setSalt(generateRandomUserSalt());
                    }
                    // Checks if password has changed or default hash method not equal to current user hash method
                    if (!existingUser.getPassword().equals(user.getPassword())
                            && !existingUser.getPassword().equals(hash(existingUser.getPasswordHashMethod(), user.getPassword(), getApplicationSettings().getSalt(), existingUser.getSalt()))
                            || !existingUser.getPasswordHashMethod().equals(getApplicationSettings().getDefaultHashImplementation())) {
                        existingUser.setPasswordHashMethod(getApplicationSettings().getDefaultHashImplementation());
                        existingUser.setPassword(hash(existingUser.getPasswordHashMethod(), user.getPassword(), getApplicationSettings().getSalt(), existingUser.getSalt()));
                    }
                    entityManager.merge(existingUser);
                } else {
                    throw new AccessDeniedException();
                }
            }

            return fillUserSettings(new User(user));
        } else {
            throw new AccessDeniedException();
        }
    }

    @Transactional
    @RequireUser
    @Override
    public UserSettings updateUserSettings(UserSettings userSettings) throws AccessDeniedException {
        User user = getSessionUser();
        if (!user.getUserSettings().equals(userSettings)) {
            throw new AccessDeniedException();
        }
        user.getUserSettings().copyFrom(userSettings);
        return unproxy(user.getUserSettings());
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @RefreshBackendPermissions
    @Override
    public User removeUser(User user) throws AccessDeniedException {
        EntityManager entityManager = getSessionEntityManager();
        user = entityManager.find(User.class, user.getId());
        // Don't allow user to delete himself
        if (user.equals(getSessionUser())) {
            throw new IllegalArgumentException();
        }
        // Allow manager to remove users only managed by himself
        if (!getSessionUser().getAdmin() && !getSessionUser().getAllManagedUsers().contains(user)) {
            throw new AccessDeniedException();
        }
        entityManager.createQuery("DELETE FROM UIStateEntry s WHERE s.user=:user").setParameter("user", user).executeUpdate();
        for (NotificationSettings settings : entityManager
                .createQuery("SELECT S FROM " + NotificationSettings.class.getName() + " S WHERE S.user = :user", NotificationSettings.class)
                .setParameter("user", user)
                .getResultList()) {
            entityManager.createQuery("DELETE FROM NotificationTemplate T WHERE T.settings = :settings")
                    .setParameter("settings", settings)
                    .executeUpdate();
        }
        entityManager.createQuery("DELETE FROM NotificationSettings s WHERE s.user=:user").setParameter("user", user).executeUpdate();
        entityManager.createQuery("UPDATE Device d SET d.owner=null WHERE d.owner=:user").setParameter("user", user).executeUpdate();
        for (Device device : user.getDevices()) {
            device.getUsers().remove(user);
        }
        for (GeoFence geoFence : user.getGeoFences()) {
            geoFence.getUsers().remove(user);
        }
        for (Report report : user.getReports()) {
            report.getUsers().remove(user);
        }
        for (Group group : user.getGroups()) {
            group.getUsers().remove(user);
        }
        for (User managedUser : user.getManagedUsers()) {
            managedUser.setManagedBy(user.getManagedBy());
        }

        entityManager.remove(user);
        return fillUserSettings(user);
    }

    @Transactional
    @RequireUser
    @Override
    public List<Device> getDevices() {
        return getDevices(true);
    }

    private List<Device> getDevices(boolean full) {
        User user = getSessionUser();
        List<Device> devices;
        if (user.getAdmin()) {
            devices = getSessionEntityManager().createQuery("SELECT x FROM Device x LEFT JOIN FETCH x.latestPosition ORDER BY x.name", Device.class).getResultList();
        } else {
            devices = new ArrayList<>(user.getAllAvailableDevices());
            Collections.sort(devices, new Comparator<Device>() {
                @Override
                public int compare(Device o1, Device o2) {
                    String n1 = o1.getName() == null ? "" : o1.getName();
                    String n2 = o2.getName() == null ? "" : o2.getName();
                    return n1.compareTo(n2);
                }
            });
        }
        if (full && !devices.isEmpty()) {
            List<Maintenance> maintenaces = getSessionEntityManager().createQuery("SELECT m FROM Maintenance m WHERE m.device IN :devices ORDER BY m.indexNo ASC", Maintenance.class)
                    .setParameter("devices", devices)
                    .getResultList();
            for (Maintenance maintenance : maintenaces) {
                Device device = maintenance.getDevice();
                if (device.getMaintenances() == null) {
                    device.setMaintenances(new ArrayList<Maintenance>());
                }
                device.getMaintenances().add(maintenance);
            }

            List<Sensor> sensors = getSessionEntityManager().createQuery("SELECT s FROM Sensor s WHERE s.device IN :devices ORDER BY s.id ASC", Sensor.class)
                    .setParameter("devices", devices)
                    .getResultList();
            for (Sensor sensor : sensors) {
                Device device = sensor.getDevice();
                if (device.getSensors() == null) {
                    device.setSensors(new ArrayList<Sensor>());
                }
                device.getSensors().add(sensor);
            }

            for (Device device : devices) {
                if (device.getMaintenances() == null) {
                    device.setMaintenances(Collections.<Maintenance>emptyList());
                }
                if (device.getSensors() == null) {
                    device.setSensors(Collections.<Sensor>emptyList());
                }
            }
        }
        return devices;
    }

    @Transactional
    @RequireUser
    @ManagesDevices
    @RequireWrite
    @RefreshBackendPermissions
    @Override
    public Device addDevice(Device device) throws TraccarException {
        if (device.getName() == null || device.getName().trim().isEmpty() ||
            device.getUniqueId() == null || device.getUniqueId().isEmpty()) {
            throw new ValidationException();
        }

        User user = getSessionUser();

        if (!user.getAdmin() && user.getNumberOfDevicesToAdd() <= 0) {
            throw new MaxDeviceNumberReachedException(user.getUserWhoReachedLimitOnDevicesNumber());
        }

        EntityManager entityManager = getSessionEntityManager();
        TypedQuery<Device> query = entityManager.createQuery("SELECT x FROM Device x WHERE x.uniqueId = :id", Device.class);
        query.setParameter("id", device.getUniqueId());
        List<Device> results = query.getResultList();

        if (results.isEmpty()) {
            Group newGroup = device.getGroup() == null ? null : entityManager.find(Group.class, device.getGroup().getId());
            if (newGroup != null && !getSessionUser().hasAccessTo(newGroup)) {
                throw new AccessDeniedException();
            }

            device.setUsers(new HashSet<User>(1));
            device.getUsers().add(user);
            device.setOwner(user);
            device.setGroup(newGroup);
            entityManager.persist(device);
            for (Maintenance maintenance : device.getMaintenances()) {
                maintenance.setDevice(device);
                entityManager.persist(maintenance);
            }
            for (Sensor sensor : device.getSensors()) {
                sensor.setDevice(device);
                sensor.setId(0);
                entityManager.persist(sensor);
            }
            return device;
        } else {
            throw new DeviceExistsException();
        }
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @ManagesDevices
    @Override
    public Device updateDevice(Device device) throws TraccarException {
        if (device.getName() == null || device.getName().trim().isEmpty() ||
            device.getUniqueId() == null || device.getUniqueId().isEmpty()) {
            throw new ValidationException();
        }
        EntityManager entityManager = getSessionEntityManager();
        TypedQuery<Device> query = entityManager.createQuery("SELECT x FROM Device x WHERE x.uniqueId = :id AND x.id <> :primary_id", Device.class);
        query.setParameter("primary_id", device.getId());
        query.setParameter("id", device.getUniqueId());
        List<Device> results = query.getResultList();

        if (results.isEmpty()) {
            Device tmp_device = entityManager.find(Device.class, device.getId());
            Group newGroup = device.getGroup() == null ? null : entityManager.find(Group.class, device.getGroup().getId());
            if (!getSessionUser().hasAccessTo(device)
                    || !getSessionUser().allowedToChangeGroup(tmp_device, newGroup)) {
                throw new AccessDeniedException();
            }
            tmp_device.setName(device.getName());
            tmp_device.setUniqueId(device.getUniqueId());
            tmp_device.setDescription(device.getDescription());
            tmp_device.setPhoneNumber(device.getPhoneNumber());
            tmp_device.setPlateNumber(device.getPlateNumber());
            tmp_device.setVehicleInfo(device.getVehicleInfo());
            tmp_device.setTimeout(device.getTimeout());
            tmp_device.setIdleSpeedThreshold(device.getIdleSpeedThreshold());
            tmp_device.setMinIdleTime(device.getMinIdleTime());
            tmp_device.setSpeedLimit(device.getSpeedLimit());
            tmp_device.setIconType(device.getIconType());
            tmp_device.setIcon(device.getIcon() == null ? null : entityManager.find(DeviceIcon.class, device.getIcon().getId()));
            tmp_device.setPhoto(device.getPhoto() == null ? null : entityManager.find(Picture.class, device.getPhoto().getId()));
            tmp_device.setGroup(newGroup);

            tmp_device.setIconMode(device.getIconMode());
            tmp_device.setIconRotation(device.isIconRotation());
            tmp_device.setIconArrowMovingColor(device.getIconArrowMovingColor());
            tmp_device.setIconArrowPausedColor(device.getIconArrowPausedColor());
            tmp_device.setIconArrowStoppedColor(device.getIconArrowStoppedColor());
            tmp_device.setIconArrowOfflineColor(device.getIconArrowOfflineColor());
            tmp_device.setIconArrowRadius(device.getIconArrowRadius());
            tmp_device.setShowName(device.isShowName());
            tmp_device.setShowProtocol(device.isShowProtocol());
            tmp_device.setShowOdometer(device.isShowOdometer());

            double prevOdometer = tmp_device.getOdometer();
            tmp_device.setOdometer(device.getOdometer());
            tmp_device.setAutoUpdateOdometer(device.isAutoUpdateOdometer());

            // process maintenances
            tmp_device.setMaintenances(new ArrayList<>(device.getMaintenances()));
            List<Maintenance> currentMaintenances = new LinkedList<>(getSessionEntityManager().createQuery("SELECT m FROM Maintenance m WHERE m.device = :device", Maintenance.class)
                    .setParameter("device", device)
                    .getResultList());
            // update and delete existing
            for (Iterator<Maintenance> it = currentMaintenances.iterator(); it.hasNext(); ) {
                Maintenance existingMaintenance = it.next();
                boolean contains = false;
                for (int index = 0; index < device.getMaintenances().size(); index++) {
                    Maintenance updatedMaintenance = device.getMaintenances().get(index);
                    if (updatedMaintenance.getId() == existingMaintenance.getId()) {
                        existingMaintenance.copyFrom(updatedMaintenance);
                        updatedMaintenance.setDevice(tmp_device);
                        device.getMaintenances().remove(index);
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    getSessionEntityManager().remove(existingMaintenance);
                    it.remove();
                }
            }
            // add new
            for (Maintenance maintenance : device.getMaintenances()) {
                maintenance.setDevice(tmp_device);
                getSessionEntityManager().persist(maintenance);
                currentMaintenances.add(maintenance);
            }
            // post events if odometer changed
            if (Math.abs(prevOdometer - device.getOdometer()) >= 0.000001) {
                for (Maintenance maintenance : currentMaintenances) {
                    double serviceThreshold = maintenance.getLastService() + maintenance.getServiceInterval();
                    if (prevOdometer < serviceThreshold && device.getOdometer() >= serviceThreshold) {
                        DeviceEvent event = new DeviceEvent();
                        event.setTime(new Date());
                        event.setDevice(device);
                        event.setType(DeviceEventType.MAINTENANCE_REQUIRED);
                        event.setPosition(tmp_device.getLatestPosition());
                        event.setMaintenance(maintenance);
                        getSessionEntityManager().persist(event);
                    }
                }
            }

            // process sensors
            tmp_device.setSensors(new ArrayList<>(device.getSensors()));
            List<Sensor> currentSensors = new LinkedList<>(getSessionEntityManager().createQuery("SELECT s FROM Sensor s WHERE s.device = :device", Sensor.class)
                    .setParameter("device", device)
                    .getResultList());
            // update and delete existing
            for (Iterator<Sensor> it = currentSensors.iterator(); it.hasNext(); ) {
                Sensor existingSensor = it.next();
                boolean contains = false;
                for (int index = 0; index < device.getSensors().size(); index++) {
                    Sensor updatedSensor = device.getSensors().get(index);
                    if (updatedSensor.getId() == existingSensor.getId()) {
                        existingSensor.copyFrom(updatedSensor);
                        updatedSensor.setDevice(tmp_device);
                        device.getSensors().remove(index);
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    getSessionEntityManager().remove(existingSensor);
                    it.remove();
                }
            }
            // add new
            for (Sensor sensor : device.getSensors()) {
                sensor.setId(0);
                sensor.setDevice(tmp_device);
                getSessionEntityManager().persist(sensor);
            }

            return tmp_device;
        } else {
            throw new DeviceExistsException();
        }
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @ManagesDevices
    @RefreshBackendPermissions
    @Override
    public Device removeDevice(Device device) throws AccessDeniedException {
        EntityManager entityManager = getSessionEntityManager();
        User user = getSessionUser();
        device = entityManager.find(Device.class, device.getId());
        if (!user.hasAccessTo(device)) {
            throw new AccessDeniedException();
        }
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

            Query query = entityManager.createQuery("DELETE FROM DeviceEvent x WHERE x.device = :device");
            query.setParameter("device", device);
            query.executeUpdate();

            query = entityManager.createQuery("DELETE FROM Position x WHERE x.device = :device");
            query.setParameter("device", device);
            query.executeUpdate();

            query = entityManager.createQuery("SELECT g FROM GeoFence g WHERE :device MEMBER OF g.devices");
            query.setParameter("device", device);
            for (GeoFence geoFence : (List<GeoFence>) query.getResultList()) {
                geoFence.getDevices().remove(device);
            }

            query = entityManager.createQuery("DELETE FROM Maintenance x WHERE x.device = :device");
            query.setParameter("device", device);
            query.executeUpdate();

            query = entityManager.createQuery("DELETE FROM Sensor x WHERE x.device = :device");
            query.setParameter("device", device);
            query.executeUpdate();

            query = entityManager.createQuery("SELECT x FROM Report x WHERE :device MEMBER OF x.devices");
            query.setParameter("device", device);
            List<Report> reports = query.getResultList();
            for (Report report : reports) {
                report.getDevices().remove(device);
            }

            entityManager.remove(device);
        }
        return device;
    }

    @Transactional
    @RequireUser
    @Override
    public List<Position> getPositions(Device device, Date from, Date to, boolean filter) throws AccessDeniedException {
        if (!getSessionUser().isArchive() || !getSessionUser().hasAccessTo(device)) {
            throw new AccessDeniedException();
        }

        // refresh device
        device = entityManager.get().find(Device.class, device.getId());

        EntityManager entityManager = getSessionEntityManager();
        UserSettings filters = getSessionUser().getUserSettings();

        List<Position> positions = new LinkedList<>();
        String queryString = "SELECT x FROM Position x WHERE x.device = :device AND x.time BETWEEN :from AND :to";

        if (filter) {
            if (filters.isHideZeroCoordinates()) {
                queryString += " AND (x.latitude != 0 OR x.longitude != 0)";
            }
            if (filters.isHideInvalidLocations()) {
                queryString += " AND x.valid = TRUE";
            }
            if (filters.getSpeedModifier() != null && filters.getSpeedForFilter() != null) {
                queryString += " AND x.speed " + filters.getSpeedModifier() + " :speed";
            }
        }

        queryString += " ORDER BY x.time";

        TypedQuery<Position> query = entityManager.createQuery(queryString, Position.class);
        query.setParameter("device", device);
        query.setParameter("from", from);
        query.setParameter("to", to);

        if (filter && filters.getSpeedModifier() != null && filters.getSpeedForFilter() != null) {
            query.setParameter("speed", filters.getSpeedUnit().toKnots(filters.getSpeedForFilter()));
        }

        List<Position> queryResult = query.getResultList();

        List<Position> lastNonIdlePositionsQueryResult =  entityManager
                .createQuery("SELECT p FROM Position p WHERE p.device = :device AND p.speed > :threshold ORDER BY time DESC", Position.class)
                .setParameter("device", device)
                .setParameter("threshold", device.getIdleSpeedThreshold())
                .setMaxResults(1)
                .getResultList();
        Position latestNonIdlePosition = lastNonIdlePositionsQueryResult.isEmpty()
                ? null
                : lastNonIdlePositionsQueryResult.get(0);
        final long MIN_IDLE_TIME = (long) device.getMinIdleTime() * 1000;

        for (int i = 0; i < queryResult.size(); i++) {
            boolean add = true;
            Position position = queryResult.get(i);
            if (i > 0) {
                Position positionA = queryResult.get(i - 1);
                Position positionB = position;

                positionB.setDistance(GeoFenceCalculator.getDistance(positionA.getLongitude(), positionA.getLatitude(), positionB.getLongitude(), positionB.getLatitude()));

                if (filter && filters.isHideDuplicates()) {
                    add = !positionA.getTime().equals(positionB.getTime());
                }
                if (add && filter && filters.getMinDistance() != null) {
                    add = positionB.getDistance() >= filters.getMinDistance();
                }
            }
            // calculate Idle state
            if (position.getSpeed() != null) {
                if (position.getSpeed() > position.getDevice().getIdleSpeedThreshold()) {
                    latestNonIdlePosition = position;
                } else {
                    if (latestNonIdlePosition == null) {
                        position.setIdleStatus(Position.IdleStatus.PAUSED);
                        latestNonIdlePosition = position;
                    } else {
                        if (position.getTime().getTime() - latestNonIdlePosition.getTime().getTime() > MIN_IDLE_TIME) {
                            position.setIdleSince(latestNonIdlePosition.getTime());
                            position.setIdleStatus(Position.IdleStatus.IDLE);
                        } else {
                            position.setIdleStatus(Position.IdleStatus.PAUSED);
                        }
                    }
                }
            }
            if (add) positions.add(queryResult.get(i));
        }

        return new ArrayList<>(positions);
    }

    @RequireUser
    @Transactional
    @Override
    public List<Position> getLatestPositions() {
        List<Position> positions = new ArrayList<>();
        List<Device> devices = getDevices(false);
        List<GeoFence> geoFences = getGeoFences(false);
        GeoFenceCalculator geoFenceCalculator = new GeoFenceCalculator(getGeoFences());
        if (devices != null && !devices.isEmpty()) {
            for (Device device : devices) {
                if (device.getLatestPosition() != null) {
                    Position position = device.getLatestPosition();
                    // calculate geo-fences
                    for (GeoFence geoFence : geoFences) {
                        if (geoFenceCalculator.contains(geoFence, position)) {
                            if (position.getGeoFences() == null) {
                                position.setGeoFences(new LinkedList<GeoFence>());
                            }
                            position.getGeoFences().add(geoFence);
                        }
                    }

                    position.setDistance(device.getOdometer());

                    // calculate 'idle since'
                    if (position.getSpeed() != null) {
                        if (position.getSpeed() > device.getIdleSpeedThreshold()) {
                            position.setIdleStatus(Position.IdleStatus.MOVING);
                        } else {
                            Date latestNonIdlePositionTime = movementDetector.getNonIdlePositionTime(device);
                            long minIdleTime = (long) device.getMinIdleTime() * 1000;
                            if (latestNonIdlePositionTime != null
                                    && position.getTime().getTime() - latestNonIdlePositionTime.getTime() > minIdleTime) {
                                position.setIdleSince(latestNonIdlePositionTime);
                                position.setIdleStatus(Position.IdleStatus.IDLE);
                            } else {
                                position.setIdleStatus(Position.IdleStatus.PAUSED);
                            }
                        }
                    }

                    positions.add(position);
                }
            }
        }
        return positions;
    }

    @Transactional
    @Override
    public ApplicationSettings getApplicationSettings() {
        return applicationSettings.get();
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @RequireWrite
    @Override
    public void updateApplicationSettings(ApplicationSettings applicationSettings) {
        getSessionEntityManager().merge(applicationSettings);
        eventService.applicationSettingsChanged();
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN })
    @RequireWrite
    @Override
    public void saveDefaultUserSettigs(UserSettings userSettings) {
        if (getApplicationSettings().getUserSettings() == null) {
            getSessionEntityManager().persist(userSettings);
            getApplicationSettings().setUserSettings(userSettings);
        } else {
            getApplicationSettings().getUserSettings().copyFrom(userSettings);
        }
    }

    @Transactional
    @RequireUser
    @Override
    public UserSettings getDefaultUserSettings() {
        return getApplicationSettings().getUserSettings() == null ? new UserSettings() : unproxy(getApplicationSettings().getUserSettings());
    }

    private UserSettings getUserSettingsForNewUser() {
        ApplicationSettings applicationSettings = this.applicationSettings.get();
        return applicationSettings.getUserSettings() == null ? new UserSettings() : applicationSettings.getUserSettings().copy();
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @Override
    public void saveRoles(List<User> users) throws InvalidMaxDeviceNumberForUserException {
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
            user.setArchive(_user.isArchive());
            if (user.getId() != currentUser.getId()) {
                user.setReadOnly(_user.getReadOnly());
                user.setBlocked(_user.isBlocked());
                validateMaximumNumberOfDevices(user, user.getMaxNumOfDevices(), _user.getMaxNumOfDevices());
                user.setMaxNumOfDevices(_user.getMaxNumOfDevices());
                user.setExpirationDate(_user.getExpirationDate());
            }
        }
    }

    @Transactional
    @RequireUser
    @Override
    public Map<User, Boolean> getDeviceShare(Device device) {
        device = getSessionEntityManager().find(Device.class, device.getId());
        List<User> users = getUsers();
        Map<User, Boolean> result = new HashMap<>(users.size());
        for (User user : users) {
            result.put(fillUserSettings(user), device.getUsers().contains(user));
        }
        return result;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @RefreshBackendPermissions
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

    private User fillUserSettings(User user) {
        if (user.getUserSettings() instanceof HibernateProxy) {
            user.setUserSettings(unproxy(user.getUserSettings()));
        }
        return user;
    }

    private <T> T unproxy(T entity) {
        if (entity instanceof HibernateProxy) {
            return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }

    @Transactional
    @RequireUser
    @Override
    public List<GeoFence> getGeoFences() {
        return getGeoFences(true);
    }

    private List<GeoFence> getGeoFences(boolean includeTransferDevices) {
        User user = getSessionUser();
        Set<GeoFence> geoFences;
        if (user.getAdmin()) {
            geoFences = new HashSet<>(getSessionEntityManager().createQuery("SELECT g FROM GeoFence g LEFT JOIN FETCH g.devices", GeoFence.class).getResultList());
        } else {
            geoFences = user.getAllAvailableGeoFences();
        }

        if (includeTransferDevices) {
            for (GeoFence geoFence : geoFences) {
                geoFence.setTransferDevices(new HashSet<>(geoFence.getDevices()));
            }
        }

        return new ArrayList<>(geoFences);
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public GeoFence addGeoFence(GeoFence geoFence) throws TraccarException {
        if (geoFence.getName() == null || geoFence.getName().trim().isEmpty()) {
            throw new ValidationException();
        }

        geoFence.setUsers(new HashSet<User>());
        geoFence.getUsers().add(getSessionUser());
        geoFence.setDevices(geoFence.getTransferDevices());
        getSessionEntityManager().persist(geoFence);

        return geoFence;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public GeoFence updateGeoFence(GeoFence updatedGeoFence) throws TraccarException {
        if (updatedGeoFence.getName() == null || updatedGeoFence.getName().trim().isEmpty()) {
            throw new ValidationException();
        }

        GeoFence geoFence = getSessionEntityManager().find(GeoFence.class, updatedGeoFence.getId());
        geoFence.copyFrom(updatedGeoFence);

        // used to check access to the device
        List<Device> devices = getDevices(false);

        // process devices
        for (Iterator<Device> it = geoFence.getDevices().iterator(); it.hasNext(); ) {
            Device next = it.next();
            if (!updatedGeoFence.getTransferDevices().contains(next) && devices.contains(next)) {
                it.remove();
            }
        }
        updatedGeoFence.getTransferDevices().retainAll(devices);
        geoFence.getDevices().addAll(updatedGeoFence.getTransferDevices());

        return geoFence;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public GeoFence removeGeoFence(GeoFence geoFence) {
        User user = getSessionUser();
        geoFence = getSessionEntityManager().find(GeoFence.class, geoFence.getId());
        if (user.getAdmin() || user.getManager()) {
            geoFence.getUsers().removeAll(getUsers());
        }
        geoFence.getUsers().remove(user);
        if (geoFence.getUsers().isEmpty()) {
            Query query = entityManager.get().createQuery("DELETE FROM DeviceEvent x WHERE x.geoFence = :geoFence");
            query.setParameter("geoFence", geoFence);
            query.executeUpdate();

            query = entityManager.get().createQuery("SELECT x FROM Report x WHERE :geoFence MEMBER OF x.geoFences");
            query.setParameter("geoFence", geoFence);
            List<Report> reports = query.getResultList();
            for (Report report : reports) {
                report.getGeoFences().remove(geoFence);
            }

            getSessionEntityManager().remove(geoFence);
        }
        return geoFence;
    }

    @Transactional
    @RequireUser
    @Override
    public Map<User, Boolean> getGeoFenceShare(GeoFence geoFence) {
        geoFence = getSessionEntityManager().find(GeoFence.class, geoFence.getId());
        List<User> users = getUsers();
        Map<User, Boolean> result = new HashMap<>(users.size());
        for (User user : users) {
            result.put(fillUserSettings(user), geoFence.getUsers().contains(user));
        }
        return result;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @Override
    public void saveGeoFenceShare(GeoFence geoFence, Map<User, Boolean> share) {
        EntityManager entityManager = getSessionEntityManager();
        geoFence = entityManager.find(GeoFence.class, geoFence.getId());

        for (User user : getUsers()) {
            Boolean shared = share.get(user);
            if (shared == null) continue;
            if (shared) {
                geoFence.getUsers().add(user);
            } else {
                geoFence.getUsers().remove(user);
            }
            entityManager.merge(user);
        }
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public String sendCommand(Command command) throws AccessDeniedException {
        Device device = getSessionEntityManager().find(Device.class, command.getDeviceId());
        if (!getSessionUser().hasAccessTo(device)) {
            throw new AccessDeniedException();
        }

        if (applicationSettings.get().isAllowCommandsOnlyForAdmins() && !getSessionUser().getAdmin()) {
            throw new AccessDeniedException();
        }

        ObjectMapper jsonMapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        try {
            Class<?> contextClass = Class.forName("org.traccar.Context");
            Method getPermissionsManager = contextClass.getDeclaredMethod("getConnectionManager");
            Object connectionManager = getPermissionsManager.invoke(null);
            Object activeDevice = connectionManager.getClass().getDeclaredMethod("getActiveDevice", long.class)
                    .invoke(connectionManager, command.getDeviceId());
            if (activeDevice == null) {
                result.put("success", false);
                result.put("reason", "The device is not registered on the server");
            } else {
                Class<?> backendCommandClass = Class.forName("org.traccar.model.Command");
                Object backendCommand = backendCommandClass.newInstance();
                Class<?> backendJsonConverterClass = null;
                try {
                    backendJsonConverterClass = Class.forName("org.traccar.web.JsonConverter");
                } catch (ClassNotFoundException e) {
                    backendJsonConverterClass = Class.forName("org.traccar.http.JsonConverter");
                }

                Method objectFromJson;
                try {
                    Class<?> backendFactoryClass = Class.forName("org.traccar.model.Factory");
                    objectFromJson = backendJsonConverterClass.getDeclaredMethod("objectFromJson", Reader.class, backendFactoryClass);
                    backendCommand = objectFromJson.invoke(null, new StringReader(jsonMapper.writeValueAsString(command)), backendCommand);
                } catch (ClassNotFoundException e) {
                    objectFromJson = backendJsonConverterClass.getDeclaredMethod("objectFromJson", Reader.class, Class.class);
                    backendCommand = objectFromJson.invoke(null, new StringReader(jsonMapper.writeValueAsString(command)), backendCommandClass);
                }

                Method sendCommand = activeDevice.getClass().getDeclaredMethod("sendCommand", backendCommandClass);
                sendCommand.invoke(activeDevice, backendCommand);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InstantiationException | JsonProcessingException e) {
            log("Unable to invoke command through reflection", e);
            result.put("success", false);
            result.put("reason", e.getClass().getName() + ": " + e.getLocalizedMessage());
        } catch (InvocationTargetException ite) {
            log("Error invoking command through reflection", ite);
            result.put("success", false);
            if (ite.getCause() == null) {
                result.put("reason", ite.getClass().getName() + ": " + ite.getLocalizedMessage());
            } else {
                result.put("reason", ite.getCause().getClass().getName() + ": " + ite.getCause().getLocalizedMessage());
            }
        }

        try {
            return jsonMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log("Unable to prepare JSON result", e);
            return "{success: false, reason: \"Unable to prepare result\"}";
        }
    }
}
