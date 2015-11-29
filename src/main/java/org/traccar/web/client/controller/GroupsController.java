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
package org.traccar.web.client.controller;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.view.GroupsDialog;
import org.traccar.web.client.view.NavView;
import org.traccar.web.shared.model.Group;

import java.util.List;

public class GroupsController implements NavView.GroupsHandler {
    private final Messages i18n = GWT.create(Messages.class);

    @Override
    public void onShowGroups() {
        final GroupServiceAsync service = GWT.create(GroupService.class);
        service.getGroups(new BaseAsyncCallback<List<Group>>(i18n) {
            @Override
            public void onSuccess(List<Group> result) {
                GroupProperties groupProperties = GWT.create(GroupProperties.class);
                final ListStore<Group> groupStore = new ListStore<>(groupProperties.id());
                groupStore.addAll(result);

                GroupsDialog.GroupsHandler handler = new GroupsDialog.GroupsHandler() {
                    @Override
                    public void onSave() {
                        for (Store<Group>.Record record : groupStore.getModifiedRecords()) {
                            final Group originalGroup = record.getModel();
                            Group group = new Group(originalGroup.getId(), null).copyFrom(originalGroup);
                            for (Store.Change<Group, ?> change : record.getChanges()) {
                                change.modify(group);
                            }
                            if (group.getId() < 0) {
                                service.addGroup(group, new BaseAsyncCallback<Group>(i18n) {
                                    @Override
                                    public void onSuccess(Group result) {
                                        groupStore.remove(originalGroup);
                                        groupStore.add(result);
                                    }
                                });
                            } else {
                                service.updateGroup(group, new BaseAsyncCallback<Group>(i18n) {
                                    @Override
                                    public void onSuccess(Group result) {
                                        groupStore.update(result);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onRemove(final Group group) {
                        service.removeGroup(group, new BaseAsyncCallback<Void>(i18n) {
                            @Override
                            public void onSuccess(Void result) {
                                groupStore.remove(group);
                            }
                        });
                    }
                };

                new GroupsDialog(groupStore, handler).show();
            }
        });
    }
}
