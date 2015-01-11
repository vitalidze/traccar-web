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
import org.traccar.web.shared.model.NotificationSettings;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Properties;

@Singleton
public class NotificationServiceImpl extends RemoteServiceServlet implements NotificationService {
    @Inject
    private Provider<User> sessionUser;

    @Inject
    private Provider<EntityManager> entityManager;

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @Override
    public void checkSettings(NotificationSettings settings) {
        try {
            Session s = getSession(settings);
            Transport t = s.getTransport("smtp");
            t.connect();
            t.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
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
