/*
 * Copyright 2016 Vitaly Litvak (vitavaque@gmail.com)
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.format.EncodedPolyline;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.traccar.web.shared.model.LonLat;
import org.traccar.web.shared.model.Position;

import java.util.ArrayList;
import java.util.List;

public class OSRMv5MatchService extends MatchService {
    private static class Result extends JavaScriptObject {
        protected Result() {
        }

        public final native String getCode() /*-{
            return this.code;
        }-*/;

        public final native Matching[] getMatchings() /*-{
            return this.matchings;
        }-*/;

        public final native TracePoint[] getTracePoints() /*-{
            return this.tracepoints;
        }-*/;
    }

    private static class Matching extends JavaScriptObject {
        protected Matching() {
        }

        public final native String getGeometry() /*-{
            return this.geometry;
        }-*/;
    }

    private static class TracePoint extends JavaScriptObject {
        protected TracePoint() {
        }

        public final native int getWayPointIndex() /*-{
            return this.waypoint_index;
        }-*/;

        public final native int getMatchingsIndex() /*-{
            return this.matchings_index;
        }-*/;

        public final native double[] getLocation() /*-{
            return this.location;
        }-*/;
    }

    public OSRMv5MatchService(String url) {
        super(url);
    }

    @Override
    public void load(final Track track, final Callback callback) {
        runQuery(track.getPositions(), 0, new Track(), track.getStyle(), callback);
    }

    private void runQuery(final List<Position> originalPositions,
                          final int startIndex,
                          final Track snappedTrack,
                          final ArchiveStyle style,
                          final Callback callback) {
        StringBuilder url = new StringBuilder(this.url);
        if (url.charAt(url.length() - 1) != '/') {
            url.append('/');
        }
        StringBuilder timestamps = new StringBuilder();
        boolean first = true;
        int i = startIndex;
        while (url.length() + timestamps.length() < 1950 && i < originalPositions.size()) {
            if (first) {
                first = false;
            } else {
                url.append(';');
                timestamps.append(';');
            }
            Position position = originalPositions.get(i);
            url.append(formatLonLat(position.getLongitude()))
                    .append(',')
                    .append(formatLonLat(position.getLatitude()));
            timestamps.append(position.getTime().getTime() / 1000);
            i++;
        }

        final int nextBatchIndex = i;

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url
                .append("?timestamps=").append(timestamps).toString());
        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        Result result = JsonUtils.safeEval(response.getText());

                        if (!result.getCode().equalsIgnoreCase("Ok")) {
                            callback.onError(-1, result.getCode());
                            return;
                        }

                        int lastIndex = startIndex;

                        for (int matchingIndex = 0; matchingIndex < result.getMatchings().length; matchingIndex++) {
                            Matching matching = result.getMatchings()[matchingIndex];

                            List<Position> originalTrack = new ArrayList<>();
                            List<Position> snappedPositions = new ArrayList<>();
                            for (int pointIndex = lastIndex; pointIndex < result.getTracePoints().length; pointIndex++) {
                                TracePoint point = result.getTracePoints()[pointIndex];
                                if (point == null) {
                                    if (snappedPositions.isEmpty()) {
                                        originalTrack.add(originalPositions.get(pointIndex));
                                    } else {
                                        break;
                                    }
                                } else {
                                    if (point.getMatchingsIndex() == matchingIndex) {
                                        double[] latLon = point.getLocation();
                                        Position snapped = new Position(originalPositions.get(point.getWayPointIndex()));
                                        snapped.setLatitude(latLon[1]);
                                        snapped.setLongitude(latLon[0]);
                                        snappedPositions.add(snapped);
                                    } else {
                                        break;
                                    }
                                }
                            }
                            lastIndex += originalTrack.size() + snappedPositions.size();
                            // decode geometry
                            EncodedPolyline encodedPolyline = new EncodedPolyline();
                            VectorFeature[] loadedGeometries = encodedPolyline.read(matching.getGeometry());
                            List<LonLat> geometry = new ArrayList<>();
                            for (VectorFeature feature : loadedGeometries) {
                                LineString lineString = LineString.narrowToLineString(feature.getJSObject().getProperty("geometry"));
                                for (int i = 0; i < lineString.getNumberOfComponents(); i++) {
                                    Point point = Point.narrowToPoint(lineString.getComponent(i));
                                    geometry.add(new LonLat(point.getX(), point.getY()));
                                }
                            }
                            snappedTrack.addSegment(originalTrack, null, style);
                            snappedTrack.addSegment(snappedPositions, geometry, style);
                        }
                        if (lastIndex < nextBatchIndex) {
                            snappedTrack.addSegment(originalPositions.subList(lastIndex, nextBatchIndex), null, style);
                        }

                        if (nextBatchIndex < originalPositions.size()) {
                            runQuery(originalPositions, nextBatchIndex, snappedTrack, style, callback);
                        } else {
                            callback.onSuccess(snappedTrack);
                        }
                    } else {
                        callback.onError(response.getStatusCode(), response.getText());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    callback.onError(-1, exception.getLocalizedMessage());
                }
            });
        } catch (RequestException re) {
            GWT.log("Request failed", re);
        }
    }
}
