package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.shared.model.EventRule;
import org.traccar.web.shared.model.User;

import java.util.List;

public interface EventRuleServiceAsync {
    void getEventRules(User user, AsyncCallback<List<EventRule>> async);

    void addEventRule(User user, EventRule eventRule, AsyncCallback<EventRule> async);

    void updateEventRule(User user, EventRule eventRule, AsyncCallback<EventRule> async);

    void removeEventRule(EventRule eventRule, AsyncCallback<Void> async);
}
