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
package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.traccar.web.shared.model.AccessDeniedException;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.User;

import java.util.List;
import java.util.Map;

@RemoteServiceRelativePath("groupService")
public interface GroupService extends RemoteService {
    Map<Group, List<Group>> getGroups();
    Group addGroup(Group parent, Group group);
    void updateGroups(Map<Group, List<Group>> groups) throws AccessDeniedException;
    void removeGroups(List<Group> groups) throws AccessDeniedException;
    Map<User, Boolean> getGroupShare(Group group);
    void saveGroupShare(Group group, Map<User, Boolean> share);

}
