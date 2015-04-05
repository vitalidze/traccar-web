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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowNumberer;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.MaintenanceProperties;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Maintenance;

import java.util.*;

public class MaintenanceDialog implements SelectionChangedEvent.SelectionChangedHandler<Maintenance> {

    private static MaintenanceDialogUiBinder uiBinder = GWT.create(MaintenanceDialogUiBinder.class);

    interface MaintenanceDialogUiBinder extends UiBinder<Widget, MaintenanceDialog> {
    }

    @UiField
    Window window;

    @UiField(provided = true)
    ColumnModel<Maintenance> columnModel;

    @UiField(provided = true)
    final ListStore<Maintenance> maintenanceStore;

    int nextIndex;

    @UiField
    Grid<Maintenance> grid;

    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();

    @UiField
    NumberField<Double> odometer;

    @UiField
    CheckBox autoUpdateOdometer;

    @UiField
    TextButton removeButton;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    final Device device;

    abstract class CalculatableCell extends AbstractCell<Double> {
        @Override
        public void render(Context context, Double value, SafeHtmlBuilder sb) {
            sb.append(calculate(maintenanceStore.get(context.getIndex())));
        }

        abstract double calculate(Maintenance m);

        double getValue(Maintenance m, ValueProvider<Maintenance, Double> property) {
            Store.Change<Maintenance, Double> change = maintenanceStore.getRecord(m).getChange(property);
            return change == null || change.getValue() == null ? property.getValue(m) : change.getValue();
        }
    }

