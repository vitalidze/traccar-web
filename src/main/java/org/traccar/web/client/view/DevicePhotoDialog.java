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
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent;
import com.sencha.gxt.widget.core.client.form.FileUploadField;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.DeviceIcon;
import org.traccar.web.shared.model.Picture;
import org.traccar.web.shared.model.PictureType;

public class DevicePhotoDialog {

    private static DevicePhotoDialogUiBinder uiBinder = GWT.create(DevicePhotoDialogUiBinder.class);

    interface DevicePhotoDialogUiBinder extends UiBinder<Widget, DevicePhotoDialog> {
    }

    interface DevicePhotoHandler {
        void uploaded(Picture photo);
    }

    @UiField
    Window window;

    @UiField
    FormPanel form;

    @UiField
    FileUploadField photo;

    final DevicePhotoHandler handler;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public DevicePhotoDialog(DevicePhotoHandler handler) {
        this.handler = handler;
        uiBinder.createAndBindUi(this);
        form.setAction(Picture.URL_PREFIX + PictureType.DEVICE_PHOTO.name());
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        form.submit();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    @UiHandler("form")
    public void uploadFinished(SubmitCompleteEvent event) {
        String s = event.getResults();
        if (s.indexOf('>') >= 0) {
            s = s.substring(s.indexOf('>') + 1, s.lastIndexOf('<'));
        }
        if (JsonUtils.safeToEval(s)) {
            JSONObject result = (JSONObject) JSONParser.parseStrict(s);
            handler.uploaded(DeviceIconDialog.picture(result.get(photo.getName())));
            hide();
        } else {
            new LogViewDialog(event.getResults()).show();
        }
    }
}
