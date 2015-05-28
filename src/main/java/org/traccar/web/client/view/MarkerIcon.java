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
package org.traccar.web.client.view;

import org.traccar.web.shared.model.DeviceIcon;
import org.traccar.web.shared.model.DeviceIconType;
import org.traccar.web.shared.model.Position;

public abstract class MarkerIcon {
    abstract String getKey();
    abstract String getDefaultURL();
    abstract String getSelectedURL();
    abstract String getOfflineURL();
    DeviceIconType getBuiltInIcon() {
        return null;
    }
    DeviceIcon getDatabaseIcon() {
        return null;
    }

    static class BuiltIn extends MarkerIcon {
        final DeviceIconType icon;

        BuiltIn(DeviceIconType icon) {
            this.icon = icon;
        }

        @Override
        String getKey() {
            return icon.name();
        }

        @Override
        String getOfflineURL() {
            return icon.getPositionIconType(Position.Status.OFFLINE).getURL(false);
        }

        @Override
        String getDefaultURL() {
            return icon.getPositionIconType(Position.Status.LATEST).getURL(false);
        }

        @Override
        String getSelectedURL() {
            return icon.getPositionIconType(Position.Status.LATEST).getURL(true);
        }

        @Override
        DeviceIconType getBuiltInIcon() {
            return icon;
        }
    }

    static class Database extends MarkerIcon {
        DeviceIcon icon;

        Database(DeviceIcon icon) {
            this.icon = icon;
        }

        @Override
        String getKey() {
            return Long.toString(icon.getId());
        }

        @Override
        String getDefaultURL() {
            return icon.defaultURL();
        }

        @Override
        String getSelectedURL() {
            return icon.selectedURL();
        }

        @Override
        String getOfflineURL() {
            return icon.offlineURL();
        }

        @Override
        DeviceIcon getDatabaseIcon() {
            return icon;
        }
    }
}
