package org.zanata.sync.dao;

import java.sql.Connection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.model.SystemSettings;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;


/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class SystemSettingsDAO {
    private static final Logger log =
            LoggerFactory.getLogger(SystemSettingsDAO.class);

    public static final String ENCRYPTION_FIELDS = "ENCRYPTION_FIELDS";

    @Inject
    private Connection connection;

    public SystemSettings getSystemSettings() {
        /*DSLContext dslContext = DSL.using(connection, SQLDialect.H2);

        Result<Record> settingsRecords = dslContext.select()
        .from(SYSTEM_SETTINGS_TABLE)
        .fetch();*/

        SystemSettings systemSettings = new SystemSettings();

        /*for(Record settingsRecord: settingsRecords) {
            String key = settingsRecord.getValue(SYSTEM_SETTINGS_TABLE.KEY);
            String value = settingsRecord.getValue(SYSTEM_SETTINGS_TABLE.VALUE);
            populateSystemSettings(key, value, systemSettings);
        }*/
        return systemSettings;
    }

    private void populateSystemSettings(String key, String value,
            SystemSettings systemSettings) {
        if(key.equals(ENCRYPTION_FIELDS)) {
            systemSettings.setFieldsNeedEncryption(ImmutableList.copyOf(
                Splitter.on(",").omitEmptyStrings().trimResults().split(value)));
        }
    }


    public void persist(SystemSettings systemSettings) {
        /*DSLContext dslContext = DSL.using(connection, SQLDialect.H2);

        dslContext.insertInto(SYSTEM_SETTINGS_TABLE, SYSTEM_SETTINGS_TABLE.KEY,
                SYSTEM_SETTINGS_TABLE.VALUE)
                .values(ENCRYPTION_FIELDS, Joiner.on(",")
                        .join(systemSettings.getFieldsNeedEncryption()))
                .execute();*/

        log.info("System settings saved." + systemSettings.toString());
    }
}
