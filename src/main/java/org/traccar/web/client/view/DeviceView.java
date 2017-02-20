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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ToStringValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.theme.neptune.client.base.tabs.Css3TabPanelBottomAppearance;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.TabPanel.TabPanelAppearance;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridView;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseStoreHandlers;
import org.traccar.web.client.model.DeviceStore;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.state.DeviceVisibilityChangeHandler;
import org.traccar.web.client.state.DeviceVisibilityHandler;
import org.traccar.web.shared.model.*;

import java.util.*;

public class DeviceView implements RowMouseDownEvent.RowMouseDownHandler, CellDoubleClickEvent.CellDoubleClickHandler {

    private static DeviceViewUiBinder uiBinder = GWT.create(DeviceViewUiBinder.class);

    interface DeviceViewUiBinder extends UiBinder<Widget, DeviceView> {
    }

    public interface DeviceHandler {
        void onSelected(Device device);
        void onSelected(Device device, boolean zoomIn);
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
        private final ListStore<Device> globalDeviceStore;
        private final DeviceStore deviceStore;
        private final GroupStore groupStore;
        private final Set<Device> pendingDevices = new LinkedHashSet<>();

        private GroupsHandler(ListStore<Device> globalDeviceStore,
                              DeviceStore deviceStore,
                              GroupStore groupStore) {
            this.globalDeviceStore = globalDeviceStore;
            this.deviceStore = deviceStore;
            this.groupStore = groupStore;

            this.globalDeviceStore.addStoreHandlers(this);
            this.groupStore.addStoreHandlers(this);
        }

        @Override
        public void onAdd(StoreAddEvent event) {
            groupsAdded(groups(event.getItems()));
            devicesAdded(devices(event.getItems()));
        }

        @Override
        public void onUpdate(StoreUpdateEvent event) {
            groupsUpdated(groups(event.getItems()));
            devicesUpdated(devices(event.getItems()));
        }

        @Override
        public void onRemove(StoreRemoveEvent event) {
            GroupedDevice node = (GroupedDevice) event.getItem();
            if (deviceStore.contains(node)) {
                pendingDevices.addAll(devices(node));
                Group parent = (Group) deviceStore.getParent(node);
                deviceStore.remove(node);
                if (parent != null) {
                    removeGroupsIfEmpty(parent);
                }
            }
        }

        List<Group> groups(List items) {
            return items.isEmpty() || !(items.get(0) instanceof Group) ? Collections.<Group>emptyList() : (List<Group>) items;
        }

        List<Device> devices(List items) {
            return items.isEmpty() || !(items.get(0) instanceof Device) ? Collections.<Device>emptyList() : (List<Device>) items;
        }

        void groupsAdded(List<Group> groups) {
            List<Device> withoutGroups = new ArrayList<>();
            for (Iterator<Device> it = pendingDevices.iterator(); it.hasNext(); ) {
                Device device = it.next();
                if (device.getGroup() == null) {
                    withoutGroups.add(device);
                } else {
                    if (addDeviceGroups(device)) {
                        deviceStore.add(device.getGroup(), device);
                        it.remove();
                    }
                }
            }
            if (withoutGroups.size() == pendingDevices.size()) {
                for (Device device : pendingDevices) {
                    deviceStore.add(device);
                }
                pendingDevices.clear();
            }
        }

        void groupsUpdated(List<Group> groups) {
            Set<Device> devicesToUpdate = new HashSet<>();
            for (Group group : groups) {
                if (!deviceStore.contains(group)) {
                    continue;
                }
                // check parents
                Group oldParent = (Group) deviceStore.getParent(group);
                Group newParent = groupStore.getParent(group);
                if (Objects.equals(oldParent, newParent)) {
                    deviceStore.update(group);
                } else {
                    devicesToUpdate.addAll(devices(group));
                    groupStore.remove(group);
                }
            }

            devicesAdded(new ArrayList<>(devicesToUpdate));
        }

        List<Device> devices(GroupedDevice node) {
            if (deviceStore.contains(node)) {
                List<Device> result = new ArrayList<>();
                for (GroupedDevice child : deviceStore.getAllChildren(node)) {
                    if (child instanceof Device) {
                        result.add((Device) child);
                    }
                }
                return result;
            } else {
                return Collections.emptyList();
            }
        }

        boolean addDeviceGroups(Device device) {
            return addGroupsHierarchy(device.getGroup());
        }

