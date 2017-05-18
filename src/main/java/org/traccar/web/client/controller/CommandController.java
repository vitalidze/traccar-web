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
package org.traccar.web.client.controller;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.view.CommandDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.LogViewDialog;
import org.traccar.web.shared.model.Command;
import org.traccar.web.shared.model.CommandType;
import org.traccar.web.shared.model.Device;

import java.util.HashMap;

public class CommandController implements ContentController, DeviceView.CommandHandler, CommandDialog.CommandHandler {

    interface CommandMapper extends ObjectMapper<Command> {}

    private final CommandMapper commandMapper = GWT.create(CommandMapper.class);

    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
    }

    @Override
    public void onCommand(Device device) {
        new CommandDialog(device, this).show();
    }

    @Override
    public void onSend(Device device,
                       CommandType type,
                       int frequency,
                       int timezone,
                       int radius,
                       String phoneNumber, String message,
                       String rawCommand) {
        Command command = new Command();
        command.setType(type);
        command.setDeviceId((int) device.getId());
        command.setAttributes(new HashMap<String, Object>());
        switch (type) {
            case positionPeriodic:
                command.getAttributes().put(CommandType.KEY_FREQUENCY, frequency);
                break;
            case setTimezone:
                command.getAttributes().put(CommandType.KEY_TIMEZONE, timezone);
                break;
            case movementAlarm:
                command.getAttributes().put(CommandType.KEY_RADIUS, radius);
                break;
            case sendSms:
                command.getAttributes().put(CommandType.KEY_PHONE_NUMBER, phoneNumber);
                command.getAttributes().put(CommandType.KEY_MESSAGE, message);
                break;
            case custom:
                command.getAttributes().put(CommandType.KEY_DATA, rawCommand);
                break;
        }

        final Messages i18n = GWT.create(Messages.class);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "traccar/rest/sendCommand");

        try {
            builder.sendRequest("[" + commandMapper.write(command) + "]", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    new LogViewDialog("<pre>" + response.getText() + "</pre>").show();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errRemoteCall());
                }
            });
        } catch (RequestException e) {
            new AlertMessageBox(i18n.error(), e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
