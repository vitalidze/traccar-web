/*
 * Copyright 2017 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client.renderer;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.shared.model.Group;

public class GroupSafeHtmlRenderer extends AbstractSafeHtmlRenderer<Group> {
    private final GroupStore groupStore;

    public GroupSafeHtmlRenderer(GroupStore groupStore) {
        this.groupStore = groupStore;
    }

    @Override
    public SafeHtml render(Group group) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        for (int i = 0; i < groupStore.getDepth(group); i++) {
            builder.appendHtmlConstant("&nbsp;&nbsp;&nbsp;");
        }
        return builder.appendEscaped(group.getName() == null ? "" : group.getName()).toSafeHtml();
    }
}
