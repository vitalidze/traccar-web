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

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UserTest {

    /**
     * Users hierarchy:
     *
     * <pre>
     *                top manager [m1] --- (g1, g7)
     *                /          \__________
     *      (g2) --- manager [m2]           \
     *              /      \                manager [m3] --- (g5)
     * (g3) --- user [u1]  user [u2]  --- (g4)    \
     *                                            user [u3] --- (g6)
     * </pre>
     */
    @Test
    public void testAvailableGeoFences() {
        // set up users hierarchy
        User m1 = new User("m1");
        m1.setManager(true);
        User m2 = new User("m2");
        m2.setManager(true);
        m2.setManagedBy(m1);
        User m3 = new User("m3");
        m3.setManager(true);
        m3.setManagedBy(m1);

        m1.setManagedUsers(set(m2, m3));

        User u1 = new User("u1");
        User u2 = new User("u2");
        m2.setManagedUsers(set(u1, u2));
        u1.setManagedBy(m2);
        u2.setManagedBy(m2);

        User u3 = new User("u3");
        m3.setManagedUsers(set(u3));
        u3.setManagedBy(m3);

        // set up geo-fences
        GeoFence g1 = new GeoFence(1, "g1");
        GeoFence g2 = new GeoFence(2, "g2");
        GeoFence g3 = new GeoFence(3, "g3");
        GeoFence g4 = new GeoFence(4, "g4");
        GeoFence g5 = new GeoFence(5, "g5");
        GeoFence g6 = new GeoFence(6, "g6");
        GeoFence g7 = new GeoFence(7, "g7");

        m1.setGeoFences(Arrays.asList(g1, g7));
        m2.setGeoFences(Arrays.asList(g1, g2));
        m3.setGeoFences(Arrays.asList(g5));
        u1.setGeoFences(Arrays.asList(g3));
        u2.setGeoFences(Arrays.asList(g4));
        u3.setGeoFences(Arrays.asList(g6));

        // test
        assertEquals(set(g1, g2, g3, g4, g5, g6, g7), m1.getAllAvailableGeoFences());
        assertEquals(set(g1, g2, g3, g4, g7), m2.getAllAvailableGeoFences());
        assertEquals(set(g1, g5, g6, g7), m3.getAllAvailableGeoFences());
        assertEquals(set(g1, g2, g3, g7), u1.getAllAvailableGeoFences());
        assertEquals(set(g1, g2, g4, g7), u2.getAllAvailableGeoFences());
        assertEquals(set(g1, g5, g6, g7), u3.getAllAvailableGeoFences());
    }

    private <T> Set<T> set(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
