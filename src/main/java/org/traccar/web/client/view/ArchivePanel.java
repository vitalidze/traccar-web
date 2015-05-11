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

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.NumberCell;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.*;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.LiveGridView;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.PositionProperties;
import org.traccar.web.client.state.GridStateHandler;
import org.traccar.web.shared.model.Position;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ArchivePanel implements SelectionChangedEvent.SelectionChangedHandler<Position> {
    private static ArchivePanelUiBinder uiBinder = GWT.create(ArchivePanelUiBinder.class);

    interface ArchivePanelUiBinder extends UiBinder<Widget, ArchivePanel> {
    }

    @UiField
    VerticalLayoutContainer contentPanel;

    @UiField(provided = true)
    final ColumnModel<Position> columnModel;

    @UiField(provided = true)
    final ListStore<Position> positionStore;

    @UiField(provided = true)
    final LiveGridView<Position> view;

    @UiField
    Grid<Position> grid;

    @UiField
    LabelToolItem totalDistance;

    @UiField
    LabelToolItem averageSpeed;

    @UiField(provided = true)
    final Messages i18n = GWT.create(Messages.class);

    final PagingLoader<PagingLoadConfig, PagingLoadResult<Position>> loader;
    private final PagingMemoryProxy memoryProxy;
    final ArchiveView.ArchiveHandler archiveHandler;

    static class PagingMemoryProxy extends MemoryProxy<PagingLoadConfig, PagingLoadResult<Position>> {
        List<Position> positions = Collections.emptyList();

        public PagingMemoryProxy() {
            super(null); //data is useless in this case, memoryProxy only designed to hold, not to search
        }
        @Override
        public void load(PagingLoadConfig config, Callback<PagingLoadResult<Position>, Throwable> callback) {
            List<Position> results = positions.subList(config.getOffset(), Math.min(positions.size(), config.getOffset() + config.getLimit())); // Get results list based on the data the proxy was created with
            callback.onSuccess(new PagingLoadResultBean<Position>(results, positions.size(), config.getOffset()));  // again, data from the config
        }

        public void setPositions(List<Position> positions) {
            this.positions = positions;
        }
    }

    public ArchivePanel(ArchiveView.ArchiveHandler archiveHandler) {
        this.archiveHandler = archiveHandler;

        PositionProperties positionProperties = GWT.create(PositionProperties.class);

        this.positionStore = new ListStore<Position>(positionProperties.id());

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

        view = new LiveGridView<Position>();
        view.setForceFit(true);
        view.setStripeRows(true);

        uiBinder.createAndBindUi(this);

        memoryProxy = new PagingMemoryProxy();
        loader = new PagingLoader<PagingLoadConfig, PagingLoadResult<Position>>(memoryProxy);
        loader.setRemoteSort(true);
        loader.addLoadHandler(new LoadHandler<PagingLoadConfig, PagingLoadResult<Position>>() {
            @Override
            public void onLoad(LoadEvent<PagingLoadConfig, PagingLoadResult<Position>> event) {
                view.setForceFit(true);
                view.refresh();
            }
        });

        grid.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                visible = event.getHeight() > 16;
                initialize();
            }
        });

        grid.setLoader(loader);

        new GridStateHandler<Position>(grid).loadState();

        grid.getSelectionModel().addSelectionChangedHandler(this);
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
    }

    void updateTotals(List<Position> positions) {
        double totalDistance = 0;
        double averageSpeed = 0;
        for (int i = 0; i < positions.size(); i++) {
            Position position = positions.get(i);
            totalDistance += position.getDistance();
            averageSpeed += position.getSpeed();
        }
        averageSpeed = averageSpeed / positions.size();
        this.totalDistance.setLabel(ApplicationContext.getInstance().getFormatterUtil().getDistanceFormat().format(totalDistance));
        this.averageSpeed.setLabel(ApplicationContext.getInstance().getFormatterUtil().getSpeedFormat().format(averageSpeed));
    }

    public VerticalLayoutContainer getContentPanel() {
        return contentPanel;
    }

    public void setPositions(List<Position> positions) {
        memoryProxy.setPositions(positions);
        initialized = false;
        initialize();
        updateTotals(positions);
    }

    public void selectPosition(Position position) {
        grid.getSelectionModel().select(position, false);
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Position> event) {
        if (event.getSelection().isEmpty()) {
            archiveHandler.onSelected(null);
        } else {
            archiveHandler.onSelected(event.getSelection().get(0));
        }
    }

    private boolean visible;
    private boolean initialized;
    private void initialize() {
        if (visible && !initialized) {
            loader.load(0, view.getCacheSize());
            initialized = true;
        }
    }
}
