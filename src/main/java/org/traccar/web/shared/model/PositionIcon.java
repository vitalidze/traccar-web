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
package org.traccar.web.shared.model;

public class PositionIcon {
    final String URL;
    final int width;
    final int height;

    final String selectedURL;
    final int selectedWidth;
    final int selectedHeight;

    public PositionIcon(String url,
                        int width,
                        int height,
                        String selectedURL,
                        int selectedWidth,
                        int selectedHeight) {
        URL = url;
        this.width = width;
        this.height = height;
        this.selectedURL = selectedURL;
        this.selectedWidth = selectedWidth;
        this.selectedHeight = selectedHeight;
    }

    public PositionIcon(PositionIconType iconType) {
        this(iconType.getURL(true), iconType.getWidth(), iconType.getHeight(),
             iconType.getURL(false), iconType.getWidth(), iconType.getHeight());
    }

    public String getURL() {
        return URL;
    }

    public String getSelectedURL() {
        return selectedURL;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSelectedWidth() {
        return selectedWidth;
    }

    public int getSelectedHeight() {
        return selectedHeight;
    }
}
