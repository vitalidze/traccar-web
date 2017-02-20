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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sencha.gxt.state.client.ComponentStateHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;

public class CheckBoxStateHandler extends ComponentStateHandler<CheckBoxStateHandler.CheckBoxState, CheckBox> {
    public interface CheckBoxState {
        boolean getChecked();
        void setChecked(boolean checked);
    }

    public CheckBoxStateHandler(CheckBox component) {
        super(CheckBoxState.class, component);
        component.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (getObject().isStateful()) {
                    CheckBoxState state = getState();
                    if (state.getChecked() != event.getValue()) {
                        state.setChecked(event.getValue());
                        saveState();
                    }
                }
            }
        });
    }

    @Override
    public void applyState() {
        if (getObject().isStateful()) {
            getObject().setValue(getState().getChecked(), true);
        }
    }
}
