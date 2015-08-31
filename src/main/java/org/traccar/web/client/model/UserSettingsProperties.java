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

import org.traccar.web.shared.model.UserSettings;
import org.traccar.web.shared.model.UserSettings.SpeedUnit;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface UserSettingsProperties extends PropertyAccess<UserSettings> {

    ModelKeyProvider<UserSettings> id();

    ValueProvider<UserSettings, UserSettings.SpeedUnit> speedUnit();

    public static class SpeedUnitLabelProvider implements LabelProvider<UserSettings.SpeedUnit> {

        @Override
        public String getLabel(SpeedUnit item) {
            switch (item) {
            case kilometersPerHour:
                return "km/h";
            case knots:
                return "knots";
            case milesPerHour:
                return "mph";
            default:
                return null;
            }
        }

    }

    public static class MapTypeLabelProvider implements LabelProvider<UserSettings.MapType> {
        @Override
        public String getLabel(UserSettings.MapType item) {
            return item.getName();
        }
    }
}
