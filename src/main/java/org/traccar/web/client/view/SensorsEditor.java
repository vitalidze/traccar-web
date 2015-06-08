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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.json.client.*;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.DeviceProperties;
import org.traccar.web.client.model.SensorProperties;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Sensor;
import org.traccar.web.shared.model.SensorInterval;

import java.util.*;

public class SensorsEditor implements SelectionChangedEvent.SelectionChangedHandler<Sensor> {

    private static SensorsEditorUiBinder uiBinder = GWT.create(SensorsEditorUiBinder.class);

    interface SensorsEditorUiBinder extends UiBinder<Widget, SensorsEditor> {
    }

    @UiField
    SimpleContainer panel;

    @UiField(provided = true)
    ComboBox<Device> deviceCombo;

    @UiField
    TextButton copyFromButton;

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

    public SensorsEditor(Device device, ListStore<Device> deviceStore) {
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

        final ValueProvider<Sensor, String> intervalsProperty = new ValueProvider<Sensor, String>() {
            @Override
            public String getValue(Sensor object) {
                return i18n.edit();
            }

            @Override
            public void setValue(Sensor object, String value) {
            }

            @Override
            public String getPath() {
                return "intervals";
            }
        };
        ColumnConfig<Sensor, String> intervalsColumn = new ColumnConfig<Sensor, String>(intervalsProperty, 90, i18n.intervals());
        intervalsColumn.setFixed(true);
        intervalsColumn.setResizable(false);
        // IMPORTANT we want the text element (cell parent) to only be as wide as
        // the cell and not fill the cell
        intervalsColumn.setColumnTextClassName(CommonStyles.get().inlineBlock());
        intervalsColumn.setColumnTextStyle(SafeStylesUtils.fromTrustedString("padding: 1px 3px 0;"));
        TextButtonCell intervalsEditButton = new TextButtonCell();
        intervalsEditButton.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                int row = event.getContext().getIndex();
                final Sensor sensor = sensorStore.get(row);
                new SensorIntervalsDialog(new SensorIntervalsDialog.SensorIntervalsHandler() {
                    @Override
                    public List<SensorInterval> getIntervals() {
                        return intervals(sensor);
                    }

                    @Override
                    public void setIntervals(List<SensorInterval> intervals) {
                        intervals = new ArrayList<SensorInterval>(intervals);
                        Collections.sort(intervals, new Comparator<SensorInterval>() {
                            @Override
                            public int compare(SensorInterval o1, SensorInterval o2) {
                                return o1.getValue() > o2.getValue() ? 1 : -1;
                            }
                        });
                        JSONArray array = new JSONArray();
                        for (SensorInterval interval : intervals) {
                            JSONObject jsonInterval = new JSONObject();
                            jsonInterval.put("text", new JSONString(interval.getText()));
                            jsonInterval.put("value", new JSONNumber(interval.getValue()));
                            array.set(array.size(), jsonInterval);
                        }
                        sensor.setIntervals(array.toString());
                        sensorStore.getRecord(sensor).addChange(intervalsProperty, i18n.edit());
                    }
                }).show();
            }
        });
        intervalsColumn.setCell(intervalsEditButton);
        columnConfigList.add(intervalsColumn);

        for (ColumnConfig<Sensor, ?> columnConfig : columnConfigList) {
            columnConfig.setHideable(false);
            columnConfig.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }

        columnModel = new ColumnModel<Sensor>(columnConfigList);

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        deviceCombo = new ComboBox<Device>(deviceStore, deviceProperties.label());
        deviceCombo.addSelectionHandler(new SelectionHandler<Device>() {
            @Override
            public void onSelection(SelectionEvent<Device> event) {
                copyFromButton.setEnabled(event.getSelectedItem() != null);
            }
        });

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

    @UiHandler("copyFromButton")
    public void onCopyFromClicked(SelectEvent event) {
        Device device = deviceCombo.getCurrentValue();
        for (Sensor sensor : device.getSensors()) {
            boolean found = false;
            for (int i = 0; i < sensorStore.size(); i++) {
                Sensor next = sensorStore.get(i);
                if (next.getParameterName().equals(sensor.getParameterName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Sensor newSensor = new Sensor(sensor);
                newSensor.setId(-sensorStore.size());
                sensorStore.add(newSensor);
            }
        }
    }

    public static List<SensorInterval> intervals(Sensor sensor) {
        if (sensor.getIntervals() == null) {
            return Collections.emptyList();
        } else {
            JSONArray array = (JSONArray) JSONParser.parseStrict(sensor.getIntervals());
            List<SensorInterval> intervals = new ArrayList<SensorInterval>(array.size());
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonInterval = (JSONObject) array.get(i);
                SensorInterval interval = new SensorInterval();
                interval.setText(((JSONString) jsonInterval.get("text")).stringValue());
                interval.setValue(((JSONNumber) jsonInterval.get("value")).doubleValue());
                intervals.add(interval);
            }
            return intervals;
        }
    }
}
