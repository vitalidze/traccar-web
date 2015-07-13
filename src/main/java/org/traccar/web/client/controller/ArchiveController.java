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

import java.util.Date;
import java.util.List;

import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.ArchiveStyle;
import org.traccar.web.client.Track;
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

    public ArchiveController(ArchiveHandler archiveHandler, UserSettingsDialog.UserSettingsHandler userSettingsHandler, ListStore<Device> deviceStore) {
        this.archiveHandler = archiveHandler;
        this.userSettingsHandler = userSettingsHandler;
        this.archiveView = new ArchiveView(this, deviceStore);
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
    public void onLoad(final Device device, Date from, Date to, boolean filter, boolean snapToRoads, final ArchiveStyle style) {
        if (device != null && from != null && to != null) {
            Application.getDataService().getPositions(device, from, to, filter, snapToRoads, new BaseAsyncCallback<List<Position>>(i18n) {
                @Override
                public void onSuccess(List<Position> result) {
                    archiveHandler.onClear(device);
                    if (result.isEmpty()) {
                        new AlertMessageBox(i18n.error(), i18n.errNoResults()).show();
                    }
                    archiveHandler.onDrawTrack(new Track(result, style));
                    archiveView.showPositions(device, result);
                }
            });
        } else {
            new AlertMessageBox(i18n.error(), i18n.errFillFields()).show();
        }
    }

    @Override
    public void onClear(Device device) {
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
}
