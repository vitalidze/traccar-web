/*
 * Copyright 2016 Vitaly Litvak (vitavaque@gmail.com)
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

import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.GroupedDevice;

public class DeviceStore extends TreeStore<GroupedDevice> {
    public DeviceStore() {
        super(new ModelKeyProvider<GroupedDevice>() {
            @Override
            public String getKey(GroupedDevice item) {
                return (item.getClass() == Group.class ? "g" : "d") + item.getId();
            }
        });
    }

    public Device getDevice(GroupedDevice node) {
        return isDevice(node) ? (Device) node : null;
    }

    public boolean isDevice(GroupedDevice item) {
        return item.getClass() == Device.class;
    }

    public boolean isGroup(GroupedDevice item) {
        return item.getClass() == Group.class;
    }
}
