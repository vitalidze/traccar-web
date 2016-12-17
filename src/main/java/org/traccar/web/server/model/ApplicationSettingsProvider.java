package org.traccar.web.server.model;

import org.traccar.web.shared.model.ApplicationSettings;
import org.traccar.web.shared.model.PasswordHashMethod;

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
        if (resultList.isEmpty()) {
            ApplicationSettings appSettings = new ApplicationSettings();
            appSettings.setSalt(PasswordUtils.generateRandomString());
            appSettings.setDefaultHashImplementation(PasswordHashMethod.PBKDF2WithHmacSha1);
            entityManager.get().persist(appSettings);
            return appSettings;
        }
        return resultList.get(0);
    }
}
