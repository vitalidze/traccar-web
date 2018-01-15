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
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.box.AbstractInputMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.view.*;
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;

public class SettingsController implements NavView.SettingsHandler {

    private Messages i18n = GWT.create(Messages.class);
    private final UserSettingsDialog.UserSettingsHandler userSettingsHandler;
    private final UserSettingsDialog.UserSettingsHandler defaultUserSettingsHandler;
    private final GeoFenceController geoFenceController;
    private final DeviceController deviceController;
    private final UserDialog.EventRuleHandler eventRuleHandler;

    public SettingsController(UserSettingsDialog.UserSettingsHandler userSettingsHandler,
                              UserSettingsDialog.UserSettingsHandler defaultUserSettingsHandler,
                              GeoFenceController geoFenceController,
                              DeviceController deviceController) {
        this.userSettingsHandler = userSettingsHandler;
        this.defaultUserSettingsHandler = defaultUserSettingsHandler;
        this.geoFenceController = geoFenceController;
        this.deviceController = deviceController;
        eventRuleHandler = new EventRuleHandlerImpl();
    }

    @Override
    public void onAccountSelected() {
        new UserDialog(
                ApplicationContext.getInstance().getUser(), geoFenceController, deviceController,
                new UserDialog.UserHandler() {
                    @Override
                    public void onSave(final User user, final ListStore<EventRule> eventRulesStore) {
                        Application.getDataService().updateUser(user, new BaseAsyncCallback<User>(i18n) {
                            @Override
                            public void onSuccess(User result) {
                                ApplicationContext.getInstance().setUser(result);
                                eventRuleHandler.onSave(eventRulesStore, result);
                            }
                        });
                    }
        }, eventRuleHandler).show();
    }

    @Override
    public void onPreferencesSelected() {
        new UserSettingsDialog(ApplicationContext.getInstance().getUserSettings(), userSettingsHandler).show();
    }

