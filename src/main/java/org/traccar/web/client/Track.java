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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track {

    private List<TrackSegment> segments = new LinkedList<>();

    public Track() {
    }

    public Track(List<Position> positions) {
        segments.add(new TrackSegment(positions, null, new ArchiveStyle()));
    }

    public Track(List<Position> positions, ArchiveStyle style) {
        segments.add(new TrackSegment(positions, null, style));
    }

    public void setStyle(ArchiveStyle style) {
        for (TrackSegment segment : segments) {
            segment.setStyle(style);
        }
    }

    public void addSegment(List<Position> positions, VectorFeature[] geometry, ArchiveStyle style) {
        if (positions.size() > 0) {
            segments.add(new TrackSegment(positions, geometry, style));
        }
    }

    public ArchiveStyle getStyle() {
        if (segments.size() > 0)
            return segments.get(0).getStyle();
        else
            return new ArchiveStyle();
    }

    public List<Position> getPositions() {
        if (segments.isEmpty()) {
            return Collections.emptyList();
        } else if (segments.size() == 1) {
            return segments.get(0).getPositions();
        } else {
            List<Position> positions = new LinkedList<>();
            for (TrackSegment segment : segments) {
                for (Position position : segment.getPositions()) {
                    positions.add(position);
                }
            }
            return positions;
        }
    }

    public List<TrackSegment> getSegments() {
        return segments;
    }

    public List<Position> getTimePositions(long timePrintInterval) {
        List<Position> withTime = new LinkedList<>();
        long prevTime = -1;
        for (Position position : getPositions()) {
            if (prevTime < 0 ||
                    (position.getTime().getTime() - prevTime >= timePrintInterval * 60 * 1000)) {
                withTime.add(position);
                prevTime = position.getTime().getTime();
            }
        }
        return withTime;
    }
}