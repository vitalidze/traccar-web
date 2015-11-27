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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackendRefresher implements MethodInterceptor {
    @Inject
    private Logger logger;
    @Inject
    private Provider<HttpServletRequest> request;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object result = methodInvocation.proceed();
        if (!refreshWithReflection()) {
            refreshWithAPI();
        }
        return result;
    }

    private boolean refreshWithReflection() {
        try {
            Class<?> contextClass = Class.forName("org.traccar.Context");
            Method getPermissionsManager = contextClass.getDeclaredMethod("getPermissionsManager");
            Object permissionsManager = getPermissionsManager.invoke(null);
            Method refresh = permissionsManager.getClass().getDeclaredMethod("refresh");
            refresh.invoke(permissionsManager);
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to refresh permissions via reflection", e);
        }
        return false;
    }

    private void refreshWithAPI() {
        String address = request.get().getRequestURL().toString();
        address = address.substring(0, address.indexOf("traccar/"));
        address += "api/device/link?deviceId=-1&userId=-1";
        try {
            URL url = new URL(address);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            logger.info("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            logger.info(response.toString());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to refresh permissions via API", e);
        }
    }
}
