/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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
package org.traccar.web.client.controller;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.box.AbstractInputMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.NotificationService;
import org.traccar.web.client.model.NotificationServiceAsync;
import org.traccar.web.client.model.UserProperties;
import org.traccar.web.client.view.*;
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;

public class SettingsController implements DeviceView.SettingsHandler {

    private Messages i18n = GWT.create(Messages.class);
    private final UserSettingsDialog.UserSettingsHandler userSettingsHandler;

    public SettingsController(UserSettingsDialog.UserSettingsHandler userSettingsHandler) {
        this.userSettingsHandler = userSettingsHandler;
    }

    @Override
    public void onAccountSelected() {
        new UserDialog(
                ApplicationContext.getInstance().getUser(),
                new UserDialog.UserHandler() {
                    @Override
                    public void onSave(UserDTO user) {
                        Application.getDataService().updateUser(user, new BaseAsyncCallback<UserDTO>(i18n) {
                            @Override
                            public void onSuccess(UserDTO result) {
                                ApplicationContext.getInstance().setUser(result);
                            }
                        });
                    }
                }).show();
    }

    @Override
    public void onPreferencesSelected() {
        new UserSettingsDialog(ApplicationContext.getInstance().getUserSettings(), userSettingsHandler).show();
    }

    @Override
    public void onUsersSelected() {
        Application.getDataService().getUsers(new BaseAsyncCallback<List<UserDTO>>(i18n) {
            @Override
            public void onSuccess(List<UserDTO> result) {
                UserProperties userProperties = GWT.create(UserProperties.class);
                final ListStore<UserDTO> userStore = new ListStore<UserDTO>(userProperties.id());
                userStore.addAll(result);

                new UsersDialog(userStore, new UsersDialog.UserHandler() {

                    @Override
                    public void onAdd() {
                        new UserDialog(
                                new UserDTO(),
                                new UserDialog.UserHandler() {
                                    @Override
                                    public void onSave(UserDTO user) {
                                        Application.getDataService().addUser(user, new BaseAsyncCallback<UserDTO>(i18n) {
                                            @Override
                                            public void onSuccess(UserDTO result) {
                                                userStore.add(result);
                                            }
                                            @Override
                                            public void onFailure(Throwable caught) {
                                                new AlertMessageBox(i18n.error(), i18n.errUsernameTaken()).show();
                                            }
                                        });
                                    }
                                }).show();
                    }

                    @Override
                    public void onRemove(final UserDTO user) {
                        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmUserRemoval());
                        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
							@Override
							public void onDialogHide(DialogHideEvent event) {
								if (event.getHideButton() == PredefinedButton.YES) {
                                    Application.getDataService().removeUser(user, new BaseAsyncCallback<UserDTO>(i18n) {
                                        @Override
                                        public void onSuccess(UserDTO result) {
                                            userStore.remove(user);
                                        }
                                    });
								}
							}
						});
                        dialog.show();
                    }

                    @Override
                    public void onSaveRoles() {
                        List<UserDTO> updatedUsers = new ArrayList<UserDTO>(userStore.getModifiedRecords().size());
                        for (Store<UserDTO>.Record record : userStore.getModifiedRecords()) {
                            UserDTO updatedUser = new UserDTO(record.getModel());
                            for (Store.Change<UserDTO, ?> change : record.getChanges()) {
                                change.modify(updatedUser);
                            }
                            updatedUsers.add(updatedUser);
                        }

                        Application.getDataService().saveRoles(updatedUsers, new BaseAsyncCallback<Void>(i18n) {
                            @Override
                            public void onSuccess(Void result) {
                                userStore.commitChanges();
                            }
                        });
                    }

                    @Override
                    public void onChangePassword(final UserDTO user) {
                        final AbstractInputMessageBox passwordInput = new AbstractInputMessageBox(new PasswordField(), i18n.changePassword(), i18n.enterNewPassword(user.getLogin())) {};
                        passwordInput.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                            @Override
                            public void onDialogHide(DialogHideEvent event) {
                                if (event.getHideButton() == PredefinedButton.OK) {
                                    final String oldPassword = user.getPassword();
                                    user.setPassword(passwordInput.getValue());
                                    Application.getDataService().updateUser(user, new BaseAsyncCallback<UserDTO>(i18n) {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            user.setPassword(oldPassword);
                                            super.onFailure(caught);
                                        }
                                    });
                                }
                            }
                        });
                        passwordInput.show();
                    }
                }).show();
            }
        });
    }

    @Override
    public void onApplicationSelected() {
        new ApplicationSettingsDialog(
                ApplicationContext.getInstance().getApplicationSettings(),
                new ApplicationSettingsDialog.ApplicationSettingsHandler() {
                    @Override
                    public void onSave(final ApplicationSettingsDTO applicationSettings) {
                        Application.getDataService().updateApplicationSettings(applicationSettings, new BaseAsyncCallback<Void>(i18n) {
                            @Override
                            public void onSuccess(Void result) {
                                ApplicationContext.getInstance().setApplicationSettings(applicationSettings);
                            }
                        });
                    }
                }).show();
    }

    @Override
    public void onNotificationsSelected() {
        final NotificationServiceAsync service = GWT.create(NotificationService.class);
        service.getSettings(new BaseAsyncCallback<NotificationSettingsDTO>(i18n) {
            @Override
            public void onSuccess(NotificationSettingsDTO settings) {
                if (settings == null) {
                    settings = new NotificationSettingsDTO();
                }
                new NotificationSettingsDialog(settings, new NotificationSettingsDialog.NotificationSettingsHandler() {
                    @Override
                    public void onSave(NotificationSettingsDTO notificationSettings) {
                        service.saveSettings(notificationSettings, new BaseAsyncCallback<Void>(i18n));
                    }

                    @Override
                    public void onTestEmail(NotificationSettingsDTO notificationSettings) {
                        service.checkEmailSettings(notificationSettings, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                new AlertMessageBox(i18n.notificationSettings(), i18n.testFailed() + "<br><br>" + caught.getLocalizedMessage()).show();
                            }

                            @Override
                            public void onSuccess(Void aVoid) {
                                MessageBox messageBox = new MessageBox(i18n.notificationSettings(), i18n.testSucceeded());
                                messageBox.setIcon(MessageBox.ICONS.info());
                                messageBox.show();
                            }
                        });
                    }

                    @Override
                    public void onTestPushbullet(NotificationSettingsDTO notificationSettings) {
                        service.checkPushbulletSettings(notificationSettings, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                new AlertMessageBox(i18n.notificationSettings(), i18n.testFailed() + "<br><br>" + caught.getLocalizedMessage()).show();
                            }

                            @Override
                            public void onSuccess(Void aVoid) {
                                MessageBox messageBox = new MessageBox(i18n.notificationSettings(), i18n.testSucceeded());
                                messageBox.setIcon(MessageBox.ICONS.info());
                                messageBox.show();
                            }
                        });
                    }

                    @Override
                    public void onTestMessageTemplate(NotificationTemplateDTO template) {
                        service.checkTemplate(template, new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                new AlertMessageBox(i18n.notificationSettings(), i18n.testFailed() + "<br><br>" + caught.getLocalizedMessage()).show();
                            }

                            @Override
                            public void onSuccess(String result) {
                                new MessageBox(i18n.notificationSettings(), result).show();
                            }
                        });
                    }
                }).show();
            }
        });
    }
}
