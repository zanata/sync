package org.zanata.sync.component;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dao.SystemSettingsDAO;
import org.zanata.sync.model.SystemSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
@NoArgsConstructor
public class AppConfiguration implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(AppConfiguration.class);
    private static final String DB_DIR = "db";
    private static final String DB_FILE_NAME = "zanata-sync-db";

    private static final String SYS_PROP_DATA_PATH = "data.path";
    private static final String REPO_DIR = "repository";

    private static final String DEFAULT_PATH = System.getProperty("java.io.tmpdir");

    @Inject
    private SystemSettingsDAO systemSettingsDAO;

    @Getter
    private SystemSettings systemSettings;

    @Getter
    private String buildVersion;

    @Getter
    private String buildInfo;

    @Getter
    private File repoDir;

    private File settingsFile;

    @PostConstruct
    public void init() {
        Properties infoProps = loadBuildInProps("info.properties");
        buildInfo = infoProps.getProperty("build.info");
        buildVersion = infoProps.getProperty("build.version");

        log.info("load settings...");
        this.systemSettings = systemSettingsDAO.getSystemSettings();

        String dataPath = getDataPathSystemProp();
        this.systemSettings.setDataPath(dataPath);
        checkDirectory(dataPath, Paths.get(dataPath).toFile());

        this.repoDir = Paths.get(buildRepoDirectory()).toFile();
        checkDirectory(REPO_DIR, repoDir);
    }

    private static Properties loadBuildInProps(String propFileName) {
        ClassLoader contextClassLoader =
                Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = contextClassLoader
                        .getResourceAsStream(propFileName)) {
            Properties buildInProperties = new Properties();
            buildInProperties.load(inputStream);
            return buildInProperties;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @VisibleForTesting
    public AppConfiguration(File repoDir) {
        this.repoDir = repoDir;
    }

    public void updateSettingsAndSave(boolean deleteJobDir,
            List<String> fieldsNeedEncryption) {
        systemSettings.updateSettings(fieldsNeedEncryption);
        systemSettingsDAO.persist(systemSettings);
    }

    private static void checkDirectory(String nameOfDirectory, File directory) {
        if (!directory.exists()) {
            directory.mkdir();
        }
        Preconditions.checkState(directory.isDirectory(),
                "%s directory %s must be a directory",
                nameOfDirectory, directory);
        Preconditions.checkState(directory.canRead(),
            "%s directory %s must be readable", nameOfDirectory,
            directory);
        Preconditions.checkState(directory.canWrite(),
            "%s directory %s must be writable", nameOfDirectory,
            directory);
    }

    private String buildRepoDirectory() {
        return removeTrailingSlash(systemSettings.getDataPath())
                + File.separatorChar + REPO_DIR;
    }

    private static String removeTrailingSlash(String string) {
        return CharMatcher.is(File.separatorChar).trimTrailingFrom(string);
    }

    public List<String> getFieldsNeedEncryption() {
        return systemSettings.getFieldsNeedEncryption();
    }

    public String getDataPath() {
        return systemSettings.getDataPath();
    }

    public String getDBFilePath() {
        String dataDir = removeTrailingSlash(systemSettings.getDataPath());
        String dbDir = FilenameUtils.concat(dataDir, DB_DIR);
        return FilenameUtils.concat(dbDir, DB_FILE_NAME);
    }

    public static String getDBFilePathFromSystemProp() {
        String dataDir = getDataPathSystemProp();
        String dbDir = FilenameUtils.concat(dataDir, DB_DIR);
        return FilenameUtils.concat(dbDir, DB_FILE_NAME);
    }

    private static String getDataPathSystemProp() {
        String dataPath = System.getProperty(SYS_PROP_DATA_PATH, DEFAULT_PATH);
        return removeTrailingSlash(dataPath);
    }
}
