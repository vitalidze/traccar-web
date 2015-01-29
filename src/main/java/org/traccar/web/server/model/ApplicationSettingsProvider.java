package org.traccar.web.server.model;

import org.traccar.web.shared.model.ApplicationSettings;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class ApplicationSettingsProvider implements Provider<ApplicationSettings> {
    @Inject
    private Provider<EntityManager> entityManager;

    @Override
    public ApplicationSettings get() {
        TypedQuery<ApplicationSettings> query = entityManager.get().createQuery("SELECT x FROM ApplicationSettings x", ApplicationSettings.class);
        List<ApplicationSettings> resultList = query.getResultList();
        return resultList.isEmpty() ? new ApplicationSettings() : resultList.get(0);
    }
}
