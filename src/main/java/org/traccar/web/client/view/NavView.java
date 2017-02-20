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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.shared.model.Report;

public class NavView {
    private static NavViewUiBinder uiBinder = GWT.create(NavViewUiBinder.class);

    interface NavViewUiBinder extends UiBinder<Widget, NavView> {
    }

    public interface SettingsHandler {
        void onAccountSelected();
        void onPreferencesSelected();
        void onUsersSelected();
        void onApplicationSelected();
        void onNotificationsSelected();
        void onDefaultPreferencesSelected();
    }

    private final SettingsHandler settingsHandler;

    public interface ImportHandler {
        void onImport();
    }

    private final ImportHandler importHandler;

    public interface LogHandler {
        void onShowTrackerServerLog();
        void onShowWrapperLog();
    }

    final LogHandler logHandler;

    public interface GroupsHandler {
        void onShowGroups();
    }

    final GroupsHandler groupsHandler;

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    @UiField
    TextButton settingsButton;

    @UiField
    TextButton settingsAccount;

    @UiField
    TextButton settingsPreferences;

    @UiField
    MenuItem settingsUsers;

    @UiField
    MenuItem settingsGlobal;

    @UiField
    MenuItem settingsNotifications;

    @UiField
    MenuItem settingsDefaultPreferences;

    @UiField
    TextButton logsButton;

    @UiField
    MenuItem showTrackerServerLog;

    @UiField
    TextButton groupsButton;

    @UiField
    TextButton importButton;

    @UiField
    TextButton reportsButton;

    @UiField(provided = true)
    final Messages i18n = GWT.create(Messages.class);

    public NavView(SettingsHandler settingsHandler,
                   ListStore<Report> reportListStore,
                   ReportsMenu.ReportHandler reportHandler,
                   ImportHandler importHandler,
                   LogHandler logHandler,
                   GroupsHandler groupsHandler) {
        this.settingsHandler = settingsHandler;
        this.importHandler = importHandler;
        this.logHandler = logHandler;
        this.groupsHandler = groupsHandler;

        uiBinder.createAndBindUi(this);

        boolean readOnly = ApplicationContext.getInstance().getUser().getReadOnly();
        boolean admin = ApplicationContext.getInstance().getUser().getAdmin();
        boolean manager = ApplicationContext.getInstance().getUser().getManager();

        settingsButton.setVisible(admin || manager);
        settingsAccount.setVisible(!readOnly);

        settingsGlobal.setVisible(!readOnly && admin);
        settingsDefaultPreferences.setVisible(!readOnly && admin);
        logsButton.setVisible(admin);
        showTrackerServerLog.setVisible(admin);
        settingsUsers.setVisible(!readOnly && (admin || manager));
        settingsNotifications.setVisible(!readOnly && (admin || manager));
        importButton.setVisible(!readOnly &&
                (!ApplicationContext.getInstance().getApplicationSettings().isDisallowDeviceManagementByUsers()
                    || admin || manager));

        groupsButton.setVisible(!readOnly);

        reportsButton.setMenu(new ReportsMenu(reportListStore, reportHandler, new ReportsMenu.ReportSettingsHandler() {
            @Override
            public void setSettings(ReportsDialog dialog) {
            }
        }));
    }

    @UiHandler("settingsAccount")
    public void onSettingsAccountClicked(SelectEvent event) {
        settingsHandler.onAccountSelected();
    }

    @UiHandler("settingsPreferences")
    public void onSettingsPreferencesClicked(SelectEvent event) {
        settingsHandler.onPreferencesSelected();
    }

    @UiHandler("settingsUsers")
    public void onSettingsUsersSelected(SelectionEvent<Item> event) {
        settingsHandler.onUsersSelected();
    }

    @UiHandler("settingsGlobal")
    public void onSettingsGlobalSelected(SelectionEvent<Item> event) {
        settingsHandler.onApplicationSelected();
    }

    @UiHandler("settingsNotifications")
    public void onSettingsNotificationsSelected(SelectionEvent<Item> event) {
        settingsHandler.onNotificationsSelected();
    }

    @UiHandler("settingsDefaultPreferences")
    public void onSettingsDefaultPreferencesSelected(SelectionEvent<Item> event) {
        settingsHandler.onDefaultPreferencesSelected();
    }

    @UiHandler("logoutButton")
    public void onLogoutClicked(SelectEvent event) {
        Application.getDataService().logout(new BaseAsyncCallback<Boolean>(i18n) {
            @Override
            public void onSuccess(Boolean result) {
                Window.Location.reload();
            }
        });
    }

    @UiHandler("showTrackerServerLog")
    public void onShowTrackerServerLog(SelectionEvent<Item> event) {
        logHandler.onShowTrackerServerLog();
    }

    @UiHandler("showWrapperLog")
    public void onShowWrapperLog(SelectionEvent<Item> event) {
        logHandler.onShowWrapperLog();
    }

    @UiHandler("importButton")
    public void onImportClicked(SelectEvent event) {
        importHandler.onImport();
    }

    @UiHandler("groupsButton")
    public void onGroupsClicked(SelectEvent event) {
        groupsHandler.onShowGroups();
    }
}
