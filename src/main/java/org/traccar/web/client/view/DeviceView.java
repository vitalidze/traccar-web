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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.ToStringValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreSortEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.theme.neptune.client.base.tabs.Css3TabPanelBottomAppearance;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel.TabPanelAppearance;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import com.sencha.gxt.widget.core.client.grid.*;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseStoreHandlers;
import org.traccar.web.client.model.DeviceProperties;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.state.DeviceVisibilityChangeHandler;
import org.traccar.web.client.state.DeviceVisibilityHandler;
import org.traccar.web.client.state.GridStateHandler;
import org.traccar.web.shared.model.Device;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.UserSettings;

public class DeviceView implements RowMouseDownEvent.RowMouseDownHandler, CellDoubleClickEvent.CellDoubleClickHandler {

    private static DeviceViewUiBinder uiBinder = GWT.create(DeviceViewUiBinder.class);

    interface DeviceViewUiBinder extends UiBinder<Widget, DeviceView> {
    }

    public interface DeviceHandler {
        void onSelected(Device device);
        void onAdd();
        void onEdit(Device device);
        void onShare(Device device);
        void onRemove(Device device);
        void onMouseOver(int mouseX, int mouseY, Device device);
        void onMouseOut(int mouseX, int mouseY, Device device);
        void doubleClicked(Device device);
        void onClearSelection();
    }

    public interface GeoFenceHandler {
        void onAdd();
        void onEdit(GeoFence geoFence);
        void onRemove(GeoFence geoFence);
        void onSelected(GeoFence geoFence);
        void onShare(GeoFence geoFence);
        void setGeoFenceListView(ListView<GeoFence, String> geoFenceListView);
    }

    public interface CommandHandler {
        void onCommand(Device device);
    }

    private static class GroupsHandler extends BaseStoreHandlers {
        private final ListStore<Device> deviceStore;
        private final ListStore<Group> groupStore;
        private final GroupingView<Device> view;
        private final ColumnConfig<Device, ?> groupColumn;
        private boolean grouped;

        private GroupsHandler(ListStore<Device> deviceStore,
                              ListStore<Group> groupStore,
                              final GroupingView<Device> view,
                              final ColumnConfig<Device, ?> groupColumn) {
            this.deviceStore = deviceStore;
            this.groupStore = groupStore;
            this.view = view;
            this.groupColumn = groupColumn;
        }

        public void init() {
            this.deviceStore.addStoreHandlers(this);
            this.groupStore.addStoreHandlers(this);
            if (refreshDevices(this.groupStore.getAll(), false)) {
                refreshView();
            }
            view.refresh(true);
            view.refresh(false);
        }

        @Override
        public void onAdd(StoreAddEvent event) {
            if (refreshDevices(event.getItems(), false)) {
                refreshView();
            }
        }

        @Override
        public void onUpdate(StoreUpdateEvent event) {
            if (refreshDevices(event.getItems(), false)) {
                refreshView();
            }
        }

        @Override
        public void onRemove(StoreRemoveEvent event) {
            if (refreshDevices(Collections.singletonList(event.getItem()), true)) {
                refreshView();
            }
        }

        boolean refreshDevices(List items, boolean remove) {
            boolean needRefresh = false;
            for (Object item : items) {
                if (item instanceof Group) {
                    Group group = (Group) item;
                    for (int i = 0; i < deviceStore.size(); i++) {
                        Device device = deviceStore.get(i);
                        if (device.getGroup() != null && device.getGroup().getId() == group.getId()) {
                            device.setGroup(remove ? null : group);
                            deviceStore.update(device);
                            needRefresh = true;
                        }
                    }
                }
                else if (!remove && item instanceof Device) {
                    if (grouped) {
                        view.groupBy(null);
                    }
                    needRefresh = true;
                }
            }
            return needRefresh;
        }

        void refreshView() {
            if (groupStore.size() == 0) {
                if (grouped) {
                    view.groupBy(null);
                    grouped = false;
                }
            } else {
                boolean doGroup = false;
                for (int i = 0; i < groupStore.size(); i++) {
                    Group group = groupStore.get(i);
                    for (int j = 0; j < deviceStore.size(); j++) {
                        if (group.equals(deviceStore.get(j).getGroup())) {
                            doGroup = true;
                            break;
                        }
                    }
                    if (doGroup) {
                        break;
                    }
                }
                if (doGroup) {
                    if (grouped) {
                        view.refresh(true);
                    }
                    view.groupBy(groupColumn);
                    grouped = true;
                } else {
                    if (grouped) {
                        view.groupBy(null);
                        groupColumn.setHidden(true);
                        view.refresh(true);
                        grouped = false;
                    }
                }
            }
        }
    }

