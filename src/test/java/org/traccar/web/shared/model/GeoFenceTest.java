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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeoFenceTest {
    @Test
    public void testPointsParsing() {
        GeoFence gf = new GeoFence();
        assertTrue(gf.points().isEmpty());

        gf.setPoints("");
        assertTrue(gf.points().isEmpty());

        gf.setPoints("1 2");
        assertEquals(1, gf.points().size());
        assertEquals(1, gf.points().get(0).lon, 0.01);
        assertEquals(2, gf.points().get(0).lat, 0.01);

        gf.setPoints("-1 2");
        assertEquals(1, gf.points().size());
        assertEquals(-1, gf.points().get(0).lon, 0.01);
        assertEquals(2, gf.points().get(0).lat, 0.01);

        gf.setPoints("1 -2");
        assertEquals(1, gf.points().size());
        assertEquals(1, gf.points().get(0).lon, 0.01);
        assertEquals(-2, gf.points().get(0).lat, 0.01);

        gf.setPoints("-1 -2");
        assertEquals(1, gf.points().size());
        assertEquals(-1, gf.points().get(0).lon, 0.01);
        assertEquals(-2, gf.points().get(0).lat, 0.01);

        gf.setPoints("1.456123 2");
        assertEquals(1, gf.points().size());
        assertEquals(1.456123, gf.points().get(0).lon, 0.00000001);
        assertEquals(2, gf.points().get(0).lat, 0.01);

        gf.setPoints("1.456123 2.987123");
        assertEquals(1, gf.points().size());
        assertEquals(1.456123, gf.points().get(0).lon, 0.00000001);
        assertEquals(2.987123, gf.points().get(0).lat, 0.01);

        gf.setPoints("1.456123 2.987123,3.456789 4.5678");
        assertEquals(2, gf.points().size());
        assertEquals(1.456123, gf.points().get(0).lon, 0.00000001);
        assertEquals(2.987123, gf.points().get(0).lat, 0.01);
        assertEquals(3.456789, gf.points().get(1).lon, 0.00000001);
        assertEquals(4.5678, gf.points().get(1).lat, 0.01);

        gf.setPoints("1 2,3 4,-5 -7,-8 0,-10 -11");
        assertEquals(5, gf.points().size());
        assertEquals(1, gf.points().get(0).lon, 0.01);
        assertEquals(2, gf.points().get(0).lat, 0.01);
        assertEquals(3, gf.points().get(1).lon, 0.01);
        assertEquals(4, gf.points().get(1).lat, 0.01);
        assertEquals(-5, gf.points().get(2).lon, 0.01);
        assertEquals(-7, gf.points().get(2).lat, 0.01);
        assertEquals(-8, gf.points().get(3).lon, 0.01);
        assertEquals(0, gf.points().get(3).lat, 0.01);
        assertEquals(-10, gf.points().get(4).lon, 0.01);
        assertEquals(-11, gf.points().get(4).lat, 0.01);
    }
}
