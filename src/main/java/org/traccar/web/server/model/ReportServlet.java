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

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.traccar.web.server.reports.ReportGenerator;
import org.traccar.web.server.reports.ReportGeneratorFactory;
import org.traccar.web.shared.model.Report;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ReportServlet extends HttpServlet {
    @Inject
    protected Logger logger;

    @Inject
    private Provider<ReportGeneratorFactory> generators;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = GsonUtils.create();
        Report report;
        Reader reader = null;
        try {
            reader = new InputStreamReader(req.getInputStream());
            report = gson.fromJson(reader, Report.class);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Unable to read report generation request", ex);
            IOUtils.closeQuietly(reader);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ReportGenerator generator = generators.get().getGenerator(report);
        if (generator == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        generator.generate(report);

        resp.sendError(HttpServletResponse.SC_OK);
    }
}