    @Override
    public void onUsersSelected() {
        Application.getDataService().getUsers(new BaseAsyncCallback<List<User>>(i18n) {
            @Override
            public void onSuccess(List<User> result) {
                UserProperties userProperties = GWT.create(UserProperties.class);
                final ListStore<User> userStore = new ListStore<>(userProperties.id());
                userStore.addAll(result);

                new UsersDialog(userStore, new UsersDialog.UserHandler() {

                    @Override
                    public void onAdd() {
                        class AddHandler implements UserDialog.UserHandler {
                            @Override
                            public void onSave(final User user, final ListStore<EventRule> eventRulesStore) {
                                Application.getDataService().addUser(user, new BaseAsyncCallback<User>(i18n) {
                                    @Override
                                    public void onSuccess(User result) {
                                        userStore.add(result);
                                        eventRuleHandler.onSave(eventRulesStore, result);
                                    }

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        AlertMessageBox msg;
                                        if (caught instanceof InvalidMaxDeviceNumberForUserException) {
                                            InvalidMaxDeviceNumberForUserException e = (InvalidMaxDeviceNumberForUserException) caught;
                                            msg = new AlertMessageBox(i18n.error(), i18n.errMaxNumOfDevicesExceeded(e.getAllowedDevicesNumber()));
                                        } else {
                                            msg = new AlertMessageBox(i18n.error(), i18n.errUsernameTaken());
                                        }
                                        msg.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                                            @Override
                                            public void onDialogHide(DialogHideEvent event) {
                                                new UserDialog(user, geoFenceController, deviceController, AddHandler.this, eventRuleHandler).show();
                                            }
                                        });
                                        msg.show();
                                    }
                                });
                            }
                        }

                        new UserDialog(new User(), geoFenceController, deviceController, new AddHandler(), eventRuleHandler).show();
                    }

                    @Override
                    public void onRemove(final User user) {
                        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmUserRemoval());
                        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                            @Override
                            public void onDialogHide(DialogHideEvent event) {
                                if (event.getHideButton() == PredefinedButton.YES) {
                                    Application.getDataService().removeUser(user, new BaseAsyncCallback<User>(i18n) {
                                        @Override
                                        public void onSuccess(User result) {
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
                        List<User> updatedUsers = new ArrayList<>(userStore.getModifiedRecords().size());
                        for (Store<User>.Record record : userStore.getModifiedRecords()) {
                            User updatedUser = new User(record.getModel());
                            for (Store.Change<User, ?> change : record.getChanges()) {
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
                    public void onChangePassword(final User user) {
                        final AbstractInputMessageBox passwordInput = new AbstractInputMessageBox(new PasswordField(), i18n.changePassword(), i18n.enterNewPassword(user.getLogin())) {
                        };
                        passwordInput.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                            @Override
                            public void onDialogHide(DialogHideEvent event) {
                                if (event.getHideButton() == PredefinedButton.OK) {
                                    final String oldPassword = user.getPassword();
                                    user.setPassword(passwordInput.getValue());
                                    Application.getDataService().updateUser(user, new BaseAsyncCallback<User>(i18n) {
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
                    public void onSave(final ApplicationSettings applicationSettings) {
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
        service.getSettings(new BaseAsyncCallback<NotificationSettings>(i18n) {
            @Override
            public void onSuccess(NotificationSettings settings) {
                if (settings == null) {
                    settings = new NotificationSettings();
                }
                new NotificationSettingsDialog(settings, new NotificationSettingsDialog.NotificationSettingsHandler() {
                    @Override
                    public void onSave(NotificationSettings notificationSettings) {
                        service.saveSettings(notificationSettings, new BaseAsyncCallback<Void>(i18n));
                    }

                    @Override
                    public void onTestEmail(NotificationSettings notificationSettings) {
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
                    public void onTestPushbullet(NotificationSettings notificationSettings) {
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
                    public void onTestMessageTemplate(NotificationTemplate template) {
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

    @Override
    public void onDefaultPreferencesSelected() {
        Application.getDataService().getDefaultUserSettings(new BaseAsyncCallback<UserSettings>(i18n) {
            @Override
            public void onSuccess(UserSettings result) {
                new UserSettingsDialog(result, defaultUserSettingsHandler).show();
            }
        });
    }

    public static class EventRuleHandlerImpl implements UserDialog.EventRuleHandler {
        final EventRuleServiceAsync service = GWT.create(EventRuleService.class);
        private Messages i18n = GWT.create(Messages.class);
        @Override
        public void onShowEventRules(final ListStore<EventRule> eventRulesStore,User user) {
            final EventRuleServiceAsync service = GWT.create(EventRuleService.class);
            service.getEventRules(user, new BaseAsyncCallback<List<EventRule>>(i18n) {
                @Override
                public void onSuccess(List<EventRule> result) {
                    eventRulesStore.replaceAll(result);
                }
            });
        }
        @Override
        public void onSave(final ListStore<EventRule> eventRulesStore, User user) {
            for (Store<EventRule>.Record record : eventRulesStore.getModifiedRecords()) {
                final EventRule originalEventRule = record.getModel();
                EventRule eventRule = new EventRule().copyFromClient(originalEventRule);
                for (Store.Change<EventRule, ?> change : record.getChanges()) {
                    change.modify(eventRule);
                }
                eventRule.setTimeZoneShift((long) new Date().getTimezoneOffset()*60*1000);
                if (eventRule.getId() <= 0) {
                    eventRule.setId(0);
                    service.addEventRule(user, eventRule, new BaseAsyncCallback<EventRule>(i18n) {
                        @Override
                        public void onSuccess(EventRule result) {
                            eventRulesStore.remove(originalEventRule);
                            eventRulesStore.add(result);
                        }
                    });
                } else {
                    service.updateEventRule(user, eventRule, new BaseAsyncCallback<EventRule>(i18n) {
                        @Override
                        public void onSuccess(EventRule result) {
                            eventRulesStore.update(result);
                        }
                    });
                }
            }
        }
        @Override
        public void onRemove(final ListStore<EventRule> eventRulesStore, final EventRule eventRule) {
            service.removeEventRule(eventRule, new BaseAsyncCallback<Void>(i18n) {
                @Override
                public void onSuccess(Void result) {
                    eventRulesStore.remove(eventRule);
                }
            });
        }
    }
}
