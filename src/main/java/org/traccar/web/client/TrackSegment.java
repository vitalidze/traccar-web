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

import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.traccar.web.shared.model.Position;

import java.util.*;

public class TrackSegment {

    private ArchiveStyle style;
    private final List<Position> positions;
    private final VectorFeature[] geometry;

    public TrackSegment(List<Position> positions, VectorFeature[] geometry, ArchiveStyle style) {
        this.positions = positions;
        this.geometry = geometry;
        this.style = style;
    }

    public List<Position> getPositions() {
        return this.positions;
    }

    public VectorFeature[] getGeometry() {
        return geometry;
    }

    public void setStyle(ArchiveStyle style) {
        this.style = style;
    }

    public ArchiveStyle getStyle() {
        return this.style;
    }
}