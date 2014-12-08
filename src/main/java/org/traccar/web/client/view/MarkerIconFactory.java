/*
 * Copyright 2014 Anton Tananaev (anton.tananaev@gmail.com), Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client.view;

import org.gwtopenmaps.openlayers.client.Icon;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.Size;
import org.traccar.web.shared.model.PositionIconType;

import java.util.HashMap;
import java.util.Map;

public class MarkerIconFactory {
    private static final Map<PositionIconType, Size> sizes = new HashMap<PositionIconType, Size>();
    private static final Map<PositionIconType, Pixel> offsets = new HashMap<PositionIconType, Pixel>();

    public static Icon getIcon(PositionIconType type, boolean selected) {
        Size size = sizes.get(type);
        if (size == null) {
            size = new Size(type.getWidth(), type.getHeight());
            sizes.put(type, size);
        }
        Pixel offset = offsets.get(type);
        if (offset == null) {
            offset = new Pixel(-type.getWidth() / 2f, -type.getHeight());
            offsets.put(type, offset);
        }
        return type == null ? null : new Icon(type.getURL(selected), size, offset);
    }
}
