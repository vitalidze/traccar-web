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

public enum PositionIconType {
    iconLatest("marker-green.png", true, "marker.png", true),
    iconOffline("marker-green.png", true, "img/marker-white.png", false),
    iconArchive("marker-gold.png", true, "marker-blue.png", true);

    private final String selectedURL;
    private final String notSelectedURL;

    PositionIconType(String selectedURL, boolean selectedFromCloudFlare,
                     String notSelectedURL, boolean notSelectedFromCloudFlare) {
        this.selectedURL = (selectedFromCloudFlare ? "http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/" : "") + selectedURL;
        this.notSelectedURL = (notSelectedFromCloudFlare ? "http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/" : "") + notSelectedURL;
    }

    public String getURL(boolean selected) {
        return selected ? selectedURL : notSelectedURL;
    }
}
