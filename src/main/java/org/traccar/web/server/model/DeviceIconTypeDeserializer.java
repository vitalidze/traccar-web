/*
 * Copyright 2016 Vitaly Litvak (vitavaque@gmail.com)
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.traccar.web.shared.model.DeviceIconType;

import java.io.IOException;

public class DeviceIconTypeDeserializer extends JsonDeserializer<DeviceIconType> {
    @Override
    public DeviceIconType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode tree = p.readValueAsTree();
        if (tree.isObject()) {
            return DeviceIconType.valueOf(tree.get("type").asText());
        } else {
            return DeviceIconType.valueOf(tree.asText());
        }
    }
}
