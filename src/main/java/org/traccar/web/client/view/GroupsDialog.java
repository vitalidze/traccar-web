/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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

import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.dnd.core.client.*;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;
import org.traccar.web.client.controller.GroupsController;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.GroupProperties;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.shared.model.Group;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import java.util.*;

public class GroupsDialog implements SelectionChangedEvent.SelectionChangedHandler<Group> {

    private static GroupsDialogUiBinder uiBinder = GWT.create(GroupsDialogUiBinder.class);

    interface GroupsDialogUiBinder extends UiBinder<Widget, GroupsDialog> {
    }

    public interface GroupsHandler {
        void onAdd(Group parent, Group group, GroupsController.GroupAddHandler groupsHandler);
        void onSave(GroupsController.ChangesSaveHandler groupsHandler);
        void onRemove(Group group);
        void onCancelSaving(List<Group> newGroups);
        void onShare(Group group);
    }

    private final GroupsHandler groupsHandler;

    private class DragHandler implements DndDragStartEvent.DndDragStartHandler,
                                                DndDragCancelEvent.DndDragCancelHandler,
                                                DndDropEvent.DndDropHandler {

        private final Map<Group, Map<ValueProvider<Group, ?>, ?>> changes;

        private DragHandler() {
            this.changes = new HashMap<>();
        }

        @Override
        public void onDrop(DndDropEvent event) {
            for (Map.Entry<Group, Map<ValueProvider<Group, ?>, ?>> entry : changes.entrySet()) {
                Group group = entry.getKey();
                Group parent = groupStore.getParent(group);
                if (parent != null && !grid.isExpanded(parent)) {
                    grid.setExpanded(parent, true);
                }
                Store.Record record = groupStore.getRecord(group);
                for (Map.Entry<ValueProvider<Group, ?>, ?> changeEntry : entry.getValue().entrySet()) {
                    record.addChange(changeEntry.getKey(), changeEntry.getValue());
                }
            }
            changes.clear();
        }

        @Override
        public void onDragStart(DndDragStartEvent event) {
            List<TreeStore.TreeNode<Group>> nodes = (List<TreeStore.TreeNode<Group>>) event.getData();
            for (TreeStore.TreeNode<Group> node : nodes) {
                Group group = node.getData();
                registerChange(group, groupProperties.name());
                registerChange(group, groupProperties.description());
            }
        }

        private void registerChange(Group group, ValueProvider<Group, ?> valueProvider) {
            Store.Record record = groupStore.getRecord(group);
            Store.Change<Group, ?> change = record.getChange(valueProvider);
            if (change != null) {
                Map changes = this.changes.get(group);
                if (changes == null) {
                    changes = new HashMap();
                    this.changes.put(group, changes);
                }
                changes.put(valueProvider, change.getValue());
            }
        }

        @Override
        public void onDragCancel(DndDragCancelEvent event) {
            changes.clear();
        }
    }

    @UiField
    Window window;

    @UiField
    TextButton addButton;

    @UiField
    TextButton removeButton;

    @UiField
    TextButton shareButton;

    final GroupStore groupStore;
    private List<Group> newGroups;

    @UiField(provided = true)
    TreeGrid<Group> grid;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    GroupProperties groupProperties = GWT.create(GroupProperties.class);

    public GroupsDialog(final GroupStore groupStore, final GroupsHandler groupsHandler) {
        this.groupStore = groupStore;
        this.groupsHandler = groupsHandler;
        this.newGroups = new ArrayList<>();

        List<ColumnConfig<Group, ?>> columnConfigList = new LinkedList<>();
        ColumnConfig<Group, String> colName = new ColumnConfig<>(groupProperties.name(), 50, i18n.name());
        colName.setSortable(false);
        columnConfigList.add(colName);
        ColumnConfig<Group, String> colDescription = new ColumnConfig<>(groupProperties.description(), 100, i18n.description());
        colDescription.setSortable(false);
        columnConfigList.add(colDescription);
        ColumnModel<Group> columnModel = new ColumnModel<>(columnConfigList);

        grid = new TreeGrid<>(groupStore, columnModel, colName);
        grid.getSelectionModel().addSelectionChangedHandler(this);
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        grid.getTreeView().setAutoFill(true);
        grid.getTreeView().setStripeRows(true);
        grid.getTreeView().setSortingEnabled(false);
        grid.setAutoExpand(true);

        DragHandler dragHandler = new DragHandler();
        TreeGridDragSource<Group> dragSource = new TreeGridDragSource<>(grid);
        dragSource.addDragStartHandler(dragHandler);
        dragSource.addDragCancelHandler(dragHandler);

        TreeGridDropTarget<Group> dropTarget = new TreeGridDropTarget<>(grid);
        dropTarget.setAllowSelfAsSource(true);
        dropTarget.setAutoExpand(true);
        dropTarget.setAllowDropOnLeaf(true);
        dropTarget.setFeedback(DND.Feedback.BOTH);
        dropTarget.addDropHandler(dragHandler);

        uiBinder.createAndBindUi(this);

        GridInlineEditing<Group> editing = new GridInlineEditing<>(grid);
        editing.addEditor(colName, new TextField());
        editing.addEditor(colDescription, new TextField());

        window.addHideHandler(new HideEvent.HideHandler() {
            @Override
            public void onHide(HideEvent event) {
                groupsHandler.onCancelSaving(newGroups);
                // workaround to remove relation between grid and current tree store
                grid.reconfigure(null, grid.getColumnModel(), grid.getTreeColumn());
            }
        });
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Group> event) {
        shareButton.setEnabled(!event.getSelection().isEmpty() && event.getSelection().get(0).getId() >= 0);
        removeButton.setEnabled(!event.getSelection().isEmpty());
    }
    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        final Group parent = grid.getSelectionModel().getSelectedItem();
        Group newGroup = new Group();

        groupsHandler.onAdd(parent, newGroup, new GroupsController.GroupAddHandler() {
            @Override
            public void groupAdded(Group addedGroup) {
                if (parent == null) {
                    groupStore.add(addedGroup);
                } else {
                    groupStore.add(parent, addedGroup);
                    grid.setExpanded(parent, true);
                }
                newGroups.add(addedGroup);
                groupStore.getRecord(addedGroup).addChange(groupProperties.name(), i18n.newGroup());
            }
        });
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmGroupRemoval());
        final Group group = grid.getSelectionModel().getSelectedItem();
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.YES) {
                    groupsHandler.onRemove(group);
                }
            }
        });
        dialog.show();
    }

    @UiHandler("shareButton")
    public void onShareClicked(SelectEvent event) {
        groupsHandler.onShare(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        groupsHandler.onSave(new GroupsController.ChangesSaveHandler() {
            @Override
            public void changesSaved() {
                newGroups.clear();
            }
        });
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
