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
package org.traccar.web.client.view;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.traccar.web.shared.model.SensorInterval;

import java.util.Arrays;
import java.util.List;

public class SensorIntervalTextTest {
    @Test
    public void test1() {
        List<SensorInterval> intervals = Arrays.asList(
                new SensorInterval("Critical", 0),
                new SensorInterval("Low", 20),
                new SensorInterval("Medium", 35),
                new SensorInterval("Good", 70),
                new SensorInterval("Full", 90)
        );

        assertEquals("Critical", intervalText(-1, intervals));
        assertEquals("Critical", intervalText(0, intervals));
        assertEquals("Critical", intervalText(1, intervals));
        assertEquals("Critical", intervalText(19.999, intervals));
        assertEquals("Low", intervalText(20, intervals));
        assertEquals("Low", intervalText(34.999, intervals));
        assertEquals("Medium", intervalText(35, intervals));
        assertEquals("Medium", intervalText(69.999, intervals));
        assertEquals("Good", intervalText(70, intervals));
        assertEquals("Good", intervalText(89.999, intervals));
        assertEquals("Full", intervalText(90, intervals));
        assertEquals("Full", intervalText(100, intervals));
    }

    static String intervalText(double value, List<SensorInterval> intervals) {
        String valueText = null;
        for (SensorInterval interval : intervals) {
            if (valueText == null) {
                valueText = interval.getText();
            }
            if (value < interval.getValue()) {
                break;
            }
            valueText = interval.getText();
        }
        return valueText;
    }
}
