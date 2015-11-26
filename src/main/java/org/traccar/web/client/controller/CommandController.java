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
import com.google.gwt.http.client.*;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.view.CommandDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.shared.model.Device;

public class CommandController implements ContentController, DeviceView.CommandHandler {
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
        new CommandDialog().show();
    }
}
