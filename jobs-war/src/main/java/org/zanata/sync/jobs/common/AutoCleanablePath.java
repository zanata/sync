/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.sync.jobs.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class AutoCleanablePath implements AutoCloseable {
    private static final Logger log =
            LoggerFactory.getLogger(AutoCleanablePath.class);
    private final Path workingDir;

    public AutoCleanablePath(Path workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public void close() throws Exception {
        if (log.isDebugEnabled()) {
            try {
                Path tempDir =
                        Files.createTempDirectory(LocalDate.now().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd_")));
                log.debug("======= move job working directory to {}", tempDir);
                FileUtils.copyDirectory(workingDir.toFile(), tempDir.toFile());
            } catch (IOException e) {
                log.debug(
                        "error copying working dir to temp folder: {}",
                        workingDir);
            }
        }
        if (workingDir != null) {
            try {
                FileUtils.cleanDirectory(workingDir.toFile());
            } catch (IOException e) {
                log.warn("failed to clean up working dir: {}", workingDir);
            }
        }
    }

    public Path toPath() {
        return workingDir;
    }

    public File toFile() {
        return workingDir.toFile();
    }
}
