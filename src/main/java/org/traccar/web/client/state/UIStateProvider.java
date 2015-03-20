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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.state.client.AbstractRpcProvider;
import org.traccar.web.client.model.UIStateService;
import org.traccar.web.client.model.UIStateServiceAsync;

public class UIStateProvider extends AbstractRpcProvider {
    private static final UIStateServiceAsync uiStateService = GWT.create(UIStateService.class);

    @Override
    public void getValue(String name, AsyncCallback<String> callback) {
        uiStateService.getValue(name, callback);
    }

    @Override
    public void setValue(String name, String value) {
        uiStateService.setValue(name, value, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void aVoid) {
            }
        });
    }
}
