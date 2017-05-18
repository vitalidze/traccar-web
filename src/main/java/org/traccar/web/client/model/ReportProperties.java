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
package org.traccar.web.client.model;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.Report;
import org.traccar.web.shared.model.ReportType;

public interface ReportProperties extends PropertyAccess<Report> {
    ModelKeyProvider<Report> id();

    ValueProvider<Report, String> name();

    ValueProvider<Report, ReportType> type();

    class ReportTypeLabelProvider implements LabelProvider<ReportType>, ValueProvider<Report, String> {
        final Messages i18n = GWT.create(Messages.class);

        @Override
        public String getLabel(ReportType item) {
            return i18n.reportType(item);
        }

        @Override
        public String getValue(Report report) {
            return report.getType() == null ? "" : getLabel(report.getType());
        }

        @Override
        public void setValue(Report object, String value) {
        }

        @Override
        public String getPath() {
            return "type";
        }
    }
}
