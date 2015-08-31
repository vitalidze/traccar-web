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

import org.traccar.web.shared.model.*;

public abstract class MarkerIcon {
    abstract String getKey();

    abstract String getDefaultURL();
    abstract int getDefaultWidth();
    abstract int getDefaultHeight();

    abstract String getSelectedURL();
    int getSelectedWidth() {
        return getDefaultWidth();
    }
    int getSelectedHeight() {
        return getDefaultHeight();
    }

    abstract String getOfflineURL();
    int getOfflineWidth() {
        return getDefaultWidth();
    }
    int getOfflineHeight() {
        return getDefaultHeight();
    }

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
        int getDefaultWidth() {
            return icon.getPositionIconType(Position.Status.LATEST).getWidth();
        }

        @Override
        int getDefaultHeight() {
            return icon.getPositionIconType(Position.Status.LATEST).getHeight();
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
        int getDefaultWidth() {
            return getWidth(icon.getDefaultIcon());
        }

        @Override
        int getDefaultHeight() {
            return getHeight(icon.getDefaultIcon());
        }

        @Override
        String getSelectedURL() {
            return icon.selectedURL();
        }

        @Override
        int getSelectedWidth() {
            return getWidth(icon.getSelectedIcon());
        }

        @Override
        int getSelectedHeight() {
            return getHeight(icon.getSelectedIcon());
        }

        @Override
        String getOfflineURL() {
            return icon.offlineURL();
        }

        @Override
        int getOfflineWidth() {
            return getWidth(icon.getOfflineIcon());
        }

        @Override
        int getOfflineHeight() {
            return getHeight(icon.getOfflineIcon());
        }

        @Override
        DeviceIcon getDatabaseIcon() {
            return icon;
        }

        private int getWidth(Picture pic) {
            return pic == null ? 0 : pic.getWidth();
        }

        private int getHeight(Picture pic) {
            return pic == null ? 0 : pic.getHeight();
        }
    }

    public static MarkerIcon create(Device device) {
        if (device.getIconType() == null) {
            return new MarkerIcon.Database(device.getIcon());
        } else {
            return new MarkerIcon.BuiltIn(device.getIconType());
        }
    }

    public static PositionIcon create(Position position) {
        MarkerIcon deviceIcon = create(position.getDevice());
        String url = position.getStatus() == Position.Status.OFFLINE ? deviceIcon.getOfflineURL() : deviceIcon.getDefaultURL();
        int width = position.getStatus() == Position.Status.OFFLINE ? deviceIcon.getOfflineWidth() : deviceIcon.getDefaultWidth();
        int height = position.getStatus() == Position.Status.OFFLINE ? deviceIcon.getOfflineHeight() : deviceIcon.getDefaultHeight();
        return new PositionIcon(url, width, height,
                deviceIcon.getSelectedURL(), deviceIcon.getSelectedWidth(), deviceIcon.getSelectedHeight());
    }
}
