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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.NotificationSettingsProperties;
import org.traccar.web.shared.model.NotificationSettings;

import java.util.Arrays;

public class NotificationSettingsDialog implements Editor<NotificationSettings> {

    private static final NotificationSettingsDialogUiBinder uiBinder = GWT.create(NotificationSettingsDialogUiBinder.class);

    interface NotificationSettingsDialogUiBinder extends UiBinder<Widget, NotificationSettingsDialog> {
    }

    private final NotificationSettingsDriver driver = GWT.create(NotificationSettingsDriver.class);

    interface NotificationSettingsDriver extends SimpleBeanEditorDriver<NotificationSettings, NotificationSettingsDialog> {
    }

    public interface NotificationSettingsHandler {
        public void onSave(NotificationSettings notificationSettings);
        public void onTest(NotificationSettings notificationSettings);
    }

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

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public NotificationSettingsDialog(NotificationSettings notificationSettings, NotificationSettingsHandler notificationSettingsHandler) {
        this.notificationSettingsHandler = notificationSettingsHandler;

        ListStore<NotificationSettings.SecureConnectionType> secureConnectionTypeStore = new ListStore<NotificationSettings.SecureConnectionType>(new EnumKeyProvider<NotificationSettings.SecureConnectionType>());
        secureConnectionTypeStore.addAll(Arrays.asList(NotificationSettings.SecureConnectionType.values()));

        secureConnectionType = new ComboBox<NotificationSettings.SecureConnectionType>(secureConnectionTypeStore, new NotificationSettingsProperties.SecureConnectionTypeLabelProvider());
        secureConnectionType.setForceSelection(true);
        secureConnectionType.setTriggerAction(TriggerAction.ALL);

        uiBinder.createAndBindUi(this);

        port.addValidator(new MinNumberValidator<Integer>(1));
        port.addValidator(new MaxNumberValidator<Integer>(65535));

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
        notificationSettingsHandler.onSave(driver.flush());
    }

    @UiHandler("testButton")
    public void onTestClicked(SelectEvent event) {
        notificationSettingsHandler.onTest(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onRegisterClicked(SelectEvent event) {
        window.hide();
    }
}
