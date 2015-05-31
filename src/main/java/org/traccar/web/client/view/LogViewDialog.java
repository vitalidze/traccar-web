/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.Application;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;

public class LogViewDialog {
    private static LogViewDialogUiBinder uiBinder = GWT.create(LogViewDialogUiBinder.class);

    interface LogViewDialogUiBinder extends UiBinder<Widget, LogViewDialog> {
    }

    @UiField
    Window window;

    @UiField
    ScrollPanel logScroll;

    @UiField
    HTML logArea;

    @UiField(provided = true)
    String log;

    public LogViewDialog(String log) {
        this.log = log;

        uiBinder.createAndBindUi(this);

        logScroll.getElement().getStyle().setBackgroundColor("white");
        logArea.getElement().getStyle().setPadding(2, Style.Unit.PX);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }


    @UiHandler("closeButton")
    public void onCloseClicked(SelectEvent event) {
        hide();
    }
}
