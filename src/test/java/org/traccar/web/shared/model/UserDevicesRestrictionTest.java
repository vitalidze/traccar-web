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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class UserDevicesRestrictionTest {
    User u1;
    User u2;
    User u3;
    User u4;
    User u5;

    User m2;
    User m4;
    User m3;
    User m1;

    /**
     * Users hierarchy with devices restrictions:
     *
     * <pre>
     *                top manager [m1] (100)
     *                /             \_____
     *       (null) manager [m2]        manager[m3] (50)
     *             /      \               /     \
     *      (10) user u1   u2 (20) (null) u3    m4 (null)
     *                                         / \
     *                                   (10) u4  u5 (null)
     * </pre>
     */
    @Before
    public void createStrictHierarchy() {
        u1 = u("u1", 10);
        u2 = u("u2", 20);
        u3 = u("u3", null);
        u4 = u("u4", 10);
        u5 = u("u5", null);

        m2 = m("m2", null, u1, u2);
        m4 = m("m4", null, u4, u5);
        m3 = m("m3", 50, u3, m4);
        m1 = m("m1", 100, m2, m3);
    }

    @Test
    public void testStrictHierarchyInitial() {
        testStrictHierarchy(100, 100, 50, 50, 10, 20, 50, 10, 50);
    }

    @Test
    public void testStrictHierarchyAddDevicesToLowestLevels() {
        devices(u1, 5);
        testStrictHierarchy(95, 95, 50, 50, 5, 20, 50, 10, 50);
        devices(u2, 15);
        testStrictHierarchy(80, 80, 50, 50, 5, 5, 50, 10, 50);
        devices(u3, 10);
        testStrictHierarchy(70, 70, 40, 40, 5, 5, 40, 10, 40);
        devices(u4, 7);
        testStrictHierarchy(63, 63, 33, 33, 5, 5, 33, 3, 33);
        devices(u5, 13);
        testStrictHierarchy(50, 50, 20, 20, 5, 5, 20, 3, 20);
        devices(m1, 30);
        testStrictHierarchy(20, 20, 20, 20, 5, 5, 20, 3, 20);
        devices(m2, 10);
        testStrictHierarchy(10, 10, 10, 10, 5, 5, 10, 3, 10);
        devices(m3, 8);
        testStrictHierarchy(2, 2, 2, 2, 2, 2, 2, 2, 2);
        devices(m4, 2);
        testStrictHierarchy(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testStrictNumberOfDevicesToDistribute() {
        assertEquals(20, m1.getNumberOfDevicesToDistribute());
        assertEquals(20, m2.getNumberOfDevicesToDistribute());
        assertEquals(40, m3.getNumberOfDevicesToDistribute());
        assertEquals(40, m4.getNumberOfDevicesToDistribute());
        assertEquals(10, u1.getNumberOfDevicesToDistribute());
        assertEquals(20, u2.getNumberOfDevicesToDistribute());
        assertEquals(40, u3.getNumberOfDevicesToDistribute());
        assertEquals(10, u4.getNumberOfDevicesToDistribute());
        assertEquals(40, u5.getNumberOfDevicesToDistribute());
    }

    private void testStrictHierarchy(int dm1,
                                     int dm2,
                                     int dm3,
                                     int dm4,
                                     int du1,
                                     int du2,
                                     int du3,
                                     int du4,
                                     int du5) {
        assertEquals("Allowed devices for manager 1", dm1, m1.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for manager 2", dm2, m2.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for manager 3", dm3, m3.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for manager 4", dm4, m4.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 1", du1, u1.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 2", du2, u2.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 3", du3, u3.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 4", du4, u4.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 5", du5, u5.getNumberOfDevicesToAdd());
    }

    User u6;
    User u7;
    User u8;
    User u9;

    User m5;
    User m6;
    User m7;
    User m8;
    User m9;

    /**
     * Users hierarchy with devices restrictions:
     *
     * <pre>
     *                top manager [m5] (100)
     *                /
     *             manager [m6] (50)
     *               /
     *             manager [m7] (100)
     *              /    \___________
     *   (90) manager [m8]          manager [m9] (90)
     *            / \____           /      \_____
     * (null) user [u6]  u7 (null) (null) u8    u9 (null)
     * </pre>
     */
    @Before
    public void createWeirdHierarchy() {
        u6 = u("u6", null);
        u7 = u("u7", null);
        u8 = u("u8", null);
        u9 = u("u9", null);

        m8 = m("m8", 90, u6, u7);
        m9 = m("m9", 90, u8, u9);
        m7 = m("m7", 100, m8, m9);
        m6 = m("m6", 50, m7);
        m5 = m("m5", 100, m6);
    }

    @Test
    public void testWeirdHierarchyNumberOfDevicesToDistribute() {
        assertEquals(50, m5.getNumberOfDevicesToDistribute());
        assertEquals(0, m6.getNumberOfDevicesToDistribute());
        assertEquals(0, m7.getNumberOfDevicesToDistribute());
        assertEquals(90, m8.getNumberOfDevicesToDistribute());
        assertEquals(90, m9.getNumberOfDevicesToDistribute());
    }

    @Test
    public void testWeirdHierarchyInitial() {
        testWeirdHierarchy(100, 50, 50, 50, 50, 50, 50, 50, 50);
    }

    @Test
    public void testWeirdHierarchyAddDevicesToLowestLevels() {
        devices(m8, 25);
        testWeirdHierarchy(75, 25, 25, 25, 25, 25, 25, 25, 25);
        devices(m9, 25);
        testWeirdHierarchy(50, 0, 0, 0, 0, 0, 0, 0, 0);
        devices(m5, 15);
        testWeirdHierarchy(35, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private void testWeirdHierarchy(int dm5,
                                    int dm6,
                                    int dm7,
                                    int dm8,
                                    int dm9,
                                    int du6,
                                    int du7,
                                    int du8,
                                    int du9) {
        assertEquals("Allowed devices for manager 5", dm5, m5.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for manager 6", dm6, m6.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for manager 7", dm7, m7.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for manager 8", dm8, m8.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for manager 9", dm9, m9.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 6", du6, u6.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 7", du7, u7.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 8", du8, u8.getNumberOfDevicesToAdd());
        assertEquals("Allowed devices for user 9", du9, u9.getNumberOfDevicesToAdd());
    }

    private User m(String name, Integer maxNumOfDevices, User... managedUsers) {
        User manager = new User(name);
        manager.setManager(true);
        manager.setManagedUsers(new HashSet<>(Arrays.asList(managedUsers)));
        manager.setMaxNumOfDevices(maxNumOfDevices);
        for (User managedUser : managedUsers) {
            managedUser.setManagedBy(manager);
        }
        return manager;
    }

    private User u(String name, Integer maxNumOfDevices) {
        User user = new User(name);
        user.setMaxNumOfDevices(maxNumOfDevices);
        user.setManagedUsers(Collections.<User>emptySet());
        return user;
    }

    private void devices(User u, int number) {
        u.setDevices(new HashSet<Device>());
        for (int i = 0; i < number; i++) {
            Device device = new Device();
            device.setName(u.getLogin() + "-d" + i);
            device.setUniqueId(device.getName());
            u.getDevices().add(device);
        }
    }
}
