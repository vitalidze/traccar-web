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
package org.traccar.web.client;

import com.google.gwt.i18n.client.CurrencyList;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import org.traccar.web.shared.model.DistanceUnit;
import org.traccar.web.shared.model.SpeedUnit;

public class FormatterUtil {

    public DateTimeFormat getTimeFormat() {
        return DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
    }

    public DateTimeFormat getRequestTimeFormat() {
        return DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss Z");
    }

    private class SpeedNumberFormat extends NumberFormat {

        private final SpeedUnit speedUnit;

        public SpeedNumberFormat(SpeedUnit speedUnit) {
            super("0.##", CurrencyList.get().getDefault(), true);
            this.speedUnit = speedUnit;
        }

        @Override
        public String format(double number) {
            return super.format((Double.isNaN(number) ? 0d : number) * speedUnit.getFactor()) + " " + speedUnit.getUnit();
        }

    }

    private class DistanceNumberFormat extends NumberFormat {

        private final DistanceUnit distanceUnit;

        public DistanceNumberFormat(DistanceUnit distanceUnit) {
            super("0.##", CurrencyList.get().getDefault(), true);
            this.distanceUnit = distanceUnit;
        }

        @Override
        public String format(double number) {
            return super.format((Double.isNaN(number) ? 0d : number) * distanceUnit.getFactor()) + " " + distanceUnit.getUnit();
        }

    }

    public NumberFormat getSpeedFormat() {
        return new SpeedNumberFormat(ApplicationContext.getInstance().getUserSettings().getSpeedUnit());
    }

    public NumberFormat getDistanceFormat() {
        SpeedUnit speedUnit = ApplicationContext.getInstance().getUserSettings().getSpeedUnit();
        return new DistanceNumberFormat(speedUnit.getDistanceUnit());
    }
}
