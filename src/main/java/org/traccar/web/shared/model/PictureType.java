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

public enum PictureType {
    MARKER(40 * 1024, 256, 256),
    DEVICE_PHOTO(300 * 1024, 1024, 768),
    SENSOR_INTERVAL(40 * 1024, 128, 128);

    final int maxFileSize;
    final int maxWidth;
    final int maxHeight;

    PictureType(int maxFileSize, int maxWidth, int maxHeight) {
        this.maxFileSize = maxFileSize;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }
}
