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
        SafeHtml listCell(Style style, MarkerIcon marker);

        @XTemplates.XTemplate("<div class='{style.thumbWrap}' style='background: #ffffff;'><span class=\"x-editable\">{text}</span><div class='{style.thumb}' style='background: url(\"{pictureURL}\") no-repeat center center;'></div></div>")
        SafeHtml pictureView(Style style, String text, String pictureURL);
    }

    public interface DeviceMarkerHandler {
        void onSave(MarkerIcon icon);
    }

    @UiField
    Window window;

    @UiField(provided = true)
    ListView<MarkerIcon, MarkerIcon> view;

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

    static class MergingCallback extends BaseAsyncCallback<List<DeviceIcon>> {
        final AsyncCallback<List<MarkerIcon>> markerLoaderCallback;

        MergingCallback(Messages i18n, AsyncCallback<List<MarkerIcon>> markerLoaderCallback) {
            super(i18n);
            this.markerLoaderCallback = markerLoaderCallback;
        }

        @Override
        public void onSuccess(List<DeviceIcon> loaded) {
            List<MarkerIcon> result = new ArrayList<MarkerIcon>(loaded.size() + DeviceIconType.values().length);
            for (DeviceIcon icon : loaded) {
                result.add(new MarkerIcon.Database(icon));
            }
            for (DeviceIconType icon : DeviceIconType.values()) {
                result.add(new MarkerIcon.BuiltIn(icon));
            }
            markerLoaderCallback.onSuccess(result);
        }
    }

    RpcProxy<Object, List<MarkerIcon>> hybridProxy = new RpcProxy<Object, List<MarkerIcon>>() {
        @Override
        public void load(Object loadConfig, AsyncCallback<List<MarkerIcon>> callback) {
            picturesService.getMarkerPictures(new MergingCallback(i18n, callback));
        }
    };

    MarkerIcon selected;

    final ListStore<MarkerIcon> store;

    final Resources resources = GWT.create(Resources.class);
    final MarkerListItemTemplate renderer = GWT.create(MarkerListItemTemplate.class);

    public DeviceMarkersDialog(final MarkerIcon selectedIcon, DeviceMarkerHandler handler) {
        this.handler = handler;

        ModelKeyProvider<MarkerIcon> keyProvider = new ModelKeyProvider<MarkerIcon>() {
            @Override
            public String getKey(MarkerIcon item) {
                return item.getKey();
            }
        };

        resources.css().ensureInjected();
        final Style style = resources.css();

        ListViewCustomAppearance<MarkerIcon> appearance = new ListViewCustomAppearance<MarkerIcon>("." + style.thumbWrap(), style.over(), style.select()) {
            @Override
            public void renderEnd(SafeHtmlBuilder builder) {
                renderer.renderEndListItem(CommonStyles.get());
            }

            @Override
            public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
                builder.append(renderer.renderListItem(style, content));
            }
        };

        store = new ListStore<MarkerIcon>(keyProvider);
        Loader<Object, List<MarkerIcon>> loader = new Loader<Object, List<MarkerIcon>>(hybridProxy);
        loader.addLoadHandler(new ListStoreBinding<Object, MarkerIcon, List<MarkerIcon>>(store));

        view = new ListView<MarkerIcon, MarkerIcon>(store, new IdentityValueProvider<MarkerIcon>() {
            @Override
            public void setValue(MarkerIcon object, MarkerIcon value) {
            }
        }, appearance);

        view.setCell(new SimpleSafeHtmlCell<MarkerIcon>(new AbstractSafeHtmlRenderer<MarkerIcon>() {
            @Override
            public SafeHtml render(MarkerIcon object) {
                return renderer.listCell(style, object);
            }
        }));

        view.getSelectionModel().setSelectionMode(com.sencha.gxt.core.client.Style.SelectionMode.SINGLE);
        view.getSelectionModel().addSelectionHandler(new SelectionHandler<MarkerIcon>() {
            @Override
            public void onSelection(SelectionEvent<MarkerIcon> event) {
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

        window.setHeadingText(i18n.overlayType(UserSettings.OverlayType.MARKERS));

        selected = selectedIcon;
        store.addStoreHandlers(new BaseStoreHandlers<MarkerIcon>() {
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
        handler.onSave(view.getSelectionModel().getSelectedItem());
        hide();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        hide();
    }

    private void selectionChanged() {
        MarkerIcon marker = view.getSelectionModel().getSelectedItem();
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

        editButton.setEnabled(marker instanceof MarkerIcon.Database);
        removeButton.setEnabled(marker instanceof MarkerIcon.Database);
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
                        MarkerIcon marker = new MarkerIcon.Database(added);
                        selected = marker;
                        store.add(0, marker);
                    }
                });
            }
        }).show();
    }

    @UiHandler("editButton")
    public void editIcon(SelectEvent event) {
        final MarkerIcon.Database marker = (MarkerIcon.Database) view.getSelectionModel().getSelectedItem();
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
        final MarkerIcon.Database marker = (MarkerIcon.Database) view.getSelectionModel().getSelectedItem();
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmDeviceIconRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.YES) {
                    picturesService.removeMarkerPicture(marker.icon, new BaseAsyncCallback<Void>(i18n) {
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
