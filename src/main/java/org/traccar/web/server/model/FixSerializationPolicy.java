package org.traccar.web.server.model;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.servlet.http.HttpServletRequest;

public class FixSerializationPolicy implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (methodInvocation.getMethod().getName().equals("doGetSerializationPolicy")) {
            Object result = methodInvocation.proceed();
            if (result == null) {
                HttpServletRequest request = (HttpServletRequest) methodInvocation.getArguments()[0];
                String localModuleBaseURL = request.getScheme() + "://" +
                        request.getRemoteHost() + ":" + request.getLocalPort() +
                        "/traccar/";
                methodInvocation.getArguments()[1] = localModuleBaseURL;
                return methodInvocation.proceed();
            }
            return result;
        }
        return methodInvocation.proceed();
    }
}
