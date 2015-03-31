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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.traccar.web.client.model.NotificationService;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.DeviceEvent;
import org.traccar.web.shared.model.NotificationSettings;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Singleton
public class NotificationServiceImpl extends RemoteServiceServlet implements NotificationService {
    @Inject
    private Provider<User> sessionUser;

    @Inject
    private Provider<EntityManager> entityManager;

    public static class NotificationSender extends ScheduledTask {
        @Inject
        Provider<EntityManager> entityManager;

        @Transactional
        @Override
        public void doWork() throws Exception {
            Map<User, Set<DeviceEvent>> events = new HashMap<User, Set<DeviceEvent>>();
            List<User> admins = null;
            Map<User, List<User>> managers = new HashMap<User, List<User>>();

            for (DeviceEvent event : entityManager.get().createQuery("SELECT e FROM DeviceEvent e INNER JOIN FETCH e.position WHERE e.notificationSent = :false", DeviceEvent.class)
                                    .setParameter("false", false)
                                    .getResultList()) {
                Device device = event.getDevice();

                for (User user : device.getUsers()) {
                    if (user.isNotifications()) {
                        addEvent(events, user, event);
                    }
                    List<User> userManagers = managers.get(user);
                    if (userManagers == null) {
                        userManagers = new LinkedList<User>();
                        User manager = user.getManagedBy();
                        while (manager != null) {
                            if (manager.isNotifications()) {
                                userManagers.add(manager);
                            }
                            manager = manager.getManagedBy();
                        }
                        if (userManagers.isEmpty()) {
                            userManagers = Collections.emptyList();
                        }
                        managers.put(user, userManagers);
                    }
                    for (User manager : userManagers) {
                        addEvent(events, manager, event);
                    }
                }

                if (admins == null) {
                    admins = entityManager.get().createQuery("SELECT u FROM User u WHERE u.admin=:true AND u.notifications=:true", User.class)
                            .setParameter("true", true)
                            .getResultList();
                }

                for (User admin : admins) {
                    addEvent(events, admin, event);
                }
            }

            for (Map.Entry<User, Set<DeviceEvent>> entry : events.entrySet()) {
                User user = entry.getKey();
                if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                    logger.warning("User '" + user.getLogin() + "' has empty email field");
                    continue;
                }

                NotificationSettings settings = findNotificationSettings(user);
                if (settings == null) {
                    logger.warning("Unable to find notification settings for '" + user.getLogin() + "' (id=" + user.getId() + "), thus he won't receive any notifications.");
                    continue;
                }

                List<DeviceEvent> deviceEvents = new ArrayList<DeviceEvent>(entry.getValue());
                Collections.sort(deviceEvents, new Comparator<DeviceEvent>() {
                    @Override
                    public int compare(DeviceEvent o1, DeviceEvent o2) {
                        int r = o1.getDevice().getName().compareTo(o2.getDevice().getName());
                        if (r == 0) {
                            return o1.getTime().compareTo(o2.getTime());
                        }
                        return r;
                    }
                });

                logger.info("Sending notification to '" + user.getEmail() + "'...");

                Session session = getSession(settings);
                Message msg = new MimeMessage(session);
                Transport transport = null;
                try {
                    msg.setFrom(new InternetAddress(settings.getFromAddress()));
                    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getLogin() + " <" + user.getEmail() + ">", false));
                    msg.setSubject("[traccar-web] Notification");
                    if (deviceEvents.size() == 1) {
                        DeviceEvent event = deviceEvents.get(0);
                        msg.setText("Device '" + event.getDevice().getName() + "' went offline at " + event.getTime());
                    } else {
                        StringBuilder body = new StringBuilder("Following devices went offline:\n");
                        for (DeviceEvent event : deviceEvents) {
                            body.append("\n  '").append(event.getDevice().getName()).append("' (").append(event.getDevice().getUniqueId()).append(") at ").append(event.getTime());
                        }
                        msg.setText(body.toString());
                    }
                    msg.setHeader("X-Mailer", "traccar-web.sendmail");
                    msg.setSentDate(new Date());

