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

import org.traccar.web.client.TrackSegment;
import org.traccar.web.shared.model.Position;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Track {

    public List<TrackSegment> segments = new LinkedList<TrackSegment>();

    public Track() {

    }

    public Track(List<Position> positions) {
        segments.clear();
        segments.add(new TrackSegment(positions,new ArchiveStyle()));
    }

    public Track(List<Position> positions, ArchiveStyle style) {
        segments.clear();
        segments.add(new TrackSegment(positions,style));
    }

    public void setStyle(ArchiveStyle style) {
        for (TrackSegment segment : segments) {
            segment.setStyle(style);
        }
    }

    public ArchiveStyle getStyle() {
        return segments.get(0).getStyle();
    }

    public List<Position> getPositions() {
        List<Position> positions = new LinkedList<Position>();
        for (TrackSegment segment : segments) {
            for (Position position : segment.getPositions()) {
                positions.add(position);
            }
        }
        return positions;
    }

    public List<Position> getSortedPositions() {
        List<Position> sortedPositions = new LinkedList<Position>(this.getPositions());
        Collections.sort(sortedPositions, new Comparator<Position>() {
            @Override
            public int compare(Position o1, Position o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        return sortedPositions;
    }

}