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

import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.GeoFenceWindow;
import org.traccar.web.shared.model.GeoFence;

public class GeoFenceController implements DeviceView.GeoFenceHandler {
    @Override
    public void onAdd() {
        new GeoFenceWindow(new GeoFence(), new GeoFenceWindow.GeoFenceHandler() {
            @Override
            public void onSave(GeoFence device) {
            }
        }).show();
    }
}
