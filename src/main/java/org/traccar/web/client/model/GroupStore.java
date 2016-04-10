/*
 * Copyright 2016 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client.model;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.TreeStore;
import org.traccar.web.shared.model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupStore extends TreeStore<Group> {
    public GroupStore() {
        super(GWT.<GroupProperties>create(GroupProperties.class).id());
    }

    public ListStore<Group> toListStore() {
        ListStore<Group> result = new ListStore<>(getKeyProvider());
        result.addAll(toList());
        return result;
    }

    private void addChildren(List<Group> list, List<Group> children) {
        for (Group child : children) {
            list.add(child);
            addChildren(list, getChildren(child));
        }
    }

    public List<Group> toList() {
        List<Group> result = new ArrayList<>();
        addChildren(result, getRootItems());
        return result;
    }
}
