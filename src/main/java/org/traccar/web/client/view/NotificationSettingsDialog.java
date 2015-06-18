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
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.ToStringValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.NotificationSettingsProperties;
import org.traccar.web.shared.model.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NotificationSettingsDialog implements Editor<NotificationSettings> {

    private static NotificationSettingsDialogUiBinder uiBinder = GWT.create(NotificationSettingsDialogUiBinder.class);

    interface NotificationSettingsDialogUiBinder extends UiBinder<Widget, NotificationSettingsDialog> {
    }

    private NotificationSettingsDriver driver = GWT.create(NotificationSettingsDriver.class);

    interface NotificationSettingsDriver extends SimpleBeanEditorDriver<NotificationSettings, NotificationSettingsDialog> {
    }

    public interface NotificationSettingsHandler {
        void onSave(NotificationSettings notificationSettings);
        void onTestEmail(NotificationSettings notificationSettings);
        void onTestPushbullet(NotificationSettings notificationSettings);
        void onTestMessageTemplate(NotificationTemplate template);
    }

    private final NotificationSettings settings;
    private final NotificationSettingsHandler notificationSettingsHandler;

    @UiField
    Window window;

    @UiField
    TextField fromAddress;

    @UiField
    TextField server;

    @UiField
    NumberField<Integer> port;

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField(provided = true)
    ComboBox<NotificationSettings.SecureConnectionType> secureConnectionType;

    @UiField
    CheckBox useAuthorization;

    @UiField
    TextField username;

    @UiField
    PasswordField password;

    @UiField
    TextField pushbulletAccessToken;

    NotificationTemplate messageTemplate;

    @Ignore
    @UiField(provided = true)
    ComboBox<DeviceEventType> eventType;

    @Ignore
    @UiField
    TextField messageSubject;

    @Ignore
    @UiField
    TextArea messageBody;

    @Ignore
    @UiField
    TextField messageContentType;

    @UiField(provided = true)
    Grid<MessagePlaceholder> placeholderGrid;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public NotificationSettingsDialog(NotificationSettings notificationSettings, NotificationSettingsHandler notificationSettingsHandler) {
        this.settings = notificationSettings;
        this.notificationSettingsHandler = notificationSettingsHandler;

        ListStore<NotificationSettings.SecureConnectionType> secureConnectionTypeStore = new ListStore<NotificationSettings.SecureConnectionType>(new EnumKeyProvider<NotificationSettings.SecureConnectionType>());
        secureConnectionTypeStore.addAll(Arrays.asList(NotificationSettings.SecureConnectionType.values()));

        secureConnectionType = new ComboBox<NotificationSettings.SecureConnectionType>(secureConnectionTypeStore, new NotificationSettingsProperties.SecureConnectionTypeLabelProvider());
        secureConnectionType.setForceSelection(true);
        secureConnectionType.setTriggerAction(TriggerAction.ALL);

        ListStore<DeviceEventType> eventTypeStore = new ListStore<DeviceEventType>(new EnumKeyProvider<DeviceEventType>());
        eventTypeStore.addAll(Arrays.asList(DeviceEventType.values()));

        eventType = new ComboBox<DeviceEventType>(eventTypeStore, new LabelProvider<DeviceEventType>() {
            @Override
            public String getLabel(DeviceEventType item) {
                return i18n.deviceEventType(item);
            }
        });
        eventType.setForceSelection(true);
        eventType.setTriggerAction(TriggerAction.ALL);

        // set up placeholder grid
        List<ColumnConfig<MessagePlaceholder, ?>> placeholderColumns = new LinkedList<ColumnConfig<MessagePlaceholder, ?>>();
        placeholderColumns.add(new ColumnConfig<MessagePlaceholder, String>(new ToStringValueProvider<MessagePlaceholder>() {
            @Override
            public String getValue(MessagePlaceholder ph) {
                return "${" + ph.name() + "}";
            }
        }, 118));
        placeholderColumns.get(placeholderColumns.size() - 1).setFixed(true);
        placeholderColumns.add(new ColumnConfig<MessagePlaceholder, String>(new ToStringValueProvider<MessagePlaceholder>() {
            @Override
            public String getValue(MessagePlaceholder ph) {
                return i18n.placeholderDescription(ph);
            }
        }));
        placeholderColumns.get(placeholderColumns.size() - 1).setHeader(i18n.description());
        ListStore<MessagePlaceholder> placeholderListStore = new ListStore<MessagePlaceholder>(new EnumKeyProvider<MessagePlaceholder>());
        placeholderListStore.addAll(Arrays.asList(MessagePlaceholder.values()));
        GridView<MessagePlaceholder> placeholderGridView = new GridView<MessagePlaceholder>();
        placeholderGridView.setStripeRows(true);
        placeholderGridView.setAutoFill(true);
        placeholderGrid = new Grid<MessagePlaceholder>(placeholderListStore, new ColumnModel<MessagePlaceholder>(placeholderColumns), placeholderGridView);

        uiBinder.createAndBindUi(this);

        port.addValidator(new MinNumberValidator<Integer>(Integer.valueOf(1)));
        port.addValidator(new MaxNumberValidator<Integer>(Integer.valueOf(65535)));

        driver.initialize(this);
        driver.edit(notificationSettings);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        window.hide();
        flushTemplate();
        notificationSettingsHandler.onSave(driver.flush());
    }

    @UiHandler("testEmailButton")
    public void onTestEmailClicked(SelectEvent event) {
        notificationSettingsHandler.onTestEmail(driver.flush());
    }

    @UiHandler("testPushbulletButton")
    public void onTestPushbulletClicked(SelectEvent event) {
        notificationSettingsHandler.onTestPushbullet(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    @UiHandler("eventType")
    public void onEventTypeChanged(SelectionEvent<DeviceEventType> event) {
        // save previously edited template
        flushTemplate();
        messageTemplate = settings.getTransferTemplates().get(event.getSelectedItem());
        if (messageTemplate == null) {
            messageTemplate = new NotificationTemplate();
            messageTemplate.setType(event.getSelectedItem());
            messageTemplate.setBody(i18n.defaultNotificationTemplate(event.getSelectedItem(), "${deviceName}", "${geoFenceName}", "${eventTime}", "${positionTime}", "${maintenanceName}"));
            settings.getTransferTemplates().put(event.getSelectedItem(), messageTemplate);
        }
        messageSubject.setText(messageTemplate.getSubject());
        messageBody.setText(messageTemplate.getBody());
        messageContentType.setText(messageTemplate.getContentType());
    }

    @UiHandler("testTemplateButton")
    public void onTestTemplateClicked(SelectEvent event) {
        flushTemplate();
        if (messageTemplate != null) {
            notificationSettingsHandler.onTestMessageTemplate(messageTemplate);
        }
    }

    private void flushTemplate() {
        if (messageTemplate != null) {
            messageTemplate.setSubject(messageSubject.getText());
            messageTemplate.setBody(messageBody.getText());
            messageTemplate.setContentType(messageContentType.getText());
        }
    }
}
