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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseStoreHandlers;
import org.traccar.web.shared.model.Report;
import org.traccar.web.shared.model.ReportType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReportsMenu extends Menu {
    public interface ReportHandler {
        ReportsDialog createDialog();
    }

    public interface ReportSettingsHandler {
        void setSettings(ReportsDialog dialog);
    }

    private final Messages i18n = GWT.create(Messages.class);
    private final ListStore<Report> reports;
    private final Map<String, MenuItem> userReports = new HashMap<>();
    private final ReportHandler reportHandler;
    private final ReportSettingsHandler reportSettingsHandler;

    public ReportsMenu(ListStore<Report> reports,
                       final ReportHandler reportHandler,
                       final ReportSettingsHandler reportSettingsHandler) {
        this.reports = reports;
        this.reportHandler = reportHandler;
        this.reportSettingsHandler = reportSettingsHandler;
        syncReports();
        for (final ReportType type : ReportType.values()) {
            MenuItem reportItem = new MenuItem(i18n.reportType(type));
            reportItem.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    ReportsDialog dialog = reportHandler.createDialog();
                    dialog.selectReportType(type);
                    reportSettingsHandler.setSettings(dialog);
                    dialog.show();
                }
            });
            add(reportItem);
        }
        this.reports.addStoreHandlers(new BaseStoreHandlers<Report>() {
            @Override
            public void onAnything() {
                syncReports();
            }
        });
    }

    private void syncReports() {
        // process deleted reports
        for (Iterator<String> it = userReports.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            if (reports.findModelWithKey(key) == null) {
                MenuItem menuItem = userReports.get(key);
                it.remove();
                remove(menuItem);
            }
        }

        // process added and updated reports
        for (int i = 0; i < reports.size(); i++) {
            final Report report = reports.get(i);
            String key = reports.getKeyProvider().getKey(report);
            MenuItem reportItem = userReports.get(key);

            if (reportItem == null) {
                reportItem = new MenuItem(report.getName());
                reportItem.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        ReportsDialog dialog = reportHandler.createDialog();
                        dialog.selectReport(report);
                        reportSettingsHandler.setSettings(dialog);
                        dialog.show();
                    }
                });

                if (userReports.isEmpty()) {
                    insert(new SeparatorMenuItem(), 0);
                }
                insert(reportItem, i);

                userReports.put(key, reportItem);
            } else {
                reportItem.setText(report.getName());
            }
        }
    }
}
