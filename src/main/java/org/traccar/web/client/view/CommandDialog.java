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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.shared.model.CommandType;

import java.util.Arrays;

public class CommandDialog {
    private static ImportDialogUiBinder uiBinder = GWT.create(ImportDialogUiBinder.class);

    interface ImportDialogUiBinder extends UiBinder<Widget, CommandDialog> {
    }

    @UiField
    Window window;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    @UiField(provided = true)
    ComboBox<CommandType> typeCombo;

    public CommandDialog() {
        ListStore<CommandType> commandTypes = new ListStore<>(new EnumKeyProvider<CommandType>());
        commandTypes.addAll(Arrays.asList(CommandType.values()));
        this.typeCombo = new ComboBox<>(commandTypes, new LabelProvider<CommandType>() {
            @Override
            public String getLabel(CommandType item) {
                return i18n.commandType(item);
            }
        });
        uiBinder.createAndBindUi(this);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

}
