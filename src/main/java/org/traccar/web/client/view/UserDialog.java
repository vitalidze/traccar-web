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

import com.sencha.gxt.widget.core.client.form.validator.RegExValidator;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;

public class UserDialog implements Editor<User> {

    private static UserDialogUiBinder uiBinder = GWT.create(UserDialogUiBinder.class);

    interface UserDialogUiBinder extends UiBinder<Widget, UserDialog> {
    }

    private UserDriver driver = GWT.create(UserDriver.class);

    interface UserDriver extends SimpleBeanEditorDriver<User, UserDialog> {
    }

    public interface UserHandler {
        public void onSave(User user);
    }

    private UserHandler userHandler;

    @UiField
    Window window;

    @UiField
    TextField login;

    @UiField
    PasswordField password;

    @UiField
    CheckBox admin;

    @UiField
    CheckBox manager;

    @UiField
    TextField email;

    @UiField
    CheckBox notifications;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public UserDialog(User user, UserHandler userHandler) {
        this.userHandler = userHandler;
        uiBinder.createAndBindUi(this);

        if (ApplicationContext.getInstance().getUser().getAdmin()) {
            admin.setEnabled(true);
        }

        if (ApplicationContext.getInstance().getUser().getAdmin() ||
            ApplicationContext.getInstance().getUser().getManager()) {
            manager.setEnabled(true);
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

    @UiHandler("saveButton")
    public void onLoginClicked(SelectEvent event) {
        window.hide();
        userHandler.onSave(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onRegisterClicked(SelectEvent event) {
        window.hide();
    }

}
