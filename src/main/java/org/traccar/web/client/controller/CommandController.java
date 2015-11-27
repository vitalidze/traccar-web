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
import org.traccar.web.client.view.CommandDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.LogViewDialog;
import org.traccar.web.shared.model.CommandType;
import org.traccar.web.shared.model.Device;

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
                    ApplicationContext.getInstance().setBackendApiAvailable(response.getStatusCode() == 302);
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

    static class Command extends JavaScriptObject {
        protected Command() {
        }

        public final native void setType(String type)  /*-{
            this.type = type;
        }-*/;

        public final native void setDeviceId(int deviceId) /*-{
            this.deviceId = deviceId;
        }-*/;

        public final native void setCommand(String command) /*-{
            this.command = command;
        }-*/;

        public final native void set(String attribute, int value) /*-{
            if (this.data === undefined) {
                this.data = {};
            }
            this.data[attribute] = value;
        }-*/;
    }

    @Override
    public void onSend(Device device, CommandType type, int frequency, String rawCommand) {
        Command command = JavaScriptObject.createObject().cast();
        command.setType(type.name());
        command.setDeviceId((int) device.getId());
        switch (type) {
            case positionPeriodic:
                command.set(CommandType.KEY_FREQUENCY, frequency);
                break;
            case CUSTOM:
                command.setCommand(rawCommand);
                break;
        }

        String url = "api/command/" + (type == CommandType.CUSTOM ? "raw" : "send");
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
        try {
            builder.sendRequest(JsonUtils.stringify(command), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    StringBuilder text = new StringBuilder("<h2>Request completed</h2>")
                            .append("<h3>Status: ").append(response.getStatusText()).append("</h3>")
                            .append("<h3>Response</h3>")
                            .append("<pre>")
                            .append(response.getText())
                            .append("</pre>");
                    new LogViewDialog(text.toString()).show();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    StringBuilder text = new StringBuilder("<h2>Request failed</h2>")
                            .append("<h3>Error: ").append(exception.getLocalizedMessage()).append("</h3>");
                    new LogViewDialog(text.toString()).show();
                }
            });
        } catch (RequestException ex) {
            GWT.log("Unexpected error during command request", ex);
        }
    }
}
