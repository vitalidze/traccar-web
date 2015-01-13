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
package org.traccar.web.client.view;

import java.util.*;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.state.client.GridStateHandler;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.grid.*;
import com.sencha.gxt.widget.core.client.menu.*;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.ArchiveStyle;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseStoreHandlers;
import org.traccar.web.client.model.DeviceProperties;
import org.traccar.web.client.model.PositionProperties;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.Position;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.NumberCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.shared.model.PositionIconType;

public class ArchiveView implements SelectionChangedEvent.SelectionChangedHandler<Position> {

    private static ArchiveViewUiBinder uiBinder = GWT.create(ArchiveViewUiBinder.class);

    public ArchiveStyle style = new ArchiveStyle();

    interface ArchiveViewUiBinder extends UiBinder<Widget, ArchiveView> {
    }

    public interface ArchiveHandler {
        public void onSelected(Position position);
        public void onLoad(Device device, Date from, Date to, boolean filter, ArchiveStyle style);
        public void onFilterSettings();
        public void onClear();
    }

    private ArchiveHandler archiveHandler;

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    ListStore<Device> deviceStore;

    @UiField
    DateField fromDate;

    @UiField
    TimeField fromTime;

    @UiField
    DateField toDate;

    @UiField
    TimeField toTime;

    @UiField(provided = true)
    ComboBox<Device> deviceCombo;

    @UiField(provided = true)
    ColumnModel<Position> columnModel;

    @UiField(provided = true)
    ListStore<Position> positionStore;

    @UiField
    Grid<Position> grid;

    @UiField
    CheckBox disableFilter;

    @UiField(provided = true)
    TextButton styleButtonTrackColor;

    @UiField
    TextButton styleButton;

    @UiField(provided = true)
    ColorMenu smallColorMenu;

    @UiField(provided = true)
    ColorMenu fullColorMenu;

