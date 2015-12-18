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
package org.traccar.web.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.*;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.DataServiceAsync;
import org.traccar.web.client.view.CommandDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.LogViewDialog;
import org.traccar.web.shared.model.Command;
import org.traccar.web.shared.model.CommandType;
import org.traccar.web.shared.model.Device;

import java.util.HashMap;

public class CommandController implements ContentController, DeviceView.CommandHandler, CommandDialog.CommandHandler {
    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
        try {
            new RequestBuilder(RequestBuilder.GET, "api").sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    ApplicationContext.getInstance().setBackendApiAvailable(response.getStatusCode() != 404);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    GWT.log("Error during backend API check", exception);
                }
            });
        } catch (RequestException requestException) {
            GWT.log("Unexpected error during backend API check", requestException);
        }
    }

    @Override
    public void onCommand(Device device) {
        new CommandDialog(device, this).show();
    }

    @Override
    public void onSend(Device device, CommandType type, int frequency, int timezone, String rawCommand) {
        Command command = new Command();
        command.setType(type);
        command.setDeviceId((int) device.getId());
        command.setAttributes(new HashMap<String, Integer>());
        switch (type) {
            case positionPeriodic:
                command.getAttributes().put(CommandType.KEY_FREQUENCY, frequency);
                break;
            case setTimezone:
                command.getAttributes().put(CommandType.KEY_TIMEZONE, timezone);
                break;
            case CUSTOM:
                command.setCommand(rawCommand);
                break;
        }

        DataServiceAsync service = GWT.create(DataService.class);
        Messages i18n = GWT.create(Messages.class);
        service.sendCommand(command, new BaseAsyncCallback<String>(i18n) {
            @Override
            public void onSuccess(String result) {
                new LogViewDialog("<pre>" + result + "</pre>").show();
            }
        });
    }
}
