/*
 * Copyright 2018 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.model;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.jpa.AvailableSettings;
import org.junit.Assume;
import org.junit.Test;

import javax.persistence.Persistence;
import java.util.Properties;

public class SchemaGeneratorTest {
    private void generateSchema(Class<? extends Dialect> dialect, String suffix) {
        String persistenceUnitName = "test";
        String destination = "hbm2schema-" + suffix + ".sql";
        System.out.println("Generating DDL create script to : " + destination);

        final Properties persistenceProperties = new Properties();

        // XXX force persistence properties : remove database target
        persistenceProperties.setProperty(org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO, "");
        persistenceProperties.setProperty(AvailableSettings.SCHEMA_GEN_DATABASE_ACTION, "none");

        // XXX force persistence properties : define create script target from metadata to destination
        // persistenceProperties.setProperty(AvailableSettings.SCHEMA_GEN_CREATE_SCHEMAS, "true");
        persistenceProperties.setProperty(AvailableSettings.SCHEMA_GEN_SCRIPTS_ACTION, "create");
        persistenceProperties.setProperty(AvailableSettings.SCHEMA_GEN_CREATE_SOURCE, "metadata");
        persistenceProperties.setProperty(AvailableSettings.SCHEMA_GEN_SCRIPTS_CREATE_TARGET, destination);

        persistenceProperties.setProperty(org.hibernate.cfg.AvailableSettings.DIALECT, dialect.getName());

        Persistence.generateSchema(persistenceUnitName, persistenceProperties);
    }

    @Test
    public void generate() {
        // Disable schema generator in ordinary test run
        Assume.assumeTrue(false);
        generateSchema(MySQL5InnoDBDialect.class, "mysql");
        generateSchema(H2Dialect.class, "h2");
    }
}
