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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.traccar.web.shared.model.DeviceIconType;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.PositionIconType;

import java.io.IOException;

class DeviceIconTypeSerializer extends JsonSerializer<DeviceIconType> {
    static final DeviceIconTypeSerializer INSTANCE = new DeviceIconTypeSerializer();

    @Override
    public void serialize(DeviceIconType deviceIcon,
                          JsonGenerator json,
                          SerializerProvider serializerProvider) throws IOException {
        json.writeStartObject();
        json.writeStringField("type", deviceIcon.name());
        for (Position.Status status : Position.Status.values()) {
            json.writeObjectFieldStart(status.name());

            PositionIconType positionIcon = deviceIcon.getPositionIconType(status);
            json.writeNumberField("width", positionIcon.getWidth());
            json.writeNumberField("height", positionIcon.getHeight());

            json.writeArrayFieldStart("urls");
            json.writeString(positionIcon.getURL(false));
            json.writeString(positionIcon.getURL(true));
            json.writeEndArray();

            json.writeEndObject();
        }
        json.writeEndObject();
    }
}
