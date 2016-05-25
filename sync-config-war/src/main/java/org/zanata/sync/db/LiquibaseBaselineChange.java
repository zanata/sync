package org.zanata.sync.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Joiner;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LiquibaseBaselineChange implements CustomTaskChange {
    private static final Logger log =
            LoggerFactory.getLogger(LiquibaseBaselineChange.class);
    private String baselineSql;

    @Override
    public String getConfirmationMessage() {
        return "H2 baseline executed";
    }

    @Override
    public void setUp() throws SetupException {
        URL resource = Thread.currentThread().getContextClassLoader()
                .getResource("db/baseline.sql");
        if (resource != null) {
            try (InputStream inputStream = resource.openStream()) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(inputStream));
                List<String> lines =
                        reader.lines().collect(Collectors.toList());
                baselineSql = Joiner.on("\n").join(lines);
            } catch (IOException e) {
                throw com.google.common.base.Throwables.propagate(e);
            }
        }
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            final JdbcConnection conn = (JdbcConnection) database.getConnection();
            log.info("about to execute baselineSql: {}", baselineSql);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(baselineSql);
            }
        } catch (DatabaseException | SQLException e) {
            throw new CustomChangeException(e);
        }
        // connection is not closed in a finally clause since doing so causes liquibase to throw exceptions.
    }
}