        boolean addGroupsHierarchy(Group group) {
            List<Group> groupsHierarchy = new ArrayList<>();
            Group nextParent = group;
            while (nextParent != null) {
                if (!groupStore.contains(nextParent)) {
                    return false;
                }
                groupsHierarchy.add(0, nextParent);
                nextParent = groupStore.getParent(nextParent);
            }
            for (Group nextGroup : groupsHierarchy) {
                if (!deviceStore.contains(nextGroup)) {
                    Group parent = groupStore.getParent(nextGroup);
                    if (parent == null) {
                        deviceStore.add(nextGroup);
                    } else {
                        deviceStore.add(parent, nextGroup);
                    }
                }
            }
            return true;
        }

        void devicesAdded(List<Device> devices) {
            Comparator<GroupedDevice> byName = new Comparator<GroupedDevice>() {
                @Override
                public int compare(GroupedDevice o1, GroupedDevice o2) {
                    String n1 = o1.getName() == null ? "" : o1.getName();
                    String n2 = o2.getName() == null ? "" : o2.getName();
                    return n1.compareTo(n2);
                }
            };

            Map<Group, List<Device>> byGroup = new HashMap<>();
            List<Device> deviceWithoutGroups = new ArrayList<>();
            for (Device device : devices) {
                if (device.getGroup() == null) {
                    deviceWithoutGroups.add(device);
                } else {
                    List<Device> groupDevices = byGroup.get(device.getGroup());
                    if (groupDevices == null) {
                        groupDevices = new ArrayList<>();
                        byGroup.put(device.getGroup(), groupDevices);
                    }
                    groupDevices.add(device);
                }
            }

            List<Group> sortedGroups = new ArrayList<>(byGroup.keySet());
            Collections.sort(sortedGroups, byName);
            Collections.sort(deviceWithoutGroups, byName);

            for (Group group : sortedGroups) {
                List<Device> groupDevices = byGroup.get(group);
                Collections.sort(groupDevices, byName);
                if (addGroupsHierarchy(group)) {
                    for (Device device : groupDevices) {
                        deviceStore.add(group, device);
                    }
                } else {
                    pendingDevices.addAll(groupDevices);
                }
            }

            if (pendingDevices.isEmpty()) {
                for (Device device : deviceWithoutGroups) {
                    deviceStore.add(device);
                }
            } else {
                pendingDevices.addAll(deviceWithoutGroups);
            }
        }

        void devicesUpdated(List<Device> devices) {
            for (Device device : devices) {
                if (deviceStore.contains(device)) {
                    Group oldGroup = (Group) deviceStore.getParent(device);
                    Group newGroup = device.getGroup();
                    if (Objects.equals(oldGroup, newGroup)) {
                        deviceStore.update(device);
                    } else {
                        deviceStore.remove(device);
                        devicesAdded(Collections.singletonList(device));
                        removeGroupsIfEmpty(oldGroup);
                    }
                } else {
                    if (device.getGroup() == null) {
                        deviceStore.add(device);
                    } else {
                        devicesAdded(Collections.singletonList(device));
                    }
                }
            }
        }

        void removeGroupsIfEmpty(Group group) {
            Group parent = group;
            while (parent != null && devices(parent).isEmpty()) {
                Group nextParent = (Group) deviceStore.getParent(parent);
                deviceStore.remove(parent);
                parent = nextParent;
            }
        }
    }

    private static class DeviceGridView extends TreeGridView<GroupedDevice> {
        Messages i18n = GWT.create(Messages.class);

        final DeviceVisibilityHandler deviceVisibilityHandler;
        final GroupStore groupStore;

