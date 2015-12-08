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
import org.traccar.web.client.state.DeviceVisibilityState;
import org.traccar.web.client.state.StateAutoBeanFactory;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.shared.model.Device;

import java.util.*;

public class VisibilityController implements ContentController, DeviceView.DeviceVisibilityHandler {
    public static final String STATE_KEY_DEVICE_VISIBILITY = "deviceVisibility";

    private final Messages i18n = GWT.create(Messages.class);
    private final UIStateServiceAsync service = GWT.create(UIStateService.class);
    private final StateAutoBeanFactory factory = GWT.create(StateAutoBeanFactory.class);
    private final LinkedList<DeviceVisibilityChangeHandler> visibilityChangeHandlers = new LinkedList<>();
    private DeviceVisibilityState deviceVisibility;
    private Map<Long, Boolean> cachedVisibility = new HashMap<>();

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

    private void loadedDeviceVisibility(DeviceVisibilityState deviceVisibility) {
        this.deviceVisibility = deviceVisibility;
        if (deviceVisibility.getHiddenForced() == null) {
            deviceVisibility.setHiddenForced(new HashSet<Long>());
        }
        if (deviceVisibility.getVisibleForced() == null) {
            deviceVisibility.setVisibleForced(new HashSet<Long>());
        }
        for (Map.Entry<Long, Boolean> entry : cachedVisibility.entrySet()) {
            Long deviceId = entry.getKey();
            boolean calculated = calculateVisibility(deviceId);
            if (entry.getValue() != calculated) {
                entry.setValue(calculated);
                fireChange(deviceId, calculated);
            }
        }
    }

    @Override
    public boolean isVisible(Device device) {
        Long deviceId = device.getId();
        Boolean visibility = cachedVisibility.get(deviceId);
        if (visibility == null) {
            visibility = calculateVisibility(deviceId);
            cachedVisibility.put(deviceId, visibility);
        }
        return visibility;
    }

    private boolean calculateVisibility(Long deviceId) {
        return deviceVisibility != null
                && (deviceVisibility.getVisibleForced().contains(deviceId)
                || !deviceVisibility.getHiddenForced().contains(deviceId));
    }

    @Override
    public void setVisible(Device device, boolean b) {
        (b ? deviceVisibility.getHiddenForced() : deviceVisibility.getVisibleForced()).remove(device.getId());
        (b ? deviceVisibility.getVisibleForced() : deviceVisibility.getHiddenForced()).add(device.getId());
        service.setValue(STATE_KEY_DEVICE_VISIBILITY,
                AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(deviceVisibility)).getPayload(),
                new BaseAsyncCallback<Void>(i18n));
        Boolean previous = cachedVisibility.get(device.getId());
        if (previous == null || previous != b) {
            cachedVisibility.put(device.getId(), b);
            fireChange(device.getId(), b);
        }
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
}
