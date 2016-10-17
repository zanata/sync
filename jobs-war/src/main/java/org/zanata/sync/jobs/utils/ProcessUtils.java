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
package org.zanata.sync.jobs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ProcessUtils {
    private static final Logger log =
            LoggerFactory.getLogger(ProcessUtils.class);

    public static List<String> runNativeCommand(Path workingDir, String... commands) {
        ProcessBuilder processBuilder =
                new ProcessBuilder(commands)
                        .directory(workingDir.toFile())
                        .redirectErrorStream(true);
        ImmutableList.Builder<String> resultBuilder = ImmutableList.builder();
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(),
                            Charsets.UTF_8))) {
                String line = reader.readLine();
                while (line != null) {
                    resultBuilder.add(line);
                    line = reader.readLine();
                }
                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    throw new RepoSyncException("exit code is " + exitValue);
                }
            } catch (IOException e) {
                log.error("error running native git", e);
                throw new RepoSyncException(e);
            } catch (InterruptedException e) {
                log.error("interrupted while waiting for the exit code");
                throw new RepoSyncException(e);
            } finally {
                process.destroyForcibly();
            }
        } catch (Exception e) {
            log.error("error running native command", e);
            throw new RepoSyncException(e);
        }
        ImmutableList<String> output = resultBuilder.build();
        log.debug("{} output: \n{}", commands[0],
                Joiner.on("\t" + System.lineSeparator()).join(output));
        return output;
    }

    public static List<String> runNativeCommand(Path workingDir, long timeout,
            TimeUnit timeUnit,
            String... commands) {
        ProcessBuilder processBuilder =
                new ProcessBuilder(commands)
                        .directory(workingDir.toFile())
                        .redirectErrorStream(true);
        ImmutableList.Builder<String> resultBuilder = ImmutableList.builder();
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(),
                            Charsets.UTF_8))) {
                String line = reader.readLine();
                while (line != null) {
                    resultBuilder.add(line);
                    line = reader.readLine();
                }
                boolean exited = process.waitFor(timeout, timeUnit);
                if (exited) {
                    int exitValue = process.exitValue();
                    if (exitValue != 0) {
                        throw new RepoSyncException("exit code is " + exitValue);
                    }
                } else {
                    log.error("timeout checking process exit code");
                    throw new RepoSyncException("timeout checking process exit code");
                }
            } catch (IOException e) {
                log.error("error running native git", e);
                throw new RepoSyncException(e);
            } catch (InterruptedException e) {
                log.error("interrupted while waiting for the exit code");
                throw new RepoSyncException(e);
            } finally {
                process.destroyForcibly();
            }
        } catch (Exception e) {
            log.error("error running native command", e);
            throw new RepoSyncException(e);
        }
        ImmutableList<String> output = resultBuilder.build();
        log.debug("{} output: \n{}", commands[0],
                Joiner.on("\t" + System.lineSeparator()).join(output));
        return output;
    }
}
