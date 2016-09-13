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
package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.common.model.Credentials;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * This will try to use the native git executable on PATH.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class NativeGitSyncService implements RepoSyncService {
    private static final Logger log =
            LoggerFactory.getLogger(NativeGitSyncService.class);

    private String branch;
    private String url;
    private File workingDir;
    private Credentials credentials;
    private String commitMessage;

    @Override
    public void cloneRepo() throws RepoSyncException {
        String[] protocolAndRest = protocolAndRest(url);
        String urlWithAuth =
                String.format("%s://%s:%s@%s", protocolAndRest[0],
                        urlEncode(credentials.getUsername()),
                        urlEncode(credentials.getSecret()), protocolAndRest[1]);

        // git clone into current directory
        log.info("start git clone using native git");
        // --no-single-branch to fetch the tip of each remote branch
        runNativeCommand(workingDir, "git", "clone", "--depth", "1",
                "--no-single-branch", urlWithAuth, ".");
        // check if the repo already has the requested branch
        String targetBranch = getBranch();
        log.info("check branch existence: {}", targetBranch);
        List<String> output =
                runNativeCommand(workingDir, "git", "branch", "--no-color",
                        "--list", "--all", String.format("**/%s", targetBranch));
        if (output.isEmpty()) {
            // remote repo doesn't contain this branch. We need to create the branch locally
            log.info("create and check out local branch: {}", targetBranch);
            runNativeCommand(workingDir, "git", "checkout", "-b", targetBranch);
        } else {
            log.info("check out branch: {}", targetBranch);
            runNativeCommand(workingDir, "git", "checkout", targetBranch);
        }
    }

    private static List<String> runNativeCommand(File workingDir, String... commands) {
        ProcessBuilder processBuilder =
                new ProcessBuilder(commands)
                        .directory(workingDir)
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

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RepoSyncException(e);
        }
    }

    private String getBranch() {
        if (Strings.isNullOrEmpty(branch)) {
            log.debug("will use master as default branch");
            return "master";
        } else {
            return branch;
        }
    }

    private static String[] protocolAndRest(String url) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url),
                "url is null or empty");
        String[] split = url.split("://");
        Preconditions
                .checkArgument(split.length == 2, "url should have protocol");
        log.debug("url [{}] -> {} + {}", url, split[0], split[1]);
        return split;
    }

    @Override
    public void syncTranslationToRepo() throws RepoSyncException {
        List<String> output =
                runNativeCommand(workingDir, "git", "status", "--porcelain");
        if (output.isEmpty()) {
            log.info("nothing changed so nothing to do");
            return;
        }
        runNativeCommand(workingDir, "git", "add", ".");
        runNativeCommand(workingDir, "git", "commit", "-m", commitMessage(),
                "--author", commitAuthor());
        runNativeCommand(workingDir, "git", "push", "--set-upstream", "origin", getBranch());
    }

    @Override
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public void setZanataUser(String zanataUrl, String zanataUsername) {
        commitMessage = String
                .format("Zanata Sync created by %s from %s", zanataUsername,
                        zanataUrl);
    }

    @Override
    public String commitMessage() {
        return commitMessage;
    }
}