    @UiField(provided = true)
    Menu routeMarkersType;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public ArchiveView(ArchiveHandler archiveHandler, ListStore<Position> positionStore, ListStore<Device> deviceStore) {
        this.archiveHandler = archiveHandler;
        this.positionStore = positionStore;
        deviceStore.addStoreHandlers(deviceStoreHandlers);
        this.deviceStore = deviceStore;

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        deviceCombo = new ComboBox<Device>(deviceStore, deviceProperties.label());

        PositionProperties positionProperties = GWT.create(PositionProperties.class);

        List<ColumnConfig<Position, ?>> columnConfigList = new LinkedList<ColumnConfig<Position, ?>>();

        ColumnConfig<Position, Boolean> columnConfigValid = new ColumnConfig<Position, Boolean>(positionProperties.valid(), 25, i18n.valid());
        columnConfigList.add(columnConfigValid);

        ColumnConfig<Position, Date> columnConfigDate = new ColumnConfig<Position, Date>(positionProperties.time(), 25, i18n.time());
        columnConfigDate.setCell(new DateCell(ApplicationContext.getInstance().getFormatterUtil().getTimeFormat()));
        columnConfigList.add(columnConfigDate);

        ColumnConfig<Position, String> columnConfigAddress = new ColumnConfig<Position, String>(positionProperties.address(), 25, i18n.address());
        columnConfigList.add(columnConfigAddress);

        columnConfigList.add(new ColumnConfig<Position, Double>(positionProperties.latitude(), 25, i18n.latitude()));
        columnConfigList.add(new ColumnConfig<Position, Double>(positionProperties.longitude(), 25, i18n.longitude()));
        columnConfigList.add(new ColumnConfig<Position, Double>(positionProperties.altitude(), 25, i18n.altitude()));

        ColumnConfig<Position, Double> columnConfigSpeed = new ColumnConfig<Position, Double>(positionProperties.speed(), 25, i18n.speed());
        columnConfigSpeed.setCell(new NumberCell<Double>(ApplicationContext.getInstance().getFormatterUtil().getSpeedFormat()));
        columnConfigList.add(columnConfigSpeed);

        ColumnConfig<Position, Double> columnConfigDistance = new ColumnConfig<Position, Double>(positionProperties.distance(), 25, i18n.distance());
        columnConfigDistance.setCell(new NumberCell<Double>(ApplicationContext.getInstance().getFormatterUtil().getDistanceFormat()));
        columnConfigList.add(columnConfigDistance);

        columnConfigList.add(new ColumnConfig<Position, Double>(positionProperties.course(), 25, i18n.course()));
        columnConfigList.add(new ColumnConfig<Position, Double>(positionProperties.power(), 25, i18n.power()));

        columnModel = new ColumnModel<Position>(columnConfigList);

        // set up 'Totals' row
        AggregationRowConfig<Position> totals = new AggregationRowConfig<Position>();
        totals.setRenderer(columnConfigSpeed, new AggregationNumberSummaryRenderer<Position, Double>(ApplicationContext.getInstance().getFormatterUtil().getSpeedFormat(), new SummaryType.AvgSummaryType<Double>()));
        totals.setRenderer(columnConfigDistance, new AggregationNumberSummaryRenderer<Position, Double>(ApplicationContext.getInstance().getFormatterUtil().getDistanceFormat(), new SummaryType.SumSummaryType<Double>()));

        columnModel.addAggregationRow(totals);

        // Element that displays the current track color
        styleButtonTrackColor = new TextButton();
        styleButtonTrackColor.getElement().getStyle().setProperty("backgroundColor","#".concat(style.DEFAULT_COLOR));
        styleButtonTrackColor.getElement().getStyle().setCursor(Style.Cursor.TEXT);
        // Menu with the small palette
        smallColorMenu = new ExtColorMenu(style.COLORS, style.COLORS);
        smallColorMenu.setColor(style.DEFAULT_COLOR);
        smallColorMenu.getPalette().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                style.setTrackColor(event.getValue());
                smallColorMenu.hide(true);
                fullColorMenu.getPalette().setValue("", false);
                styleButtonTrackColor.getElement().getStyle().setProperty("backgroundColor","#".concat(style.getTrackColor()));
            }
        });
        // Menu with the complete palette
        fullColorMenu = new ColorMenu();
        fullColorMenu.getPalette().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                style.setTrackColor(event.getValue());
                fullColorMenu.hide(true);
                smallColorMenu.getPalette().setValue("",false);
                styleButtonTrackColor.getElement().getStyle().setProperty("backgroundColor","#".concat(style.getTrackColor()));
            }
        });
        // Markers
        routeMarkersType = new Menu();
        final CheckMenuItem item1 = new CheckMenuItem(i18n.standardMarkers());
        item1.setGroup("markers");
        item1.setChecked(true);
        item1.addSelectionHandler(new SelectionHandler<Item>() {
            @Override
            public void onSelection(SelectionEvent<Item> selectionEvent) {
                style.setIconType(PositionIconType.iconArchive);
            }
        });
        final CheckMenuItem item2 = new CheckMenuItem(i18n.reducedMarkers());
        item2.setGroup("markers");
        item2.addSelectionHandler(new SelectionHandler<Item>() {
            @Override
            public void onSelection(SelectionEvent<Item> selectionEvent) {
                style.setIconType(PositionIconType.dotArchive);
            }
        });
        routeMarkersType.add(item1);
        routeMarkersType.add(item2);

        uiBinder.createAndBindUi(this);

        new GridStateHandler<Position>(grid).loadState();

        grid.getSelectionModel().addSelectionChangedHandler(this);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Initialize with current time
        long min = 60 * 1000;
        Date now = new Date();
        Date to = new Date(((now.getTime() + 15 * min) / (15 * min)) * 15 * min);
        Date from = new Date(to.getTime() - 60 * min);
        fromDate.setValue(from);
        fromTime.setValue(from);
        toDate.setValue(to);
        toTime.setValue(to);
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Position> event) {
        if (event.getSelection().isEmpty()) {
            archiveHandler.onSelected(null);
        } else {
            archiveHandler.onSelected(event.getSelection().get(0));
        }
    }

    @SuppressWarnings("deprecation")
    private static Date getCombineDate(DateField dateField, TimeField timeField) {
        Date result = null;
        Date date = dateField.getValue();
        Date time = timeField.getValue();
        if (date != null && time != null) {
            result = new Date(
                    date.getYear(), date.getMonth(), date.getDate(),
                    time.getHours(), time.getMinutes(), time.getSeconds());
        }
        return result;
    }

    @UiHandler("loadButton")
    public void onLoadClicked(SelectEvent event) {
        archiveHandler.onLoad(
                deviceCombo.getValue(),
                getCombineDate(fromDate, fromTime),
                getCombineDate(toDate, toTime),
                !disableFilter.getValue(),
                style
        );
    }

    @UiHandler("clearButton")
    public void onClearClicked(SelectEvent event) {
        archiveHandler.onClear();
    }

    @UiHandler("csvButton")
    public void onCSVClicked(SelectionEvent<Item> event) {
        DateTimeFormat jsonTimeFormat = ApplicationContext.getInstance().getFormatterUtil().getRequestTimeFormat();

        Window.open("/traccar/export/csv" +
                        "?deviceId=" + (deviceCombo.getValue() == null ? null : deviceCombo.getValue().getId()) +
                        "&from=" + jsonTimeFormat.format(getCombineDate(fromDate, fromTime)).replaceFirst("\\+", "%2B") +
                        "&to=" + jsonTimeFormat.format(getCombineDate(toDate, toTime)).replaceFirst("\\+", "%2B") +
                        "&filter=" + !disableFilter.getValue(),
                "_blank", null);
    }

    @UiHandler("gpxButton")
    public void onGPXClicked(SelectionEvent<Item> event) {
        DateTimeFormat jsonTimeFormat = ApplicationContext.getInstance().getFormatterUtil().getRequestTimeFormat();

        Window.open("/traccar/export/gpx" +
                    "?deviceId=" + (deviceCombo.getValue() == null ? null : deviceCombo.getValue().getId()) +
                    "&from=" + jsonTimeFormat.format(getCombineDate(fromDate, fromTime)).replaceFirst("\\+", "%2B") +
                    "&to=" + jsonTimeFormat.format(getCombineDate(toDate, toTime)).replaceFirst("\\+", "%2B") +
                    "&filter=" + !disableFilter.getValue(),
                    "_blank", null);
    }

    @UiHandler("importButton")
    public void onImportClicked(SelectionEvent<Item> event) {
        if (deviceCombo.getValue() == null) {
            new AlertMessageBox(i18n.error(), i18n.errFillFields()).show();
        } else {
            new ImportDialog(deviceCombo.getValue()).show();
        }
    }

    @UiHandler("filterButton")
    public void onFilterClicked(SelectEvent event) {
        archiveHandler.onFilterSettings();
    }

    private StoreHandlers<Device> deviceStoreHandlers = new BaseStoreHandlers<Device>() {

        @Override
        public void onAnything() {
            Device oldDevice = deviceCombo.getValue();
            if (oldDevice != null) {
                deviceCombo.setValue(deviceStore.findModel(oldDevice));
            } else if (deviceStore.size() == 1) {
                deviceCombo.setValue(deviceStore.get(0));
            }
        }

    };

    public void selectPosition(Position position) {
        grid.getSelectionModel().select(positionStore.findModel(position), false);
    }

    public void selectDevice(Device device) {
        deviceCombo.setValue(device,false);
        positionStore.clear();
    }
}
