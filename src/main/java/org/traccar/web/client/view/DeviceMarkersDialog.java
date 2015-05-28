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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.SimpleSafeHtmlCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.*;
import com.sencha.gxt.theme.base.client.listview.ListViewCustomAppearance;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.BaseStoreHandlers;
import org.traccar.web.client.model.PicturesService;
import org.traccar.web.client.model.PicturesServiceAsync;
import org.traccar.web.shared.model.*;

import java.util.ArrayList;
import java.util.List;

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

    interface MarkerListItemTemplate extends XTemplates {
        @XTemplates.XTemplate("<div class='{style.thumbWrap}' style='border: 1px solid white'>{content}</div>")
        SafeHtml renderListItem(Style style, SafeHtml content);

        @XTemplates.XTemplate("<div class='{styles.clear}'></div>'")
        SafeHtml renderEndListItem(CommonStyles.Styles styles);

        @XTemplates.XTemplate("<div class='{style.thumb}' style='background: url(\"{marker.offlineURL}\") no-repeat center center;'></div>")
        SafeHtml listCell(Style style, Marker marker);

        @XTemplates.XTemplate("<div class='{style.thumbWrap}' style='background: #ffffff;'><span class=\"x-editable\">{text}</span><div class='{style.thumb}' style='background: url(\"{pictureURL}\") no-repeat center center;'></div></div>")
        SafeHtml pictureView(Style style, String text, String pictureURL);
    }

    public interface DeviceMarkerHandler {
        void onSave(DeviceIconType icon);
    }

    @UiField
    Window window;

    @UiField(provided = true)
    ListView<Marker, Marker> view;

    @UiField(provided = true)
    BorderLayoutContainer.BorderLayoutData northData;

    @UiField(provided = true)
    BorderLayoutContainer.BorderLayoutData centerData;

    @UiField(provided = true)
    BorderLayoutContainer.BorderLayoutData eastData;

    @UiField
    VerticalLayoutContainer panelImages;

    @UiField
    HTML defaultImage;

    @UiField
    HTML selectedImage;

    @UiField
    HTML offlineImage;

    @UiField
    TextButton addButton;

    @UiField
    TextButton editButton;

    @UiField
    TextButton removeButton;

    @UiField(provided = true)
    final Messages i18n = GWT.create(Messages.class);

    final PicturesServiceAsync picturesService = GWT.create(PicturesService.class);

    final DeviceMarkerHandler handler;

    static abstract class Marker {
        abstract String getKey();
        abstract String getDefaultURL();
        abstract String getSelectedURL();
        abstract String getOfflineURL();
    }

    static class BuiltInMarker extends Marker {
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

    static class DatabaseMarker extends Marker {
        DeviceIcon icon;

        DatabaseMarker(DeviceIcon icon) {
            this.icon = icon;
        }

        @Override
        String getKey() {
            return Long.toString(icon.getId());
        }

        @Override
        String getDefaultURL() {
            return icon.defaultURL();
        }

        @Override
        String getSelectedURL() {
            return icon.selectedURL();
        }

        @Override
        String getOfflineURL() {
            return icon.offlineURL();
        }
    }

    static class MergingCallback extends BaseAsyncCallback<List<DeviceIcon>> {
        final AsyncCallback<List<Marker>> markerLoaderCallback;

        MergingCallback(Messages i18n, AsyncCallback<List<Marker>> markerLoaderCallback) {
            super(i18n);
            this.markerLoaderCallback = markerLoaderCallback;
        }

        @Override
        public void onSuccess(List<DeviceIcon> loaded) {
            List<Marker> result = new ArrayList<Marker>(loaded.size() + DeviceIconType.values().length);
            for (DeviceIcon icon : loaded) {
                result.add(new DatabaseMarker(icon));
            }
            for (DeviceIconType icon : DeviceIconType.values()) {
                result.add(new BuiltInMarker(icon));
            }
            markerLoaderCallback.onSuccess(result);
        }
    }

    RpcProxy<Object, List<Marker>> hybridProxy = new RpcProxy<Object, List<Marker>>() {
        @Override
        public void load(Object loadConfig, AsyncCallback<List<Marker>> callback) {
            picturesService.getMarkerPictures(new MergingCallback(i18n, callback));
        }
    };

    Marker selected;

    final ListStore<Marker> store;

    final Resources resources = GWT.create(Resources.class);
    final MarkerListItemTemplate renderer = GWT.create(MarkerListItemTemplate.class);

    public DeviceMarkersDialog(final DeviceIconType selectedIcon, DeviceMarkerHandler handler) {
        this.handler = handler;

        ModelKeyProvider<Marker> keyProvider = new ModelKeyProvider<Marker>() {
            @Override
            public String getKey(Marker item) {
                return item.getKey();
            }
        };

        resources.css().ensureInjected();
        final Style style = resources.css();

        ListViewCustomAppearance<Marker> appearance = new ListViewCustomAppearance<Marker>("." + style.thumbWrap(), style.over(), style.select()) {
            @Override
            public void renderEnd(SafeHtmlBuilder builder) {
                renderer.renderEndListItem(CommonStyles.get());
            }

            @Override
            public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
                builder.append(renderer.renderListItem(style, content));
            }
        };

        store = new ListStore<Marker>(keyProvider);
        Loader<Object, List<Marker>> loader = new Loader<Object, List<Marker>>(hybridProxy);
        loader.addLoadHandler(new ListStoreBinding<Object, Marker, List<Marker>>(store));

        view = new ListView<Marker, Marker>(store, new IdentityValueProvider<Marker>() {
            @Override
            public void setValue(Marker object, Marker value) {
            }
        }, appearance);

        view.setCell(new SimpleSafeHtmlCell<Marker>(new AbstractSafeHtmlRenderer<Marker>() {
            @Override
            public SafeHtml render(Marker object) {
                return renderer.listCell(style, object);
            }
        }));

        view.getSelectionModel().setSelectionMode(com.sencha.gxt.core.client.Style.SelectionMode.SINGLE);
        view.getSelectionModel().addSelectionHandler(new SelectionHandler<Marker>() {
            @Override
            public void onSelection(SelectionEvent<Marker> event) {
                selectionChanged();
            }
        });

        northData = new BorderLayoutContainer.BorderLayoutData(28);
        northData.setSplit(false);

        eastData = new BorderLayoutContainer.BorderLayoutData(100);
        eastData.setSplit(false);
        eastData.setMargins(new Margins(5, 0, 0, 0));

        centerData = new BorderLayoutContainer.BorderLayoutData();
        centerData.setMargins(new Margins(0, 5, 0, 0));

        uiBinder.createAndBindUi(this);

        selected = new BuiltInMarker(selectedIcon);
        store.addStoreHandlers(new BaseStoreHandlers<Marker>() {
            @Override
            public void onAnything() {
                for (int i = 0; i < store.size(); i++) {
                    if (store.get(i).getKey().equals(selected.getKey())) {
                        view.getElement(i).ensureVisible();
                        view.getElement(i).scrollIntoView();
                        view.getSelectionModel().select(i, false);
                        break;
                    }
                }
            }
        });
        loader.load();

        selectionChanged();
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

    private void selectionChanged() {
        Marker marker = view.getSelectionModel().getSelectedItem();
        if (marker == null) {
            defaultImage.setHTML("");
            selectedImage.setHTML("");
            offlineImage.setHTML("");
        } else {
            defaultImage.setHTML(renderer.pictureView(resources.css(), i18n.defaultIcon(), marker.getDefaultURL()));
            selectedImage.setHTML(renderer.pictureView(resources.css(), i18n.selectedIcon(), marker.getSelectedURL()));
            offlineImage.setHTML(renderer.pictureView(resources.css(), i18n.offlineIcon(), marker.getOfflineURL()));
        }
        panelImages.forceLayout();

        editButton.setEnabled(marker instanceof DatabaseMarker);
        removeButton.setEnabled(marker instanceof DatabaseMarker);
    }

    @UiHandler("addButton")
    public void addIcon(SelectEvent event) {
        new DeviceIconDialog(false, new DeviceIconDialog.DeviceIconHandler() {
            @Override
            public void uploaded(Picture defaultIcon, Picture selectedIcon, Picture offlineIcon) {
                DeviceIcon marker = new DeviceIcon();
                marker.setDefaultIcon(defaultIcon);
                marker.setSelectedIcon(selectedIcon);
                marker.setOfflineIcon(offlineIcon);
                picturesService.addMarkerPicture(marker, new BaseAsyncCallback<DeviceIcon>(i18n) {
                    @Override
                    public void onSuccess(DeviceIcon added) {
                        Marker marker = new DatabaseMarker(added);
                        selected = marker;
                        store.add(0, marker);
                    }
                });
            }
        }).show();
    }

    @UiHandler("editButton")
    public void editIcon(SelectEvent event) {
        final DatabaseMarker marker = (DatabaseMarker) view.getSelectionModel().getSelectedItem();
        new DeviceIconDialog(true, new DeviceIconDialog.DeviceIconHandler() {
            @Override
            public void uploaded(Picture defaultIcon, Picture selectedIcon, Picture offlineIcon) {
                DeviceIcon icon = marker.icon;
                if (defaultIcon != null) icon.setDefaultIcon(defaultIcon);
                if (selectedIcon != null) icon.setSelectedIcon(selectedIcon);
                if (offlineIcon != null) icon.setOfflineIcon(offlineIcon);
                if (defaultIcon != null || selectedIcon != null || offlineIcon != null) {
                    picturesService.updateMarkerPicture(icon, new BaseAsyncCallback<DeviceIcon>(i18n) {
                        @Override
                        public void onSuccess(DeviceIcon updated) {
                            selected = marker;
                            marker.icon = updated;
                            selectionChanged();
                        }
                    });
                }
            }
        }).show();
    }

    @UiHandler("removeButton")
    public void removeIcon(SelectEvent event) {
        final Marker marker = view.getSelectionModel().getSelectedItem();
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmDeviceIconRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.YES) {
                    picturesService.removeMarkerPicture(((DatabaseMarker) marker).icon, new BaseAsyncCallback<Void>(i18n) {
                        @Override
                        public void onSuccess(Void result) {
                            view.getSelectionModel().deselectAll();
                            store.remove(marker);
                        }
                    });
                }
            }
        });
        dialog.show();
    }
}
