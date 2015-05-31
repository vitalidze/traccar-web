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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.traccar.web.shared.model.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dataService")
public interface DataService extends RemoteService {

    UserDTO authenticated() throws IllegalStateException;

    UserDTO login(String login, String password, boolean passwordHashed);

    UserDTO login(String login, String password);

    boolean logout();

    UserDTO register(String login, String password);

    List<UserDTO> getUsers();

    UserDTO addUser(UserDTO user);

    UserDTO updateUser(UserDTO user);

    UserDTO removeUser(UserDTO user);

    List<DeviceDTO> getDevices();

    DeviceDTO addDevice(DeviceDTO device) throws TraccarException;

    DeviceDTO updateDevice(DeviceDTO device) throws TraccarException;

    DeviceDTO removeDevice(DeviceDTO device);

    Map<UserDTO, Boolean> getDeviceShare(DeviceDTO device);

    void saveDeviceShare(DeviceDTO device, Map<UserDTO, Boolean> share);

    List<Position> getPositions(DeviceDTO device, Date from, Date to, boolean filter);

    List<Position> getLatestPositions();

    List<Position> getLatestNonIdlePositions();

    ApplicationSettingsDTO getApplicationSettings();

    void updateApplicationSettings(ApplicationSettingsDTO applicationSettings);

    String getTrackerServerLog(short sizeKb);

    void saveRoles(List<UserDTO> users);

    List<GeoFence> getGeoFences();

    GeoFence addGeoFence(GeoFence geoFence) throws TraccarException;

    GeoFence updateGeoFence(GeoFence geoFence) throws TraccarException;

    GeoFence removeGeoFence(GeoFence geoFence);

    Map<UserDTO, Boolean> getGeoFenceShare(GeoFence geoFence);

    void saveGeoFenceShare(GeoFence geoFence, Map<UserDTO, Boolean> share);
}
