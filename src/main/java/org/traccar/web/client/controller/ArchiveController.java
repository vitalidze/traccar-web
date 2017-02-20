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

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.ArchiveStyle;
import org.traccar.web.client.MatchService;
import org.traccar.web.client.OSRMv4MatchService;
import org.traccar.web.client.OSRMv5MatchService;
import org.traccar.web.client.Track;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.ArchiveView;
import org.traccar.web.client.view.FilterDialog;
import org.traccar.web.client.view.ReportsMenu;
import org.traccar.web.client.view.UserSettingsDialog;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.MatchServiceType;
import org.traccar.web.shared.model.Position;
import org.traccar.web.shared.model.PositionIconType;
import org.traccar.web.shared.model.Report;
import org.traccar.web.shared.model.UserSettings;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                             ListStore<Device> deviceStore,
                             ListStore<Report> reportStore,
                             ReportsMenu.ReportHandler reportHandler) {
        this.archiveHandler = archiveHandler;
        this.userSettingsHandler = userSettingsHandler;
        this.archiveView = new ArchiveView(this, deviceStore, reportStore, reportHandler);
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

    private void loadSnappedPointsAndShowTrack(final Device device) {
        MatchService matchService = getMatchService();
        if (matchService == null) {
            new AlertMessageBox(i18n.error(), i18n.errSnapToRoads(-1, "Match service implementation cannot be found"));
        } else {
            matchService.load(originalTracks.get(device.getId()), new MatchService.Callback() {
                @Override
                public void onSuccess(Track track) {
                    snappedTracks.put(device.getId(), track);
                    showArchive(device);
                }

                @Override
                public void onError(int code, String text) {
                    new AlertMessageBox(i18n.error(), i18n.errSnapToRoads(code, text)).show();
                }
            });
        }
    }

    private MatchService getMatchService() {
        MatchServiceType matchServiceType = ApplicationContext.getInstance().getApplicationSettings().getMatchServiceType();
        String url = ApplicationContext.getInstance().getApplicationSettings().getMatchServiceURL();
        if (matchServiceType != null) {
            switch (matchServiceType) {
                case OSRM_V4:
                    return new OSRMv4MatchService(url);
                case OSRM_V5:
                    return new OSRMv5MatchService(url);
            }
        }
        return null;
    }
}
