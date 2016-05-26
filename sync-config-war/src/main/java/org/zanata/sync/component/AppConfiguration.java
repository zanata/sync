package org.zanata.sync.component;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dao.SystemSettingsDAO;
import org.zanata.sync.model.SystemSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
@NoArgsConstructor
public class AppConfiguration implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(AppConfiguration.class);

    private static final String REPO_DIR = "repository";

    @Inject
    private SystemSettingsDAO systemSettingsDAO;

    @Getter
    private String buildVersion;

    @Getter
    private String buildInfo;

    @Getter
    private File repoDir;

    @PostConstruct
    public void init() {
        Properties infoProps = loadBuildInProps("info.properties");
        buildInfo = infoProps.getProperty("build.info");
        buildVersion = infoProps.getProperty("build.version");


//        checkDirectory(REPO_DIR, repoDir);
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


    private static String removeTrailingSlash(String string) {
        return CharMatcher.is(File.separatorChar).trimTrailingFrom(string);
    }

    public List<String> getFieldsNeedEncryption() {
        Optional<SystemSettings> encryptFieldsOpt = systemSettingsDAO
                .getSystemSettings(SystemSettings.FIELDS_NEED_ENCRYPTION);
        if (encryptFieldsOpt.isPresent()) {
            return Splitter.on(",").omitEmptyStrings().trimResults()
                    .splitToList(encryptFieldsOpt.get().getValue());
        }
        return ImmutableList.of();
    }

}
