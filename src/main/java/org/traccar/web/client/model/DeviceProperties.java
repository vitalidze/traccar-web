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

import org.traccar.web.shared.model.Device;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface DeviceProperties extends PropertyAccess<Device> {

    ModelKeyProvider<Device> id();

    ValueProvider<Device, String> uniqueId();

    ValueProvider<Device, String> name();

    ValueProvider<Device, String> description();

    ValueProvider<Device, String> phoneNumber();

    ValueProvider<Device, String> plateNumber();

    ValueProvider<Device, String> vehicleInfo();

    ValueProvider<Device, Integer> timeout();

    ValueProvider<Device, Double> idleSpeedThreshold();

    ValueProvider<Device, String> iconArrowMovingColor();
    ValueProvider<Device, String> iconArrowPausedColor();
    ValueProvider<Device, String> iconArrowStoppedColor();
    ValueProvider<Device, String> iconArrowOfflineColor();

    @Path("name")
    LabelProvider<Device> label();

    ValueProvider<Device, Double> odometer();

    ValueProvider<Device, Boolean> autoUpdateOdometer();

    ValueProvider<Device, Boolean> sendNotifications();

    ValueProvider<Device, Long> ownerId();

}
