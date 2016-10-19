package org.zanata.sync.jobs.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.zanata.client.commands.BasicOptions;
import org.zanata.client.commands.BasicOptionsImpl;
import org.zanata.client.commands.ConfigurableOptionsImpl;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.PutProjectCommand;
import org.zanata.client.commands.PutProjectOptionsImpl;
import org.zanata.client.commands.PutVersionCommand;
import org.zanata.client.commands.PutVersionOptionsImpl;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.ZanataConfig;
import org.zanata.sync.jobs.plugin.zanata.util.PushPullOptionsUtil;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Ignore("I run tests here to experiment stuff or do some ad hoc work")
public class ExperimentTest {

    @Test
    public void createProjectVersions() throws Exception {
        // find . -type f -name "zanata.xml" | xargs sed -i 's#http://localhost:8080/zanata/#http://zanata:8080/#g'

        Set<File> projectConfigs = PushPullOptionsUtil.findProjectConfigs(
                Paths.get(
                        "/home/pahuang/work/test/docs-Red_Hat_Enterprise_Linux_OpenStack_Platform")
                        .toFile());

        // create all the projects and versions under this repo
        for (File projectConfig : projectConfigs) {
            // apply the file
            ZanataConfig zanataConfig = getZanataConfig(projectConfig);

            PutProjectOptionsImpl projectOpts = new PutProjectOptionsImpl();
            setCommonField(projectOpts);
            projectOpts.setProjectSlug(zanataConfig.getProject());
            projectOpts.setProjectName(zanataConfig.getProject());
            projectOpts.setDefaultProjectType(zanataConfig.getProjectType());

            new PutProjectCommand(projectOpts).run();

            PutVersionOptionsImpl versionOpts =
                    new PutVersionOptionsImpl();
            setCommonField(versionOpts);
            versionOpts.setVersionProject(zanataConfig.getProject());
            versionOpts.setProjectType(zanataConfig.getProjectType());
            versionOpts.setVersionSlug(zanataConfig.getProjectVersion());

            new PutVersionCommand(versionOpts).run();
        }
    }

    private static ZanataConfig getZanataConfig(File projectConfig)
            throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(ZanataConfig.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (ZanataConfig) unmarshaller
                .unmarshal(projectConfig);
    }

    private static <O extends ConfigurableOptionsImpl> O setCommonField(O opts)
            throws MalformedURLException {
        opts.setBatchMode(true);
        opts.setUrl(URI.create("http://localhost:8180").toURL());
        opts.setUsername("admin");
        opts.setKey("b6d7044e9ee3b2447c28fb7c50d86d98");
        return opts;
    }
}
