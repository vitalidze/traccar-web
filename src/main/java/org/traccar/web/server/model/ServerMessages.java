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

import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ServerMessages {
    @Inject
    protected Logger logger;

    Properties defaultMessages = new Properties();
    Map<String, Properties> localeMessages = new HashMap<>();

    public ServerMessages() throws IOException {
        defaultMessages.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/traccar/web/client/i18n/Messages.properties"));
    }

    public String message(String locale, String key) {
        Properties localeMessages = this.localeMessages.get(locale);
        if (localeMessages == null) {
            localeMessages = new Properties();
            InputStream messagesIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/traccar/web/client/i18n/Messages_" + locale + ".properties");
            if (messagesIS == null) {
                localeMessages = defaultMessages;
            } else {
                try {
                    localeMessages.load(new InputStreamReader(messagesIS, "UTF-8"));
                } catch (IOException ioex) {
                    logger.log(Level.WARNING, "I/O error while reading localized strings", ioex);
                    localeMessages = defaultMessages;
                } finally {
                    IOUtils.closeQuietly(messagesIS);
                }
            }
            this.localeMessages.put(locale, localeMessages);
        }
        return localeMessages.getProperty(key, defaultMessages.getProperty(key));
    }
}
