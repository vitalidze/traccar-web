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
package org.traccar.web.client.state;

import com.sencha.gxt.widget.core.client.grid.Grid;

public class GridStateHandler<M> extends com.sencha.gxt.state.client.GridStateHandler<M> {
    public GridStateHandler(Class<GridState> stateType, Grid<M> component, String key) {
        super(stateType, component, key);
    }

    public GridStateHandler(Grid<M> component) {
        super(component);
    }

    public GridStateHandler(Grid<M> component, String key) {
        super(component, key);
    }

    @Override
    public void applyState() {
        super.applyState();
        getObject().getView().getHeader().refresh();
    }
}
