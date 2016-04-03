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
package org.traccar.web.server.model;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.GroupService;
import org.traccar.web.shared.model.AccessDeniedException;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.*;

@Singleton
public class GroupServiceImpl extends RemoteServiceServlet implements GroupService {
    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    private Provider<User> sessionUser;

    @Inject
    private DataService dataService;

    @Transactional
    @RequireUser
    @Override
    public Map<Group, List<Group>> getGroups() {
        List<Group> groups;

        if (sessionUser.get().getAdmin()) {
            groups = entityManager.get().createQuery("SELECT x FROM Group x", Group.class).getResultList();
        } else {
            groups = new ArrayList<>(sessionUser.get().getAllAvailableGroups());
        }

        Map<Group, List<Group>> result = new HashMap<>();
        for (Group group : groups) {
            List<Group> children = result.get(group.getParent());
            if (children == null) {
                children = new ArrayList<>();
                result.put(group.getParent(), children);
            }
            children.add(group);
        }
        for (List<Group> children : result.values()) {
            Collections.sort(children, new Comparator<Group>() {
                @Override
                public int compare(Group o1, Group o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
        }
        return result;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public Group addGroup(Group group) {
        Group toSave = new Group().copyFrom(group);

        toSave.setUsers(new HashSet<User>(1));
        toSave.getUsers().add(sessionUser.get());
        entityManager.get().persist(toSave);

        return toSave;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public Group updateGroup(Group group) throws AccessDeniedException {
        User user = sessionUser.get();
        Group toSave = entityManager.get().find(Group.class, group.getId());
        if (!user.hasAccessTo(toSave)) {
            throw new AccessDeniedException();
        }

        toSave.copyFrom(group);

        return toSave;
    }

    @Transactional
    @RequireUser
    @RequireWrite
    @Override
    public void removeGroup(Group group) throws AccessDeniedException {
        User user = sessionUser.get();
        Group toRemove = entityManager.get().find(Group.class, group.getId());
        if (!user.hasAccessTo(toRemove)) {
            throw new AccessDeniedException();
        }

        if (user.getAdmin() || user.getManager()) {
            toRemove.getUsers().removeAll(dataService.getUsers());
        }
        toRemove.getUsers().remove(user);
        if (toRemove.getUsers().isEmpty()) {
            entityManager.get().createQuery("UPDATE Device d SET d.group=null WHERE d.group=:group").setParameter("group", toRemove).executeUpdate();
            entityManager.get().remove(toRemove);
        }
    }

    @Transactional
    @RequireUser
    @Override
    public Map<User, Boolean> getGroupShare(Group group) {
        group = entityManager.get().find(Group.class, group.getId());
        List<User> users = dataService.getUsers();
        Map<User, Boolean> result = new HashMap<>(users.size());
        for (User user : users) {
            user = new User(user);
            user.setUserSettings(null);
            user.setPassword(null);
            result.put(user, group.getUsers().contains(user));
        }
        return result;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @Override
    public void saveGroupShare(Group group, Map<User, Boolean> share) {
        group = entityManager.get().find(Group.class, group.getId());

        for (User user : dataService.getUsers()) {
            Boolean shared = share.get(user);
            if (shared == null) continue;
            user = entityManager.get().find(User.class, user.getId());
            if (shared) {
                group.getUsers().add(user);
            } else {
                group.getUsers().remove(user);
            }
        }
    }
}