                    transport = session.getTransport("smtp");
                    transport.connect();
                    transport.sendMessage(msg, msg.getAllRecipients());

                    for (DeviceEvent event : deviceEvents) {
                        event.setNotificationSent(true);
                    }
                } catch (MessagingException me) {
                    logger.log(Level.SEVERE, "Unable to send Email message", me);
                } finally {
                    if (transport != null) {
                        transport.close();
                    }
                }
            }
        }

        private void addEvent(Map<User, Set<DeviceEvent>> events, User user, DeviceEvent event) {
            Set<DeviceEvent> userEvents = events.get(user);
            if (userEvents == null) {
                userEvents = new HashSet<DeviceEvent>();
                events.put(user, userEvents);
            }
            userEvents.add(event);
        }

        private NotificationSettings findNotificationSettings(User user) {
            NotificationSettings s = getNotificationSettings(user);

            // lookup settings in manager hierarchy
            if (s == null) {
                User manager = user.getManagedBy();
                while (s == null && manager != null) {
                    s = getNotificationSettings(manager);
                    manager = manager.getManagedBy();
                }
            }

            // take settings from first admin ordered by id
            if (s == null) {
                List<NotificationSettings> settings = entityManager.get().createQuery("SELECT s FROM NotificationSettings s WHERE s.user.admin=:true ORDER BY s.user.id ASC", NotificationSettings.class)
                        .setParameter("true", true)
                        .getResultList();
                if (!settings.isEmpty()) {
                    s = settings.get(0);
                }
            }

            return s;
        }

        private NotificationSettings getNotificationSettings(User user) {
            List<NotificationSettings> settings = entityManager.get().createQuery("SELECT n FROM NotificationSettings n WHERE n.user = :user", NotificationSettings.class)
                    .setParameter("user", user)
                    .getResultList();
            return settings.isEmpty() ? null : settings.get(0);
        }
    }

    @Inject
    private NotificationSender notificationSender;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void init() throws ServletException {
        super.init();

        scheduler.scheduleAtFixedRate(notificationSender, 0, 1, TimeUnit.MINUTES);
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @Override
    public void checkSettings(NotificationSettings settings) {
        // Validate smtp settings
        try {
            Session s = getSession(settings);
            Transport t = s.getTransport("smtp");
            t.connect();
            t.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
        // Validate 'From Address'
        try {
            new InternetAddress(settings.getFromAddress(), true);
        } catch (AddressException ae) {
            throw new IllegalArgumentException(ae);
        }
    }

    private static Session getSession(NotificationSettings settings) {
        final boolean DEBUG = false;
        Properties props = new Properties();

        props.put("mail.smtp.host", settings.getServer());
        props.put("mail.smtp.auth", Boolean.toString(settings.isUseAuthorization()));
        props.put("mail.debug", Boolean.toString(DEBUG));
        props.put("mail.smtp.port", Integer.toString(settings.getPort()));

        switch (settings.getSecureConnectionType()) {
            case SSL_TLS:
                props.put("mail.smtp.socketFactory.port", Integer.toString(settings.getPort()));
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.socketFactory.timeout", 10 * 1000);
                break;
            case STARTTLS:
                props.put("mail.smtp.starttls.required", "true");
                break;
        }

        final String userName = settings.getUsername();
        final String password = settings.getPassword();

        Authenticator authenticator = settings.isUseAuthorization() ? new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(userName, password);
            }
        } : null;

        Session s = Session.getInstance(props, authenticator);
        s.setDebug(DEBUG);

        return s;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public NotificationSettings getSettings() {
        List<NotificationSettings> settings = entityManager.get().createQuery("SELECT n FROM NotificationSettings n WHERE n.user = :user", NotificationSettings.class)
                .setParameter("user", sessionUser.get())
                .getResultList();
        return settings.isEmpty() ? null : settings.get(0);
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @Override
    public void saveSettings(NotificationSettings settings) {
        NotificationSettings currentSettings = getSettings();
        if (currentSettings == null) {
            currentSettings = settings;
            settings.setUser(sessionUser.get());
        } else {
            currentSettings.copyFrom(settings);
        }
        entityManager.get().persist(currentSettings);
    }
}
