/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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
package org.traccar.web.client.model;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.traccar.web.shared.model.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dataService")
public interface DataService extends RemoteService {

    User authenticated();

    User login(String login, String password, boolean passwordHashed) throws TraccarException;

    User login(String login, String password) throws TraccarException;

    boolean logout();

    User register(String login, String password);

    List<User> getUsers();

    User addUser(User user);

    User updateUser(User user);

    User removeUser(User user);

    List<Device> getDevices();

    Device addDevice(Device device) throws TraccarException;

    Device updateDevice(Device device) throws TraccarException;

    Device removeDevice(Device device);

    Map<User, Boolean> getDeviceShare(Device device);

    void saveDeviceShare(Device device, Map<User, Boolean> share);

    List<Position> getPositions(Device device, Date from, Date to, boolean filter, boolean snapToRoads);

    List<Position> getLatestPositions();

    List<Position> getLatestNonIdlePositions();

    ApplicationSettings getApplicationSettings();

    void updateApplicationSettings(ApplicationSettings applicationSettings);

    String getTrackerServerLog(short sizeKb);

    void saveRoles(List<User> users);

    List<GeoFence> getGeoFences();

    GeoFence addGeoFence(GeoFence geoFence) throws TraccarException;

    GeoFence updateGeoFence(GeoFence geoFence) throws TraccarException;

    GeoFence removeGeoFence(GeoFence geoFence);

    Map<User, Boolean> getGeoFenceShare(GeoFence geoFence);

    void saveGeoFenceShare(GeoFence geoFence, Map<User, Boolean> share);
}
