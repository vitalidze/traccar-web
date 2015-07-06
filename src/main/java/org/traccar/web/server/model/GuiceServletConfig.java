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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.EventService;
import org.traccar.web.shared.model.ApplicationSettings;
import org.traccar.web.shared.model.Picture;
import org.traccar.web.shared.model.User;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GuiceServletConfig extends GuiceServletContextListener {
    private static final String PERSISTENCE_DATASTORE = "java:/DefaultDS";
    private static final String PERSISTENCE_UNIT_DEBUG = "debug";
    private static final String PERSISTENCE_UNIT_RELEASE = "release";

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(getModule());
    }

    ServletModule getModule() {
        return new ServletModule() {
            @Override
            protected void configureServlets() {
                String persistenceUnit;
                try {
                    Context context = new InitialContext();
                    context.lookup(PERSISTENCE_DATASTORE);
                    persistenceUnit = PERSISTENCE_UNIT_RELEASE;
                } catch (NamingException e) {
                    persistenceUnit = PERSISTENCE_UNIT_DEBUG;
                }

                install(new JpaPersistModule(persistenceUnit));

                filter("/traccar/*").through(PersistFilter.class);
                filter("/", "/traccar.html", "/m/", "/m/index.html").through(LocaleFilter.class);

                serve("/traccar/dataService").with(DataServiceImpl.class);
                serve("/traccar/uiStateService").with(UIStateServiceImpl.class);
                serve("/traccar/eventService").with(EventServiceImpl.class);
                serve("/traccar/notificationService").with(NotificationServiceImpl.class);
                serve("/traccar/picturesService").with(PicturesServiceImpl.class);

                serve("/traccar/rest/*").with(RESTApiServlet.class);
                serve("/traccar/export/*").with(ExportServlet.class);
                serve("/traccar/import/*").with(ImportServlet.class);
                serve("/traccar/s/login").with(LoginServlet.class);
                serve(Picture.URL_PREFIX + "*").with(PicturesServlet.class);

                UserCheck userCheck = new UserCheck();
                requestInjection(userCheck);

                bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequireUser.class), userCheck);
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(ManagesDevices.class), userCheck);
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequireWrite.class), userCheck);

                MethodCallLogger methodCallLogger = new MethodCallLogger();
                requestInjection(methodCallLogger);
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(LogCall.class), methodCallLogger);

                bind(User.class).toProvider(CurrentUserProvider.class);
                bind(ApplicationSettings.class).toProvider(ApplicationSettingsProvider.class);
                bind(DataService.class).to(DataServiceImpl.class);
                bind(EventService.class).to(EventServiceImpl.class);
            }
        };
    }
}
