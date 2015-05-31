package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.shared.model.NotificationSettingsDTO;
import org.traccar.web.shared.model.NotificationTemplateDTO;

public interface NotificationServiceAsync {
    void checkEmailSettings(NotificationSettingsDTO settings, AsyncCallback<Void> async);

    void getSettings(AsyncCallback<NotificationSettingsDTO> async);

    void saveSettings(NotificationSettingsDTO settings, AsyncCallback<Void> async);

    void checkPushbulletSettings(NotificationSettingsDTO settings, AsyncCallback<Void> async);

    void checkTemplate(NotificationTemplateDTO template, AsyncCallback<String> async);
}
