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

import com.google.gwt.user.client.Random;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ToStringValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.RegExValidator;
import com.sencha.gxt.widget.core.client.grid.*;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

import java.util.*;

import com.google.gwt.regexp.shared.RegExp;

public class UserDialog implements Editor<User> {
    private static RegExp EVENT_RULE_TIME_FRAME_PATTERN = RegExp.compile("^" + EventRule.TIME_FRAME_REGEX + "(," + EventRule.TIME_FRAME_REGEX + ")*$", "i");
    private static RegExp EVENT_RULE_COURSE_PATTERN = RegExp.compile("^" + EventRule.COURSE_REGEX + "(," + EventRule.COURSE_REGEX +")*$");

    private static UserDialogUiBinder uiBinder = GWT.create(UserDialogUiBinder.class);

    interface UserDialogUiBinder extends UiBinder<Widget, UserDialog> {
    }

    private UserDriver driver = GWT.create(UserDriver.class);

    interface UserDriver extends SimpleBeanEditorDriver<User, UserDialog> {
    }

    public interface UserHandler {
        void onSave(User user, ListStore<EventRule> eventRulesStore);
    }

    private UserHandler userHandler;

    public interface EventRuleHandler {
        void onShowEventRules(ListStore<EventRule> eventRulesStore, User user);
        void onSave(ListStore<EventRule> eventRulesStore, User user);
        void onRemove(ListStore<EventRule> eventRulesStore, EventRule eventRule);
    }

    private User user;

    private EventRuleHandler eventRuleHandler;

    @UiField
    Window window;

    @UiField
    TextField login;

    @UiField
    PasswordField password;

    @UiField
    TextField firstName;

    @UiField
    TextField lastName;

    @UiField
    TextField companyName;

    @UiField
    TextField phoneNumber;

    @UiField
    CheckBox admin;

    @UiField
    CheckBox manager;

    @UiField
    CheckBox readOnly;

    @UiField
    DateField expirationDate;

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField
    NumberField<Integer> maxNumOfDevices;

    @UiField
    TextField email;

    @UiField
    Grid<DeviceEventType> grid;

    @UiField(provided = true)
    GridView<DeviceEventType> view;

    @UiField(provided = true)
    ColumnModel<DeviceEventType> columnModel;

    @UiField(provided = true)
    ListStore<DeviceEventType> notificationEventStore;

    @UiField
    @Editor.Ignore
    TextButton addButton;

    @UiField
    @Editor.Ignore
    TextButton removeButton;

    @UiField
    Grid<EventRule> eventRulesGrid;

    @UiField(provided = true)
    GridView<EventRule> eventRulesView;

    @UiField(provided = true)
    ColumnModel<EventRule> eventRulesColumnModel;

    @UiField(provided = true)
    ListStore<EventRule> eventRulesStore;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    private EventRuleProperties eventRulesProperties = GWT.create(EventRuleProperties.class);

