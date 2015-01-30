package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EventServiceAsync {
    void applicationSettingsChanged(AsyncCallback<Void> async);
}
