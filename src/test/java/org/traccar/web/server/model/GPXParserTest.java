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
package org.traccar.web.server.model;

import org.junit.Test;
import org.traccar.web.shared.model.Position;

import javax.xml.stream.XMLStreamException;
import java.text.ParseException;

import static org.junit.Assert.*;

public class GPXParserTest {
    @Test
    public void testTraccarOutput() throws XMLStreamException, ParseException {
        GPXParser.Result r = new GPXParser().parse(getClass().getResourceAsStream("/org/traccar/web/server/model/test_traccar.gpx"), null);
        assertNotNull(r);
        assertNotNull(r.positions);
        assertEquals(2, r.positions.size());

        Position p = r.positions.get(0);
        assertEquals(-1, p.getLatitude(), 0.01);
        assertEquals(-3, p.getLongitude(), 0.01);
        assertEquals("ADDR1", p.getAddress());
        assertEquals(5.5, p.getSpeed(), 0.01);
        assertEquals(7.7, p.getCourse(), 0.01);
        assertEquals(55.5, p.getAltitude(), 0.01);
        assertEquals(1420096479000L, p.getTime().getTime());
        assertEquals("<info><protocol>gps103</protocol><alarm>acc on</alarm><type>import_start</type></info>", p.getOther());

        p = r.positions.get(1);
        assertEquals(-2, p.getLatitude(), 0.01);
        assertEquals(-4, p.getLongitude(), 0.01);
        assertEquals("ADDR2", p.getAddress());
        assertEquals(8.8, p.getSpeed(), 0.01);
        assertEquals(10.10, p.getCourse(), 0.01);
        assertEquals(77.7, p.getAltitude(), 0.01);
        assertEquals(1420102196000L, p.getTime().getTime());
        assertEquals("<info><protocol>gps103</protocol><alarm>acc on</alarm><type>import_end</type></info>", p.getOther());

        assertNotNull(r.latestPosition);
        assertTrue(r.latestPosition == r.positions.get(1));
    }

    @Test
    public void testForeign() throws XMLStreamException, ParseException {
        GPXParser.Result r = new GPXParser().parse(getClass().getResourceAsStream("/org/traccar/web/server/model/test_foreign.gpx"), null);
        assertNotNull(r);
        assertNotNull(r.positions);
        assertEquals(6, r.positions.size());

        Object[][] expected = new Object[][] {
                { 10.02591601, 11.01236986, 109.43, 1404758632655L },
                { 12.0259547, 13.01249098, 104.76, 1404758644193L },
                { 14.02606332, 15.01255586, 105.04, 1404758649182L },
                { 16.02613562, 17.01263875, 107.89, 1404758652179L },
                { 18.02623771, 19.01269516, 108.71, 1404758656194L },
                { 20.02631864, 21.01279424, 108.77, 1404758660181L } };

        for (int i = 0; i < r.positions.size(); i++) {
            Position p = r.positions.get(i);
            assertEquals((Double) expected[i][0], p.getLatitude(), 0.00000000001);
            assertEquals((Double) expected[i][1], p.getLongitude(), 0.00000000001);
            assertEquals((Double) expected[i][2], p.getAltitude(), 0.0001);
            assertEquals(((Long) expected[i][3]).longValue(), p.getTime().getTime());
            assertNull(p.getAddress());
            assertNull(p.getSpeed());
            assertNull(p.getCourse());
            assertEquals("<info><protocol>gpx_import</protocol><type>import" + (i == 0 ? "_start" : i == r.positions.size() - 1 ? "_end" : "") + "</type></info>", p.getOther());
        }

        assertNotNull(r.latestPosition);
        assertTrue(r.latestPosition == r.positions.get(5));
    }

    @Test
    public void testWithForeignExtensions() throws XMLStreamException, ParseException {
        GPXParser.Result r = new GPXParser().parse(getClass().getResourceAsStream("/org/traccar/web/server/model/test_foreign_ext.gpx"), null);
        assertEquals(1, r.positions.size());
        Position p = r.positions.get(0);
        assertEquals(10.02591601, p.getLatitude(), 0.00000000001);
        assertEquals(11.01236986, p.getLongitude(), 0.00000000001);
        assertEquals(109.43, p.getAltitude(), 0.0001);
        assertEquals(1404758632655L, p.getTime().getTime());
        assertNull(p.getAddress());
        assertNull(p.getCourse());
        assertNull(p.getPower());
        assertNull(p.getSpeed());
        assertEquals("<info><protocol>gpx_import</protocol><Primary_ID>PID4</Primary_ID><Secondary_ID>SID4</Secondary_ID><additional-a>X</additional-a><additional-b>Y</additional-b><type>import_start</type></info>", p.getOther());
    }
}
