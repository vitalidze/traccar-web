package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

public interface EventServiceAsync {
    void applicationSettingsChanged(AsyncCallback<Void> async);

    void devicesChanged(AsyncCallback<Void> async);
}
