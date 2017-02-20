package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

public interface LogServiceAsync {
    void getTrackerServerLog(short sizeKb, AsyncCallback<String> async);

    void getWrapperLog(short sizeKb, AsyncCallback<String> async);
}
