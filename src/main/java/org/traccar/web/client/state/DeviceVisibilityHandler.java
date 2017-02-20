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
package org.traccar.web.client.state;

import org.traccar.web.shared.model.Device;

public interface DeviceVisibilityHandler extends DeviceVisibilityProvider {
    void setVisible(Device device, boolean b);
    void idle(Device device);
    void moving(Device device);
    void offlineStatusChanged(Device device, boolean offline);
    void updated(Device device);
    void addVisibilityChangeHandler(DeviceVisibilityChangeHandler visibilityChangeHandler);

    boolean getHideOnline();
    void setHideOnline(boolean hideOnline);

    boolean getHideOffline();
    void setHideOffline(boolean hideOffline);

    boolean getHideIdle();
    void setHideIdle(boolean hideIdle);

    boolean getHideMoving();
    void setHideMoving(boolean hideMoving);

    boolean isHiddenGroup(Long groupId);
    void addHiddenGroup(Long groupId);
    void removeHiddenGroup(Long groupId);
}
