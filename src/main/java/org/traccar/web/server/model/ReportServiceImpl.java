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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.traccar.web.client.model.ReportService;
import org.traccar.web.shared.model.Device;
import org.traccar.web.shared.model.GeoFence;
import org.traccar.web.shared.model.Report;
import org.traccar.web.shared.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Singleton
public class ReportServiceImpl extends RemoteServiceServlet implements ReportService {
    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    private Provider<User> sessionUser;

    @Transactional
    @RequireUser
    @Override
    public List<Report> getReports() {
        List<Report> reports;

        if (sessionUser.get().getAdmin()) {
            reports = entityManager.get().createQuery("SELECT x FROM Report x", Report.class).getResultList();
        } else {
            reports = new ArrayList<>(sessionUser.get().getAllAvailableReports());
        }

        List<Report> result = new ArrayList<>(reports.size());
        for (Report report : reports) {
            result.add(unproxy(report));
        }
        return result;
    }

    private Report unproxy(Report report) {
        Report result = new Report().copyFrom(report);
        result.setDevices(new HashSet<Device>(report.getDevices().size()));
        for (Device device : report.getDevices()) {
            result.getDevices().add(new Device(device));
        }
        result.setGeoFences(new HashSet<GeoFence>(report.getGeoFences().size()));
        for (GeoFence geoFence : report.getGeoFences()) {
            result.getGeoFences().add(new GeoFence().copyFrom(geoFence));
        }
        return result;
    }

    @Transactional
    @RequireUser
    @Override
    public Report addReport(Report report) {
        Report toSave = new Report().copyFrom(report);

        toSave.setDevices(new HashSet<Device>(report.getDevices().size()));
        toSave.setGeoFences(new HashSet<GeoFence>(report.getGeoFences().size()));
        toSave.setUsers(new HashSet<User>(1));
        toSave.getUsers().add(sessionUser.get());
        entityManager.get().persist(toSave);

        return unproxy(toSave);
    }

    @Transactional
    @RequireUser
    @Override
    public Report updateReport(Report report) {
        Report toSave = entityManager.get().find(Report.class, report.getId());

        toSave.copyFrom(report);
        processDevicesAndGeoFences(report, toSave);

        return unproxy(toSave);
    }

    @Transactional
    @RequireUser
    @Override
    public void removeReport(Report report) {
        Report toRemove = entityManager.get().find(Report.class, report.getId());
        entityManager.get().remove(toRemove);
    }

    private void processDevicesAndGeoFences(Report report, Report toSave) {
        for (Device device : report.getDevices()) {
            if (!toSave.getDevices().contains(device)) {
                toSave.getDevices().add(entityManager.get().find(Device.class, device.getId()));
            }
        }
        for (Iterator<Device> it = toSave.getDevices().iterator(); it.hasNext(); ) {
            if (!report.getDevices().contains(it.next())) {
                it.remove();
            }
        }

        for (GeoFence geoFence : report.getGeoFences()) {
            if (!toSave.getGeoFences().contains(geoFence)) {
                toSave.getGeoFences().add(entityManager.get().find(GeoFence.class, geoFence.getId()));
            }
        }
        for (Iterator<GeoFence> it = toSave.getGeoFences().iterator(); it.hasNext(); ) {
            if (!report.getGeoFences().contains(it.next())) {
                it.remove();
            }
        }
    }
}
