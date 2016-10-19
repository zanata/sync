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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.tools.ant.taskdefs.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ProcessUtils {
    private static final Logger log =
            LoggerFactory.getLogger(ProcessUtils.class);
    private static final ShutdownHookProcessDestroyer PROCESS_DESTROYER =
            new ShutdownHookProcessDestroyer();

    public static List<String> runNativeCommand(Path workingDir,
            String... commands) {
        return runNativeCommand(workingDir, ExecuteWatchdog.INFINITE_TIMEOUT,
                commands);
    }

    public static List<String> runNativeCommand(Path workingDir,
            long timeoutInMilli, String... commands) {
        Preconditions.checkArgument(commands != null && commands.length > 0,
                "You must provide commands to run");

        CommandLine commandLine = CommandLine.parse(commands[0]);
        ImmutableList<String> args =
                ImmutableList.copyOf(commands).subList(1, commands.length);
        for (String arg : args) {
            commandLine.addArgument(arg);
        }

        Executor executor = new DefaultExecutor();

        ImmutableList.Builder<String> output = ImmutableList.builder();
        executor.setStreamHandler(new PumpStreamHandler(new LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                log.info(line);
                output.add(line);
            }
        }));
        ExecuteWatchdog watchDog =
                new ExecuteWatchdog(timeoutInMilli);
        executor.setWatchdog(watchDog);
        executor.setWorkingDirectory(workingDir.toFile());
        executor.setProcessDestroyer(PROCESS_DESTROYER);

        try {
            int exitCode = executor.execute(commandLine);
            if (Execute.isFailure(exitCode) && watchDog.killedProcess()) {
                // it was killed on purpose by the watchdog
                log.error(
                        "process {} taking too long to run and killed by watchdog",
                        commandLine);
            }
        } catch (IOException e) {
            log.error("error running:{}", commandLine);
            throw Throwables.propagate(e);
        }

        return output.build();
    }

    public static boolean isExecutableOnPath(String execName) {
        return Stream.of(System.getenv("PATH").split(Pattern.quote(
                File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve(execName)));
    }
}
