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
import java.util.Collections;
import java.util.List;

public class OSRMv4MatchService extends MatchService {
    private static class Matchings extends JavaScriptObject {
        protected Matchings() {
        }

        public final native Matching[] getMatchings() /*-{
            return this.matchings;
        }-*/;
    }

    private static class Matching extends JavaScriptObject {
        protected Matching() {
        }

        public final native double[][] getMatchedPoints() /*-{
            return this.matched_points;
        }-*/;

        public final native int[] getIndices() /*-{
            return this.indices;
        }-*/;

        public final native String getGeometry() /*-{
            return this.geometry;
        }-*/;
    }

    public OSRMv4MatchService(String url) {
        super(url);
    }

    @Override
    public void load(final Track track, final Callback callback) {
        final List<Position> originalPositions = track.getPositions();
        StringBuilder body = new StringBuilder("");
        for (Position position : originalPositions) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append("loc=").append(formatLonLat(position.getLatitude()))
                    .append(',').append(formatLonLat(position.getLongitude()))
                    .append("&t=").append(position.getTime().getTime() / 1000);
        }

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
        builder.setHeader("Content-type", "application/x-www-form-urlencoded");
        try {
            builder.sendRequest(body.toString(), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        Matchings matchings = JsonUtils.safeEval(response.getText());
                        Track snappedTrack = new Track();
                        int lastIndex = 0;
                        for (Matching matching : matchings.getMatchings()) {
                            // add original track segment
                            List<Position> originalTrack = lastIndex < matching.getIndices()[0]
                                    ? Collections.<Position>emptyList()
                                    : originalPositions.subList(lastIndex, matching.getIndices()[0]);
                            // add snapped track segment
                            List<Position> snappedPositions = new ArrayList<>(matching.getIndices().length);
                            for (int i = 0; i < matching.getIndices().length; i++) {
                                int snappedPositionIndex = matching.getIndices()[i];
                                double[] latLon = matching.getMatchedPoints()[i];
                                Position snapped = new Position(originalPositions.get(snappedPositionIndex));
                                snapped.setLatitude(latLon[0]);
                                snapped.setLongitude(latLon[1]);
                                snappedPositions.add(snapped);
                            }
                            EncodedPolyline encodedPolyline = new EncodedPolyline();
                            VectorFeature[] loadedGeometry = encodedPolyline.read(matching.getGeometry());
                            snappedTrack.addSegment(originalTrack, null, track.getStyle());
                            List<LonLat> geometry = new ArrayList<>();
                            for (VectorFeature feature : loadedGeometry) {
                                LineString lineString = LineString.narrowToLineString(feature.getJSObject().getProperty("geometry"));
                                for (int i = 0; i < lineString.getNumberOfComponents(); i++) {
                                    Point point = Point.narrowToPoint(lineString.getComponent(i));
                                    geometry.add(new LonLat(point.getX() / 10, point.getY() / 10));
                                }
                            }
                            snappedTrack.addSegment(snappedPositions, geometry, track.getStyle());
                            lastIndex = matching.getIndices()[matching.getIndices().length - 1] + 1;
                        }
                        if (lastIndex < originalPositions.size()) {
                            snappedTrack.addSegment(originalPositions.subList(lastIndex, originalPositions.size()), null, track.getStyle());
                        }
                        callback.onSuccess(snappedTrack);
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
