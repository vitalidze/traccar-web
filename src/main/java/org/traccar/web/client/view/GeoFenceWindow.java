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
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.shared.model.*;

import java.util.Arrays;

public class GeoFenceWindow implements Editor<GeoFence> {

    private static GeoFenceDialogUiBinder uiBinder = GWT.create(GeoFenceDialogUiBinder.class);

    interface GeoFenceDialogUiBinder extends UiBinder<Widget, GeoFenceWindow> {
    }

    private GeoFenceDriver driver = GWT.create(GeoFenceDriver.class);

    interface GeoFenceDriver extends SimpleBeanEditorDriver<GeoFence, GeoFenceWindow> {
    }

    public interface GeoFenceHandler {
        public void onSave(GeoFence device);
    }

    private GeoFenceHandler geoFenceHandler;

    @UiField
    Window window;

    @UiField
    TextField name;

    @UiField
    TextArea description;

    @UiField(provided = true)
    ComboBox<GeoFenceType> type;

    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();

    @UiField
    NumberField<Double> radius;

    @UiField
    ColorPalette color;

    public GeoFenceWindow(GeoFence device, GeoFenceHandler geoFenceHandler) {
        this.geoFenceHandler = geoFenceHandler;

        ListStore<GeoFenceType> geoFenceTypeStore = new ListStore<GeoFenceType>(
                new EnumKeyProvider<GeoFenceType>());
        geoFenceTypeStore.addAll(Arrays.asList(GeoFenceType.values()));
        type = new ComboBox<GeoFenceType>(
                geoFenceTypeStore, new GeoFenceProperties.GeoFenceTypeLabelProvider());

        type.setForceSelection(true);
        type.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        uiBinder.createAndBindUi(this);

        driver.initialize(this);
        driver.edit(device);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        window.hide();
        geoFenceHandler.onSave(driver.flush());
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

}