        private DeviceGridView(DeviceVisibilityHandler deviceVisibilityHandler, GroupStore groupStore) {
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

                List<Group> groupsList = groupStore.toList();
                if (!groupsList.isEmpty()) {
                    MenuItem groups = new MenuItem(i18n.groups());
                    groups.setSubMenu(new Menu());
                    for (final Group group : groupsList) {
                        SafeHtmlBuilder name = new SafeHtmlBuilder();
                        for (int i = 0; i < groupStore.getDepth(group); i++) {
                            name.appendHtmlConstant("&nbsp;&nbsp;");
                        }
                        name.appendEscaped(group.getName());
                        CheckMenuItem groupItem = new CheckMenuItem();
                        groupItem.setHTML(name.toSafeHtml());
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

    private static class DeviceOnlyCheckBoxCell extends CheckBoxCell {
        private final DeviceStore deviceStore;

        private DeviceOnlyCheckBoxCell(DeviceStore deviceStore) {
            this.deviceStore = deviceStore;
        }

        @Override
        public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
            GroupedDevice item = deviceStore.findModelWithKey((String) context.getKey());

            if (item instanceof Device) {
                super.render(context, value, sb);
            }
        }
    }

    private final DeviceHandler deviceHandler;

    private final GeoFenceHandler geoFenceHandler;

    private final CommandHandler commandHandler;

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

    ColumnModel<GroupedDevice> columnModel;

    final DeviceStore deviceStore;

    @UiField(provided = true)
    StoreFilterField<GroupedDevice> deviceFilter;

    @UiField(provided = true)
    TreeGrid<GroupedDevice> grid;

    TreeGridView<GroupedDevice> view;

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
                      final ListStore<Device> globalDeviceStore,
                      final ListStore<GeoFence> geoFenceStore,
                      GroupStore groupStore,
                      final ListStore<Report> reportStore,
                      final ReportsMenu.ReportHandler reportHandler) {
        this.deviceHandler = deviceHandler;
        this.geoFenceHandler = geoFenceHandler;
        this.commandHandler = commandHandler;
        this.geoFenceStore = geoFenceStore;

        // create a new devices store so the filtering will not affect global store
        this.deviceStore = new DeviceStore(groupStore, globalDeviceStore);
        this.deviceStore.setAutoCommit(true);

        Resources resources = GWT.create(Resources.class);
        HeaderIconTemplate headerTemplate = GWT.create(HeaderIconTemplate.class);

        List<ColumnConfig<GroupedDevice, ?>> columnConfigList = new LinkedList<>();

        // 'Visible' column
        ColumnConfig<GroupedDevice, Boolean> colVisible = new ColumnConfig<>(new ValueProvider<GroupedDevice, Boolean>() {
            @Override
            public Boolean getValue(GroupedDevice node) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    return deviceVisibilityHandler.isVisible(device);
                }
                return true;
            }

            @Override
            public void setValue(GroupedDevice node, Boolean value) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    deviceVisibilityHandler.setVisible(device, value);
                }
            }

