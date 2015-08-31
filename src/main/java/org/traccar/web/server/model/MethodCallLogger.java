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
import javax.inject.Singleton;
import java.util.logging.Logger;

@Singleton
public class MethodCallLogger implements MethodInterceptor {
    @Inject
    private Logger logger;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        LogCall annotation = methodInvocation.getMethod().getAnnotation(LogCall.class);
        if (annotation.value().isEmpty()) {
            logger.info(methodInvocation.getMethod().getDeclaringClass().getSimpleName() + "." + methodInvocation.getMethod().getName() + "()");
        } else {
            String s = annotation.value();
            Object[] args = methodInvocation.getArguments();
            for (int argNo = 0; argNo < args.length; argNo++) {
                s = s.replace("{" + argNo + "}", args[argNo] == null ? "null" : args[argNo].toString());
            }
            logger.info(s);
        }
        return methodInvocation.proceed();
    }
}
