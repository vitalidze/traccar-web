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

import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.GroupProperties;
import org.traccar.web.shared.model.Group;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import java.util.LinkedList;
import java.util.List;

public class GroupsDialog implements SelectionChangedEvent.SelectionChangedHandler<Group> {

    private static GroupsDialogUiBinder uiBinder = GWT.create(GroupsDialogUiBinder.class);

    interface GroupsDialogUiBinder extends UiBinder<Widget, GroupsDialog> {
    }

    public interface GroupsHandler {
        void onSave();
        void onRemove(Group group);
        void onShare(Group group);
    }

    private final GroupsHandler groupsHandler;

    @UiField
    Window window;

    @UiField
    TextButton addButton;

    @UiField
    TextButton removeButton;

    @UiField
    TextButton shareButton;

    @UiField(provided = true)
    ColumnModel<Group> columnModel;

    @UiField(provided = true)
    final ListStore<Group> groupStore;

    @UiField
    Grid<Group> grid;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    GroupProperties groupProperties = GWT.create(GroupProperties.class);

    public GroupsDialog(ListStore<Group> groupStore, GroupsHandler groupsHandler) {
        this.groupStore = groupStore;
        this.groupsHandler = groupsHandler;

        List<ColumnConfig<Group, ?>> columnConfigList = new LinkedList<>();
        ColumnConfig<Group, String> colName = new ColumnConfig<>(groupProperties.name(), 50, i18n.name());
        columnConfigList.add(colName);
        ColumnConfig<Group, String> colDescription = new ColumnConfig<>(groupProperties.description(), 100, i18n.description());
        columnConfigList.add(colDescription);
        columnModel = new ColumnModel<>(columnConfigList);

        uiBinder.createAndBindUi(this);

        grid.getSelectionModel().addSelectionChangedHandler(this);
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);

        GridEditing<Group> editing = new GridInlineEditing<>(grid);
        editing.addEditor(colName, new TextField());
        editing.addEditor(colDescription, new TextField());
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
        removeButton.setEnabled(!event.getSelection().isEmpty() && event.getSelection().get(0).getId() >= 0);
    }
    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        Group newGroup = new Group(-groupStore.size() - 1, "");
        groupStore.add(newGroup);
        groupStore.getRecord(newGroup).addChange(groupProperties.name(), i18n.newGroup());
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
        groupsHandler.onSave();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
