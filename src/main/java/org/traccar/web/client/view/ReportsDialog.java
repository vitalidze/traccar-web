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
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.form.*;
import org.traccar.web.client.model.DeviceProperties;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.model.ReportProperties;
import org.traccar.web.client.widget.PeriodComboBox;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.GeoFenceType;
import org.traccar.web.shared.model.ReportType;

import java.util.Arrays;

public class ReportsDialog {
    private static ReportsDialogDialogUiBinder uiBinder = GWT.create(ReportsDialogDialogUiBinder.class);

    interface ReportsDialogDialogUiBinder extends UiBinder<Widget, ReportsDialog> {
    }

    @UiField
    Window window;

    @UiField
    TextField name;

    @UiField(provided = true)
    ComboBox<ReportType> type;

    @UiField(provided = true)
    final ListStore<Device> deviceStore;

    @UiField(provided = true)
    final ListView<Device, String> deviceList;

    @UiField(provided = true)
    final ListStore<GeoFence> geoFenceStore;

    @UiField(provided = true)
    final ListView<GeoFence, String> geoFenceList;

    @UiField(provided = true)
    final PeriodComboBox periodCombo;

    @UiField
    DateField fromDate;

    @UiField
    TimeField fromTime;

    @UiField
    DateField toDate;

    @UiField
    TimeField toTime;

    public ReportsDialog(ListStore<Device> deviceStore, ListStore<GeoFence> geoFenceStore) {
        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        this.deviceStore = deviceStore;
        this.deviceList = new ListView<Device, String>(deviceStore, deviceProperties.name());

        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);
        this.geoFenceStore = geoFenceStore;
        this.geoFenceList = new ListView<GeoFence, String>(geoFenceStore, geoFenceProperties.name());

        ListStore<ReportType> geoFenceTypeStore = new ListStore<ReportType>(
                new EnumKeyProvider<ReportType>());
        geoFenceTypeStore.addAll(Arrays.asList(ReportType.values()));
        type = new ComboBox<ReportType>(
                geoFenceTypeStore, new ReportProperties.ReportTypeLabelProvider());
        type.setForceSelection(true);
        type.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        periodCombo = new PeriodComboBox();

        uiBinder.createAndBindUi(this);

        periodCombo.init(fromDate, fromTime, toDate, toTime);
        periodCombo.selectFirst();
    }

    public void show() {
        window.show();
    }
}
