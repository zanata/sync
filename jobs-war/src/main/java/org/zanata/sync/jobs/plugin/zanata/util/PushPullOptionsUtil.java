/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.jobs.plugin.zanata.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBException;

import org.apache.commons.configuration.ConfigurationException;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.PushPullOptions;
import org.zanata.sync.jobs.common.exception.ZanataSyncException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public final class PushPullOptionsUtil {
    // TODO make this configurable?
    public static final int MAX_DEPTH = 10;

    /**
     * You typically call this after clone the source repo and before doing a
     * push to or pull from Zanata
     *
     * @param options
     *         push or pull options
     * @param <O>
     *         PushOptions or PullOptions
     * @return the options after applying project config (zanata.xml) and modify
     * the source and trans dir (because current working directory is not the
     * same as what push and pull commands will be executed within
     */
    public static <O extends PushPullOptions> O applyProjectConfig(O options,
            File projectConfig) {
        try {
            options.setProjectConfig(projectConfig);
            // unset previous values so that we can reload them from project config
            options.setSrcDir(null);
            options.setTransDir(null);
            options.setProj(null);
            options.setProjectVersion(null);
            options.setProjectType(null);

            OptionsUtil.applyConfigFiles(options);

            File baseDir = projectConfig.getParentFile();
            // we need to adjust src-dir and trans-dir to be relative to zanata base dir
            options.setSrcDir(
                    new File(baseDir, options.getSrcDir() != null ?
                            options.getSrcDir().getPath() : "."));
            options.setTransDir(
                    new File(baseDir, options.getTransDir() != null ?
                            options.getTransDir().getPath() : "."));
            //disable commandhook
            if (!options.getCommandHooks().isEmpty()) {
                throw new ZanataSyncException(
                        "Commandhook in zanata.xml is not supported", null);
            }
        } catch (ConfigurationException | JAXBException e) {
            throw new ZanataSyncException("Failed applying project config", e);
        }
        return options;
    }

    /**
     * @param repoBase
     *         base path of a source repo.
     * @return absolute paths for all the project configs found under repoBase
     */
    public static Set<File> findProjectConfigs(File repoBase) {
        try {
            Stream<Path> pathStream = Files.find(repoBase.toPath(), MAX_DEPTH,
                    (path, basicFileAttributes) ->
                            basicFileAttributes.isRegularFile() &&
                                    path.toFile().getName()
                                            .equals("zanata.xml"));
            return pathStream.map(Path::toFile).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new ZanataSyncException("Failed finding project config", e);
        }
    }
}
