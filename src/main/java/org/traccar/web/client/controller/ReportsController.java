/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.ReportService;
import org.traccar.web.client.model.ReportServiceAsync;
import org.traccar.web.client.view.NavView;
import org.traccar.web.client.view.ReportsDialog;
import org.traccar.web.client.view.ReportsMenu;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.Report;

import java.util.List;

public class ReportsController implements ContentController, ReportsMenu.ReportHandler {
    private final Messages i18n = GWT.create(Messages.class);
    private final ReportMapper reportMapper = GWT.create(ReportMapper.class);
    private final ListStore<Report> reportStore;
    private final ListStore<Device> deviceStore;
    private final ListStore<GeoFence> geoFenceStore;

    interface ReportMapper extends ObjectMapper<Report> {}

    public interface ReportHandler {
        void reportAdded(Report report);
        void reportUpdated(Report report);
        void reportRemoved(Report report);
    }

    public ReportsController(ListStore<Report> reportStore, ListStore<Device> deviceStore, ListStore<GeoFence> geoFenceStore) {
        this.reportStore = reportStore;
        this.deviceStore = deviceStore;
        this.geoFenceStore = geoFenceStore;
    }

    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
        final ReportServiceAsync service = GWT.create(ReportService.class);
        service.getReports(new BaseAsyncCallback<List<Report>>(i18n) {
                               @Override
                               public void onSuccess(List<Report> result) {
                                   reportStore.addAll(result);
                               }
                           });
    }

    private void generate(Report report) {
        FormPanel form = new FormPanel("_blank");
        form.setVisible(false);
        form.setAction("traccar/report" + (report.isPreview() ? "/" + report.getName() + ".html" : ""));
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_URLENCODED);
        HorizontalPanel container = new HorizontalPanel();
        container.add(new Hidden("report", reportMapper.write(report)));
        container.add(new Hidden("locale", LocaleInfo.getCurrentLocale().getLocaleName()));
        form.add(container);
        RootPanel.get().add(form);
        try {
            form.submit();
        } finally {
            RootPanel.get().remove(form);
        }
    }

    @Override
    public ReportsDialog createDialog() {
        final ReportServiceAsync service = GWT.create(ReportService.class);
        return new ReportsDialog(reportStore, deviceStore, geoFenceStore, new ReportsDialog.ReportHandler() {
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
        });
    }
}
