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
package org.traccar.web.client.model;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.*;

public interface EventRuleProperties extends PropertyAccess<EventRule> {
    ModelKeyProvider<EventRule> id();

    ValueProvider<EventRule, User> user();

    ValueProvider<EventRule, Device> device();

    ValueProvider<EventRule, GeoFence> geoFence();

    ValueProvider<EventRule, DeviceEventType> deviceEventType();

    ValueProvider<EventRule, String> timeFrame();

    ValueProvider<EventRule, Long> timeZoneShift();

    ValueProvider<EventRule, String> course();

}
