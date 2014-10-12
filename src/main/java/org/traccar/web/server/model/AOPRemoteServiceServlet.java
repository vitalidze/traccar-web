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
import java.util.List;

abstract class AOPRemoteServiceServlet extends RemoteServiceServlet {
    final Object proxy;

    public AOPRemoteServiceServlet(Class<?> iface) {
        this.proxy = Proxy.newProxyInstance(iface.getClassLoader(), new Class<?> [] { iface }, new AOPHandler(this));
    }

    class AOPHandler implements InvocationHandler {
        final Object target;

        AOPHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            try {
                checkAccess(targetMethod);
                checkDeviceManagementAccess(targetMethod);
                beginTransaction(targetMethod);
                return targetMethod.invoke(target, args);
            } finally {
                endTransaction(targetMethod);
            }
        }

        void checkAccess(Method method) throws Throwable {
            RequireUser requireUser = method.getAnnotation(RequireUser.class);
            if (requireUser == null) return;
            beginTransaction();
            try {
                User user = getSessionUser();
                if (user == null) {
                    throw new SecurityException("Not logged in");
                }
                if (requireUser.roles().length > 0) {
                    StringBuilder roles = new StringBuilder();
                    for (Role role : requireUser.roles()) {
                        if (roles.length() > 0) {
                            roles.append(" or ");
                        }
                        roles.append(role.toString());
                        if (role.has(user)) {
                            return;
                        }
                    }
                    throw new SecurityException("User must have " + roles + " role");
                }
            } finally {
                if (method.getAnnotation(Transactional.class) == null) {
                    endTransaction(false);
                }
            }
        }

        void checkDeviceManagementAccess(Method method) throws Throwable {
            ManagesDevices managesDevices = method.getAnnotation(ManagesDevices.class);
            if (managesDevices == null) return;
            beginTransaction();
            try {
                User user = getSessionUser();
                if (user == null) {
                    throw new SecurityException("Not logged in");
                }
                if (!user.getAdmin() && !user.getManager()) {
                    ApplicationSettings applicationSettings = getSessionEntityManager().createQuery("SELECT x FROM ApplicationSettings x", ApplicationSettings.class).getSingleResult();
                    if (applicationSettings.isDisallowDeviceManagementByUsers()) {
                        throw new SecurityException("Users are not allowed to manage devices");
                    }
                }
            } finally {
                if (method.getAnnotation(Transactional.class) == null) {
                    endTransaction(false);
                }
            }
        }

        void beginTransaction(Method method) {
            Transactional transactional = method.getAnnotation(Transactional.class);
            if (transactional == null) return;
            beginTransaction();
        }

        void beginTransaction() {
            EntityManager entityManager = getSessionEntityManager();
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }
        }

        void endTransaction(Method method) throws Throwable {
            Transactional transactional = method.getAnnotation(Transactional.class);
            if (transactional == null) return;
            endTransaction(transactional.commit());
        }

        void endTransaction(boolean commit) throws Throwable {
            EntityManager entityManager = getSessionEntityManager();
            try {
                if (entityManager.getTransaction().isActive()) {
                    if (commit) {
                        try {
                            entityManager.getTransaction().commit();
                        } catch (Throwable t) {
                            entityManager.getTransaction().rollback();
                            throw t;
                        }
                    } else {
                        entityManager.getTransaction().rollback();
                    }
                }
            } finally {
                closeSessionEntityManager();
            }
        }
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
                RPCRequest rpcRequest = RPC.decodeRequest(payload, proxy.getClass(), this);
                onAfterRequestDeserialized(rpcRequest);
                return RPC.invokeAndEncodeResponse(proxy, rpcRequest.getMethod(),
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
        perThreadRequest.set(req);
        perThreadResponse.set(resp);
        try {
            String payload = RPCServletUtils.readContent(req, null, null);
            String result = makeRestCall(payload);
            if (result != null) {
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

    private String makeRestCall(String payload) {
        String methodName = getThreadLocalRequest().getPathInfo().substring(1);
        try {
            Object[] args = new Gson().fromJson(payload, Object[].class);
            Class<?>[] argClasses = new Class<?>[args == null ? 0 : args.length];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    argClasses[i] = args[i].getClass();
                }
            }
            Method method = proxy.getClass().getDeclaredMethod(methodName, argClasses);
            Object result = method.invoke(proxy, args);
            return result == null ? null : new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(result);
        } catch (NoSuchMethodException nsme) {
            log("Method not found: " + methodName);
            try {
                getThreadLocalResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (Exception ex) {}
            return "Method not found: " + methodName;
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

    abstract EntityManager getSessionEntityManager();
    abstract void closeSessionEntityManager();
    abstract User getSessionUser();
}
