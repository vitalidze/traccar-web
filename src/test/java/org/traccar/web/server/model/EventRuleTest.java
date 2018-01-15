/*
 * Copyright 2018 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.server.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.traccar.web.shared.model.EventRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventRuleTest {
    @Test
    public void testTimeRegex() {
        Pattern p = Pattern.compile(EventRule.TIME_REGEX);
        assertTrue(p.matcher("11:05am").matches());
        assertTrue(p.matcher("11am").matches());
        assertFalse(p.matcher("13am").matches());
        assertTrue(p.matcher("3am").matches());
        assertTrue(p.matcher("0am").matches());
        assertTrue(p.matcher("10pm").matches());
        assertFalse(p.matcher("10:60pm").matches());
        assertFalse(p.matcher("10:70pm").matches());
        assertFalse(p.matcher("100pm").matches());
        assertFalse(p.matcher("10").matches());
        assertFalse(p.matcher("10m").matches());
    }

    @Test
    public void testTimeFrameRegex() {
        Pattern p = Pattern.compile(EventRule.TIME_FRAME_REGEX);
        assertTrue(p.matcher("11:05am-11:06am").matches());
        assertTrue(p.matcher("11:05am-11:06pm").matches());
        assertTrue(p.matcher("12:05am-11:06pm").matches());
        assertTrue(p.matcher("12am-11pm").matches());
        assertFalse(p.matcher("11:5am-11:06am").matches());
        assertFalse(p.matcher("11:05am-11:6am").matches());
        assertFalse(p.matcher("11:05am-11:06am,5am-6am").matches());

        Matcher m = p.matcher("11:05am-11:06am,5am-6am");
        int matchesCount = 0;
        while (m.find()) {
            matchesCount++;
        }
        assertEquals(2, matchesCount);
    }

    @Test
    public void testCourseRegex() {
        Pattern p = Pattern.compile(EventRule.COURSE_REGEX);
        assertTrue(p.matcher("11-11").matches());
        assertTrue(p.matcher("111-11").matches());
        assertTrue(p.matcher("123-321").matches());
        assertFalse(p.matcher("11").matches());
        assertFalse(p.matcher("11-1111").matches());
    }
}
