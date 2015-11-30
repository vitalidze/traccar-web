package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.Report;
import org.traccar.web.shared.model.User;

import java.util.List;
import java.util.Map;

public interface GroupServiceAsync {
    void getGroups(AsyncCallback<List<Group>> async);

    void addGroup(Group group, AsyncCallback<Group> async);

    void updateGroup(Group group, AsyncCallback<Group> async);

    void removeGroup(Group group, AsyncCallback<Void> async);

    void getGroupShare(Group group, AsyncCallback<Map<User, Boolean>> async);

    void saveGroupShare(Group group, Map<User, Boolean> share, AsyncCallback<Void> async);
}
