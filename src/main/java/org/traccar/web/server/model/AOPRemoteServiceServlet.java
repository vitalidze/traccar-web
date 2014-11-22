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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.traccar.web.shared.model.ApplicationSettings;
import org.traccar.web.shared.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

abstract class AOPRemoteServiceServlet extends RemoteServiceServlet {
    public AOPRemoteServiceServlet() {
    }

    /**
     * <p>Taken from RemoteServiceServlet implementation from GWT 2.6.0</p>
     *
     * <p><b>IMPORTANT NOTE</b>: May need to be updated when version of GWT is changed</p>
     */
    @Override
    public String processCall(String payload) throws SerializationException {
        if (getThreadLocalRequest().getContentType().startsWith("text/x-gwt-rpc")) {
            // First, check for possible XSRF situation
            checkPermutationStrongName();

            try {
                RPCRequest rpcRequest = RPC.decodeRequest(payload, getClass(), this);
                onAfterRequestDeserialized(rpcRequest);
                return RPC.invokeAndEncodeResponse(this, rpcRequest.getMethod(),
                        rpcRequest.getParameters(), rpcRequest.getSerializationPolicy(),
                        rpcRequest.getFlags());
            } catch (IncompatibleRemoteServiceException ex) {
                log(
                        "An IncompatibleRemoteServiceException was thrown while processing this call.",
                        ex);
                return RPC.encodeResponseForFailure(null, ex);
            } catch (RpcTokenException tokenException) {
                log("An RpcTokenException was thrown while processing this call.",
                        tokenException);
                return RPC.encodeResponseForFailure(null, tokenException);
            }
        } else {
            return makeRestCall(payload);
        }
    }

    @Override
    protected String readContent(HttpServletRequest request) throws ServletException, IOException {
        if (request.getContentType().startsWith("text/x-gwt-rpc")) {
            return super.readContent(request);
        } else {
            return RPCServletUtils.readContent(request, null, null);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // warm up thread local variables
        if (perThreadRequest == null) {
            getThreadLocalRequest();
        }
        if (perThreadResponse == null) {
            getThreadLocalResponse();
        }

        perThreadRequest.set(req);
        perThreadResponse.set(resp);
        try {
            String payload = req.getParameter("payload");
            if (payload == null) {
                payload = "";
            }
            String result = makeRestCall(payload);
            if (result != null) {
                if (!resp.containsHeader("Content-Type")) {
                    resp.setHeader("Content-Type", "application/json;charset=UTF-8");
                }
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(result);
                if (resp.getStatus() != HttpServletResponse.SC_INTERNAL_SERVER_ERROR &&
                    resp.getStatus() != HttpServletResponse.SC_UNAUTHORIZED &&
                    resp.getStatus() != HttpServletResponse.SC_NOT_FOUND) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
            }
        } finally {
            perThreadRequest.set(null);
            perThreadResponse.set(null);
        }
    }

    private final ThreadLocal<SimpleDateFormat> requestDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        }
    };

    private String makeRestCall(String payload) {
        String methodName = getThreadLocalRequest().getPathInfo().substring(1);
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create();
            Object[] args = gson.fromJson(payload, Object[].class);
            Class<?>[] argClasses = new Class<?>[args == null ? 0 : args.length];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    argClasses[i] = args[i] == null ? null : args[i].getClass();
                }
            }
            Method method = null;
            try {
                method = getClass().getDeclaredMethod(methodName, argClasses);
            } catch (NoSuchMethodException nsme) {
                /**
                 * Try to find method by name and number of arguments
                 */
                for (Method declaredMethod : getClass().getDeclaredMethods()) {
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
                                        args[i] = gson.fromJson("\"" + args[i].toString() + "\"", expectedType);
                                    }
                                } else {
                                    args[i] = gson.fromJson(args[i].toString(), expectedType);
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
            Object result = method.invoke(this, args);
            return result == null ? null : gson.toJson(result);
        } catch (ParseException pe) {
            log("Unable to parse date: " + pe.getLocalizedMessage());
            try {
                getThreadLocalResponse().sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (Exception ex) {}
            return "Unable to parse date: " + methodName;
        } catch (NoSuchMethodException nsme) {
            log("Method not found: " + methodName);
            try {
                getThreadLocalResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (Exception ex) {}
            return "Method not found: " + methodName;
        } catch (IllegalArgumentException iae) {
            log("Method " + methodName + " illegal arguments: " + iae.getLocalizedMessage());
            try {
                getThreadLocalResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (Exception ex) {}
            return "Method " + methodName + " illegal arguments: " + iae.getLocalizedMessage();
        } catch (InvocationTargetException ite) {
            log("Error during method '" + methodName + "' call: " + ite.getLocalizedMessage(), ite);
            int errorCode = 0;
            if (ite.getCause() instanceof SecurityException) {
                errorCode = HttpServletResponse.SC_UNAUTHORIZED;
            } else {
                errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            try {
                getThreadLocalResponse().sendError(errorCode);
            } catch (Exception ex) {}
            return "Error during method '" + methodName + "' call: " + ite.getLocalizedMessage();
        } catch (IllegalAccessException iae) {
            log("Method '" + methodName + "' is not accessible");
            try {
                getThreadLocalResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {}
            return "Method '" + methodName + "' is not accessible";
        }
    }
}
