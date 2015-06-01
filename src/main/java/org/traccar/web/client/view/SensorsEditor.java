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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.SensorProperties;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Sensor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SensorsEditor implements SelectionChangedEvent.SelectionChangedHandler<Sensor> {

    private static MaintenanceDialogUiBinder uiBinder = GWT.create(MaintenanceDialogUiBinder.class);

    interface MaintenanceDialogUiBinder extends UiBinder<Widget, SensorsEditor> {
    }

    @UiField
    SimpleContainer panel;

    @UiField(provided = true)
    ColumnModel<Sensor> columnModel;

    @UiField(provided = true)
    final ListStore<Sensor> sensorStore;

    @UiField
    Grid<Sensor> grid;

    @UiField
    TextButton removeButton;

    @UiField
    VerticalLayoutContainer mainContainer;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    final Device device;

    public SensorsEditor(Device device) {
        this.device = device;

        final SensorProperties sensorProperties = GWT.create(SensorProperties.class);

        this.sensorStore = new ListStore<Sensor>(sensorProperties.id());

        List<ColumnConfig<Sensor, ?>> columnConfigList = new LinkedList<ColumnConfig<Sensor, ?>>();

        ColumnConfig<Sensor, String> nameColumn = new ColumnConfig<Sensor, String>(sensorProperties.name(), 25, i18n.name());
        columnConfigList.add(nameColumn);
        ColumnConfig<Sensor, String> parameterNameColumn = new ColumnConfig<Sensor, String>(sensorProperties.parameterName(), 25, i18n.parameter());
        columnConfigList.add(parameterNameColumn);
        ColumnConfig<Sensor, String> descriptionColumn = new ColumnConfig<Sensor, String>(sensorProperties.description(), 25, i18n.description());
        columnConfigList.add(descriptionColumn);
        ColumnConfig<Sensor, Boolean> visibleColumn = new ColumnConfig<Sensor, Boolean>(sensorProperties.visible(), 70, i18n.visible());
        visibleColumn.setCell(new CheckBoxCell());
        visibleColumn.setFixed(true);
        columnConfigList.add(visibleColumn);

        for (ColumnConfig<Sensor, ?> columnConfig : columnConfigList) {
            columnConfig.setHideable(false);
            columnConfig.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }

        columnModel = new ColumnModel<Sensor>(columnConfigList);

        uiBinder.createAndBindUi(this);

        // set up grid
        for (Sensor sensor : device.getSensors()) {
            sensorStore.add(sensor);
        }

        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedHandler(this);

        GridEditing<Sensor> editing = new GridInlineEditing<Sensor>(grid);
        editing.addEditor(nameColumn, new TextField());
        editing.addEditor(parameterNameColumn, new TextField());
        editing.addEditor(descriptionColumn, new TextField());
        editing.addEditor(visibleColumn, new CheckBox());
    }

    public Container getPanel() {
        return panel;
    }

    public void flush() {
        sensorStore.commitChanges();
        device.setSensors(new ArrayList<Sensor>(sensorStore.getAll()));
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        Sensor sensor = new Sensor();
        sensor.setId(-sensorStore.size());
        sensorStore.add(sensor);
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        sensorStore.remove(sensorStore.indexOf(grid.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Sensor> event) {
        removeButton.setEnabled(!event.getSelection().isEmpty());
    }
}
