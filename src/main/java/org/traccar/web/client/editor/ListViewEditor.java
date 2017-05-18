/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.event.ShowEvent;

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
            listView.getSelectionModel().select(new ArrayList<>(value), false);
            if (listView.isVisible()) {
                showSelection();
            } else {
                listView.addResizeHandler(new ResizeHandler() {
                    @Override
                    public void onResize(ResizeEvent event) {
                        showSelection();
                    }
                });
            }
        }
    }

    private void showSelection() {
        for (T item : listView.getSelectionModel().getSelectedItems()) {
            int index = listView.getStore().indexOf(item);
            XElement element = listView.getElement(index);
            if (element != null) {
                element.scrollIntoView(listView.getElement(), false);
                listView.focus();
                break;
            }
        }
    }

    @Override
    public Set<T> getValue() {
        return new HashSet<>(listView.getSelectionModel().getSelectedItems());
    }
}
