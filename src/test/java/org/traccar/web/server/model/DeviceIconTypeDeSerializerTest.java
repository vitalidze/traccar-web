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
import org.junit.Test;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.DeviceIconType;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DeviceIconTypeDeSerializerTest {
    ObjectMapper jackson = JacksonUtils.create();

    @Test
    public void testDefaultIconSerializeDeserialize() throws IOException {
        Device device = new Device();
        device.setIconType(DeviceIconType.DEFAULT);
        String json = jackson.writeValueAsString(device);

        device = jackson.readValue(json, Device.class);

        assertEquals(DeviceIconType.DEFAULT, device.getIconType());
    }

    @Test
    public void testDefaultIconFromTypeString() throws IOException {
        DeviceIconType type = jackson.readValue("{\"type\": \"DEFAULT\"}", DeviceIconType.class);
        assertEquals(DeviceIconType.DEFAULT, type);
    }

    @Test
    public void testDefaultIconFromString() throws IOException {
        DeviceIconType type = jackson.readValue("\"DEFAULT\"", DeviceIconType.class);
        assertEquals(DeviceIconType.DEFAULT, type);
    }
}
