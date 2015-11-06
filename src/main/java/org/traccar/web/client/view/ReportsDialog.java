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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.client.controller.ReportsController;
import org.traccar.web.client.editor.DateTimeEditor;
import org.traccar.web.client.editor.ListViewEditor;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.widget.PeriodComboBox;
import org.traccar.web.shared.model.*;

import java.util.*;

public class ReportsDialog implements Editor<Report>, ReportsController.ReportHandler {
    private static ReportsDialogDialogUiBinder uiBinder = GWT.create(ReportsDialogDialogUiBinder.class);

    interface ReportsDialogDialogUiBinder extends UiBinder<Widget, ReportsDialog> {
    }

    interface ReportDriver extends SimpleBeanEditorDriver<Report, ReportsDialog> {
    }

    public interface ReportHandler {
        void onAdd(Report report, ReportsController.ReportHandler handler);
        void onUpdate(Report report, ReportsController.ReportHandler handler);
        void onRemove(Report report, ReportsController.ReportHandler handler);
        void onGenerate(Report report);
    }

    final ReportHandler reportHandler;
    final ReportDriver driver = GWT.create(ReportDriver.class);

    @UiField
    Window window;

    @UiField
    Grid<Report> grid;

    @UiField(provided = true)
    ColumnModel<Report> columnModel;

    @UiField(provided = true)
    final ListStore<Report> reportStore;

    @UiField
    TextField name;

    @UiField(provided = true)
    ComboBox<ReportType> type;

    @UiField(provided = true)
    final ListStore<Device> deviceStore;

    @UiField(provided = true)
    final ListView<Device, String> devicesList;

    final ListViewEditor<Device> devices;

    @UiField(provided = true)
    final ListStore<GeoFence> geoFenceStore;

    @UiField(provided = true)
    final ListView<GeoFence, String> geoFencesList;

    final ListViewEditor<GeoFence> geoFences;

    @UiField(provided = true)
    final PeriodComboBox period;

    @UiField
    @Ignore
    DateField fromDateField;

    @UiField
    @Ignore
    TimeField fromTimeField;

    final DateTimeEditor fromDate;

    @UiField
    @Ignore
    DateField toDateField;

    @UiField
    @Ignore
    TimeField toTimeField;

    final DateTimeEditor toDate;

    @UiField
    @Ignore
    TextButton removeButton;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public ReportsDialog(ListStore<Report> reportStore,
                         ListStore<Device> deviceStore,
                         ListStore<GeoFence> geoFenceStore,
                         ReportHandler reportHandler) {
        ReportProperties reportProperties = GWT.create(ReportProperties.class);

        this.reportStore = reportStore;
        this.reportHandler = reportHandler;

        List<ColumnConfig<Report, ?>> columnConfigList = new LinkedList<ColumnConfig<Report, ?>>();
        columnConfigList.add(new ColumnConfig<Report, String>(reportProperties.name(), 25, i18n.name()));
        columnConfigList.add(new ColumnConfig<Report, String>(new ReportProperties.ReportTypeLabelProvider(), 25, i18n.type()));
        columnModel = new ColumnModel<Report>(columnConfigList);

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        this.deviceStore = deviceStore;
        this.devicesList = new ListView<Device, String>(deviceStore, deviceProperties.name());

        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);
        this.geoFenceStore = geoFenceStore;
        this.geoFencesList = new ListView<GeoFence, String>(geoFenceStore, geoFenceProperties.name());

        ListStore<ReportType> geoFenceTypeStore = new ListStore<ReportType>(
                new EnumKeyProvider<ReportType>());
        geoFenceTypeStore.addAll(Arrays.asList(ReportType.values()));
        type = new ComboBox<ReportType>(
                geoFenceTypeStore, new ReportProperties.ReportTypeLabelProvider());
        type.setForceSelection(true);
        type.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        period = new PeriodComboBox();

        uiBinder.createAndBindUi(this);

        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<Report>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<Report> event) {
                if (event.getSelection().isEmpty()) {
                    driver.edit(new Report());
                } else {
                    driver.edit(event.getSelection().get(0));
                }
                removeButton.setEnabled(!event.getSelection().isEmpty());
            }
        });

        period.init(fromDateField, fromTimeField, toDateField, toTimeField);

        geoFences = new ListViewEditor<GeoFence>(geoFencesList);
        devices = new ListViewEditor<Device>(devicesList);
        fromDate = new DateTimeEditor(fromDateField, fromTimeField);
        toDate = new DateTimeEditor(toDateField, toTimeField);

        driver.initialize(this);
        driver.edit(new Report());
        period.selectFirst();
    }

    public void show() {
        window.show();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        Report report = driver.flush();
        if (driver.hasErrors()) {
            return;
        }
        if (grid.getSelectionModel().getSelectedItem() == null) {
            reportHandler.onAdd(report, this);
        } else {
            reportHandler.onUpdate(report, this);
        }
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmReportRemoval());
        final Report report = driver.flush();
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.YES) {
                    reportHandler.onRemove(report, ReportsDialog.this);
                }
            }
        });
        dialog.show();
    }

    @UiHandler("newButton")
    public void onNewClicked(SelectEvent event) {
        grid.getSelectionModel().deselectAll();
    }

    @UiHandler("generateButton")
    public void onGenerateClicked(SelectEvent event) {
        reportHandler.onGenerate(driver.flush());
    }

    @Override
    public void reportAdded(Report report) {
        reportStore.add(report);
    }

    @Override
    public void reportUpdated(Report report) {
        reportStore.update(report);
    }

    @Override
    public void reportRemoved(Report report) {
        reportStore.remove(report);
    }
}
