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
package org.traccar.web.client.widget;

import com.google.gwt.i18n.client.LocaleInfo;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import java.util.Arrays;

public class LanguageComboBox extends ComboBox<String> {
    public LanguageComboBox() {
        super(new ListStore<String>(new ModelKeyProvider<String>() {
            @Override
            public String getKey(String item) {
                return item;
            }
        }), new LabelProvider<String>() {
            @Override
            public String getLabel(String item) {
                return (item.equals("default") ? "english" : LocaleInfo.getLocaleNativeDisplayName(item));
            }
        });

        getStore().addAll(Arrays.asList(LocaleInfo.getAvailableLocaleNames()));
        setForceSelection(true);
        setTriggerAction(ComboBoxCell.TriggerAction.ALL);
    }
}
