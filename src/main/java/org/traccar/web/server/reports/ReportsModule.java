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
package org.traccar.web.server.reports;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.traccar.web.shared.model.ReportType;

public class ReportsModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder.newMapBinder(binder(), ReportType.class, ReportGenerator.class)
                .addBinding(ReportType.GENERAL_INFORMATION).to(ReportGI.class);
        MapBinder.newMapBinder(binder(), ReportType.class, ReportGenerator.class)
                .addBinding(ReportType.DRIVES_AND_STOPS).to(ReportDS.class);
        MapBinder.newMapBinder(binder(), ReportType.class, ReportGenerator.class)
                .addBinding(ReportType.OVERSPEEDS).to(ReportOS.class);
        MapBinder.newMapBinder(binder(), ReportType.class, ReportGenerator.class)
                .addBinding(ReportType.GEO_FENCE_IN_OUT).to(ReportGFIO.class);
        MapBinder.newMapBinder(binder(), ReportType.class, ReportGenerator.class)
                .addBinding(ReportType.EVENTS).to(ReportEV.class);
        MapBinder.newMapBinder(binder(), ReportType.class, ReportGenerator.class)
                .addBinding(ReportType.MILEAGE_DETAIL).to(ReportMD.class);
        MapBinder.newMapBinder(binder(), ReportType.class, ReportGenerator.class)
            .addBinding(ReportType.GRAPH).to(ReportGraph.class);
    }
}
