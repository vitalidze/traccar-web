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
package org.traccar.web.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import java.util.Date;

public enum Period implements IsSerializable {
    TODAY {
        @Override
        public Date getStartDate() {
            return resetTime(new Date());
        }

        @Override
        public Date getEndDate() {
            return addDays(getStartDate(), 1);
        }
    },
    YESTERDAY {
        @Override
        public Date getStartDate() {
            return addDays(getEndDate(), -1);
        }

        @Override
        public Date getEndDate() {
            return resetTime(new Date());
        }
    },
    THIS_WEEK {
        @Override
        public Date getStartDate() {
            Date date = resetTime(new Date());
            while(date.getDay() != CalendarUtil.getStartingDayOfWeek()){
                CalendarUtil.addDaysToDate(date, -1);
            }
            return date;
        }

        @Override
        public Date getEndDate() {
            return addDays(getStartDate(), 7);
        }
    },
    PREVIOUS_WEEK {
        @Override
        public Date getStartDate() {
            return addDays(THIS_WEEK.getStartDate(), -7);
        }

        @Override
        public Date getEndDate() {
            return THIS_WEEK.getStartDate();
        }
    },
    THIS_MONTH {
        @Override
        public Date getStartDate() {
            return firstDayOfMonth(resetTime(new Date()));
        }

        @Override
        public Date getEndDate() {
            return addMonths(getStartDate(), 1);
        }
    },
    PREVIOUS_MONTH {
        @Override
        public Date getStartDate() {
            return firstDayOfMonth(addMonths(resetTime(new Date()), -1));
        }

        @Override
        public Date getEndDate() {
            return firstDayOfMonth(resetTime(new Date()));
        }
    },
    CUSTOM {
        @Override
        public Date getStartDate() {
            return null;
        }

        @Override
        public Date getEndDate() {
            return null;
        }
    };

    static Date resetTime(Date date) {
        CalendarUtil.resetTime(date);
        return date;
    }

    static Date addDays(Date date, int days) {
        CalendarUtil.addDaysToDate(date, days);
        return date;
    }

    static Date firstDayOfMonth(Date date) {
        CalendarUtil.setToFirstDayOfMonth(date);
        return date;
    }

    static Date addMonths(Date date, int months) {
        CalendarUtil.addMonthsToDate(date, months);
        return date;
    }

    public abstract Date getStartDate();
    public abstract Date getEndDate();
}
