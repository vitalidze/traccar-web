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
package org.traccar.web.shared.model;

public class MaxDeviceNumberReachedException extends TraccarException {
    private User reachedLimit;

    public MaxDeviceNumberReachedException() {
    }

    public MaxDeviceNumberReachedException(User reachedLimit) {
        this.reachedLimit = new User();
        this.reachedLimit.setAdmin(reachedLimit.getAdmin());
        this.reachedLimit.setMaxNumOfDevices(reachedLimit.getMaxNumOfDevices());
        this.reachedLimit.setCompanyName(reachedLimit.getCompanyName());
        this.reachedLimit.setEmail(reachedLimit.getEmail());
        this.reachedLimit.setBlocked(reachedLimit.isBlocked());
        this.reachedLimit.setExpirationDate(reachedLimit.getExpirationDate());
        this.reachedLimit.setLogin(reachedLimit.getLogin());
        this.reachedLimit.setFirstName(reachedLimit.getFirstName());
        this.reachedLimit.setLastName(reachedLimit.getLastName());
        this.reachedLimit.setManager(reachedLimit.getManager());
        this.reachedLimit.setPhoneNumber(reachedLimit.getPhoneNumber());
    }

    public User getReachedLimit() {
        return reachedLimit;
    }
}
