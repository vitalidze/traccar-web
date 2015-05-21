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
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.SimpleSafeHtmlCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.theme.base.client.listview.ListViewCustomAppearance;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent;
import com.sencha.gxt.widget.core.client.form.FileUploadField;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import org.traccar.web.shared.model.DeviceIconType;
import org.traccar.web.shared.model.Position;

public class DeviceMarkersDialog {
    private static DeviceMarkersDialogUiBinder uiBinder = GWT.create(DeviceMarkersDialogUiBinder.class);

    interface DeviceMarkersDialogUiBinder extends UiBinder<Widget, DeviceMarkersDialog> {
    }

    interface Resources extends ClientBundle {
        @ClientBundle.Source("PicturesListView.css")
        Style css();
    }

    interface Style extends CssResource {
        String thumb();

        String thumbWrap();

        String over();

        String select();
    }

    public interface DeviceMarkerHandler {
        void onSave(DeviceIconType icon);
    }

    @UiField
    Window window;

    ListStore<Marker> store;

    @UiField(provided = true)
    ListView<Marker, Marker> view;

    @UiField(provided = true)
    BorderLayoutContainer.BorderLayoutData centerData;

    @UiField(provided = true)
    BorderLayoutContainer.BorderLayoutData eastData;

    @UiField(provided = true)
    BorderLayoutContainer.BorderLayoutData southData;

    @UiField
    VerticalLayoutContainer panelImages;

    @UiField
    Image defaultImage;

    @UiField
    Image selectedImage;

    @UiField
    Image offlineImage;

    @UiField
    FormPanel form;

    @UiField
    FileUploadField fileToImport;

    final DeviceMarkerHandler handler;

    static abstract class Marker {
        abstract String getKey();
        abstract String getDefaultURL();
        abstract String getSelectedURL();
        abstract String getOfflineURL();
    }

    class BuiltInMarker extends Marker {
        final DeviceIconType icon;

        BuiltInMarker(DeviceIconType icon) {
            this.icon = icon;
        }

        @Override
        String getKey() {
            return icon.name();
        }

        @Override
        String getOfflineURL() {
            return icon.getPositionIconType(Position.Status.OFFLINE).getURL(false);
        }

        @Override
        String getDefaultURL() {
            return icon.getPositionIconType(Position.Status.LATEST).getURL(false);
        }

        @Override
        String getSelectedURL() {
            return icon.getPositionIconType(Position.Status.LATEST).getURL(true);
        }
    }

    public DeviceMarkersDialog(DeviceIconType selectedIcon, DeviceMarkerHandler handler) {
        this.handler = handler;

        ModelKeyProvider<Marker> keyProvider = new ModelKeyProvider<Marker>() {
            @Override
            public String getKey(Marker item) {
                return item.getKey();
            }
        };

        Marker selectedMarker = null;
        store = new ListStore<Marker>(keyProvider);
        for (DeviceIconType icon : DeviceIconType.values()) {
            Marker marker = new BuiltInMarker(icon);
            store.add(marker);
            if (selectedIcon == icon) {
                selectedMarker = marker;
            }
        }

        final Resources resources = GWT.create(Resources.class);
        resources.css().ensureInjected();

        final Style style = resources.css();

        ListViewCustomAppearance<Marker> appearance = new ListViewCustomAppearance<Marker>("." + style.thumbWrap(), style.over(), style.select()) {
            @Override
            public void renderEnd(SafeHtmlBuilder builder) {
                String markup = new StringBuilder("<div class=\"").append(CommonStyles.get().clear()).append("\"></div>").toString();
                builder.appendHtmlConstant(markup);
            }

            @Override
            public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
                builder.appendHtmlConstant("<div class='" + style.thumbWrap() + "' style='border: 1px solid white'>");
                builder.append(content);
                builder.appendHtmlConstant("</div>");
            }
        };

        view = new ListView<Marker, Marker>(store, new IdentityValueProvider<Marker>() {
            @Override
            public void setValue(Marker object, Marker value) {
            }
        }, appearance);

        view.setCell(new SimpleSafeHtmlCell<Marker>(new AbstractSafeHtmlRenderer<Marker>() {
            @Override
            public SafeHtml render(Marker object) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                return builder
                        .appendHtmlConstant("<div class=\"")
                        .appendHtmlConstant(style.thumb())
                        .appendHtmlConstant("\" style=\"background: url(")
                        .appendHtmlConstant(object.getOfflineURL())
                        .appendHtmlConstant(") no-repeat center center;\"></div>")
                        .toSafeHtml();
            }
        }));

        view.getSelectionModel().setSelectionMode(com.sencha.gxt.core.client.Style.SelectionMode.SINGLE);
        view.getSelectionModel().addSelectionHandler(new SelectionHandler<Marker>() {
            @Override
            public void onSelection(SelectionEvent<Marker> event) {
                updateImages();
            }
        });

        eastData = new BorderLayoutContainer.BorderLayoutData(85);
        eastData.setSplit(false);
        eastData.setMargins(new Margins(5, 0, 0, 0));

        centerData = new BorderLayoutContainer.BorderLayoutData();
        centerData.setMargins(new Margins(0, 5, 0, 0));

        southData = new BorderLayoutContainer.BorderLayoutData(32);
        southData.setSplit(false);
        southData.setMargins(new Margins(5, 0, 0, 5));

        uiBinder.createAndBindUi(this);

        form.addSubmitCompleteHandler(new SubmitCompleteEvent.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                new LogViewDialog(event.getResults()).show();
            }
        });

        view.getSelectionModel().select(selectedMarker, false);

        updateImages();
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onOKClicked(SelectEvent event) {
        Marker selected = view.getSelectionModel().getSelectedItem();
        handler.onSave(selected == null ? null : ((BuiltInMarker) selected).icon);
        hide();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        hide();
    }

    @UiHandler("uploadButton")
    public void onUploadClicked(SelectEvent event) {
        form.submit();
    }

    private void updateImages() {
        Marker marker = view.getSelectionModel().getSelectedItem();
        if (marker == null) {
            defaultImage.setUrl("");
            selectedImage.setUrl("");
            offlineImage.setUrl("");
        } else {
            defaultImage.setUrl(marker.getDefaultURL());
            selectedImage.setUrl(marker.getSelectedURL());
            offlineImage.setUrl(marker.getOfflineURL());
        }
        panelImages.forceLayout();
    }
}
