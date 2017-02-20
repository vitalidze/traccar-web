/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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

import java.util.Set;

public interface DeviceVisibilityState {
    boolean getHideOnline();
    void setHideOnline(boolean hideOnline);

    boolean getHideOffline();
    void setHideOffline(boolean hideOffline);

    boolean getHideIdle();
    void setHideIdle(boolean hideIdle);

    boolean getHideMoving();
    void setHideMoving(boolean hideMoving);

    Set<Long> getHiddenForced();
    void setHiddenForced(Set<Long> hiddenForced);

    Set<Long> getVisibleForced();
    void setVisibleForced(Set<Long> visibleForced);

    Set<Long> getHiddenGroups();
    void setHiddenGroups(Set<Long> hiddenGroups);
}
