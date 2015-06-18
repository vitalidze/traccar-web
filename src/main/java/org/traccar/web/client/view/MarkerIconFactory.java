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
import org.traccar.web.shared.model.PositionIcon;
import org.traccar.web.shared.model.PositionIconType;

import java.util.HashMap;
import java.util.Map;

public class MarkerIconFactory {
    public static Icon getIcon(PositionIcon icon, boolean selected) {
        if (icon == null) {
            return null;
        }
        Size size = new Size(selected ? icon.getSelectedWidth() : icon.getWidth(),
                             selected ? icon.getSelectedHeight() : icon.getHeight());
        Pixel offset = new Pixel(-size.getWidth() / 2f, -size.getHeight());
        return new Icon(selected ? icon.getSelectedURL() : icon.getURL(), size, offset);
    }
}
