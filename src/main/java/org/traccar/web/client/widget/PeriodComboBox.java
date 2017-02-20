/*
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

import java.util.Arrays;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.TimeField;
import org.traccar.web.client.i18n.Messages;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.shared.model.Period;

public class PeriodComboBox extends ComboBox<Period> {
	private DateField fromDate;
	private TimeField fromTime;
	private DateField toDate;
	private TimeField toTime;

	private static final Messages i18n = GWT.create(Messages.class);

	public PeriodComboBox() {
		super(new ListStore<>(new EnumKeyProvider<Period>()), new LabelProvider<Period>() {
			@Override
			public String getLabel(Period item) {
				return i18n.period(item);
			}
		});

		getStore().addAll(Arrays.asList(Period.values()));

		setForceSelection(true);
		setEmptyText(i18n.periodComboBox_SelectPeriod());
		setTriggerAction(ComboBoxCell.TriggerAction.ALL);

		addSelectionHandler(new SelectionHandler<Period>() {
			@Override
			public void onSelection(SelectionEvent<Period> event) {
				setDateTimefd(event.getSelectedItem());
			}
		});
	}

	private void setDateTimefd(Period period){
		if (period != null && period != Period.CUSTOM){
			fromTime.setValue(period.getStartDate());
			fromDate.setValue(period.getStartDate());
			toDate.setValue(period.getEndDate());
			toTime.setValue(period.getEndDate());
		}
	}

	public void init(DateField fromDate, TimeField fromTime, DateField toDate, TimeField toTime) {
		this.fromDate = fromDate;
		this.fromTime = fromTime;
		this.toDate = toDate;
		this.toTime = toTime;
	}

	public void selectFirst() {
		setValue(getStore().get(0));
		setDateTimefd(getStore().get(0));
	}

	public void update() {
		setDateTimefd(getValue());
	}
}
