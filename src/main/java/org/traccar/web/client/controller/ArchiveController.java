/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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
package org.traccar.web.client.controller;

import java.util.*;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.*;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.format.EncodedPolyline;
import org.traccar.web.client.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.ArchiveView;
import org.traccar.web.client.view.FilterDialog;
import org.traccar.web.client.view.UserSettingsDialog;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.traccar.web.shared.model.PositionIconType;
import org.traccar.web.shared.model.UserSettings;

public class ArchiveController implements ContentController, ArchiveView.ArchiveHandler {

    public interface ArchiveHandler {
        void onSelected(Position position);
        void onClear(Device device);
        void onDrawTrack(Track track);
    }

    private final ArchiveHandler archiveHandler;

    private final UserSettingsDialog.UserSettingsHandler userSettingsHandler;

    private final ArchiveView archiveView;

    private final Messages i18n = GWT.create(Messages.class);

    private boolean snapToRoads;
    private final Map<Long, Track> originalTracks;
    private final Map<Long, Track> snappedTracks;
    private final ListStore<Device> deviceStore;

    public ArchiveController(ArchiveHandler archiveHandler,
                             UserSettingsDialog.UserSettingsHandler userSettingsHandler,
                             ListStore<Device> deviceStore) {
        this.archiveHandler = archiveHandler;
        this.userSettingsHandler = userSettingsHandler;
        this.archiveView = new ArchiveView(this, deviceStore);
        this.originalTracks = new HashMap<>();
        this.snappedTracks = new HashMap<>();
        this.deviceStore = deviceStore;
    }

    @Override
    public ContentPanel getView() {
        return archiveView.getView();
    }

    @Override
    public void run() {
    }

    @Override
    public void onSelected(Position position) {
        archiveHandler.onSelected(position);
    }

    @Override
    public void onLoad(final Device device, Date from, Date to, boolean filter, final ArchiveStyle style) {
        if (device != null && from != null && to != null) {
            final AutoProgressMessageBox progress = new AutoProgressMessageBox(i18n.archive(), i18n.loadingData());
            progress.auto();
            progress.show();
            Application.getDataService().getPositions(device, from, to, filter, new BaseAsyncCallback<List<Position>>(i18n) {
                @Override
                public void onSuccess(List<Position> result) {
                    archiveHandler.onClear(device);
                    if (result.isEmpty()) {
                        progress.hide();
                        new AlertMessageBox(i18n.error(), i18n.errNoResults()).show();
                    }
                    originalTracks.put(device.getId(), new Track(result, style));
                    snappedTracks.remove(device.getId());
                    if (snapToRoads) {
                        loadSnappedPointsAndShowTrack(device);
                    } else {
                        showArchive(device);
                    }
                    progress.hide();
                }

                @Override
                public void onFailure(Throwable caught) {
                    progress.hide();
                    super.onFailure(caught);
                }
            });
        } else {
            new AlertMessageBox(i18n.error(), i18n.errFillFields()).show();
        }
    }

    private void showArchive(Device device) {
        archiveHandler.onClear(device);
        Track track = snapToRoads ? snappedTracks.get(device.getId()) : originalTracks.get(device.getId());
        archiveHandler.onDrawTrack(track);
        archiveView.showPositions(device, track.getPositions());
    }

    @Override
    public void onSnapToRoads(boolean snapToRoads) {
        this.snapToRoads = snapToRoads;
        for (Map.Entry<Long, Track> entry : originalTracks.entrySet()) {
            Long deviceId = entry.getKey();
            Device device = deviceStore.findModelWithKey(deviceId.toString());
            Track snappedTrack = snappedTracks.get(deviceId);
            if (snapToRoads && snappedTrack == null) {
                loadSnappedPointsAndShowTrack(device);
            } else {
                showArchive(device);
            }
        }
    }

    @Override
    public void onClear(Device device) {
        originalTracks.remove(device.getId());
        snappedTracks.remove(device.getId());
        archiveHandler.onClear(device);
    }

    @Override
    public void onFilterSettings() {
        new FilterDialog(ApplicationContext.getInstance().getUserSettings(), userSettingsHandler).show();
    }

    @Override
    public void onChangeArchiveMarkerType(PositionIconType newMarkerType) {
        UserSettings settings = ApplicationContext.getInstance().getUserSettings();
        settings.setArchiveMarkerType(newMarkerType);
        userSettingsHandler.onSave(settings);
    }

    public void selectPosition(Position position) {
        archiveView.selectPosition(position);
    }

    public void selectDevice(Device device) {
        archiveView.selectDevice(device);
    }

    public static class Matchings extends JavaScriptObject {
        protected Matchings() {
        }

        public final native Matching[] getMatchings() /*-{
            return this.matchings;
        }-*/;
    }

    public static class Matching extends JavaScriptObject {
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

    private void loadSnappedPointsAndShowTrack(final Device device) {
        final Track track = originalTracks.get(device.getId());

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

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "https://router.project-osrm.org/match");
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
                            VectorFeature[] geometry = encodedPolyline.read(matching.getGeometry());
                            snappedTrack.addSegment(originalTrack, null, track.getStyle());
                            snappedTrack.addSegment(snappedPositions, geometry, track.getStyle());
                            lastIndex = matching.getIndices()[matching.getIndices().length - 1] + 1;
                        }
                        if (lastIndex < originalPositions.size()) {
                            snappedTrack.addSegment(originalPositions.subList(lastIndex, originalPositions.size()), null, track.getStyle());
                        }
                        snappedTracks.put(device.getId(), snappedTrack);
                        showArchive(device);
                    } else {
                        GWT.log("Incorrect response code: " + response.getStatusCode());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    GWT.log("Request error", exception);
                }
            });
        } catch (RequestException re) {
            GWT.log("Request failed", re);
        }
    }

    static native String formatLonLat(double lonLat) /*-{
        return lonLat.toFixed(6);
    }-*/;
}
