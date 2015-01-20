package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.shared.model.NotificationSettings;

public interface NotificationServiceAsync {
    void checkSettings(NotificationSettings settings, AsyncCallback<Void> async);

    void getSettings(AsyncCallback<NotificationSettings> async);

    void saveSettings(NotificationSettings settings, AsyncCallback<Void> async);
}
