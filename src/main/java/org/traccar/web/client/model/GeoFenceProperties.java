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
package org.traccar.web.client.model;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.GeoFenceType;

public interface GeoFenceProperties extends PropertyAccess<GeoFence> {
    ModelKeyProvider<GeoFence> id();

    ValueProvider<GeoFence, String> name();

    ValueProvider<GeoFence, String> description();

    ValueProvider<GeoFence, String> color();

    ValueProvider<GeoFence, Float> radius();

    ValueProvider<GeoFence, GeoFenceType> type();

    public static class GeoFenceTypeLabelProvider implements LabelProvider<GeoFenceType> {
        final Messages i18n = GWT.create(Messages.class);

        @Override
        public String getLabel(GeoFenceType item) {
            return i18n.geoFenceType(item);
        }
    }
}
