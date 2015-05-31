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

import com.google.gwt.user.client.rpc.IsSerializable;

public enum SpeedUnit implements IsSerializable {
    knots("kn", 1d, DistanceUnit.km),
    kilometersPerHour("km/h", 1.852, DistanceUnit.km),
    milesPerHour("mph", 1.150779, DistanceUnit.mile);

    final String unit;
    final double factor;
    final DistanceUnit distanceUnit;

    SpeedUnit(String unit, double factor, DistanceUnit distanceUnit) {
        this.unit = unit;
        this.factor = factor;
        this.distanceUnit = distanceUnit;
    }

    public double getFactor() {
        return factor;
    }

    public String getUnit() {
        return unit;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public double toKnots(double speed) {
        return speed / factor;
    }
}
