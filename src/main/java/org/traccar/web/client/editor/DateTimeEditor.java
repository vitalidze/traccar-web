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

import static org.traccar.web.client.DateTimeFieldUtil.getCombineDate;

import com.google.gwt.editor.client.LeafValueEditor;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.TimeField;

import java.util.Date;

public class DateTimeEditor implements LeafValueEditor<Date> {
    private final DateField dateField;
    private final TimeField timeField;

    public DateTimeEditor(DateField dateField, TimeField timeField) {
        this.dateField = dateField;
        this.timeField = timeField;
    }

    @Override
    public void setValue(Date value) {
        dateField.setValue(value);
        timeField.setValue(value);
    }

    @Override
    public Date getValue() {
        return getCombineDate(dateField, timeField);
    }
}
