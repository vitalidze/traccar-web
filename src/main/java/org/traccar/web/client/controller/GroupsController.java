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
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Window;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.view.GroupsDialog;
import org.traccar.web.client.view.NavView;
import org.traccar.web.client.view.UserShareDialog;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.User;

import java.util.*;

public class GroupsController implements NavView.GroupsHandler, ContentController {
    private final Messages i18n = GWT.create(Messages.class);
    private final TreeStore<Group> groupStore;

    public interface GroupAddHandler {
        void groupAdded(Group group);
    }

    public interface ChangesSaveHandler {
        void changesSaved();
    }

    public GroupsController() {
        GroupProperties groupProperties = GWT.create(GroupProperties.class);
        this.groupStore = new TreeStore<>(groupProperties.id());
    }

    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
        final GroupServiceAsync service = GWT.create(GroupService.class);
        service.getGroups(new BaseAsyncCallback<Map<Group, List<Group>>>(i18n) {
            @Override
            public void onSuccess(Map<Group, List<Group>> result) {
                // TODO sort result somehow to add hierarchy sequentially
                for (Map.Entry<Group, List<Group>> entry : result.entrySet()) {
                    Group parent = entry.getKey();
                    List<Group> children = entry.getValue();
                    if (parent == null) {
                        for (Group child : children) {
                            groupStore.add(child);
                        }
                    } else {
                        groupStore.add(parent, children);
                    }
                }
            }
        });
    }

    @Override
    public void onShowGroups() {
        final GroupServiceAsync service = GWT.create(GroupService.class);
        final Map<Group, List<Group>> originalParents = getParents();

        GroupsDialog.GroupsHandler handler = new GroupsDialog.GroupsHandler() {
            @Override
            public void onAdd(Group parent, Group group, final GroupAddHandler groupsHandler) {
                service.addGroup(parent, group, new BaseAsyncCallback<Group>(i18n) {
                    @Override
                    public void onSuccess(Group result) {
                        groupsHandler.groupAdded(result);
                    }
                });
            }

            @Override
            public void onSave(final ChangesSaveHandler groupsHandler) {
                final List<Group> listToSave = new ArrayList<>();
                for (Store<Group>.Record record : groupStore.getModifiedRecords()) {
                    Group originalGroup = record.getModel();
                    Group group = new Group(originalGroup.getId()).copyFrom(originalGroup);
                    for (Store.Change<Group, ?> change : record.getChanges()) {
                        change.modify(group);
                    }
                    listToSave.add(group);
                }

                for (Group group : groupStore.getAll()) {
                    Group originalParent = getOriginalParent(group);
                    Group newParent = groupStore.getParent(group);
                    if (!Objects.equals(originalParent, newParent)) {
                        listToSave.add(group);
                    }
                }

                Map<Group, List<Group>> groupsWithParents = new HashMap<>();
                for (Group group : listToSave) {
                    Group parent = groupStore.getParent(group);
                    List<Group> subGroups = groupsWithParents.get(parent);
                    if (subGroups == null) {
                        subGroups = new ArrayList<>();
                        groupsWithParents.put(parent, subGroups);
                    }
                    subGroups.add(group);
                }

                service.updateGroups(groupsWithParents, new BaseAsyncCallback<Void>(i18n) {
                    @Override
                    public void onSuccess(Void result) {
                        syncOriginalParents();
                        groupsHandler.changesSaved();
                        groupStore.commitChanges();
                    }
                });
            }

            @Override
            public void onRemove(final Group group) {
                List<Group> toRemove = new ArrayList<>();
                toRemove.add(group);
                toRemove.addAll(groupStore.getAllChildren(group));
                service.removeGroups(toRemove, new BaseAsyncCallback<Void>(i18n) {
                    @Override
                    public void onSuccess(Void result) {
                        groupStore.remove(group);
                        syncOriginalParents();
                    }
                });
            }

            @Override
            public void onCancelSaving(final List<Group> newGroups) {
                // Move updated nodes to the original parents
                for (Map.Entry<Group, List<Group>> entry : originalParents.entrySet()) {
                    Group originalParent = entry.getKey();
                    List<Group> subGroups = entry.getValue();
                    for (Group group : subGroups) {
                        if (!Objects.equals(groupStore.getParent(group), originalParent)) {
                            TreeStore.TreeNode<Group> subTree = groupStore.getSubTree(group);
                            groupStore.remove(group);
                            if (originalParent == null) {
                                groupStore.addSubTree(subGroups.indexOf(group), Collections.singletonList(subTree));
                            } else {
                                groupStore.addSubTree(originalParent, subGroups.indexOf(group), Collections.singletonList(subTree));
                            }
                        }
                    }
                }
                service.removeGroups(newGroups, new BaseAsyncCallback<Void>(i18n) {
                    @Override
                    public void onSuccess(Void result) {
                        for (Group group : newGroups) {
                            groupStore.remove(group);
                        }
                        groupStore.rejectChanges();
                    }
                });
            }

            @Override
            public void onShare(final Group group) {
                service.getGroupShare(group, new BaseAsyncCallback<Map<User, Boolean>>(i18n) {
                    @Override
                    public void onSuccess(Map<User, Boolean> result) {
                        new UserShareDialog(result, new UserShareDialog.UserShareHandler() {
                            @Override
                            public void onSaveShares(Map<User, Boolean> shares, final Window window) {
                                service.saveGroupShare(group, shares, new BaseAsyncCallback<Void>(i18n) {
                                    @Override
                                    public void onSuccess(Void result) {
                                        window.hide();
                                    }
                                });
                            }
                        }).show();
                    }
                });
            }

            private void syncOriginalParents() {
                originalParents.clear();
                originalParents.putAll(getParents());
            }

            private Group getOriginalParent(Group group) {
                for (Map.Entry<Group, List<Group>> entry : originalParents.entrySet()) {
                    Group originalParent = entry.getKey();
                    if (entry.getValue().contains(group)) {
                        return originalParent;
                    }
                }
                return null;
            }
        };

        new GroupsDialog(groupStore, handler).show();
    }

    private Map<Group, List<Group>> getParents() {
        Map<Group, List<Group>> result = new HashMap<>();

        for (Group group : groupStore.getAll()) {
            Group parent = groupStore.getParent(group);
            List<Group> subGroups = result.get(parent);
            if (subGroups == null) {
                subGroups = new ArrayList<>();
                result.put(parent, subGroups);
            }
            subGroups.add(group);
        }

        return result;
    }

    public TreeStore<Group> getGroupStore() {
        return groupStore;
    }
}
