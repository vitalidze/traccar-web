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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.cell.client.TextInputCell;
import com.sencha.gxt.cell.core.client.NumberCell;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.cell.core.client.form.DateCell;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.UserProperties;
import org.traccar.web.shared.model.Maintenance;
import org.traccar.web.shared.model.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

public class UsersDialog implements SelectionChangedEvent.SelectionChangedHandler<User> {

    private static UsersDialogUiBinder uiBinder = GWT.create(UsersDialogUiBinder.class);

    interface UsersDialogUiBinder extends UiBinder<Widget, UsersDialog> {
    }

    public interface UserHandler {
        public void onAdd();
        public void onRemove(User user);
        public void onChangePassword(User user);
        public void onSaveRoles();
    }

    private UserHandler userHandler;

    @UiField
    Window window;

    @UiField
    TextButton addButton;

    @UiField
    TextButton removeButton;

    @UiField
    TextButton changePasswordButton;

    @UiField(provided = true)
    ColumnModel<User> columnModel;

    @UiField(provided = true)
    ListStore<User> userStore;

    @UiField
    Grid<User> grid;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public UsersDialog(ListStore<User> userStore, UserHandler userHandler) {
        this.userStore = userStore;
        this.userHandler = userHandler;

        UserProperties userProperties = GWT.create(UserProperties.class);

        List<ColumnConfig<User, ?>> columnConfigList = new LinkedList<ColumnConfig<User, ?>>();
        columnConfigList.add(new ColumnConfig<User, String>(userProperties.login(), 25, i18n.name()));

        if (ApplicationContext.getInstance().getUser().getAdmin()) {
            ColumnConfig<User, Boolean> colAdmin = new ColumnConfig<User, Boolean>(userProperties.admin(), 100, i18n.administrator());
            colAdmin.setCell(new CheckBoxCell());
            colAdmin.setFixed(true);
            colAdmin.setResizable(false);
            columnConfigList.add(colAdmin);
        }

        ColumnConfig<User, Boolean> colManager = new ColumnConfig<User, Boolean>(userProperties.manager(), 70, i18n.manager());
        colManager.setCell(new CheckBoxCell());
        colManager.setFixed(true);
        colManager.setResizable(false);
        columnConfigList.add(colManager);

        ColumnConfig<User, Boolean> colReadOnly = new ColumnConfig<User, Boolean>(userProperties.readOnly(), 90, i18n.readOnly());
        colReadOnly.setCell(new CheckBoxCell());
        colReadOnly.setFixed(true);
        colReadOnly.setResizable(false);
        columnConfigList.add(colReadOnly);

        ColumnConfig<User, Boolean> colBlocked = new ColumnConfig<User, Boolean>(userProperties.blocked(), 50, i18n.blocked());
        colBlocked.setCell(new CheckBoxCell());
        colBlocked.setFixed(true);
        colBlocked.setResizable(false);
        columnConfigList.add(colBlocked);

        ColumnConfig<User, Date> colExpirationDate = new ColumnConfig<User, Date>(userProperties.expirationDate(), 156, i18n.expirationDate());
        colExpirationDate.setCell(new DateCell());
        colExpirationDate.setFixed(true);
        colExpirationDate.setResizable(false);
        columnConfigList.add(colExpirationDate);

        ColumnConfig<User, Integer> colMaxNumOfDevices = new ColumnConfig<User, Integer>(userProperties.maxNumOfDevices(), 156, i18n.maxNumOfDevices());
        colMaxNumOfDevices.setCell(new NumberCell<Integer>());
        colMaxNumOfDevices.setFixed(true);
        colMaxNumOfDevices.setResizable(false);
        columnConfigList.add(colMaxNumOfDevices);

        columnModel = new ColumnModel<User>(columnConfigList);

        uiBinder.createAndBindUi(this);

        grid.getSelectionModel().addSelectionChangedHandler(this);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        GridEditing<User> editing = new GridInlineEditing<User>(grid);
        NumberField<Integer> maxNumOfDevicesEditor = new NumberField<Integer>(new NumberPropertyEditor.IntegerPropertyEditor());
        maxNumOfDevicesEditor.setAllowDecimals(false);
        maxNumOfDevicesEditor.setAllowBlank(true);
        maxNumOfDevicesEditor.setAllowNegative(false);
        editing.addEditor(colMaxNumOfDevices, maxNumOfDevicesEditor);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<User> event) {
        removeButton.setEnabled(!event.getSelection().isEmpty());
        changePasswordButton.setEnabled(!event.getSelection().isEmpty());
    }
    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        userHandler.onAdd();
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        userHandler.onRemove(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("changePasswordButton")
    public void onChangePasswordClicked(SelectEvent event) {
        userHandler.onChangePassword(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        userHandler.onSaveRoles();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
