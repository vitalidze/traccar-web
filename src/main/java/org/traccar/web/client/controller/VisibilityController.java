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
package org.traccar.web.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.UIStateService;
import org.traccar.web.client.model.UIStateServiceAsync;
import org.traccar.web.client.state.DeviceVisibilityChangeHandler;
import org.traccar.web.client.state.DeviceVisibilityHandler;
import org.traccar.web.client.state.DeviceVisibilityState;
import org.traccar.web.client.state.StateAutoBeanFactory;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Group;

import java.util.*;

public class VisibilityController implements ContentController, DeviceVisibilityHandler {
    public static final String STATE_KEY_DEVICE_VISIBILITY = "deviceVisibility";

    private final Messages i18n = GWT.create(Messages.class);
    private final UIStateServiceAsync service = GWT.create(UIStateService.class);
    private final StateAutoBeanFactory factory = GWT.create(StateAutoBeanFactory.class);
    private final LinkedList<DeviceVisibilityChangeHandler> visibilityChangeHandlers = new LinkedList<>();
    private DeviceVisibilityState deviceVisibility;
    private final Map<Long, DeviceState> deviceState = new HashMap<>();

    private static class DeviceState {
        boolean visible;
        boolean idle = true;
        boolean offline = true;
        Long groupId;
    }

    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
        service.getValue(STATE_KEY_DEVICE_VISIBILITY, new BaseAsyncCallback<String>(i18n) {
            @Override
            public void onSuccess(String result) {
                AutoBean<DeviceVisibilityState> deviceVisibility =
                        result == null
                                ? factory.deviceVisibility()
                                : AutoBeanCodex.decode(factory, DeviceVisibilityState.class, result);
                loadedDeviceVisibility(deviceVisibility.as());
            }
        });
    }

    private DeviceState getState(Device device) {
        DeviceState state = deviceState.get(device.getId());
        if (state == null) {
            state = new DeviceState();
            state.groupId = device.getGroup() == null ? null : device.getGroup().getId();
            state.visible = calculateVisibility(device.getId(), state);
            deviceState.put(device.getId(), state);
        }
        return getState(device.getId());
    }

    private DeviceState getState(Long deviceId) {
        return deviceState.get(deviceId);
    }

    private void loadedDeviceVisibility(DeviceVisibilityState deviceVisibility) {
        this.deviceVisibility = deviceVisibility;
        if (deviceVisibility.getHiddenForced() == null) {
            deviceVisibility.setHiddenForced(new HashSet<Long>());
        }
        if (deviceVisibility.getVisibleForced() == null) {
            deviceVisibility.setVisibleForced(new HashSet<Long>());
        }
        if (deviceVisibility.getHiddenGroups() == null) {
            deviceVisibility.setHiddenGroups(new HashSet<Long>());
        }
        updateVisibilityOfAllDevices();
    }

    @Override
    public boolean isVisible(Device device) {
        return getState(device).visible;
    }

    private boolean calculateVisibility(Long deviceId, DeviceState state) {
        if (deviceVisibility == null) {
            return false;
        }

        if (deviceVisibility.getVisibleForced().contains(deviceId)) {
            return true;
        }

        if (deviceVisibility.getHiddenForced().contains(deviceId)) {
            return false;
        }

        return state != null
                && (!state.idle || !deviceVisibility.getHideIdle())
                && (state.idle || !deviceVisibility.getHideMoving())
                && (!state.offline || !deviceVisibility.getHideOffline())
                && (state.offline || !deviceVisibility.getHideOnline())
                && (state.groupId == null || !deviceVisibility.getHiddenGroups().contains(state.groupId));
    }

    @Override
    public void setVisible(Device device, boolean b) {
        (b ? deviceVisibility.getHiddenForced() : deviceVisibility.getVisibleForced()).remove(device.getId());
        (b ? deviceVisibility.getVisibleForced() : deviceVisibility.getHiddenForced()).add(device.getId());
        saveDeviceVisibility();
        DeviceState state = getState(device);
        if (state.visible != b) {
            state.visible = b;
            fireChange(device.getId(), b);
        }
    }

    private void saveDeviceVisibility() {
        service.setValue(STATE_KEY_DEVICE_VISIBILITY,
                AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(deviceVisibility)).getPayload(),
                new BaseAsyncCallback<Void>(i18n));
    }

    @Override
    public void addVisibilityChangeHandler(DeviceVisibilityChangeHandler visibilityChangeHandler) {
        visibilityChangeHandlers.add(visibilityChangeHandler);
    }

    private void fireChange(Long deviceId, boolean visibility) {
        for (Iterator<DeviceVisibilityChangeHandler> it = visibilityChangeHandlers.descendingIterator(); it.hasNext(); ) {
            it.next().visibilityChanged(deviceId, visibility);
        }
    }

    @Override
    public void idle(Device device) {
        if (!getState(device).idle) {
            getState(device).idle = true;
            updateVisibility(device);
        }
    }

    @Override
    public void moving(Device device) {
        if (getState(device).idle) {
            getState(device).idle = false;
            updateVisibility(device);
        }
    }

    @Override
    public void offlineStatusChanged(Device device, boolean offline) {
        if (getState(device).offline != offline) {
            getState(device).offline = offline;
            updateVisibility(device);
        }
    }

    @Override
    public void updated(Device device) {
        Long newGroupId = device.getGroup() == null ? null : device.getGroup().getId();
        if (!Objects.equals(newGroupId, getState(device).groupId)) {
            getState(device).groupId = newGroupId;
            updateVisibility(device);
        }
    }

    private void updateVisibility(Device device) {
        updateVisibility(device.getId());
    }

    private void updateVisibility(Long deviceId) {
        DeviceState state = getState(deviceId);
        boolean calculated = calculateVisibility(deviceId, state);
        if (state.visible != calculated) {
            state.visible = calculated;
            fireChange(deviceId, calculated);
        }
    }

    private void updateVisibilityOfAllDevices() {
        for (Long deviceId : deviceState.keySet()) {
            updateVisibility(deviceId);
        }
    }

    @Override
    public boolean getHideOnline() {
        return deviceVisibility.getHideOnline();
    }

    @Override
    public void setHideOnline(boolean hideOnline) {
        deviceVisibility.setHideOnline(hideOnline);
        clearForcedVisibility();
        saveDeviceVisibility();
        updateVisibilityOfAllDevices();
    }

    @Override
    public boolean getHideOffline() {
        return deviceVisibility.getHideOffline();
    }

    @Override
    public void setHideOffline(boolean hideOffline) {
        deviceVisibility.setHideOffline(hideOffline);
        clearForcedVisibility();
        saveDeviceVisibility();
        updateVisibilityOfAllDevices();
    }

    @Override
    public boolean getHideIdle() {
        return deviceVisibility.getHideIdle();
    }

    @Override
    public void setHideIdle(boolean hideIdle) {
        deviceVisibility.setHideIdle(hideIdle);
        clearForcedVisibility();
        saveDeviceVisibility();
        updateVisibilityOfAllDevices();
    }

    @Override
    public boolean getHideMoving() {
        return deviceVisibility.getHideMoving();
    }

    @Override
    public void setHideMoving(boolean hideMoving) {
        deviceVisibility.setHideMoving(hideMoving);
        clearForcedVisibility();
        saveDeviceVisibility();
        updateVisibilityOfAllDevices();
    }

    @Override
    public boolean isHiddenGroup(Long groupId) {
        return deviceVisibility.getHiddenGroups().contains(groupId);
    }

    @Override
    public void addHiddenGroup(Long groupId) {
        deviceVisibility.getHiddenGroups().add(groupId);
        clearForcedVisibility();
        saveDeviceVisibility();
        updateVisibilityOfAllDevices();
    }

    @Override
    public void removeHiddenGroup(Long groupId) {
        deviceVisibility.getHiddenGroups().remove(groupId);
        clearForcedVisibility();
        saveDeviceVisibility();
        updateVisibilityOfAllDevices();
    }

    private void clearForcedVisibility() {
        deviceVisibility.getVisibleForced().clear();
        deviceVisibility.getHiddenForced().clear();
    }
}
