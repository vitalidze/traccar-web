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

import com.google.inject.persist.UnitOfWork;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ScheduledTask implements Runnable {
    @Inject
    private UnitOfWork unitOfWork;

    @Inject
    protected Logger logger;

    @Override
    public final void run() {
        unitOfWork.begin();
        try {
            doWork();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error during scheduled task execution", ex);
        } finally {
            unitOfWork.end();
        }
    }

    public abstract void doWork() throws Exception;
}
