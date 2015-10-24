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
import org.traccar.web.shared.model.Report;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class ReportServiceImpl extends RemoteServiceServlet implements ReportService {
    @Transactional
    @RequireUser
    @Override
    public List<Report> getReports() {
        return null;
    }

    @Transactional
    @RequireUser
    @Override
    public Report addReport(Report report) {
        return null;
    }

    @Transactional
    @RequireUser
    @Override
    public Report updateReport(Report report) {
        return null;
    }

    @Transactional
    @RequireUser
    @Override
    public void removeReport(Report report) {

    }
}
