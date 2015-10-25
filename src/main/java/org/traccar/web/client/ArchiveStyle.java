/*
 * Copyright 2015 Antonio Fernandes (antoniopaisfernandes@gmail.com)
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
package org.traccar.web.client;

import org.traccar.web.shared.model.PositionIconType;

public class ArchiveStyle {

    public static final String[] COLORS = new String[]{
            "0000ff",
            "00b6ff",
            "27ff00",
            "ff7f17",
            "ff0000"
    };
    public static final String DEFAULT_COLOR = COLORS[0];

    private String trackColor = DEFAULT_COLOR;
    private PositionIconType iconType = null;
    private boolean zoomToTrack = false;

    public ArchiveStyle() {
    }

    public ArchiveStyle(ArchiveStyle other) {
        this.trackColor = other.trackColor;
        this.iconType = other.iconType;
        this.zoomToTrack = other.zoomToTrack;
    }

    public void setTrackColor(String color) {
        trackColor = color;
    }

    public String getTrackColor() {
        return trackColor;
    }

    public void setIconType(PositionIconType type) {
        iconType = type;
    }

    public PositionIconType getIconType() {
        return iconType;
    }

    public void setZoomToTrack(boolean zoomToTrack) {
        this.zoomToTrack = zoomToTrack;
    }

    public boolean getZoomToTrack() {
        return zoomToTrack;
    }
}
