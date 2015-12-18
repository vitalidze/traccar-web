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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static org.traccar.web.server.model.EventServiceImpl.*;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.DeviceEvent;
import org.traccar.web.shared.model.Position;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopMoveDetectorTest {
    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    Provider<EntityManager> emProvider;

    @Mock
    EntityManager entityManager;

    StopMoveDetector stopMoveDetector = new StopMoveDetector();

    Device device = new Device();

    @Mock
    PositionProvider positionProvider;

    PositionScanner scanner = new PositionScanner();

    List<Position> positions;

    @Before
    public void init() {
        when(emProvider.get()).thenReturn(entityManager);

        device.setMinIdleTime(60);

        stopMoveDetector.entityManager = emProvider;
        scanner.entityManager = emProvider;
        scanner.eventProducers.add(stopMoveDetector);
        scanner.positionProvider = positionProvider;
    }

    List<Position> readIdlePositions() throws XMLStreamException, ParseException, IOException {
        return new GPXParser().parse(getClass().getResourceAsStream("/org/traccar/web/server/model/idle_device.gpx"), null).positions;
    }

    void initPositions(List<Position> positions) throws IllegalAccessException {
        long id = 1;
        final Map<Long, Position> positionsMap = new HashMap<>(positions.size());
        for (Position position : positions) {
            position.setDevice(device);
            FieldUtils.writeField(position, "id", id++, true);
            positionsMap.put(position.getId(), position);
        }

        when(entityManager.find(eq(Position.class), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Long id = invocationOnMock.getArgumentAt(1, Long.class);
                return positionsMap.get(id);
            }
        });
        this.positions = positions;
    }

    @Test
    public void testIdleWithPrev() throws Exception {
        initPositions(readIdlePositions());

        for (int i = 0; i < positions.size(); i++) {
            when(positionProvider.getPositions()).thenReturn(positions.subList(i == 0 ? 0 : i - 1, i + 1));

            scanner.doWork();
        }

        checkIdlePositions();
    }

    @Test
    public void testIdleOneByOne() throws Exception {
        initPositions(readIdlePositions());

        for (int i = 0; i < positions.size(); i++) {
            when(positionProvider.getPositions()).thenReturn(positions.subList(i, i + 1));

            scanner.doWork();
        }

        checkIdlePositions();
    }

    @Test
    public void testIdleAllAtOnce() throws Exception {
        initPositions(readIdlePositions());

        when(positionProvider.getPositions()).thenReturn(positions);
        scanner.doWork();

        checkIdlePositions();
    }

    private void checkIdlePositions() {
        ArgumentCaptor<DeviceEvent> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(entityManager).persist(eventCaptor.capture());
        assertNotNull(eventCaptor.getValue());
        assertEquals(positions.get(0), eventCaptor.getValue().getPosition());
    }

    List<Position> readMovingPositions() throws XMLStreamException, ParseException, IOException {
        return new GPXParser().parse(getClass().getResourceAsStream("/org/traccar/web/server/model/moving_device.gpx"), null).positions;
    }

    @Test
    public void testMovingAllAtOnce() throws Exception {
        device.setMinIdleTime(180);
        initPositions(readMovingPositions());

        when(positionProvider.getPositions()).thenReturn(positions);
        scanner.doWork();

        checkMovingPositions();
    }

    @Test
    public void testMovingOneByOne() throws Exception {
        device.setMinIdleTime(180);
        initPositions(readMovingPositions());

        for (int i = 0; i < positions.size(); i++) {
            when(positionProvider.getPositions()).thenReturn(positions.subList(i, i + 1));

            scanner.doWork();
        }

        checkMovingPositions();
    }

    @Test
    public void testMovingWithPrev() throws Exception {
        device.setMinIdleTime(180);
        initPositions(readMovingPositions());

        for (int i = 0; i < positions.size(); i++) {
            when(positionProvider.getPositions()).thenReturn(positions.subList(i == 0 ? 0 : i - 1, i + 1));

            scanner.doWork();
        }

        checkMovingPositions();
    }

    void checkMovingPositions() {
        ArgumentCaptor<DeviceEvent> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);
        verify(entityManager, times(10)).persist(eventCaptor.capture());
        List<DeviceEvent> recordedEvents = eventCaptor.getAllValues();
        int i = 0;
        for (int expectedPositionIndex : new int[] {0, 54, 138, 674, 903, 998, 1336, 1361, 1494, 1539}) {
            DeviceEvent event = recordedEvents.get(i++);
            assertEquals(positions.get(expectedPositionIndex), event.getPosition());
        }
    }
}
