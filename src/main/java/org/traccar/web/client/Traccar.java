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
package org.traccar.web.client;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.state.client.StateManager;
import org.traccar.web.client.controller.LoginController;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.state.UIStateProvider;
import org.traccar.web.shared.model.ApplicationSettings;

import com.google.gwt.core.client.EntryPoint;

public class Traccar implements EntryPoint, LoginController.LoginHandler {
    private Messages i18n = GWT.create(Messages.class);

    @Override
    public void onModuleLoad() {
        Application.getDataService().getApplicationSettings(new BaseAsyncCallback<ApplicationSettings>(i18n) {
            @Override
            public void onSuccess(ApplicationSettings result) {
                ApplicationContext.getInstance().setApplicationSettings(result);
                new LoginController().login(Traccar.this);
            }
        });

        StateManager.get().setProvider(new UIStateProvider());
    }

    @Override
    public void onLogin() {
        new Application().run();
    }

}
