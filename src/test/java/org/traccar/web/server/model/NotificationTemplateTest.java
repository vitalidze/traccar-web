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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.traccar.web.shared.model.DeviceEventType;

import java.io.IOException;

public class NotificationTemplateTest {
    @Test
    public void testDefaultBodyEng() throws IOException {
        assertEquals("Device '${deviceName}' went offline at ${eventTime}", NotificationServiceImpl.defaultBody(DeviceEventType.OFFLINE, null));
        assertEquals("Device '${deviceName}' went offline at ${eventTime}", NotificationServiceImpl.defaultBody(DeviceEventType.OFFLINE, "en"));
        assertEquals("Device '${deviceName}' went offline at ${eventTime}", NotificationServiceImpl.defaultBody(DeviceEventType.OFFLINE, "default"));
        assertEquals("Device '${deviceName}' went offline at ${eventTime}", NotificationServiceImpl.defaultBody(DeviceEventType.OFFLINE, "not_exist"));
        assertEquals("Device '${deviceName}' went offline at ${eventTime}", NotificationServiceImpl.defaultBody(DeviceEventType.OFFLINE, ""));

        assertEquals("Device '${deviceName}' entered geo-fence '${geoFenceName}' at ${positionTime}", NotificationServiceImpl.defaultBody(DeviceEventType.GEO_FENCE_ENTER, null));
        assertEquals("Device '${deviceName}' exited geo-fence '${geoFenceName}' at ${positionTime}", NotificationServiceImpl.defaultBody(DeviceEventType.GEO_FENCE_EXIT, null));
    }

    @Test
    public void testDefaultBodyRus() throws IOException {
        assertEquals("Потеряна связь с устройством '${deviceName}' в ${eventTime}", NotificationServiceImpl.defaultBody(DeviceEventType.OFFLINE, "ru"));
        assertEquals("Устройство '${deviceName}' вошло в геозону '${geoFenceName}' в ${positionTime}", NotificationServiceImpl.defaultBody(DeviceEventType.GEO_FENCE_ENTER, "ru"));
        assertEquals("Устройство '${deviceName}' покинуло геозону '${geoFenceName}' в ${positionTime}", NotificationServiceImpl.defaultBody(DeviceEventType.GEO_FENCE_EXIT, "ru"));
    }
}
