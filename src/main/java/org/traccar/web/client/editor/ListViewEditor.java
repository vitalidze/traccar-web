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
package org.traccar.web.client.editor;

import com.google.gwt.editor.client.LeafValueEditor;
import com.sencha.gxt.widget.core.client.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ListViewEditor<T> implements LeafValueEditor<Set<T>> {
    private final ListView<T, ?> listView;

    public ListViewEditor(ListView<T, ?> listView) {
        this.listView = listView;
    }

    @Override
    public void setValue(Set<T> value) {
        if (value == null) {
            listView.getSelectionModel().deselectAll();
        } else {
            listView.getSelectionModel().select(new ArrayList<T>(value), false);
        }
    }

    @Override
    public Set<T> getValue() {
        return new HashSet<T>(listView.getSelectionModel().getSelectedItems());
    }
}
