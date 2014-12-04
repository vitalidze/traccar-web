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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import org.traccar.web.client.ApplicationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;

public class LoginDialog {

    private static LoginDialogUiBinder uiBinder = GWT.create(LoginDialogUiBinder.class);

    interface LoginDialogUiBinder extends UiBinder<Widget, LoginDialog> {
    }

    public interface LoginHandler {
        public void onLogin(String login, String password);
        public void onRegister(String login, String password);
    }

    private LoginHandler loginHandler;

    @UiField
    Window window;

    @UiField
    TextField login;

    @UiField
    PasswordField password;

    @UiField
    TextButton registerButton;

    public LoginDialog(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
        uiBinder.createAndBindUi(this);

        if (ApplicationContext.getInstance().getApplicationSettings().getRegistrationEnabled()) {
            registerButton.enable();
        }

        login.addKeyDownHandler(doStuffHandler);
        password.addKeyDownHandler(doStuffHandler);
    }

    /*
     * Idea from http://stackoverflow.com/questions/9808153/key-enter-handler-rootpanel-gwt
     */
    public abstract class EnterKeyHandler implements KeyDownHandler {
        public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
                enterKeyDown(event);
        }
        public abstract void enterKeyDown(KeyDownEvent event);
    }
    EnterKeyHandler doStuffHandler = new EnterKeyHandler() {
        public void enterKeyDown(KeyDownEvent event) {
            if (login.getText().equals("")) {
                login.focus();
            } else if (password.getText().equals("")) {
                password.focus();
            } else {
                loginHandler.onLogin(login.getText(), password.getText());
            }
        }
    };

    public void show() {
        window.show();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                login.focus();
            }
        });
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("loginButton")
    public void onLoginClicked(SelectEvent event) {
        loginHandler.onLogin(login.getText(), password.getText());
    }

    @UiHandler("registerButton")
    public void onRegisterClicked(SelectEvent event) {
        loginHandler.onRegister(login.getText(), password.getText());
    }

}
