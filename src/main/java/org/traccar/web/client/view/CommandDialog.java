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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.shared.model.CommandType;
import org.traccar.web.shared.model.Device;

import java.util.Arrays;

public class CommandDialog {
    private static ImportDialogUiBinder uiBinder = GWT.create(ImportDialogUiBinder.class);

    interface ImportDialogUiBinder extends UiBinder<Widget, CommandDialog> {
    }

    public interface CommandHandler {
        void onSend(Device device, CommandType type, int frequency, String rawCommand);
    }

    @UiField
    Window window;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField(provided = true)
    ComboBox<CommandType> typeCombo;

    @UiField
    FieldLabel lblFrequency;

    @UiField
    NumberField<Integer> frequency;

    @UiField(provided = true)
    ComboBox<String> frequencyUnit;

    @UiField
    FieldLabel lblCustomMessage;

    @UiField
    TextField customMessage;

    final Device device;
    final CommandHandler commandHandler;

    public CommandDialog(Device device, CommandHandler commandHandler) {
        this.device = device;
        this.commandHandler = commandHandler;

        ListStore<CommandType> commandTypes = new ListStore<>(new EnumKeyProvider<CommandType>());
        commandTypes.addAll(Arrays.asList(CommandType.values()));
        this.typeCombo = new ComboBox<>(commandTypes, new LabelProvider<CommandType>() {
            @Override
            public String getLabel(CommandType item) {
                return i18n.commandType(item);
            }
        });

        ListStore<String> frequencyUnits = new ListStore<>(new ModelKeyProvider<String>() {
            @Override
            public String getKey(String item) {
                return item;
            }
        });
        frequencyUnits.add(i18n.second());
        frequencyUnits.add(i18n.minute());
        frequencyUnits.add(i18n.hour());
        this.frequencyUnit = new ComboBox<>(frequencyUnits, new StringLabelProvider<>());
        uiBinder.createAndBindUi(this);

        typeCombo.addSelectionHandler(new SelectionHandler<CommandType>() {
            @Override
            public void onSelection(SelectionEvent<CommandType> event) {
                toggleUI(event.getSelectedItem());
            }
        });
    }

    private void toggleUI(CommandType type) {
        lblFrequency.setVisible(type == CommandType.positionPeriodic);
        frequency.setVisible(type == CommandType.positionPeriodic);
        frequencyUnit.setVisible(type == CommandType.positionPeriodic);

        lblCustomMessage.setVisible(type == CommandType.CUSTOM);
        customMessage.setVisible(type == CommandType.CUSTOM);

        window.forceLayout();
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("sendButton")
    public void onSendClicked(SelectEvent event) {
        int frequency = -1;
        if (this.frequency.getCurrentValue() != null) {
            String unit = frequencyUnit.getCurrentValue();
            frequency = this.frequency.getCurrentValue();
            frequency *=
                    i18n.minute().equals(unit) ? 60
                    : i18n.hour().equals(unit) ? 3600 : 1;
        }
        commandHandler.onSend(device, typeCombo.getCurrentValue(), frequency, customMessage.getCurrentValue());
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
