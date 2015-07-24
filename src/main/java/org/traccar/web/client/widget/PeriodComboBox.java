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

import java.util.Date;

import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.info.Info;

public class PeriodComboBox extends ComboBox<String> {
	public PeriodComboBox() {
		super(new ListStore<String>(new ModelKeyProvider<String>() {
			@Override
			public String getKey(String item) {
				return item;
			}
		}), new LabelProvider<String>() {
			@Override
			public String getLabel(String item) {
				return item;
			}
		});

		getStore().add(0, "Today");
		getStore().add(1, "Yesterday");
		getStore().add(2, "This week");
		getStore().add(3, "Previous week");
		getStore().add(4, "This month");
		getStore().add(5, "Previous month");
		getStore().add(6, "Custom");
		setForceSelection(true);
		setTriggerAction(ComboBoxCell.TriggerAction.ALL);
	}

	
	public Date getStartPeriod(int index){
		Date date = new Date();
		
		CalendarUtil.resetTime(date); 
		
		switch(index){
			case 0: //Today
				return date;
			case 1: //Yesterday
				CalendarUtil.addDaysToDate(date, -1);
				return date;
			case 2: //This week
			    while(date.getDay() != CalendarUtil.getStartingDayOfWeek()){
			    	CalendarUtil.addDaysToDate(date, -1);
			    }
				return date;
			case 3: //Previous week
			    while(date.getDay() != CalendarUtil.getStartingDayOfWeek()){
			    	CalendarUtil.addDaysToDate(date, -1);
			    }
				CalendarUtil.addDaysToDate(date, -7);
				return date;
			case 4: //This month
				CalendarUtil.setToFirstDayOfMonth(date);
				return date;
			case 5: //Previous month
				CalendarUtil.addMonthsToDate(date, -1);
				CalendarUtil.setToFirstDayOfMonth(date);
				return date;
			case 6: 
				return null;
		}
		
		return null;
	}
	

	public Date getEndOfPeriod(int index){
		Date date = new Date();

		CalendarUtil.resetTime(date); 
		
		switch(index){
			case 0: //Today
				CalendarUtil.addDaysToDate(date, 1);
				return date;
			case 1: //Yesterday
				return date;
			case 2: //This week
			    while(date.getDay() != CalendarUtil.getStartingDayOfWeek()){
			    	CalendarUtil.addDaysToDate(date, -1);
			    }
				CalendarUtil.addDaysToDate(date, 7);
				return date;
			case 3: //Last week
			    while(date.getDay() != CalendarUtil.getStartingDayOfWeek()){
			    	CalendarUtil.addDaysToDate(date, -1);
			    }
				return date;
			case 4: //This month
				CalendarUtil.setToFirstDayOfMonth(date);
				CalendarUtil.addMonthsToDate(date, 1);
				return date;
			case 5: //Last month
				CalendarUtil.setToFirstDayOfMonth(date);
				return date;
			case 6: 
				return null;
		}
		
		return null;
	}
	


}
