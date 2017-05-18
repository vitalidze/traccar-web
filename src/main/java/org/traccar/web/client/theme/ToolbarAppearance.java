/*
 * Copyright 2017 Godwin peter .O (godwin@peter.com.ng)
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
package org.traccar.web.client.theme;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.sencha.gxt.theme.neptune.client.base.toolbar.Css3ToolBarAppearance;

public class ToolbarAppearance extends Css3ToolBarAppearance {
    public ToolbarAppearance() {
        this((ToolBarResources) GWT.create(ToolBarResources.class));
    }

    public ToolbarAppearance(ToolBarResources var1) {
        super(var1);
    }

    public interface ToolBarResources extends Css3ToolBarResources {
        @ClientBundle.Source({"com/sencha/gxt/theme/base/client/container/BoxLayout.css",
                "com/sencha/gxt/theme/neptune/client/base/container/Css3HBoxLayoutContainer.css",
                "com/sencha/gxt/theme/neptune/client/base/toolbar/Css3ToolBar.css",
                "ToolBar.css"})
        @Override
        Css3ToolBarStyle style();
    }
}