    private static class DeviceGridView extends GroupingView<Device> {
        Messages i18n = GWT.create(Messages.class);

        final DeviceVisibilityHandler deviceVisibilityHandler;
        final ListStore<Group> groupStore;

        private DeviceGridView(DeviceVisibilityHandler deviceVisibilityHandler, ListStore<Group> groupStore) {
            this.deviceVisibilityHandler = deviceVisibilityHandler;
            this.groupStore = groupStore;
        }

        @Override
        protected Menu createContextMenu(int colIndex) {
            Menu menu = super.createContextMenu(colIndex);
            if (colIndex == 0) {
                CheckMenuItem idle = new CheckMenuItem(capitalize(i18n.idle()));
                idle.setChecked(!deviceVisibilityHandler.getHideIdle());
                idle.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideIdle(!deviceVisibilityHandler.getHideIdle());
                    }
                });
                menu.add(idle);

                CheckMenuItem moving = new CheckMenuItem(capitalize(i18n.moving()));
                moving.setChecked(!deviceVisibilityHandler.getHideMoving());
                moving.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideMoving(!deviceVisibilityHandler.getHideMoving());
                    }
                });
                menu.add(moving);

                CheckMenuItem offline = new CheckMenuItem(capitalize(i18n.offline()));
                offline.setChecked(!deviceVisibilityHandler.getHideOffline());
                offline.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideOffline(!deviceVisibilityHandler.getHideOffline());
                    }
                });
                menu.add(offline);

                CheckMenuItem online = new CheckMenuItem(capitalize(i18n.online()));
                online.setChecked(!deviceVisibilityHandler.getHideOnline());
                online.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideOnline(!deviceVisibilityHandler.getHideOnline());
                    }
                });
                menu.add(online);

                if (groupStore.size() > 0) {
                    MenuItem groups = new MenuItem(i18n.groups());
                    groups.setSubMenu(new Menu());
                    for (final Group group : groupStore.getAll()) {
                        CheckMenuItem groupItem = new CheckMenuItem(group.getName());
                        groupItem.setChecked(!deviceVisibilityHandler.isHiddenGroup(group.getId()));
                        groupItem.addSelectionHandler(new SelectionHandler<Item>() {
                            @Override
                            public void onSelection(SelectionEvent<Item> event) {
                                if (deviceVisibilityHandler.isHiddenGroup(group.getId())) {
                                    deviceVisibilityHandler.removeHiddenGroup(group.getId());
                                } else {
                                    deviceVisibilityHandler.addHiddenGroup(group.getId());
                                }
                            }
                        });
                        groups.getSubMenu().add(groupItem);
                    }
                    menu.add(groups);
                }
            }
            return menu;
        }

        private String capitalize(String s) {
            return Character.isUpperCase(s.charAt(0))
                    ? s
                    : (Character.toUpperCase(s.charAt(0)) + s.substring(1, s.length()));
        }
    }

    private final DeviceHandler deviceHandler;

    private final GeoFenceHandler geoFenceHandler;

    private final CommandHandler commandHandler;

    private final GroupsHandler groupsHandler;

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    @UiField
    ToolBar toolbar;

    @UiField
    TextButton addButton;

    @UiField
    TextButton editButton;

    @UiField
    TextButton shareButton;

    @UiField
    TextButton removeButton;

    @UiField
    TextButton commandButton;

    @UiField(provided = true)
    TabPanel objectsTabs;

    @UiField(provided = true)
    ColumnModel<Device> columnModel;

    @UiField(provided = true)
    final ListStore<Device> deviceStore;

    @UiField(provided = true)
    StoreFilterField<Device> deviceFilter;

    @UiField
    Grid<Device> grid;

    @UiField(provided = true)
    GroupingView<Device> view;

    @UiField(provided = true)
    TabItemConfig geoFencesTabConfig;

    @UiField(provided = true)
    ListStore<GeoFence> geoFenceStore;

    @UiField(provided = true)
    ListView<GeoFence, String> geoFenceList;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public DeviceView(final DeviceHandler deviceHandler,
                      final GeoFenceHandler geoFenceHandler,
                      final CommandHandler commandHandler,
                      final DeviceVisibilityHandler deviceVisibilityHandler,
                      final ListStore<Device> deviceStore,
                      final ListStore<GeoFence> geoFenceStore,
                      ListStore<Group> groupStore) {
        this.deviceHandler = deviceHandler;
        this.geoFenceHandler = geoFenceHandler;
        this.commandHandler = commandHandler;
        this.geoFenceStore = geoFenceStore;

        // create a new devices store so the filtering will not affect global store
        this.deviceStore = new ListStore<>(deviceStore.getKeyProvider());
        this.deviceStore.setAutoCommit(true);
        this.deviceStore.addAll(deviceStore.getAll());
        // handle events from global devices store and update local one accordingly
        deviceStore.addStoreHandlers(new BaseStoreHandlers<Device>() {
            @Override
            public void onAdd(StoreAddEvent<Device> event) {
                DeviceView.this.deviceStore.addAll(event.getIndex(), event.getItems());
            }

            @Override
            public void onUpdate(StoreUpdateEvent<Device> event) {
                for (Device device : event.getItems()) {
                    DeviceView.this.deviceStore.update(device);
                }
            }

            @Override
            public void onRemove(StoreRemoveEvent<Device> event) {
                DeviceView.this.deviceStore.remove(event.getIndex());
            }
        });
        // handle sort events from local devices store and sort global one accordingly
        this.deviceStore.addStoreSortHandler(new StoreSortEvent.StoreSortHandler<Device>() {
            @Override
            public void onSort(StoreSortEvent<Device> event) {
                deviceStore.clearSortInfo();
                for (Store.StoreSortInfo<Device> sortInfo : event.getSource().getSortInfo()) {
                    deviceStore.addSortInfo(sortInfo);
                }
            }
        });

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        Resources resources = GWT.create(Resources.class);
        HeaderIconTemplate headerTemplate = GWT.create(HeaderIconTemplate.class);

        List<ColumnConfig<Device, ?>> columnConfigList = new LinkedList<>();

        // 'Visible' column
        ColumnConfig<Device, Boolean> colVisible = new ColumnConfig<>(new ValueProvider<Device, Boolean>() {
            @Override
            public Boolean getValue(Device device) {
                return deviceVisibilityHandler.isVisible(device);
            }

            @Override
            public void setValue(Device device, Boolean value) {
                deviceVisibilityHandler.setVisible(device, value);
            }

            @Override
            public String getPath() {
                return "visible";
            }
        }, 50, headerTemplate.render(AbstractImagePrototype.create(resources.eye()).getSafeHtml()));
        colVisible.setCell(new CheckBoxCell());
        colVisible.setFixed(true);
        colVisible.setResizable(false);
        colVisible.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.visible()).toSafeHtml());
        columnConfigList.add(colVisible);

        // handle visibility change events
        deviceVisibilityHandler.addVisibilityChangeHandler(new DeviceVisibilityChangeHandler() {
            @Override
            public void visibilityChanged(Long deviceId, boolean visible) {
                Device device = deviceStore.findModelWithKey(deviceId.toString());
                deviceStore.update(device);
            }
        });

        // Name column
        ColumnConfig<Device, String> colName = new ColumnConfig<>(deviceProperties.name(), 0, i18n.name());
        colName.setCell(new AbstractCell<String>(BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT) {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb) {
                if (value == null) return;
                sb.appendEscaped(value);
            }

            @Override
            public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
                if (event.getType().equals(BrowserEvents.MOUSEOVER) || event.getType().equals(BrowserEvents.MOUSEOUT)) {
                    Element target = Element.as(event.getEventTarget());
                    int rowIndex = grid.getView().findRowIndex(target);
                    if (rowIndex != -1) {
                        if (event.getType().equals(BrowserEvents.MOUSEOVER)) {
                            deviceHandler.onMouseOver(event.getClientX(), event.getClientY(), deviceStore.get(rowIndex));
                        } else {
                            deviceHandler.onMouseOut(event.getClientX(), event.getClientY(), deviceStore.get(rowIndex));
                        }
                    }
                } else {
                    super.onBrowserEvent(context, parent, value, event, valueUpdater);
                }
            }
        });
        columnConfigList.add(colName);

        // 'Follow' column
        ColumnConfig<Device, Boolean> colFollow = new ColumnConfig<>(new ValueProvider<Device, Boolean>() {

            @Override
            public Boolean getValue(Device device) {
                return ApplicationContext.getInstance().isFollowing(device);
            }

            @Override
            public void setValue(Device device, Boolean value) {
                if (value) {
                    ApplicationContext.getInstance().follow(device);
                } else {
                    ApplicationContext.getInstance().stopFollowing(device);
                }
            }

            @Override
            public String getPath() {
                return "follow";
            }
        }, 50, headerTemplate.render(AbstractImagePrototype.create(resources.follow()).getSafeHtml()));
        colFollow.setCell(new CheckBoxCell());
        colFollow.setFixed(true);
        colFollow.setResizable(false);
        colFollow.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.follow()).toSafeHtml());
        columnConfigList.add(colFollow);

        // 'Record trace' column
        ColumnConfig<Device, Boolean> colRecordTrace = new ColumnConfig<>(new ValueProvider<Device, Boolean>() {
            @Override
            public Boolean getValue(Device device) {
                return ApplicationContext.getInstance().isRecordingTrace(device);
            }

            @Override
            public void setValue(Device device, Boolean value) {
                if (value) {
                    ApplicationContext.getInstance().recordTrace(device);
                } else {
                    ApplicationContext.getInstance().stopRecordingTrace(device);
                }
            }

            @Override
            public String getPath() {
                return "recordTrace";
            }
        }, 50, headerTemplate.render(AbstractImagePrototype.create(resources.footprints()).getSafeHtml()));
        colRecordTrace.setCell(new CheckBoxCell());
        colRecordTrace.setFixed(true);
        colRecordTrace.setResizable(false);
        colRecordTrace.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.recordTrace()).toSafeHtml());
        columnConfigList.add(colRecordTrace);

        ColumnConfig<Device, String> colGroup = new ColumnConfig<>(new ToStringValueProvider<Device>() {
            @Override
            public String getValue(Device device) {
                return device.getGroup() == null ? i18n.noGroup() : device.getGroup().getName();
            }
        }, 0, i18n.group());
        colGroup.setHidden(true);
        columnConfigList.add(colGroup);

        view = new DeviceGridView(deviceVisibilityHandler, groupStore);
        view.setStripeRows(true);
        view.setShowGroupedColumn(false);
        view.setEnableNoGroups(true);
        view.setEnableGroupingMenu(false);
        groupsHandler = new GroupsHandler(deviceStore, groupStore, view, colGroup);

        columnModel = new ColumnModel<>(columnConfigList);

        // configure device store filtering
        deviceFilter = new StoreFilterField<Device>() {
            @Override
            protected boolean doSelect(Store<Device> store, Device parent, Device item, String filter) {
                return filter.trim().isEmpty() ||
                        item.getName().toLowerCase().contains(filter.toLowerCase());
            }
        };
        deviceFilter.bind(this.deviceStore);

        // geo-fences
        geoFencesTabConfig = new TabItemConfig(i18n.overlayType(UserSettings.OverlayType.GEO_FENCES));
        
        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);

        geoFenceList = new ListView<GeoFence, String>(geoFenceStore, geoFenceProperties.name()) {
            @Override
            protected void onMouseDown(Event e) {
                int index = indexOf(e.getEventTarget().<Element>cast());
                if (index != -1) {
                    geoFenceHandler.onSelected(geoFenceList.getStore().get(index));
                }
                super.onMouseDown(e);
            }
        };
        geoFenceList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        geoFenceList.getSelectionModel().addSelectionChangedHandler(geoFenceSelectionHandler);

        geoFenceHandler.setGeoFenceListView(geoFenceList);

        // tab panel
        objectsTabs = new TabPanel(GWT.<TabPanelAppearance>create(Css3TabPanelBottomAppearance.class));

        uiBinder.createAndBindUi(this);

        grid.getSelectionModel().addSelectionChangedHandler(deviceSelectionHandler);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.addRowMouseDownHandler(this);
        grid.addCellDoubleClickHandler(this);

        view.setAutoFill(true);
        view.setForceFit(true);

        new GridStateHandler<>(grid).loadState();

        GridEditing<Device> editing = new GridInlineEditing<>(grid);
        view.setShowDirtyCells(false);
        editing.addEditor(colFollow, new CheckBox());
        editing.addEditor(colRecordTrace, new CheckBox());

        boolean readOnly = ApplicationContext.getInstance().getUser().getReadOnly();
        boolean admin = ApplicationContext.getInstance().getUser().getAdmin();
        boolean manager = ApplicationContext.getInstance().getUser().getManager();

        shareButton.setVisible(!readOnly && (admin || manager));

        addButton.setVisible(!readOnly);
        editButton.setVisible(!readOnly);
        removeButton.setVisible(!readOnly);
        commandButton.setVisible(!readOnly);
        toggleManagementButtons(null);
    }

    final SelectionChangedEvent.SelectionChangedHandler<Device> deviceSelectionHandler = new SelectionChangedEvent.SelectionChangedHandler<Device>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<Device> event) {
            toggleManagementButtons(event.getSelection().isEmpty() ? null : event.getSelection().get(0));
        }
    };

    final SelectionChangedEvent.SelectionChangedHandler<GeoFence> geoFenceSelectionHandler = new SelectionChangedEvent.SelectionChangedHandler<GeoFence>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<GeoFence> event) {
            toggleManagementButtons(event.getSelection().isEmpty() ? null : event.getSelection().get(0));
            geoFenceHandler.onSelected(event.getSelection().isEmpty() ? null : event.getSelection().get(0));
        }
    };

    @Override
    public void onRowMouseDown(RowMouseDownEvent event) {
        Device device = grid.getSelectionModel().getSelectedItem();
        if (device != null) {
            deviceHandler.onSelected(device);
        }
    }

    @Override
    public void onCellClick(CellDoubleClickEvent cellDoubleClickEvent) {
        deviceHandler.doubleClicked(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        if (editingGeoFences()) {
            geoFenceHandler.onAdd();
        } else {
            deviceHandler.onAdd();
        }
    }

    @UiHandler("editButton")
    public void onEditClicked(SelectEvent event) {
        if (editingGeoFences()) {
            geoFenceHandler.onEdit(geoFenceList.getSelectionModel().getSelectedItem());
        } else {
            deviceHandler.onEdit(grid.getSelectionModel().getSelectedItem());
        }
    }

    @UiHandler("shareButton")
    public void onShareClicked(SelectEvent event) {
        if (editingGeoFences()) {
            geoFenceHandler.onShare(geoFenceList.getSelectionModel().getSelectedItem());
        } else {
            deviceHandler.onShare(grid.getSelectionModel().getSelectedItem());
        }
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        if (editingGeoFences()) {
            geoFenceHandler.onRemove(geoFenceList.getSelectionModel().getSelectedItem());
        } else {
            deviceHandler.onRemove(grid.getSelectionModel().getSelectedItem());
        }
    }

    @UiHandler("commandButton")
    public void onCommandClicked(SelectEvent event) {
        commandHandler.onCommand(grid.getSelectionModel().getSelectedItem());
    }

    public void selectDevice(Device device) {
        grid.getSelectionModel().select(deviceStore.findModel(device), false);
        deviceHandler.onSelected(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("objectsTabs")
    public void onTabSelected(SelectionEvent<Widget> event) {
        if (event.getSelectedItem() == geoFenceList) {
            grid.getSelectionModel().deselectAll();
            deviceHandler.onClearSelection();
        } else {
            geoFenceList.getSelectionModel().deselectAll();
        }
        toggleManagementButtons(null);
    }

    private boolean editingGeoFences() {
        return objectsTabs.getActiveWidget() == geoFenceList;
    }

    private void toggleManagementButtons(Object selection) {
        boolean admin = ApplicationContext.getInstance().getUser().getAdmin();
        boolean manager = ApplicationContext.getInstance().getUser().getManager();
        boolean allowDeviceManagement = !ApplicationContext.getInstance().getApplicationSettings().isDisallowDeviceManagementByUsers();
        boolean backendApiAvailable = ApplicationContext.getInstance().isBackendApiAvailable();

        addButton.setEnabled(allowDeviceManagement || editingGeoFences() || admin || manager);
        editButton.setEnabled(selection != null && (allowDeviceManagement || editingGeoFences() || admin || manager));
        removeButton.setEnabled(selection != null && (allowDeviceManagement || editingGeoFences() || admin || manager));
        commandButton.setEnabled(selection != null && backendApiAvailable && !editingGeoFences() && (allowDeviceManagement || admin || manager));
        shareButton.setEnabled(selection != null);
    }

    interface HeaderIconTemplate extends XTemplates {
        @XTemplate("<div style=\"text-align:center;\">{img}</div>")
        SafeHtml render(SafeHtml img);
    }

    interface Resources extends ClientBundle {
        @Source("org/traccar/web/client/theme/icon/eye.png")
        ImageResource eye();

        @Source("org/traccar/web/client/theme/icon/follow.png")
        ImageResource follow();

        @Source("org/traccar/web/client/theme/icon/footprints.png")
        ImageResource footprints();
    }

    public void groupDevices() {
        groupsHandler.init();
        // ugly workaround to deal with wrong sizes of columns after grouping
        Timer t = new Timer() {
            @Override
            public void run() {
                view.refresh(true);
            }
        };
        t.schedule(500);
    }
}
