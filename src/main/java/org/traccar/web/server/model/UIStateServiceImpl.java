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
import org.traccar.web.client.model.UIStateService;
import org.traccar.web.shared.model.UIStateEntry;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;

@Singleton
public class UIStateServiceImpl extends RemoteServiceServlet implements UIStateService {

    @Inject
    private Provider<User> sessionUser;

    @Inject
    private Provider<EntityManager> entityManager;

    @Transactional
    @RequireUser
    @Override
    public String getValue(String name) {
        UIStateEntry entry = getUIStateEntry(name);
        return entry == null ? null : entry.getValue();
    }

    @Transactional
    @RequireUser
    @Override
    public void setValue(String name, String value) {
        UIStateEntry entry = getUIStateEntry(name);
        if (entry == null) {
            entry = new UIStateEntry();
            entry.setName(name);
            entry.setUser(sessionUser.get());
        }
        entry.setValue(value);
        entityManager.get().persist(entry);
    }

    private UIStateEntry getUIStateEntry(String name) {
        List<UIStateEntry> l = entityManager.get().createQuery("SELECT se FROM UIStateEntry se WHERE se.user=:user AND se.name=:name", UIStateEntry.class)
        .setParameter("user", sessionUser.get())
        .setParameter("name", name)
        .getResultList();

        return l.isEmpty() ? null : l.get(0);
    }
}
