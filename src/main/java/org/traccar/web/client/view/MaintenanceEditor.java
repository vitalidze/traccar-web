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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.NumberCell;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowNumberer;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.DeviceProperties;
import org.traccar.web.client.model.MaintenanceProperties;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Maintenance;

import java.util.*;

public class MaintenanceEditor implements SelectionChangedEvent.SelectionChangedHandler<Maintenance> {

    private static MaintenanceEditorUiBinder uiBinder = GWT.create(MaintenanceEditorUiBinder.class);

    interface MaintenanceEditorUiBinder extends UiBinder<Widget, MaintenanceEditor> {
    }

    @UiField
    SimpleContainer panel;

    @UiField(provided = true)
    ComboBox<Device> deviceCombo;

    @UiField
    TextButton copyFromButton;

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

    @UiField
    ToggleButton editButton;

    GridEditing<Maintenance> editing;
    ColumnConfig<Maintenance, String> nameColumn;
    ColumnConfig<Maintenance, Double> serviceIntervalColumn;
    ColumnConfig<Maintenance, Double> lastServiceColumn;

    @UiField
    ToolBar addRemoveToolbar;

    @UiField
    VerticalLayoutContainer mainContainer;

    @UiField(provided = true)
    NumberFormat odometerFormat = NumberFormat.getFormat("0.#");

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    final Device device;

    public MaintenanceEditor(Device device, ListStore<Device> deviceStore) {
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
        nameColumn = new ColumnConfig<Maintenance, String>(maintenanceProperties.name(), 25, i18n.serviceName());
        columnConfigList.add(nameColumn);
        serviceIntervalColumn = new ColumnConfig<Maintenance, Double>(maintenanceProperties.serviceInterval(), 140, i18n.mileageInterval() + " (" + i18n.km() + ")");
        columnConfigList.add(serviceIntervalColumn);
        serviceIntervalColumn.setFixed(true);
        serviceIntervalColumn.setResizable(false);
        serviceIntervalColumn.setHidden(true);
        serviceIntervalColumn.setCell(new NumberCell<Double>(odometerFormat));
        lastServiceColumn = new ColumnConfig<Maintenance, Double>(maintenanceProperties.lastService(), 110, i18n.lastServiceMileage() + " (" + i18n.km() + ")");
        columnConfigList.add(lastServiceColumn);
        lastServiceColumn.setFixed(true);
        lastServiceColumn.setResizable(false);
        lastServiceColumn.setHidden(true);
        lastServiceColumn.setCell(new NumberCell<Double>(odometerFormat));

        ColumnConfig<Maintenance, Double> stateColumn = new ColumnConfig<Maintenance, Double>(maintenanceProperties.lastService(), 128, i18n.state());
        stateColumn.setFixed(true);
        stateColumn.setResizable(false);
        stateColumn.setCell(new AbstractCell<Double>() {
            @Override
            public void render(Context context, Double value, SafeHtmlBuilder sb) {
                Maintenance m = maintenanceStore.get(context.getIndex());
                Store.Record record = maintenanceStore.getRecord(m);

                double serviceInterval = (Double) record.getValue(maintenanceProperties.serviceInterval());
                // do not draw anything if service interval is not set
                if (serviceInterval == 0d) {
                    sb.appendEscaped("");
                    return;
                }

                double lastService = (Double) record.getValue(maintenanceProperties.lastService());
                double remaining = lastService + serviceInterval - odometer.getCurrentValue();

                if (remaining > 0) {
                    sb.appendHtmlConstant("<font color=\"green\">" + i18n.remaining() + " " + odometerFormat.format(remaining) + " " + i18n.km() + "</font>");
                } else {
                    sb.appendHtmlConstant("<font color=\"red\">" + i18n.overdue() + " " + odometerFormat.format(-remaining) + " " + i18n.km() + "</font>");
                }
            }
        });
        columnConfigList.add(stateColumn);

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
        resetButton.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                int row = event.getContext().getIndex();
                Maintenance m = maintenanceStore.get(row);
                maintenanceStore.getRecord(m).addChange(maintenanceProperties.lastService(), odometer.getCurrentValue());
            }
        });
        resetColumn.setCell(resetButton);
        columnConfigList.add(resetColumn);

        for (ColumnConfig<Maintenance, ?> columnConfig : columnConfigList) {
            columnConfig.setSortable(false);
            columnConfig.setHideable(false);
            columnConfig.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }

        columnModel = new ColumnModel<Maintenance>(columnConfigList);

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        deviceCombo = new ComboBox<Device>(deviceStore, deviceProperties.label());
        deviceCombo.addSelectionHandler(new SelectionHandler<Device>() {
            @Override
            public void onSelection(SelectionEvent<Device> event) {
                copyFromButton.setEnabled(event.getSelectedItem() != null);
            }
        });

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

        editing = new GridInlineEditing<Maintenance>(grid);

        rowNumberer.initPlugin(grid);
    }

    public Container getPanel() {
        return panel;
    }

    public void flush() {
        maintenanceStore.commitChanges();
        device.setMaintenances(new ArrayList<Maintenance>(maintenanceStore.getAll()));
        for (int i = 0; i < device.getMaintenances().size(); i++) {
            device.getMaintenances().get(i).setIndexNo(i);
        }
        device.setOdometer(odometer.getCurrentValue());
        device.setAutoUpdateOdometer(autoUpdateOdometer.getValue());
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

    @UiHandler("editButton")
    public void onEditClicked(SelectEvent event) {
        if (editButton.getValue()) {
            startEditing();
        } else {
            stopEditing();
        }
    }

    private void startEditing() {
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

        serviceIntervalColumn.setHidden(false);
        lastServiceColumn.setHidden(false);

        addRemoveToolbar.setVisible(true);
        grid.getView().refresh(true);
        mainContainer.forceLayout();
    }

    private void stopEditing() {
        editing.removeEditor(nameColumn);
        editing.removeEditor(serviceIntervalColumn);
        editing.removeEditor(lastServiceColumn);

        serviceIntervalColumn.setHidden(true);
        lastServiceColumn.setHidden(true);

        addRemoveToolbar.setVisible(false);
        grid.getView().refresh(true);
    }

    @UiHandler("copyFromButton")
    public void onCopyFromClicked(SelectEvent event) {
        Device device = deviceCombo.getCurrentValue();
        for (Maintenance maintenance : device.getMaintenances()) {
            boolean found = false;
            for (int i = 0; i < maintenanceStore.size(); i++) {
                Maintenance next = maintenanceStore.get(i);
                if (next.getName().equals(maintenance.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Maintenance newMaintenance = new Maintenance(maintenance);
                newMaintenance.setId(0);
                newMaintenance.setIndexNo(nextIndex++);
                newMaintenance.setLastService(0); // do not copy 'last service'
                maintenanceStore.add(newMaintenance);
            }
        }
    }
}
