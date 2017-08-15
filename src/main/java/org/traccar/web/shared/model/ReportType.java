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

public enum ReportType implements IsSerializable {
    GENERAL_INFORMATION {
        @Override
        public boolean supportsMapDisplay() {
            return true;
        }
    },
    DRIVES_AND_STOPS,
    MILEAGE_DETAIL,
    OVERSPEEDS,
    GEO_FENCE_IN_OUT {
        @Override
        public boolean supportsGeoFences() {
            return true;
        }
    },
    EVENTS {
        @Override
        public boolean supportsGeoFences() {
            return true;
        }

        @Override
        public boolean supportsFiltering() {
            return false;
        }
    },
    GRAPH {
        @Override
        public boolean supportsGeoFences() {
            return false;
        }

        @Override
        public boolean supportsFiltering() {
            return false;
        }
        
        @Override
        public boolean suportsGraph() {
            return true;
        }
    };;
    

    public boolean supportsGeoFences() {
        return false;
    }

    public boolean supportsMapDisplay() {
        return false;
    }

    public boolean supportsFiltering() {
        return true;
    }
    
    public boolean suportsGraph(){
        return false;
    }
}
