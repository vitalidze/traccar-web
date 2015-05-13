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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.DeviceIconType;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.PositionIconType;

public class DeviceIconTypeSerializerTest {
    Gson gson = GsonUtils.create();

    @Test
    public void testDefaultIcon() {
        Device device = new Device();
        device.setIconType(DeviceIconType.DEFAULT);
        String json = gson.toJson(device);

        JsonObject jsonDevice = gson.fromJson(json, JsonObject.class);
        JsonObject jsonDeviceIconType = jsonDevice.getAsJsonObject("iconType");
        assertNotNull(jsonDeviceIconType);

        testDefaultPositionIconType(jsonDeviceIconType, Position.Status.OFFLINE, "OFFLINE");
        testDefaultPositionIconType(jsonDeviceIconType, Position.Status.LATEST, "LATEST");
    }

    private void testDefaultPositionIconType(JsonObject jsonDeviceIconType, Position.Status status, String iconTypeString) {
        PositionIconType positionIconType = DeviceIconType.DEFAULT.getPositionIconType(status);
        JsonObject jsonPositionIconType = jsonDeviceIconType.getAsJsonObject(iconTypeString);
        assertEquals(positionIconType.getWidth(), jsonPositionIconType.getAsJsonPrimitive("width").getAsInt());
        assertEquals(positionIconType.getHeight(), jsonPositionIconType.getAsJsonPrimitive("height").getAsInt());

        JsonArray urls = jsonPositionIconType.getAsJsonArray("urls");
        assertEquals(positionIconType.getURL(false), urls.get(0).getAsString());
        assertEquals(positionIconType.getURL(true), urls.get(1).getAsString());
    }
}
