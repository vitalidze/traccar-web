package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.traccar.web.shared.model.Group;
import org.traccar.web.shared.model.Report;

import java.util.List;

public interface GroupServiceAsync {
    void getGroups(AsyncCallback<List<Group>> async);

    void addGroup(Group group, AsyncCallback<Group> async);

    void updateGroup(Group group, AsyncCallback<Group> async);

    void removeGroup(Group group, AsyncCallback<Void> async);
}
