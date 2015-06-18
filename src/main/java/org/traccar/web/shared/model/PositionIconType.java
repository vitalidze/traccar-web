/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum PositionIconType implements IsSerializable {
    iconLatest("marker-green.png", true, "marker.png", true, 21, 25),
    iconOffline("marker-green.png", true, "marker-white.png", false, 21, 25),
    iconArchive("marker-gold.png", true, "marker-blue.png", true, 21, 25),

    sedanLatest("sedan-green.png", "sedan-red.png", 53, 20),
    sedanOffline("sedan-green.png", "sedan-white.png", 53, 20),

    universalLatest("universal-green.png", "universal-red.png", 48, 20),
    universalOffline("universal-green.png", "universal-white.png", 48, 20),

    minivanLatest("minivan-green.png", "minivan-red.png", 36, 20),
    minivanOffline("minivan-green.png", "minivan-white.png", 36, 20),

    bicycleLatest("bicycle-green.png", "bicycle-red.png", 41, 25),
    bicycleOffline("bicycle-green.png", "bicycle-gray.png", 41, 25),

    busLatest("bus-green.png", "bus-red.png", 42, 20),
    busOffline("bus-green.png", "bus-white.png", 42, 20),

    carTruckLatest("car-truck-green.png", "car-truck-red.png", 63, 25),
    carTruckOffline("car-truck-green.png", "car-truck-white.png", 63, 25),

    longTruckLatest("long-truck-green.png", "long-truck-red.png", 63, 25),
    longTruckOffline("long-truck-green.png", "long-truck-white.png", 63, 25),

    planeLatest("plane-green.png", "plane-red.png", 55, 25),
    planeOffline("plane-green.png", "plane-white.png", 55, 25),

    shipLatest("ship-green.png", "ship-red.png", 62, 25),
    shipOffline("ship-green.png", "ship-white.png", 62, 25),

    trainLatest("train-green.png", "train-red.png", 86, 25),
    trainOffline("train-green.png", "train-white.png", 86, 25),

    truckLatest("truck-green.png", "truck-red.png", 30, 20),
    truckOffline("truck-green.png", "truck-white.png", 30, 20),

    phoneLatest("phone-green.png", "phone-red.png", 12, 20),
    phoneOffline("phone-green.png", "phone-white.png", 12, 20),

    dotArchive("dot-orange.png", "dot-orange.png", 13, 14);

    private final String selectedURL;
    private final String notSelectedURL;
    private final int width;
    private final int height;

    PositionIconType(String selectedURL, String notSelectedURL, int width, int height) {
        this(selectedURL, false, notSelectedURL, false, width, height);
    }

    PositionIconType(String selectedURL, boolean selectedFromCloudFlare,
                     String notSelectedURL, boolean notSelectedFromCloudFlare,
                     int width, int height) {
        this.selectedURL = (selectedFromCloudFlare ? "http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/" : "/img/") + selectedURL;
        this.notSelectedURL = (notSelectedFromCloudFlare ? "http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/" : "/img/") + notSelectedURL;
        this.width = width;
        this.height = height;
    }

    public String getURL(boolean selected) {
        return selected ? selectedURL : notSelectedURL;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
