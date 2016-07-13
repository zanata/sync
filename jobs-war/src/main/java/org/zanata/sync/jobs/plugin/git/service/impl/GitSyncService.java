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
package org.zanata.sync.jobs.plugin.git.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.common.model.Credentials;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GitSyncService implements RepoSyncService {
    private static final Logger log =
            LoggerFactory.getLogger(GitSyncService.class);

    private Credentials credentials;
    private String url;
    private String branch;
    private File workingDir;

    @Override
    public void cloneRepo()
            throws RepoSyncException {

        log.info("doing git clone: {}", url);
        doGitClone(url, workingDir);
        checkOutBranch(workingDir, this.branch);
    }

    private void doGitClone(String repoUrl, File destPath) {
        destPath.mkdirs();
        UsernamePasswordCredentialsProvider user =
                new UsernamePasswordCredentialsProvider(
                        credentials.getUsername(),
                        credentials.getSecret());
        CloneCommand clone = Git.cloneRepository();
        clone.setBare(false)
                .setCloneAllBranches(true)
                .setDirectory(destPath).setURI(repoUrl)
                .setCredentialsProvider(user);
        try {
            clone.call();
            log.info("git clone finished: {}", repoUrl);
        } catch (GitAPIException e) {
            throw new RepoSyncException(e);
        }
    }


    private void checkOutBranch(File destPath, String branch) {
        try {
            Git git = Git.open(destPath);
            List<Ref> refs = git.branchList().setListMode(
                    ListBranchCommand.ListMode.ALL).call();
            Optional<Ref> remoteMaster = refs.stream().filter(ref -> ref.getName()
                    .equals("refs/remotes/origin/master")).findFirst();
            /* refs will have name like these:
            refs/heads/master
            refs/remotes/origin/master
            refs/remotes/origin/zanata
            */
            Optional<Ref> localBranchRef = Optional.empty();
            Optional<Ref> remoteBranchRef = Optional.empty();
            for (Ref ref : refs) {
                String refName = ref.getName();
                if (refName.equals("refs/heads/" + branch)) {
                    localBranchRef = Optional.of(ref);
                }
                if (refName.equals("refs/remotes/origin/" + branch)) {
                    remoteBranchRef = Optional.of(ref);
                }
            }

            // if remote branch head is the asking branch, we don't need to do any thing
            if (localBranchRef.isPresent()) {
                log.debug("already on branch: {}", branch);
                return;
            }

            /**
             * If branch found in local, use it.
             * If branch does not exists in remote, create new local branch based on master branch.
             */
            if (remoteBranchRef.isPresent()) {
                git.checkout()
                        .setCreateBranch(true)
                        .setForce(true).setName(branch)
                        .setStartPoint("origin/" + branch)
                        .setUpstreamMode(
                        CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
            } else {
                git.checkout()
                        .setCreateBranch(true)
                        .setForce(true).setName(branch)
                        .setStartPoint("origin/master")
                        .setUpstreamMode(
                                CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
            }
            if (log.isDebugEnabled()) {
                log.debug("current branch is: {}", git.getRepository().getBranch());
            }
        } catch (IOException | GitAPIException e) {
            throw new RepoSyncException(e);
        }
    }

    @Override
    public void syncTranslationToRepo()
            throws RepoSyncException {
        try {
            Git git = Git.open(workingDir);
            StatusCommand statusCommand = git.status();
            Status status = statusCommand.call();
            Set<String> uncommittedChanges = status.getUncommittedChanges();
            uncommittedChanges.addAll(status.getUntracked());
            if (!uncommittedChanges.isEmpty()) {
                log.info("uncommitted files in git repo: {}", uncommittedChanges);
                AddCommand addCommand = git.add();
                addCommand.addFilepattern(".");
                addCommand.call();

                log.info("commit changed files");
                CommitCommand commitCommand = git.commit();
                commitCommand.setCommitter("Zanata Auto Repo Sync", "zanata-users@redhat.com");
                commitCommand.setMessage("Zanata Auto Repo Sync (pushing translations)");
                commitCommand.call();

                log.info("push to remote repo");
                PushCommand pushCommand = git.push();
                UsernamePasswordCredentialsProvider user =
                        new UsernamePasswordCredentialsProvider(
                                credentials.getUsername(),
                                credentials.getSecret());
                pushCommand.setCredentialsProvider(user);
                pushCommand.call();
            } else {
                log.info("nothing changed so nothing to do");
            }
        } catch (IOException e) {
            throw new RepoSyncException(
                    "failed opening " + workingDir + " as git repo", e);
        } catch (GitAPIException e) {
            throw new RepoSyncException("Failed committing translations into the repo", e);
        }
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
        if (Strings.isNullOrEmpty(branch)) {
            log.debug("will use master as default branch");
            this.branch = "master";
        } else {
            this.branch = branch;
        }
    }

    @Override
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }
}
