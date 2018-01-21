/*
 * Copyright 2018 Vitaly Litvak (vitavaque@gmail.com)
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

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.DeviceEventType;
import org.traccar.web.shared.model.EventRule;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.User;

public interface EventRuleProperties extends PropertyAccess<EventRule> {
    ModelKeyProvider<EventRule> id();

    ValueProvider<EventRule, User> user();

    ValueProvider<EventRule, Device> device();

    ValueProvider<EventRule, GeoFence> geoFence();

    ValueProvider<EventRule, DeviceEventType> deviceEventType();

    ValueProvider<EventRule, String> timeFrame();

    ValueProvider<EventRule, String> dayOfWeek();

    ValueProvider<EventRule, String> course();
}
