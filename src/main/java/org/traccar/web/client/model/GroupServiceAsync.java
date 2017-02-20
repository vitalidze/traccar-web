package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.User;

import java.util.List;
import java.util.Map;

public interface GroupServiceAsync {
    void getGroups(AsyncCallback<Map<Group, Group>> async);

    void addGroup(Group parent, Group group, AsyncCallback<Group> async);

    void updateGroups(Map<Group, List<Group>> groups, AsyncCallback<Void> async);

    void removeGroups(List<Group> groups, AsyncCallback<Void> async);

    void getGroupShare(Group group, AsyncCallback<Map<User, Boolean>> async);

    void saveGroupShare(Group group, Map<User, Boolean> share, AsyncCallback<Void> async);
}