    public MaintenanceDialog(Device device) {
        this.device = device;

        final MaintenanceProperties maintenanceProperties = GWT.create(MaintenanceProperties.class);

        this.maintenanceStore = new ListStore<Maintenance>(maintenanceProperties.indexNo());

        nextIndex = device.getMaintenances().size();

        List<ColumnConfig<Maintenance, ?>> columnConfigList = new LinkedList<ColumnConfig<Maintenance, ?>>();
        RowNumberer<Maintenance> rowNumberer = new RowNumberer<Maintenance>();
        rowNumberer.setHeader("#");

        columnConfigList.add(rowNumberer);
        rowNumberer.setFixed(true);
        rowNumberer.setResizable(false);
        ColumnConfig<Maintenance, String> nameColumn = new ColumnConfig<Maintenance, String>(maintenanceProperties.name(), 25, i18n.serviceName());
        columnConfigList.add(nameColumn);
        ColumnConfig<Maintenance, Double> serviceIntervalColumn = new ColumnConfig<Maintenance, Double>(maintenanceProperties.serviceInterval(), 140, i18n.mileageInterval() + " (" + i18n.km() + ")");
        columnConfigList.add(serviceIntervalColumn);
        serviceIntervalColumn.setFixed(true);
        serviceIntervalColumn.setResizable(false);
        ColumnConfig<Maintenance, Double> lastServiceColumn = new ColumnConfig<Maintenance, Double>(maintenanceProperties.lastService(), 110, i18n.lastServiceMileage() + " (" + i18n.km() + ")");
        columnConfigList.add(lastServiceColumn);
        lastServiceColumn.setFixed(true);
        lastServiceColumn.setResizable(false);

        ColumnConfig<Maintenance, Double> remainingColumn = new ColumnConfig<Maintenance, Double>(maintenanceProperties.lastService(), 82, i18n.remaining() + " (" + i18n.km() + ")");
        remainingColumn.setFixed(true);
        remainingColumn.setResizable(false);
        remainingColumn.setCell(new CalculatableCell() {
            @Override
            double calculate(Maintenance m) {
                double serviceInterval = getValue(m, maintenanceProperties.serviceInterval());
                double lastService = getValue(m, maintenanceProperties.lastService());
                double value = lastService + serviceInterval - odometer.getCurrentValue();
                return Math.max(0, value);
            }
        });
        columnConfigList.add(remainingColumn);

        ColumnConfig<Maintenance, Double> overdueColumn = new ColumnConfig<Maintenance, Double>(maintenanceProperties.lastService(), 96, i18n.overdue() + " (" + i18n.km() + ")");
        overdueColumn.setFixed(true);
        overdueColumn.setResizable(false);
        overdueColumn.setCell(new CalculatableCell() {
            @Override
            double calculate(Maintenance m) {
                double serviceInterval = getValue(m, maintenanceProperties.serviceInterval());
                double lastService = getValue(m, maintenanceProperties.lastService());
                double value = lastService + serviceInterval - odometer.getCurrentValue();
                return -Math.min(0, value);
            }
        });
        columnConfigList.add(overdueColumn);

        ColumnConfig<Maintenance, String> resetColumn = new ColumnConfig<Maintenance, String>(new ValueProvider<Maintenance, String>() {
            @Override
            public String getValue(Maintenance object) {
                return i18n.reset();
            }

            @Override
            public void setValue(Maintenance object, String value) {
            }

            @Override
            public String getPath() {
                return "reset";
            }
        }, 46);
        resetColumn.setFixed(true);
        resetColumn.setResizable(false);
        // IMPORTANT we want the text element (cell parent) to only be as wide as
        // the cell and not fill the cell
        resetColumn.setColumnTextClassName(CommonStyles.get().inlineBlock());
        resetColumn.setColumnTextStyle(SafeStylesUtils.fromTrustedString("padding: 1px 3px 0;"));
        TextButtonCell resetButton = new TextButtonCell();
        resetColumn.setCell(resetButton);
        columnConfigList.add(resetColumn);

        for (ColumnConfig<Maintenance, ?> columnConfig : columnConfigList) {
            columnConfig.setSortable(false);
            columnConfig.setHideable(false);
            columnConfig.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }

        columnModel = new ColumnModel<Maintenance>(columnConfigList);

        uiBinder.createAndBindUi(this);

        // set up device odometer settings
        odometer.setValue(device.getOdometer());
        autoUpdateOdometer.setValue(device.isAutoUpdateOdometer());

        // set up grid
        for (Maintenance maintenance : device.getMaintenances()) {
            maintenanceStore.add(maintenance);
        }

        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedHandler(this);

        GridEditing<Maintenance> editing = new GridInlineEditing<Maintenance>(grid);
        editing.addEditor(nameColumn, new TextField());
        NumberField<Double> serviceIntervalEditor = new NumberField<Double>(doublePropertyEditor);
        serviceIntervalEditor.setAllowDecimals(false);
        serviceIntervalEditor.setAllowBlank(false);
        serviceIntervalEditor.setAllowNegative(false);
        editing.addEditor(serviceIntervalColumn, serviceIntervalEditor);
        NumberField<Double> lastServiceEditor = new NumberField<Double>(doublePropertyEditor);
        lastServiceEditor.setAllowDecimals(false);
        lastServiceEditor.setAllowBlank(false);
        lastServiceEditor.setAllowNegative(false);
        editing.addEditor(lastServiceColumn, lastServiceEditor);

        rowNumberer.initPlugin(grid);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        maintenanceStore.commitChanges();
        device.setMaintenances(new ArrayList<Maintenance>(maintenanceStore.getAll()));
        for (int i = 0; i < device.getMaintenances().size(); i++) {
            device.getMaintenances().get(i).setIndexNo(i);
        }
        device.setOdometer(odometer.getCurrentValue());
        device.setAutoUpdateOdometer(autoUpdateOdometer.getValue());
        hide();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        Maintenance maintenance = new Maintenance();
        maintenance.setIndexNo(nextIndex++);
        maintenanceStore.add(maintenance);
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        maintenanceStore.remove(maintenanceStore.indexOf(grid.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Maintenance> event) {
        removeButton.setEnabled(!event.getSelection().isEmpty());
    }

    @UiHandler("odometer")
    public void onOdometerChanged(ValueChangeEvent<Double> event) {
        grid.getView().refresh(false);
    }
}
