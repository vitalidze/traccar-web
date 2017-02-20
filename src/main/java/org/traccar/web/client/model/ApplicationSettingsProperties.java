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

import org.traccar.web.shared.model.*;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface ApplicationSettingsProperties extends PropertyAccess<ApplicationSettings> {

    ModelKeyProvider<ApplicationSettings> id();

    ValueProvider<ApplicationSettings, Boolean> registrationEnabled();

    ValueProvider<ApplicationSettings, Short> updateInterval();

    ValueProvider<ApplicationSettings, Integer> notificationExpirationPeriod();

    ValueProvider<ApplicationSettings, Boolean> disallowDeviceManagementByUsers();

    ValueProvider<ApplicationSettings, Boolean> eventRecordingEnabled();

    ValueProvider<ApplicationSettings, String> bingMapsKey();

    class PasswordHashMethodLabelProvider implements LabelProvider<PasswordHashMethod> {
        @Override
        public String getLabel(PasswordHashMethod item) {
            return item.getName();
        }
    }

    class MatchServiceTypeLabelProvider implements LabelProvider<MatchServiceType> {
        @Override
        public String getLabel(MatchServiceType item) {
            return item.getName();
        }
    }
}
