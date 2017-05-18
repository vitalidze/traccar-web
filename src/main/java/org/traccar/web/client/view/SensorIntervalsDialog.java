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
package org.traccar.web.client.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.menu.ColorMenu;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.SensorIntervalProperties;
import org.traccar.web.shared.model.Picture;
import org.traccar.web.shared.model.SensorInterval;

import java.util.LinkedList;
import java.util.List;

public class SensorIntervalsDialog implements SelectionChangedEvent.SelectionChangedHandler<SensorInterval> {

    private static SensorIntervalsDialogUiBinder uiBinder = GWT.create(SensorIntervalsDialogUiBinder.class);

    interface SensorIntervalsDialogUiBinder extends UiBinder<Widget, SensorIntervalsDialog> {
    }

    interface SensorIntervalsHandler {
        List<SensorInterval> getIntervals();
        void setIntervals(List<SensorInterval> intervals);
    }

    interface ColorTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"background-color:{0}\">&nbsp;</div>")
        SafeHtml div(String color);

        @Template("<img src=\"{0}\">")
        SafeHtml img(String src);
    }

    @UiField
    Window window;

    @UiField(provided = true)
    ColumnModel<SensorInterval> columnModel;

    @UiField(provided = true)
    final ListStore<SensorInterval> sensorStore;

    @UiField
    Grid<SensorInterval> grid;

    @UiField
    TextButton removeButton;

    @UiField
    VerticalLayoutContainer mainContainer;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    final SensorIntervalsHandler handler;

    public SensorIntervalsDialog(SensorIntervalsHandler handler) {
        this.handler = handler;

        final SensorIntervalProperties sensorProperties = GWT.create(SensorIntervalProperties.class);

        this.sensorStore = new ListStore<>(new ModelKeyProvider<SensorInterval>() {
            @Override
            public String getKey(SensorInterval item) {
                return item.getText() + "_" + item.getValue();
            }
        });

        List<ColumnConfig<SensorInterval, ?>> columnConfigList = new LinkedList<>();

        ColumnConfig<SensorInterval, Double> valueColumn = new ColumnConfig<>(sensorProperties.value(), 100, i18n.intervalFrom());
        valueColumn.setFixed(true);
        valueColumn.setResizable(false);
        columnConfigList.add(valueColumn);
        ColumnConfig<SensorInterval, String> nameColumn = new ColumnConfig<>(sensorProperties.text(), 25, i18n.text());
        columnConfigList.add(nameColumn);

        // color
        ColumnConfig<SensorInterval, String> colorColumn = new ColumnConfig<>(sensorProperties.color(), 64, i18n.color());
        colorColumn.setCell(new AbstractCell<String>() {

            final ColorTemplate template = GWT.create(ColorTemplate.class);

            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.append(template.div("#" + value));
                }
            }
        });
        colorColumn.setFixed(true);
        colorColumn.setResizable(false);
        colorColumn.setSortable(false);
        columnConfigList.add(colorColumn);

        ColumnConfig<SensorInterval, String> colorSelectColumn = new ColumnConfig<>(new ValueProvider<SensorInterval, String>() {
            @Override
            public String getValue(SensorInterval object) {
                return i18n.select();
            }

            @Override
            public void setValue(SensorInterval object, String value) {
            }

            @Override
            public String getPath() {
                return "selectColor";
            }
        }, 58);
        colorSelectColumn.setFixed(true);
        colorSelectColumn.setResizable(false);
        colorSelectColumn.setSortable(false);
        // IMPORTANT we want the text element (cell parent) to only be as wide as
        // the cell and not fill the cell
        colorSelectColumn.setColumnTextClassName(CommonStyles.get().inlineBlock());
        colorSelectColumn.setColumnTextStyle(SafeStylesUtils.fromTrustedString("padding: 1px 3px 0;"));
        TextButtonCell colorSelectButton = new TextButtonCell();
        final ColorMenu colorSelectMenu = new ColorMenu();
        colorSelectButton.setMenu(colorSelectMenu);
        colorSelectButton.addSelectHandler(new SelectEvent.SelectHandler() {
            HandlerRegistration registration;

            @Override
            public void onSelect(SelectEvent event) {
                int row = event.getContext().getIndex();
                final SensorInterval interval = sensorStore.get(row);
                unregister();
                registration = colorSelectMenu.getPalette().addValueChangeHandler(new ValueChangeHandler<String>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        colorSelectMenu.hide(true);
                        unregister();
                        selectionChanged(interval, event.getValue());
                    }
                });
            }

            void selectionChanged(SensorInterval interval, String color) {
                sensorStore.getRecord(interval).addChange(sensorProperties.color(), color);
            }

            void unregister() {
                if (registration != null) {
                    registration.removeHandler();
                    registration = null;
                }
            }
        });
        colorSelectColumn.setCell(colorSelectButton);
        columnConfigList.add(colorSelectColumn);

        // icon
        ColumnConfig<SensorInterval, Long> iconColumn = new ColumnConfig<>(sensorProperties.pictureId(), 64, i18n.icon());
        iconColumn.setCell(new AbstractCell<Long>() {

            final ColorTemplate template = GWT.create(ColorTemplate.class);

            @Override
            public void render(Context context, Long value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.append(template.img(Picture.URL_PREFIX + value));
                }
            }
        });
        iconColumn.setFixed(true);
        iconColumn.setResizable(false);
        iconColumn.setSortable(false);
        columnConfigList.add(iconColumn);

        ColumnConfig<SensorInterval, String> iconEditColumn = new ColumnConfig<>(new ValueProvider<SensorInterval, String>() {
            @Override
            public String getValue(SensorInterval object) {
                return i18n.edit();
            }

            @Override
            public void setValue(SensorInterval object, String value) {
            }

            @Override
            public String getPath() {
                return "editIcon";
            }
        }, 58);
        iconEditColumn.setFixed(true);
        iconEditColumn.setResizable(false);
        iconEditColumn.setSortable(false);
        // IMPORTANT we want the text element (cell parent) to only be as wide as
        // the cell and not fill the cell
        iconEditColumn.setColumnTextClassName(CommonStyles.get().inlineBlock());
        iconEditColumn.setColumnTextStyle(SafeStylesUtils.fromTrustedString("padding: 1px 3px 0;"));
        TextButtonCell iconSelectButton = new TextButtonCell();
        final Menu iconSelectMenu = new Menu();
        final MenuItem iconUploadMenu = new MenuItem(i18n.upload() + "...");
        iconSelectMenu.add(iconUploadMenu);
        final MenuItem iconResetMenu = new MenuItem(i18n.reset());
        iconSelectMenu.add(iconResetMenu);

        iconSelectButton.setMenu(iconSelectMenu);
        iconSelectButton.addSelectHandler(new SelectEvent.SelectHandler() {
            HandlerRegistration registrationUpload;
            HandlerRegistration registrationReset;

            @Override
            public void onSelect(SelectEvent event) {
                int row = event.getContext().getIndex();
                final SensorInterval interval = sensorStore.get(row);
                unregister();

                registrationUpload = iconUploadMenu.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        unregister();
                        new SensorIconDialog(new SensorIconDialog.SensorIconHandler() {
                            @Override
                            public void uploaded(Picture icon) {
                                selectionChanged(interval, icon == null ? null : icon.getId());
                            }
                        }).show();
                    }
                });

                registrationReset = iconResetMenu.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        unregister();
                        selectionChanged(interval, null);
                    }
                });
            }

            void selectionChanged(SensorInterval interval, Long pictureId) {
                sensorStore.getRecord(interval).addChange(sensorProperties.pictureId(), pictureId);
            }

            void unregister() {
                if (registrationReset != null) {
                    registrationReset.removeHandler();
                    registrationReset = null;
                }
                if (registrationUpload != null) {
                    registrationUpload.removeHandler();
                    registrationUpload = null;
                }
            }
        });
        iconEditColumn.setCell(iconSelectButton);
        columnConfigList.add(iconEditColumn);

        columnModel = new ColumnModel<>(columnConfigList);

        uiBinder.createAndBindUi(this);

        // set up grid
        for (SensorInterval interval : handler.getIntervals()) {
            sensorStore.add(interval);
        }

        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedHandler(this);

        GridEditing<SensorInterval> editing = new GridInlineEditing<>(grid);
        NumberField<Double> valueEditor = new NumberField<>(new NumberPropertyEditor.DoublePropertyEditor());
        valueEditor.setAllowDecimals(true);
        valueEditor.setAllowBlank(false);
        valueEditor.setAllowNegative(true);
        editing.addEditor(valueColumn, valueEditor);
        editing.addEditor(nameColumn, new TextField());
    }

    public void show() {
        window.show();
    }

    @UiHandler("saveButton")
    public void onSaveChanges(SelectEvent event) {
        sensorStore.commitChanges();
        handler.setIntervals(sensorStore.getAll());
        window.hide();
    }

    @UiHandler("cancelButton")
    public void onCancel(SelectEvent event) {
        window.hide();
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        SensorInterval newInterval = new SensorInterval();
        newInterval.setText(i18n.interval() + " #" + (sensorStore.size() + 1));
        sensorStore.add(newInterval);
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        sensorStore.remove(sensorStore.indexOf(grid.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<SensorInterval> event) {
        removeButton.setEnabled(!event.getSelection().isEmpty());
    }
}
