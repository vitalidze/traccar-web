package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import org.traccar.web.shared.model.NotificationSettings;

public interface NotificationServiceAsync {
    void checkEmailSettings(NotificationSettings settings, AsyncCallback<Void> async);

    void getSettings(AsyncCallback<NotificationSettings> async);

    void saveSettings(NotificationSettings settings, AsyncCallback<Void> async);

    void checkPushbulletSettings(NotificationSettings settings, AsyncCallback<Void> async);

    void checkTemplate(String subject, String body, AsyncCallback<String> async);
}
