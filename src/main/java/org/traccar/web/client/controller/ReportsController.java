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
package org.traccar.web.client.controller;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.ReportProperties;
import org.traccar.web.client.model.ReportService;
import org.traccar.web.client.model.ReportServiceAsync;
import org.traccar.web.client.view.ReportsDialog;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.Report;

import java.util.List;

public class ReportsController implements ArchiveController.ReportsHandler {
    private final Messages i18n = GWT.create(Messages.class);
    private final ReportMapper reportMapper = GWT.create(ReportMapper.class);
    private final ListStore<Device> deviceStore;
    private final ListStore<GeoFence> geoFenceStore;

    interface ReportMapper extends ObjectMapper<Report> {}

    public interface ReportHandler {
        void reportAdded(Report report);
        void reportUpdated(Report report);
        void reportRemoved(Report report);
    }

    public ReportsController(ListStore<Device> deviceStore, ListStore<GeoFence> geoFenceStore) {
        this.deviceStore = deviceStore;
        this.geoFenceStore = geoFenceStore;
    }

    @Override
    public void onShowReports() {
        final ReportServiceAsync service = GWT.create(ReportService.class);
        service.getReports(new BaseAsyncCallback<List<Report>>(i18n) {
            @Override
            public void onSuccess(List<Report> result) {
                ReportProperties reportProperties = GWT.create(ReportProperties.class);
                ListStore<Report> reportStore = new ListStore<Report>(reportProperties.id());
                reportStore.addAll(result);
                new ReportsDialog(reportStore, deviceStore, geoFenceStore, new ReportsDialog.ReportHandler() {
                    @Override
                    public void onAdd(Report report, final ReportHandler handler) {
                        service.addReport(report, new BaseAsyncCallback<Report>(i18n) {
                            @Override
                            public void onSuccess(Report result) {
                                handler.reportAdded(result);
                            }
                        });
                    }

                    @Override
                    public void onUpdate(Report report, final ReportHandler handler) {
                        service.updateReport(report, new BaseAsyncCallback<Report>(i18n) {
                            @Override
                            public void onSuccess(Report result) {
                                handler.reportUpdated(result);
                            }
                        });
                    }

                    @Override
                    public void onRemove(final Report report, final ReportHandler handler) {
                        service.removeReport(report, new BaseAsyncCallback<Void>(i18n) {
                            @Override
                            public void onSuccess(Void result) {
                                handler.reportRemoved(report);
                            }
                        });
                    }

                    @Override
                    public void onGenerate(Report report) {
                        generate(report);
                    }
                }).show();
            }
        });
    }

    private void generate(Report report) {
        GWT.log(reportMapper.write(report));
    }
}
