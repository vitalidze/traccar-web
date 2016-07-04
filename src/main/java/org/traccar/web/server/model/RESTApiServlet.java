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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.traccar.web.client.model.DataService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class RESTApiServlet extends HttpServlet {
    public final static String REQUEST_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    @Inject
    private DataService dataService;

    private final ThreadLocal<SimpleDateFormat> requestDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(REQUEST_DATE_PATTERN);
        }
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper jackson = JacksonUtils.create();
        String methodName = req.getPathInfo().substring(1);
        String payload = req.getParameter("payload");
        Object[] args = payload == null ? new Object[0] : jackson.readValue(payload, Object[].class);
        makeRestCall(jackson, resp, methodName, args);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper jackson = JacksonUtils.create();
        String methodName = req.getPathInfo().substring(1);
        Object[] args = jackson.readValue(new InputStreamReader(req.getInputStream()), Object[].class);
        makeRestCall(jackson, resp, methodName, args);
    }

    private void makeRestCall(ObjectMapper gson, HttpServletResponse response, String methodName, Object[] args) throws IOException {
        try {
            Class<?>[] argClasses = new Class<?>[args == null ? 0 : args.length];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    argClasses[i] = args[i] == null ? null : args[i].getClass();
                }
            }
            Method method = null;
            try {
                method = dataService.getClass().getDeclaredMethod(methodName, argClasses);
            } catch (NoSuchMethodException nsme) {
                /**
                 * Try to find method by name and number of arguments
                 */
                for (Method declaredMethod : dataService.getClass().getDeclaredMethods()) {
                    if (declaredMethod.getName().equals(methodName) && declaredMethod.getParameterTypes().length == args.length) {
                        /**
                         * Parse arguments
                         */
                        for (int i = 0; i < args.length; i++) {
                            Class<?> expectedType = declaredMethod.getParameterTypes()[i];
                            if (argClasses[i] != null && argClasses[i] != expectedType) {
                                if (argClasses[i] == String.class) {
                                    if (args[i].toString().equals("undefined")) {
                                        args[i] = null;
                                    } else if (Date.class.isAssignableFrom(expectedType)) {
                                        args[i] = requestDateFormat.get().parse(args[i].toString());
                                    } else {
                                        args[i] = gson.readValue("\"" + args[i].toString() + "\"", expectedType);
                                    }
                                } else {
                                    args[i] = gson.readValue(gson.writeValueAsString(args[i]), expectedType);
                                }
                            }
                        }
                        method = declaredMethod;
                        break;
                    }
                }
                if (method == null) {
                    throw new NoSuchMethodException();
                }
            }
            Object result = method.invoke(dataService, args);
            if (result != null) {
                if (!response.containsHeader("Content-Type")) {
                    response.setHeader("Content-Type", "application/json;charset=UTF-8");
                }
                response.setCharacterEncoding("UTF-8");
                String s = gson.writeValueAsString(result);
                response.getWriter().write(s);
            }
        } catch (ParseException pe) {
            log("Unable to parse date: " + pe.getLocalizedMessage());
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (Exception ex) {}
        } catch (NoSuchMethodException nsme) {
            log("Method not found: " + methodName);
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (Exception ex) {}
        } catch (IllegalArgumentException iae) {
            log("Method " + methodName + " illegal arguments: " + iae.getLocalizedMessage());
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (Exception ex) {}
        } catch (InvocationTargetException ite) {
            log("Error during method '" + methodName + "' call: " + ite.getLocalizedMessage(), ite);
            int errorCode = 0;
            if (ite.getCause() instanceof SecurityException) {
                errorCode = HttpServletResponse.SC_UNAUTHORIZED;
            } else {
                errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            try {
                response.sendError(errorCode);
            } catch (Exception ex) {}
        } catch (IllegalAccessException iae) {
            log("Method '" + methodName + "' is not accessible");
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {}
        } catch (Exception ex) {
            log("Method '" + methodName + "' failed with unexpected error", ex);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception e) {}
        }
    }
}
