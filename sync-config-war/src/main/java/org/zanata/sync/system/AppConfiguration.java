package org.zanata.sync.system;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.dao.SystemSettingsDAO;
import com.google.common.base.Throwables;
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

    @Inject
    private SystemSettingsDAO systemSettingsDAO;

    @Getter
    private String buildVersion;

    @Getter
    private String buildInfo;

    @PostConstruct
    public void init() {
        Properties infoProps = loadBuildInProps("info.properties");
        buildInfo = infoProps.getProperty("build.info");
        buildVersion = infoProps.getProperty("build.version");
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


}
