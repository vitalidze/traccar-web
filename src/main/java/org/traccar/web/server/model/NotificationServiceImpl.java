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

import static org.traccar.web.shared.model.MessagePlaceholder.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.Renderer;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.NotificationService;
import org.traccar.web.shared.model.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class NotificationServiceImpl extends RemoteServiceServlet implements NotificationService {

    private static Logger logger2 = Logger.getLogger(NotificationServiceImpl.class.getName());

    private static Pattern EVENT_RULE_TIME_FRAME_PATTERN = Pattern.compile("(\\d{1,2}(:\\d{1,2})?[AP]M)\\-(\\d{1,2}(:\\d{1,2})?[AP]M)");
    private static Pattern EVENT_RULE_COURSE_PATTERN = Pattern.compile("(\\d{1,3})\\-(\\d{1,3})");
    private static SimpleDateFormat EVENT_RULE_TIME_FRAME_FORMAT_01 = new SimpleDateFormat("h:mm a");
    private static SimpleDateFormat EVENT_RULE_TIME_FRAME_FORMAT_02 = new SimpleDateFormat("h a");

    @Inject
    private Provider<User> sessionUser;

    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    protected Logger logger;

    @Inject
    private DataService dataService;

    public static class NotificationSender extends ScheduledTask {
        @Inject
        Provider<EntityManager> entityManager;

        @Inject
        Provider<ApplicationSettings> applicationSettings;

        @Inject
        ServerMessages messages;

        @Transactional
        @Override
        public void doWork() throws Exception {
            logger2.warning("enter");
            Set<DeviceEventType> eventTypes = new HashSet<>();
            for (User user : entityManager.get()
                    .createQuery("SELECT u FROM User u INNER JOIN FETCH u.notificationEvents", User.class)
                    .setFlushMode(FlushModeType.COMMIT)
                    .getResultList()) {
                eventTypes.addAll(user.getNotificationEvents());
            }

            Set<DeviceEventType> eventRuleTypes = new HashSet<DeviceEventType>();
            Set<EventRule> eventRules = new HashSet<EventRule>();
            for (EventRule eventRule : entityManager.get().createQuery("FROM EventRule", EventRule.class).getResultList()) {
                eventRuleTypes.add(eventRule.getDeviceEventType());
                eventRules.add(eventRule);
            }

            logger2.warning("[eventTypes.isEmpty():" + eventTypes.isEmpty() + "], [eventRuleTypes.isEmpty():" + eventRuleTypes.isEmpty() + "]");
            if (eventTypes.isEmpty() && eventRuleTypes.isEmpty()) {
                return;
            }

            ApplicationSettings appSettings = applicationSettings.get();
            Map<User, Set<DeviceEvent>> events = new HashMap<>();
            List<User> admins = null;
            Map<User, List<User>> managers = new HashMap<>();
            Date currentDate = new Date();

            // 01 User_Notification table
            if (!eventTypes.isEmpty()) {
                for (DeviceEvent event : entityManager.get().createQuery(
                    "SELECT e FROM DeviceEvent e INNER JOIN FETCH e.position" +
                            " WHERE e.notificationSent = :false" +
                            " AND e.expired = :false" +
                            " AND e.type IN (:types)", DeviceEvent.class)
                        .setFlushMode(FlushModeType.COMMIT)
                                    .setParameter("false", false)
                        .setParameter("types", eventTypes)
                        .getResultList()) {
                // check whether event is expired
                if (currentDate.getTime() - event.getTime().getTime() > appSettings.getNotificationExpirationPeriod() * 1000 * 60) {
                    event.setExpired(true);
                    continue;
                }

                    Device device = event.getDevice();
                    logger2.warning("01 [type:" + event.getType() + "] [device.SendNotifications:" + device.getSendNotifications() + "]");
                    if (Boolean.FALSE.equals(device.getSendNotifications())) {
                        continue;
                    }

                    for (User user : device.getUsers()) {
                        if (user.getNotificationEvents().contains(event.getType())) {
                            addEventToUserAndManagers(events, user, event, managers);
                        }
                    }

                    if (admins == null) {
                        admins = entityManager.get()
                            .createQuery("SELECT u FROM User u WHERE u.admin=:true", User.class)
                            .setFlushMode(FlushModeType.COMMIT)
                                .setParameter("true", true)
                                .getResultList();
                    }

                    for (User admin : admins) {
                        addEvent(events, admin, event);
                    }
                }
            }

            // 02 Event Rules table
            if (!eventRuleTypes.isEmpty()) {
                for (DeviceEvent event : entityManager.get().createQuery("SELECT e FROM DeviceEvent e INNER JOIN FETCH e.position WHERE e.notificationSent = :false AND e.type IN (:types)", DeviceEvent.class)
                        .setParameter("false", false)
                        .setParameter("types", eventRuleTypes)
                        .getResultList()) {
                    Device device = event.getDevice();
                    logger2.warning("02 [type:" + event.getType() + "] [device.SendNotifications:" + device.getSendNotifications() + "]");
                    if (Boolean.FALSE.equals(device.getSendNotifications())) {
                        continue;
                    }

                    if (admins == null) {
                        admins = entityManager.get().createQuery("SELECT u FROM User u WHERE u.admin=:true", User.class)
                                .setParameter("true", true)
                                .getResultList();
                    }

                    for (User devUser : event.getDevice().getUsers()) {
                        logger2.warning("[device user:" + devUser.getLogin() + "]");
                    }
                    for (User adminUser : admins) {
                        logger2.warning("[adminUser:" + adminUser.getLogin() + "]");
                    }
                    logger2.warning("03 [pos.serverTime:" + event.getPosition().getServerTime() + "] [pos.Time:" + event.getPosition().getTime() + "] [now:" + new Date() + "]");
                    if (new Date().getTime() - event.getPosition().getTime().getTime() > 3*60*60*1000) {
                        event.setNotificationSent(true);
                        continue;
                    }
eventRule:          for (EventRule eventRule : eventRules) {
                        logger2.warning("03 [event.device:" + event.getDevice().getId() + "] [rule.device:" + eventRule.getDevice().getId() + "] [event.type:" + event.getType() + "] [rule.type:" + eventRule.getDeviceEventType() + "] [user:" + eventRule.getUser().getLogin() + "]");
                        logger2.warning("03 [device users contains:" + event.getDevice().getUsers().contains(eventRule.getUser()) + "] [admins contains:" + admins.contains(eventRule.getUser()) + "]");
                        if (event.getDevice().getId() == eventRule.getDevice().getId() && event.getType() == eventRule.getDeviceEventType()
                                && (event.getDevice().getUsers().contains(eventRule.getUser()) || admins.contains(eventRule.getUser()))
                                && isTimeFrameOk(event.getPosition(), eventRule.getTimeFrame(), eventRule.getTimeZoneShift())
                                && isCourseOk(event.getPosition(), eventRule.getCourse())) {
                            logger2.warning("03 event_for_notification");
                            switch (eventRule.getDeviceEventType()) {
                                case GEO_FENCE_ENTER:
                                case GEO_FENCE_EXIT:
                                    if (eventRule.getGeoFence() == null || (event.getGeoFence() != null && eventRule.getGeoFence() != null && event.getGeoFence().getId() == eventRule.getGeoFence().getId())) {
                                        addEventToUserAndManagers(events, eventRule.getUser(), event, managers);
                                        continue eventRule;
                                    }
                                    break;
                                case MAINTENANCE_REQUIRED:
                                case MOVING:
                                case OFFLINE:
                                case OVERSPEED:
                                case STOPPED:
                                    addEventToUserAndManagers(events, eventRule.getUser(), event, managers);
                                    continue eventRule;
                            }
                        }
                    }
                }
            }

            logger2.warning("user before events iteration");
            for (Map.Entry<User, Set<DeviceEvent>> entry : events.entrySet()) {
                User user = entry.getKey();
                logger2.warning("user [getLogin:" + user.getLogin() + "] [getFirstName:" + user.getFirstName() + "]");
                for (DeviceEvent deviceEvent : entry.getValue()) {
                    logger2.warning("deviceEvent [id:" + deviceEvent.getId() + "] [getType:" + deviceEvent.getType() + "] [getPosition:" + deviceEvent.getPosition() + "]");
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

                for (DeviceEvent deviceEvent : entry.getValue()) {
                    NotificationTemplate template = settings.findTemplate(deviceEvent.getType());
                    if (template == null) {
                        template = new NotificationTemplate();
                        template.setBody(defaultBody(deviceEvent.getType(), appSettings.getLanguage()));
                    }

                    Engine engine = getTemplateEngine(getTimeZone(user));
                    Map<String, Object> model = getTemplateModel(user, deviceEvent);
                    String subject = engine.transform(template.getSubject(), model);
                    String body = engine.transform(template.getBody(), model);

                    boolean sentEmail = sendEmail(settings, user, subject, body, template.getContentType());
                    boolean sentPushbullet = sendPushbullet(settings, user, subject, body);
                    if (sentPushbullet || sentEmail) {
                        deviceEvent.setNotificationSent(true);
                    }
                }
            }
        }

        private Boolean isTimeFrameOk(Position pos, String timeFrame, Long timeZoneShift) {
            if (timeFrame == null || timeFrame.trim().length() == 0) {
                return true;
            }
            Date posTime = new Date();
            posTime.setTime(2*24*60*60*1000);
//            posTime.setDate(3);
            posTime.setHours(pos.getTime().getHours());
            posTime.setMinutes(pos.getTime().getMinutes());
            logger2.warning("[posTime:" + posTime + "] [timeFrame:" + timeFrame.trim() + "] [timeZoneShift:" + timeZoneShift + "]");

            timeFrame = timeFrame.trim().toUpperCase();
            Matcher matcher = EVENT_RULE_TIME_FRAME_PATTERN.matcher(timeFrame);
            while (matcher.find()) {
                logger2.warning("[group:" + matcher.group() + "]");
                String fromTime = timeFrame.substring(matcher.start(1), matcher.end(1));
                String toTime = timeFrame.substring(matcher.start(3), matcher.end(3));
                fromTime = fromTime.replace("AM", " AM");
                fromTime = fromTime.replace("PM", " PM");
                toTime = toTime.replace("AM", " AM");
                toTime = toTime.replace("PM", " PM");
                Date fromEventRule, toEventRule, fromEventRule0 = new Date(0), toEventRule0 = new Date(0)
                        , fromEventRuleMinus1 = new Date(0), toEventRuleMinus1 = new Date(0)
                        , fromEventRulePlus1 = new Date(0), toEventRulePlus1 = new Date(0);
                try {
                    fromEventRule = EVENT_RULE_TIME_FRAME_FORMAT_01.parse(fromTime);
                } catch (ParseException e) {
                    try {
                        fromEventRule = EVENT_RULE_TIME_FRAME_FORMAT_02.parse(fromTime);
                    } catch (ParseException e1) {
                        continue;
                    }
                }
                try {
                    toEventRule = EVENT_RULE_TIME_FRAME_FORMAT_01.parse(toTime);
                } catch (ParseException e) {
                    try {
                        toEventRule = EVENT_RULE_TIME_FRAME_FORMAT_02.parse(toTime);
                    } catch (ParseException e1) {
                        continue;
                    }
                }
                fromEventRule0.setTime(2*24*60*60*1000 + fromEventRule.getTime() - new Date().getTimezoneOffset()*60*1000 + timeZoneShift);
                toEventRule0.setTime(2*24*60*60*1000 + toEventRule.getTime() - new Date().getTimezoneOffset()*60*1000 + timeZoneShift);
//                fromEventRule0.setDate(2);
//                toEventRule0.setDate(2);
                fromEventRuleMinus1.setTime(1*24*60*60*1000 + fromEventRule.getTime() - new Date().getTimezoneOffset()*60*1000 + timeZoneShift);
                toEventRuleMinus1.setTime(1*24*60*60*1000 + toEventRule.getTime() - new Date().getTimezoneOffset()*60*1000 + timeZoneShift);
//                fromEventRuleMinus1.setDate(1);
//                toEventRuleMinus1.setDate(1);
                fromEventRulePlus1.setTime(3*24*60*60*1000 + fromEventRule.getTime() - new Date().getTimezoneOffset()*60*1000 + timeZoneShift);
                toEventRulePlus1.setTime(3*24*60*60*1000 + toEventRule.getTime() - new Date().getTimezoneOffset()*60*1000 + timeZoneShift);
//                fromEventRulePlus1.setDate(3);
//                toEventRulePlus1.setDate(3);
                logger2.warning("[fromEventRule0:" + fromEventRule0 + "] [toEventRule0:" + toEventRule0 + "]");
                if (fromEventRule0.before(posTime) && toEventRule0.after(posTime)
                        || fromEventRuleMinus1.before(posTime) && toEventRuleMinus1.after(posTime)
                        || fromEventRulePlus1.before(posTime) && toEventRulePlus1.after(posTime)) {
                    logger2.warning("return true");
                    return true;
                }
            }
            return false;
        }

        private Boolean isCourseOk(Position pos, String course) {
            if (pos.getCourse() == null || course == null || course.trim().length() == 0) {
                return true;
            }
            Double posCourse = pos.getCourse();
            logger2.warning("[posCourse:" + posCourse + "] [course:" + course.trim() + "]");

            course = course.trim().toUpperCase();
            Matcher matcher = EVENT_RULE_COURSE_PATTERN.matcher(course);
            while (matcher.find()) {
                logger2.warning("[group:" + matcher.group() + "]");
                String fromCourse = course.substring(matcher.start(1), matcher.end(1));
                String toCourse = course.substring(matcher.start(2), matcher.end(2));
                logger2.warning("[fromCourse:" + fromCourse + "] [toCourse:" + toCourse + "]");
                if (Double.valueOf(fromCourse) < posCourse && Double.valueOf(toCourse) > posCourse) {
                    logger2.warning("return true");
                    return true;
                }
            }
            return false;
        }

        private void  addEventToUserAndManagers(Map<User, Set<DeviceEvent>> events, User user, DeviceEvent event, Map<User, List<User>> managers) {
            addEvent(events, user, event);
            List<User> userManagers = managers.get(user);
            if (userManagers == null) {
                userManagers = new LinkedList<>();
                User manager = user.getManagedBy();
                while (manager != null) {
                    if (!manager.getNotificationEvents().isEmpty()) {
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

        private void  addEvent(Map<User, Set<DeviceEvent>> events, User user, DeviceEvent event) {
            logger2.warning("NotificationServiceImpl.NotificationSender.addEvent()");
            // check whether user wants to receive such notification events
            // check whether user account is blocked or expired
            if (user.isBlocked() || user.isExpired()) {
                return;
            }
            // check whether user has access to the geo-fence
            if ((event.getType() == DeviceEventType.GEO_FENCE_ENTER || event.getType() == DeviceEventType.GEO_FENCE_EXIT)
                    && !user.hasAccessTo(event.getGeoFence())) {
                return;
            }

            Set<DeviceEvent> userEvents = events.get(user);
            if (userEvents == null) {
                userEvents = new HashSet<>();
                events.put(user, userEvents);
            }
            userEvents.add(event);
        }

        private NotificationSettings findNotificationSettings(User user) {
            logger2.warning("NotificationServiceImpl.NotificationSender.findNotificationSettings()");
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
                List<NotificationSettings> settings = entityManager.get()
                        .createQuery("SELECT s FROM NotificationSettings s WHERE s.user.admin=:true ORDER BY s.user.id ASC", NotificationSettings.class)
                        .setFlushMode(FlushModeType.COMMIT)
                        .setParameter("true", true)
                        .getResultList();
                if (!settings.isEmpty()) {
                    s = settings.get(0);
                }
            }

            return s;
        }

        private NotificationSettings getNotificationSettings(User user) {
            List<NotificationSettings> settings = entityManager.get()
                    .createQuery("SELECT n FROM NotificationSettings n WHERE n.user = :user", NotificationSettings.class)
                    .setFlushMode(FlushModeType.COMMIT)
                    .setParameter("user", user)
                    .getResultList();
            return settings.isEmpty() ? null : settings.get(0);
        }

        private boolean sendEmail(NotificationSettings settings, User user, String subject, String body, String contentType) {
            // perform some validation of e-mail settings
            if (settings.getServer() == null || settings.getServer().trim().isEmpty() ||
                settings.getFromAddress() == null || settings.getFromAddress().trim().isEmpty()) {
                return false;
            }

            logger.info("Sending Email notification to '" + user.getEmail() + "'...");

            Session session = getSession(settings);
            MimeMessage msg = new MimeMessage(session);
            Transport transport = null;
            try {
                msg.setFrom(new InternetAddress(settings.getFromAddress()));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getLogin() + " <" + user.getEmail() + ">", false));
                msg.setSubject(subject, "UTF-8");

                msg.setContent(body, contentType);
                msg.setHeader("X-Mailer", "traccar-web.sendmail");
                msg.setSentDate(new Date());

                transport = session.getTransport("smtp");
                transport.connect();
                transport.sendMessage(msg, msg.getAllRecipients());

                return true;
            } catch (MessagingException me) {
                logger.log(Level.SEVERE, "Unable to send Email message", me);
                return false;
            } finally {
                if (transport != null) try { transport.close(); } catch (MessagingException ignored) {}
            }
        }

        private boolean sendPushbullet(NotificationSettings settings, User user, String subject, String body) {
            // perform some validation of Pushbullet settings
            if (settings.getPushbulletAccessToken() == null || settings.getPushbulletAccessToken().trim().isEmpty()) {
                return false;
            }
            logger.info("Sending Pushbullet notification to '" + user.getEmail() + "'...");

            InputStream is = null;
            OutputStream os = null;
            try {
                URL url = new URL("https://api.pushbullet.com/v2/pushes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + settings.getPushbulletAccessToken());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                os = conn.getOutputStream();

                JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory
                JsonGenerator json = jsonFactory.createGenerator(os, JsonEncoding.UTF8);

                json.writeStartObject();
                json.writeStringField("email", user.getEmail());
                json.writeStringField("type", "note");
                json.writeStringField("title", subject);
                json.writeStringField("body", body);
                json.writeEndObject();
                json.flush();
                json.close();

                is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                try {
                    String line;
                    logger.info("Pushbullet response: ");
                    while ((line = reader.readLine()) != null) logger.info(line);
                    return true;
                } finally {
                    reader.close();
                }
            } catch (MalformedURLException mue) {
                logger.log(Level.SEVERE, "Incorrect URL", mue);
                return false;
            } catch (IOException ioex) {
                logger.log(Level.SEVERE, "I/O Error", ioex);
                return false;
            } finally {
                if (is != null ) try { is.close(); } catch (IOException ignored) {}
            }
        }

        String defaultBody(DeviceEventType type, String locale) {
            String body = messages.message(locale, "defaultNotificationTemplate[" + type.name() + "]");

            return body.replace("''", "'")
                    .replace("{1}", "${deviceName}")
                    .replace("{2}", "${geoFenceName}")
                    .replace("{3}", "${eventTime}")
                    .replace("{4}", "${positionTime}")
                    .replace("{5}", "${maintenanceName}");
        }
    }

    @Inject
    private NotificationSender notificationSender;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void init() throws ServletException {
        super.init();

        scheduler.scheduleWithFixedDelay(notificationSender, 0, 1, TimeUnit.MINUTES);
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @Override
    public void checkEmailSettings(NotificationSettings settings) {
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
    @RequireWrite
    @Override
    public void checkPushbulletSettings(NotificationSettings settings) {
        InputStream is = null;
        try {
            URL url = new URL("https://api.pushbullet.com/v2/users/me");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + settings.getPushbulletAccessToken());
            is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                logger.info("Pushbullet response: ");
                while ((line = reader.readLine()) != null) logger.info(line);
            } finally {
                reader.close();
            }
        } catch (MalformedURLException mue) {
            throw new IllegalArgumentException(mue);
        } catch (IOException ioex) {
            throw new IllegalStateException(ioex);
        } finally {
            if (is != null ) try { is.close(); } catch (IOException ignored) {}
        }
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public String checkTemplate(NotificationTemplate template) {
        List<Device> devices = dataService.getDevices();
        Device testDevice;
        if (devices.isEmpty()) {
            testDevice = new Device();
            testDevice.setName("Test-Device");
            testDevice.setUniqueId("123");
            testDevice.setMaintenances(new ArrayList<Maintenance>());
        } else {
            testDevice = devices.get(0);
        }
        List<GeoFence> geoFences = dataService.getGeoFences();
        GeoFence testGeoFence;
        if (geoFences.isEmpty()) {
            testGeoFence = new GeoFence(0l, "Some-GeoFence");
        } else {
            testGeoFence = geoFences.get(0);
        }
        Maintenance testMaintenance;
        if (testDevice.getMaintenances().isEmpty()) {
            testMaintenance = new Maintenance("Oil change");
        } else {
            testMaintenance = testDevice.getMaintenances().get(0);
        }

        Position testPosition = new Position();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, c.get(Calendar.HOUR) - 1);
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) - 15);
        testPosition.setTime(c.getTime());
        testPosition.setAddress("New York, Times Square");
        testPosition.setLatitude(40.758899);
        testPosition.setLongitude(-73.987325);
        testPosition.setAltitude(100d);
        testPosition.setSpeed(UserSettings.SpeedUnit.kilometersPerHour.toKnots(5d));
        testPosition.setCourse(30d);

        Engine engine = getTemplateEngine(getTimeZone(sessionUser.get()));
        Map<String, Object> model = getTemplateModel(sessionUser.get(), new DeviceEvent(new Date(), testDevice, testPosition, testGeoFence, testMaintenance));

        String transformedSubject = engine.transform(template.getSubject(), model);
        String transformedBody = engine.transform(template.getBody(), model);

        return "<div style=\"background-color: #ffffff;\">" +
                    "<table style=\"border-collapse: collapse;\">" +
                        "<tr><td style=\"border: 1px solid black; padding: 2px;\">" + transformedSubject + "</td></tr>" +
                        "<tr><td style=\"border: 1px solid black; padding: 4px;\">" + transformedBody + "</td></tr>" +
                    "</table>" +
                "</div>";
    }

    private static Map<String, Object> getTemplateModel(User user, DeviceEvent event) {
        Map<String, Object> model = new HashMap<>();
        model.put(deviceName.name(), event.getDevice() == null ? "N/A" : event.getDevice().getName());
        model.put(geoFenceName.name(), event.getGeoFence() == null ? "N/A" : event.getGeoFence().getName());
        model.put(eventTime.name(), event.getTime());
        if (event.getPosition() != null) {
            Position p = event.getPosition();
            model.put(positionTime.name(), p.getTime());
            model.put(positionAddress.name(), p.getAddress());
            model.put(positionLat.name(), p.getLatitude());
            model.put(positionLon.name(), p.getLongitude());
            model.put(positionAlt.name(), p.getAltitude());
            model.put(positionSpeed.name(), p.getSpeed() == null ? null : (p.getSpeed() * user.getUserSettings().getSpeedUnit().getFactor()));
            model.put(positionCourse.name(), p.getCourse());
        }
        model.put(maintenanceName.name(), event.getMaintenance() == null ? "N/A" : event.getMaintenance().getName());
        return model;
    }

    private static TimeZone getTimeZone(User user) {
        if (user == null || user.getUserSettings() == null || user.getUserSettings().getTimeZoneId() == null) {
            return TimeZone.getDefault();
        }
        String timeZoneId = user.getUserSettings().getTimeZoneId();
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        // TimeZone.getTimeZone will fall back to GMT if no zone found with such ID
        // Instead of this we will fall back to the server time zone in such case
        return timeZone.getID().equals(timeZoneId) ? timeZone : TimeZone.getDefault();
    }

    private static Engine getTemplateEngine(final TimeZone timeZone) {
        Engine engine = new Engine();
        engine.registerNamedRenderer(new NamedRenderer() {
            @Override
            public String render(Object o, String format, Locale locale) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
                dateFormat.setTimeZone(timeZone);
                return dateFormat.format((Date) o);
            }

            @Override
            public String getName() {
                return "date";
            }

            @Override
            public RenderFormatInfo getFormatInfo() {
                return null;
            }

            @Override
            public Class<?>[] getSupportedClasses() {
                return new Class<?>[]{Date.class};
            }
        });
        engine.registerRenderer(Date.class, new Renderer<Date>() {
            @Override
            public String render(Date o, Locale locale) {
                if (o == null) {
                    return "";
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", locale);
                dateFormat.setTimeZone(timeZone);
                return dateFormat.format(o);
            }
        });
        return engine;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public NotificationSettings getSettings() {
        List<NotificationSettings> settings = entityManager.get()
                .createQuery("SELECT n FROM NotificationSettings n WHERE n.user = :user", NotificationSettings.class)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("user", sessionUser.get())
                .getResultList();
        if (settings.isEmpty()) {
            return null;
        } else {
            NotificationSettings s = settings.get(0);
            s.setTransferTemplates(new HashMap<DeviceEventType, NotificationTemplate>(s.getTemplates().size()));
            for (NotificationTemplate t : s.getTemplates()) {
                s.getTransferTemplates().put(t.getType(), t);
            }
            return s;
        }
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
            entityManager.get().persist(currentSettings);
            for (NotificationTemplate newTemplate : currentSettings.getTransferTemplates().values()) {
                newTemplate.setSettings(currentSettings);
                entityManager.get().persist(newTemplate);
            }
        } else {
            currentSettings.copyFrom(settings);
            for (NotificationTemplate existingTemplate : currentSettings.getTemplates()) {
                NotificationTemplate updatedTemplate = settings.getTransferTemplates().remove(existingTemplate.getType());
                if (updatedTemplate == null) {
                    entityManager.get().remove(existingTemplate);
                } else {
                    existingTemplate.copyFrom(updatedTemplate);
                }
            }
            for (NotificationTemplate newTemplate : settings.getTransferTemplates().values()) {
                newTemplate.setSettings(currentSettings);
                entityManager.get().persist(newTemplate);
            }
        }
    }
}