    public UserDialog(final User user, UserHandler userHandler, EventRuleHandler eventRuleHandler,
                      ListStore<GeoFence> geoFenceStore, ListStore<Device> deviceStore) {
        this.user = user;
        this.userHandler = userHandler;
        this.eventRuleHandler = eventRuleHandler;
        // notification types grid
        IdentityValueProvider<DeviceEventType> identity = new IdentityValueProvider<>();
        final CheckBoxSelectionModel<DeviceEventType> selectionModel = new CheckBoxSelectionModel<>(identity);

        ColumnConfig<DeviceEventType, String> nameCol = new ColumnConfig<>(new ToStringValueProvider<DeviceEventType>() {
            @Override
            public String getValue(DeviceEventType object) {
                return i18n.deviceEventType(object);
            }
        }, 200, i18n.event());
        List<ColumnConfig<DeviceEventType, ?>> columns = new ArrayList<>();
        columns.add(selectionModel.getColumn());
        columns.add(nameCol);

        columnModel = new ColumnModel<>(columns);

        view = new NoScrollbarGridView<>();
        view.setAutoFill(true);
        view.setStripeRows(true);

        notificationEventStore = new ListStore<>(new EnumKeyProvider<DeviceEventType>());
        notificationEventStore.addAll(Arrays.asList(DeviceEventType.values()));

        // event rules grid
        IdentityValueProvider<EventRule> eventRulesIdentity = new IdentityValueProvider<>();
        final CheckBoxSelectionModel<EventRule> eventRulesSelectionModel = new CheckBoxSelectionModel<>(eventRulesIdentity);

        List<ColumnConfig<EventRule, ?>> eventRulesColumnConfigList = new ArrayList<>();

        ColumnConfig<EventRule, Device> colDevice = new ColumnConfig<>(eventRulesProperties.device(), 120, i18n.eventRuleDevice());
        ComboBoxCell<Device> cmbDeviceCell = new ComboBoxCell<Device>(deviceStore, new LabelProvider<Device>() {
            @Override
            public String getLabel(Device item) {
                return item.getName();
            }
        });
//        cmbCell.addSelectionHandler(selHandler);
        cmbDeviceCell.setWidth(100);
        cmbDeviceCell.setForceSelection(true);
        cmbDeviceCell.setAllowBlank(false);
        cmbDeviceCell.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        colDevice.setFixed(true);
        colDevice.setResizable(false);
        colDevice.setCell(cmbDeviceCell);
        eventRulesColumnConfigList.add(colDevice);

        ColumnConfig<EventRule, GeoFence> colGeoFence = new ColumnConfig<>(eventRulesProperties.geoFence(), 120, i18n.eventRuleGeoFence());
        ComboBoxCell<GeoFence> cmbGeoFenceCell = new ComboBoxCell<GeoFence>(geoFenceStore, new LabelProvider<GeoFence>() {
            @Override
            public String getLabel(GeoFence item) {
                return item.getName();
            }
        });
        cmbGeoFenceCell.setWidth(100);
        cmbGeoFenceCell.setForceSelection(true);
        cmbGeoFenceCell.setAllowBlank(true);
        cmbGeoFenceCell.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        colGeoFence.setFixed(true);
        colGeoFence.setResizable(false);
        colGeoFence.setCell(cmbGeoFenceCell);
        eventRulesColumnConfigList.add(colGeoFence);

        ListStore<DeviceEventType> deviceEventTypeStore = new ListStore<>(new EnumKeyProvider<DeviceEventType>());
        deviceEventTypeStore.addAll(Arrays.asList(DeviceEventType.values()));

        ColumnConfig<EventRule, DeviceEventType> colDeviceEventType = new ColumnConfig<>(eventRulesProperties.deviceEventType(), 120, i18n.eventRuleEvent());
        ComboBoxCell<DeviceEventType> cmbDeviceEventType = new ComboBoxCell<DeviceEventType>(deviceEventTypeStore, new LabelProvider<DeviceEventType>() {
            @Override
            public String getLabel(DeviceEventType item) {
                return i18n.deviceEventType(item);
            }
        });
        cmbDeviceEventType.setWidth(100);
        cmbDeviceEventType.setForceSelection(true);
        cmbDeviceEventType.setAllowBlank(false);
        cmbDeviceEventType.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        colDeviceEventType.setFixed(true);
        colDeviceEventType.setResizable(false);
        colDeviceEventType.setCell(cmbDeviceEventType);
        eventRulesColumnConfigList.add(colDeviceEventType);

        ColumnConfig<EventRule, String> colTimeFrame = new ColumnConfig<>(eventRulesProperties.timeFrame(), 170, i18n.timeFrame());
        colTimeFrame.setFixed(true);
        colTimeFrame.setResizable(false);
        colTimeFrame.setToolTip(SafeHtmlUtils.fromTrustedString("<div qtip=\"8pm-9:30pm,10pm-11pm\">8pm-9:30pm,10pm-11pm</div>"));
        eventRulesColumnConfigList.add(colTimeFrame);

        ColumnConfig<EventRule, String> colCourse = new ColumnConfig<>(eventRulesProperties.course(), 170, i18n.course());
        colCourse.setFixed(true);
        colCourse.setResizable(false);
        colCourse.setToolTip(SafeHtmlUtils.fromTrustedString("<div qtip=\"30-110,116-300\">30-110,116-300</div>"));
        eventRulesColumnConfigList.add(colCourse);

        eventRulesColumnModel = new ColumnModel<>(eventRulesColumnConfigList);

        eventRulesView = new GridView<>();
        eventRulesView.setAutoFill(true);
        eventRulesView.setStripeRows(true);
        eventRulesSelectionModel.addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<EventRule>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<EventRule> event) {
                removeButton.setEnabled(!event.getSelection().isEmpty());
            }
        });

        eventRulesStore = new ListStore<>(eventRulesProperties.id());
        eventRuleHandler.onShowEventRules(eventRulesStore, user);

        uiBinder.createAndBindUi(this);

        grid.setSelectionModel(selectionModel);
        grid.getView().setForceFit(true);
        grid.getView().setAutoFill(true);
        for (DeviceEventType deviceEventType : user.getTransferNotificationEvents()) {
            grid.getSelectionModel().select(deviceEventType, true);
        }

        eventRulesGrid.setSelectionModel(eventRulesSelectionModel);
        eventRulesGrid.getView().setForceFit(true);
        eventRulesGrid.getView().setAutoFill(true);

        GridEditing<EventRule> editing = new GridInlineEditing<EventRule>(eventRulesGrid);
        editing.addEditor(colTimeFrame, new TextField());
        editing.addEditor(colCourse, new TextField());



        User currentUser = ApplicationContext.getInstance().getUser();
        if (currentUser.getAdmin() || currentUser.getManager()) {
            admin.setEnabled(currentUser.getAdmin());
            manager.setEnabled(true);
            readOnly.setEnabled(true);
            expirationDate.setEnabled(true);
            maxNumOfDevices.setEnabled(true);
        }
        else {
            manager.setEnabled(false);
            admin.setEnabled(false);
            readOnly.setEnabled(false);
            expirationDate.setEnabled(false);
            maxNumOfDevices.setEnabled(false);
        }
        email.addValidator(new RegExValidator(".+@.+\\.[a-z]+", i18n.invalidEmail()));

        driver.initialize(this);
        driver.edit(user);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        // , "8pm-9:30pm,10pm-11pm", "30-110,116-300"
        EventRule newEventRule = new EventRule(user);
        Integer id = Random.nextInt();
        if (id > 0) id = -id;
        newEventRule.setId(id);
        eventRulesStore.add(newEventRule);
        eventRulesStore.getRecord(newEventRule);
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmEventRuleRemoval());
        final EventRule eventRule = eventRulesGrid.getSelectionModel().getSelectedItem();
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.YES) {
                    eventRuleHandler.onRemove(eventRulesStore, eventRule);
                }
            }
        });
        dialog.show();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        if (validate()) {
            window.hide();
            User user = driver.flush();
            user.setTransferNotificationEvents(new HashSet<>(grid.getSelectionModel().getSelectedItems()));
            userHandler.onSave(user, eventRulesStore);
        }
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    private boolean validate() {
        List<EventRule> invalidEventRules = new LinkedList<EventRule>();
        for (Store<EventRule>.Record record : eventRulesStore.getModifiedRecords()) {
            final EventRule originalEventRule = record.getModel();
            EventRule eventRule = new EventRule().copyFromClient(originalEventRule);
            for (Store.Change<EventRule, ?> change : record.getChanges()) {
                change.modify(eventRule);
            }
            if (eventRule.getDevice() == null || eventRule.getDeviceEventType() == null
                    || (eventRule.getGeoFence() == null && (eventRule.getDeviceEventType() == DeviceEventType.GEO_FENCE_ENTER || eventRule.getDeviceEventType() == DeviceEventType.GEO_FENCE_EXIT))) {
                invalidEventRules.add(originalEventRule);
                break;
            }
            if (eventRule.getTimeFrame() != null && !EVENT_RULE_TIME_FRAME_PATTERN.test(eventRule.getTimeFrame().trim())) {
                invalidEventRules.add(originalEventRule);
                break;
            }
            if (eventRule.getCourse() != null && !EVENT_RULE_COURSE_PATTERN.test(eventRule.getCourse().trim())) {
                invalidEventRules.add(originalEventRule);
                break;
            }
        }
        if (!invalidEventRules.isEmpty()) {
            eventRulesGrid.getSelectionModel().select(invalidEventRules, true);
            AlertMessageBox dialog = new AlertMessageBox(i18n.close(), i18n.alertEventRuleInvalid());
            dialog.show();
            return false;
        }

        String login = this.login.getCurrentValue();
        String password = this.password.getCurrentValue();
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            new AlertMessageBox(i18n.error(), i18n.errUsernameOrPasswordEmpty()).show();
            return false;
        }
        return true;
    }
}