            @Override
            public String getPath() {
                return "visible";
            }
        }, 50, headerTemplate.render(AbstractImagePrototype.create(resources.eye()).getSafeHtml()));
        colVisible.setCell(new DeviceOnlyCheckBoxCell(deviceStore));
        colVisible.setFixed(true);
        colVisible.setResizable(false);
        colVisible.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.visible()).toSafeHtml());
        columnConfigList.add(colVisible);

        // handle visibility change events
        deviceVisibilityHandler.addVisibilityChangeHandler(new DeviceVisibilityChangeHandler() {
            @Override
            public void visibilityChanged(Long deviceId, boolean visible) {
                Device device = globalDeviceStore.findModelWithKey(deviceId.toString());
                globalDeviceStore.update(device);
            }
        });

        // Name column
        ColumnConfig<GroupedDevice, String> colName = new ColumnConfig<>(new ToStringValueProvider<GroupedDevice>() {
            @Override
            public String getValue(GroupedDevice object) {
                return object.getName();
            }

            @Override
            public String getPath() {
                return "name";
            }
        }, 0, i18n.name());
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
                    Tree.TreeNode<GroupedDevice> node = grid.findNode(target);
                    if (node != null && node.getModel() instanceof Device) {
                        Device device = (Device) node.getModel();
                        if (event.getType().equals(BrowserEvents.MOUSEOVER)) {
                            deviceHandler.onMouseOver(event.getClientX(), event.getClientY(), device);
                        } else {
                            deviceHandler.onMouseOut(event.getClientX(), event.getClientY(), device);
                        }
                    }
                } else {
                    super.onBrowserEvent(context, parent, value, event, valueUpdater);
                }
            }
        });
        columnConfigList.add(colName);

        // 'Follow' column
        ColumnConfig<GroupedDevice, Boolean> colFollow = new ColumnConfig<>(new ValueProvider<GroupedDevice, Boolean>() {

            @Override
            public Boolean getValue(GroupedDevice node) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    return ApplicationContext.getInstance().isFollowing(deviceStore.getDevice(device));
                }
                return false;
            }

            @Override
            public void setValue(GroupedDevice node, Boolean value) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    if (value) {
                        ApplicationContext.getInstance().follow(device);
                    } else {
                        ApplicationContext.getInstance().stopFollowing(device);
                    }
                }
            }

            @Override
            public String getPath() {
                return "follow";
            }
        }, 50, headerTemplate.render(AbstractImagePrototype.create(resources.follow()).getSafeHtml()));
        colFollow.setCell(new DeviceOnlyCheckBoxCell(deviceStore));
        colFollow.setFixed(true);
        colFollow.setResizable(false);
        colFollow.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.follow()).toSafeHtml());
        columnConfigList.add(colFollow);

        // 'Record trace' column
        ColumnConfig<GroupedDevice, Boolean> colRecordTrace = new ColumnConfig<>(new ValueProvider<GroupedDevice, Boolean>() {
            @Override
            public Boolean getValue(GroupedDevice node) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    return ApplicationContext.getInstance().isRecordingTrace(device);
                }
                return false;
            }

            @Override
            public void setValue(GroupedDevice node, Boolean value) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    if (value) {
                        ApplicationContext.getInstance().recordTrace(device);
                    } else {
                        ApplicationContext.getInstance().stopRecordingTrace(device);
                    }
                }
            }

            @Override
            public String getPath() {
                return "recordTrace";
            }
        }, 50, headerTemplate.render(AbstractImagePrototype.create(resources.footprints()).getSafeHtml()));
        colRecordTrace.setCell(new DeviceOnlyCheckBoxCell(deviceStore));
        colRecordTrace.setFixed(true);
        colRecordTrace.setResizable(false);
        colRecordTrace.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.recordTrace()).toSafeHtml());
        columnConfigList.add(colRecordTrace);

        for (ColumnConfig<GroupedDevice, ?> col : columnConfigList) {
            col.setSortable(false);
        }

        view = new DeviceGridView(deviceVisibilityHandler, groupStore);
        view.setStripeRows(true);
        new GroupsHandler(globalDeviceStore, deviceStore, groupStore);

        columnModel = new ColumnModel<>(columnConfigList);

        grid = new TreeGrid<GroupedDevice>(deviceStore, columnModel, colName) {
            @Override
            protected void onRightClick(Event event) {
                EventTarget eventTarget = event.getEventTarget();
                List<GroupedDevice> selectedItems = getSelectionModel().getSelectedItems();
                boolean onSelectedRow = false;
                for (GroupedDevice selectedItem : selectedItems) {
                    if (deviceStore.isDevice(selectedItem)) {
                        int index = store.indexOf(selectedItem);
                        Element selectedRow = getView().getRow(index);
                        if (selectedRow.isOrHasChild(XElement.as(eventTarget))) {
                            onSelectedRow = true;
                            break;
                        }
                    }
                }

                if (onSelectedRow) {
                    super.onRightClick(event);
                }
            }
        };
        grid.setView(view);
        grid.setContextMenu(createDeviceGridContextMenu(reportStore, reportHandler));

        // configure device store filtering
        deviceFilter = new StoreFilterField<GroupedDevice>() {
            @Override
            protected boolean doSelect(Store<GroupedDevice> store, GroupedDevice parent, GroupedDevice item, String filter) {
                return filter.trim().isEmpty() || matches(item, filter);
            }

            boolean matches(GroupedDevice item, String filter) {
                if (deviceStore.isGroup(item)) {
                    for (GroupedDevice child : deviceStore.getChildren(item)) {
                        if (matches(child, filter)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return item.getName().toLowerCase().contains(filter.toLowerCase());
                }
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
        grid.setAutoExpand(true);

        view.setAutoFill(true);
        view.setForceFit(true);
        view.setSortingEnabled(false);

        GridEditing<GroupedDevice> editing = new GridInlineEditing<>(grid);
        view.setShowDirtyCells(false);
        editing.addEditor(colFollow, new CheckBox());
        editing.addEditor(colRecordTrace, new CheckBox());

        boolean readOnly = ApplicationContext.getInstance().getUser().getReadOnly();

        shareButton.setVisible(allowDeviceSharing());

        addButton.setVisible(!readOnly);
        editButton.setVisible(!readOnly);
        removeButton.setVisible(!readOnly);
        commandButton.setVisible(allowCommandsSending());
        toggleManagementButtons(null);
    }

    final SelectionChangedEvent.SelectionChangedHandler<GroupedDevice> deviceSelectionHandler = new SelectionChangedEvent.SelectionChangedHandler<GroupedDevice>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<GroupedDevice> event) {
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
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (node != null && deviceStore.isDevice(node)) {
            deviceHandler.onSelected(deviceStore.getDevice(node), true);
        }
    }

    @Override
    public void onCellClick(CellDoubleClickEvent cellDoubleClickEvent) {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.doubleClicked(deviceStore.getDevice(node));
        }
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
            editDevice();
        }
    }

    private void editDevice() {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.onEdit(deviceStore.getDevice(node));
        }
    }

    @UiHandler("shareButton")
    public void onShareClicked(SelectEvent event) {
        if (editingGeoFences()) {
            geoFenceHandler.onShare(geoFenceList.getSelectionModel().getSelectedItem());
        } else {
            shareDevice();
        }
    }

    private void shareDevice() {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.onShare(deviceStore.getDevice(node));
        }
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        if (editingGeoFences()) {
            geoFenceHandler.onRemove(geoFenceList.getSelectionModel().getSelectedItem());
        } else {
           removeDevice();
        }
    }

    private void removeDevice() {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.onRemove(deviceStore.getDevice(node));
        }
    }

    @UiHandler("commandButton")
    public void onCommandClicked(SelectEvent event) {
        sendCommand();
    }

    private void sendCommand() {
        commandHandler.onCommand(deviceStore.getDevice(grid.getSelectionModel().getSelectedItem()));
    }

    public void selectDevice(Device device) {
        GroupedDevice item = deviceStore.findModel(device);
        grid.getSelectionModel().select(item, false);
        grid.getView().focusRow(grid.getStore().indexOf(item));
        deviceHandler.onSelected((Device) grid.getSelectionModel().getSelectedItem());
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
        if (selection instanceof Group) {
            selection = null;
        }

        addButton.setEnabled(allowDeviceManagement() || editingGeoFences());
        editButton.setEnabled(selection != null && (allowDeviceManagement() || editingGeoFences()));
        removeButton.setEnabled(selection != null && (allowDeviceManagement() || editingGeoFences()));
        commandButton.setEnabled(selection != null && !editingGeoFences() && allowCommandsSending() && allowDeviceManagement());
        shareButton.setEnabled(selection != null);
    }

    private boolean allowDeviceManagement() {
        boolean readOnly = ApplicationContext.getInstance().getUser().getReadOnly();
        if (!readOnly) {
            boolean admin = ApplicationContext.getInstance().getUser().getAdmin();
            boolean manager = ApplicationContext.getInstance().getUser().getManager();
            return admin || manager || !ApplicationContext.getInstance().getApplicationSettings().isDisallowDeviceManagementByUsers();
        }
        return false;
    }

    private boolean allowCommandsSending() {
        boolean readOnly = ApplicationContext.getInstance().getUser().getReadOnly();
        if (!readOnly) {
            boolean admin = ApplicationContext.getInstance().getUser().getAdmin();
            return admin || !ApplicationContext.getInstance().getApplicationSettings().isAllowCommandsOnlyForAdmins();
        }
        return false;
    }

    private boolean allowDeviceSharing() {
        boolean readOnly = ApplicationContext.getInstance().getUser().getReadOnly();
        if (!readOnly) {
            return ApplicationContext.getInstance().getUser().getAdmin()
                    || ApplicationContext.getInstance().getUser().getManager();
        }
        return false;
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

    private Menu createDeviceGridContextMenu(final ListStore<Report> reportStore,
                                             final ReportsMenu.ReportHandler reportHandler) {
        Menu menu = new Menu();
        if (allowDeviceManagement()) {
            MenuItem edit = new MenuItem(i18n.edit());
            edit.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    editDevice();
                }
            });
            menu.add(edit);
        }
        if (allowDeviceSharing()) {
            MenuItem share = new MenuItem(i18n.share());
            share.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    shareDevice();
                }
            });
            menu.add(share);
        }
        if (allowDeviceManagement()) {
            MenuItem remove = new MenuItem(i18n.remove());
            remove.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    removeDevice();
                }
            });
            menu.add(remove);
        }
        if (allowCommandsSending()) {
            MenuItem command = new MenuItem(i18n.command());
            command.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    sendCommand();
                }
            });
            menu.add(command);
        }

        MenuItem report = new MenuItem(i18n.report());
        report.setSubMenu(new ReportsMenu(reportStore, reportHandler, new ReportsMenu.ReportSettingsHandler() {
            @Override
            public void setSettings(ReportsDialog dialog) {
                GroupedDevice node = grid.getSelectionModel().getSelectedItem();
                if (deviceStore.isDevice(node)) {
                    dialog.selectDevice((Device) node);
                }
            }
        }));
        menu.add(report);

        return menu;
    }
}
