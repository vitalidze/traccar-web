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

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.DeviceIconType;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.PositionIconType;

import java.io.IOException;
import java.util.Iterator;

public class DeviceIconTypeSerializerTest {
    ObjectMapper jackson = JacksonUtils.create();

    @Test
    public void testDefaultIcon() throws IOException {
        Device device = new Device();
        device.setIconType(DeviceIconType.DEFAULT);
        String json = jackson.writeValueAsString(device);

        JsonNode jsonDevice = jackson.readTree(json);
        JsonNode jsonDeviceIconType = jsonDevice.get("iconType");
        assertNotNull(jsonDeviceIconType);

        testDefaultPositionIconType(jsonDeviceIconType, Position.Status.OFFLINE, "OFFLINE");
        testDefaultPositionIconType(jsonDeviceIconType, Position.Status.LATEST, "LATEST");
    }

    private void testDefaultPositionIconType(JsonNode jsonDeviceIconType, Position.Status status, String iconTypeString) {
        PositionIconType positionIconType = DeviceIconType.DEFAULT.getPositionIconType(status);
        JsonNode jsonPositionIconType = jsonDeviceIconType.get(iconTypeString);
        assertEquals(positionIconType.getWidth(), jsonPositionIconType.get("width").asInt());
        assertEquals(positionIconType.getHeight(), jsonPositionIconType.get("height").asInt());

        Iterator<JsonNode> urls = jsonPositionIconType.get("urls").elements();
        assertEquals(positionIconType.getURL(false), urls.next().asText());
        assertEquals(positionIconType.getURL(true), urls.next().asText());

        assertEquals(DeviceIconType.DEFAULT.name(), jsonDeviceIconType.get("type").asText());
    }
}
