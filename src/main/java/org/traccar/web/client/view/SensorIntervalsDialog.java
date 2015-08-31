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
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.SensorIntervalProperties;
import org.traccar.web.shared.model.SensorInterval;

import java.util.LinkedList;
import java.util.List;

public class SensorIntervalsDialog implements SelectionChangedEvent.SelectionChangedHandler<SensorInterval> {

    private static SensorIntervalsDialogUiBinder uiBinder = GWT.create(SensorIntervalsDialogUiBinder.class);

    interface SensorIntervalsDialogUiBinder extends UiBinder<Widget, SensorIntervalsDialog> {
    }

    interface SensorIntervalsHandler {
        List<SensorInterval> getIntervals();
        void setIntervals(List<SensorInterval> intervals);
    }

    @UiField
    Window window;

    @UiField(provided = true)
    ColumnModel<SensorInterval> columnModel;

    @UiField(provided = true)
    final ListStore<SensorInterval> sensorStore;

    @UiField
    Grid<SensorInterval> grid;

    @UiField
    TextButton removeButton;

    @UiField
    VerticalLayoutContainer mainContainer;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    final SensorIntervalsHandler handler;

    public SensorIntervalsDialog(SensorIntervalsHandler handler) {
        this.handler = handler;

        final SensorIntervalProperties sensorProperties = GWT.create(SensorIntervalProperties.class);

        this.sensorStore = new ListStore<SensorInterval>(new ModelKeyProvider<SensorInterval>() {
            @Override
            public String getKey(SensorInterval item) {
                return item.getText() + "_" + item.getValue();
            }
        });

        List<ColumnConfig<SensorInterval, ?>> columnConfigList = new LinkedList<ColumnConfig<SensorInterval, ?>>();

        ColumnConfig<SensorInterval, Double> valueColumn = new ColumnConfig<SensorInterval, Double>(sensorProperties.value(), 80, i18n.intervalFrom());
        valueColumn.setFixed(true);
        valueColumn.setResizable(false);
        columnConfigList.add(valueColumn);
        ColumnConfig<SensorInterval, String> nameColumn = new ColumnConfig<SensorInterval, String>(sensorProperties.text(), 25, i18n.text());
        columnConfigList.add(nameColumn);

        columnModel = new ColumnModel<SensorInterval>(columnConfigList);

        uiBinder.createAndBindUi(this);

        // set up grid
        for (SensorInterval interval : handler.getIntervals()) {
            sensorStore.add(interval);
        }

        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedHandler(this);

        GridEditing<SensorInterval> editing = new GridInlineEditing<SensorInterval>(grid);
        NumberField<Double> valueEditor = new NumberField<Double>(new NumberPropertyEditor.DoublePropertyEditor());
        valueEditor.setAllowDecimals(true);
        valueEditor.setAllowBlank(false);
        valueEditor.setAllowNegative(true);
        editing.addEditor(valueColumn, valueEditor);
        editing.addEditor(nameColumn, new TextField());
    }

    public void show() {
        window.show();
    }

    @UiHandler("saveButton")
    public void onSaveChanges(SelectEvent event) {
        sensorStore.commitChanges();
        handler.setIntervals(sensorStore.getAll());
        window.hide();
    }

    @UiHandler("cancelButton")
    public void onCancel(SelectEvent event) {
        window.hide();
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        SensorInterval newInterval = new SensorInterval();
        newInterval.setText(i18n.interval() + " #" + (sensorStore.size() + 1));
        sensorStore.add(newInterval);
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        sensorStore.remove(sensorStore.indexOf(grid.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<SensorInterval> event) {
        removeButton.setEnabled(!event.getSelection().isEmpty());
    }
}
