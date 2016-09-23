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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.common.annotation.RepoPlugin;
import org.zanata.sync.common.model.SyncJobDetail;
import org.zanata.sync.jobs.cache.RepoCache;
import org.zanata.sync.jobs.common.exception.RepoSyncException;
import org.zanata.sync.jobs.common.model.Credentials;
import org.zanata.sync.jobs.common.model.UsernamePasswordCredential;
import org.zanata.sync.jobs.plugin.git.service.RepoSyncService;
import org.zanata.sync.plugin.git.GitPlugin;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Note JGIT doesn't support shallow clone yet. But jenkins has an abstraction
 * to use native git first then fall back to JGIT. see http://stackoverflow.com/questions/11475263/shallow-clone-with-jgit?rq=1#comment38082799_12097883
 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=475615
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
@RepoPlugin
public class GitSyncService implements RepoSyncService {
    private static final Logger log =
            LoggerFactory.getLogger(GitSyncService.class);

    @Override
    public void cloneRepo(SyncJobDetail jobDetail, Path workingDir) {
        String srcRepoUrl = jobDetail.getSrcRepoUrl();
        log.info("doing git clone: {} -> {}", srcRepoUrl,
                workingDir.toAbsolutePath());
        UsernamePasswordCredential credential =
                new UsernamePasswordCredential(jobDetail.getSrcRepoUsername(),
                        jobDetail.getSrcRepoSecret());
        doGitClone(srcRepoUrl, workingDir.toFile(), credential);
        doGitFetch(workingDir.toFile());
        checkOutBranch(workingDir.toFile(), getBranchOrDefault(jobDetail.getSrcRepoBranch()));
        cleanUpCurrentBranch(workingDir.toFile());
    }

    // if we get the repo from cache, it may contain files from other branch as untracked files
    private static void cleanUpCurrentBranch(File workingDir) {
        try (Git git = Git.open(workingDir)) {
            log.debug("git clean current work tree");
            git.clean().setCleanDirectories(true)
                    .setPaths(Sets.newHashSet(".")).call();
        } catch (IOException | GitAPIException e) {
            throw new RepoSyncException(e);
        }
    }

    private static void doGitClone(String repoUrl, File destPath,
            Credentials credentials) {
        destPath.mkdirs();

        CloneCommand clone = Git.cloneRepository();
        setUserIfProvided(clone, credentials)
                .setBare(false)
                .setCloneAllBranches(true)
                .setDirectory(destPath).setURI(repoUrl);
        try {
            clone.call();
            log.info("git clone finished: {} -> {}", repoUrl, destPath);

        } catch (GitAPIException e) {
            throw new RepoSyncException(e);
        }
    }

    private static <T extends TransportCommand<T, ?>> T setUserIfProvided(
            T command, Credentials credentials) {
        if (credentials != null &&
                !Strings.isNullOrEmpty(credentials.getUsername()) &&
                !Strings.isNullOrEmpty(credentials.getSecret())) {
            UsernamePasswordCredentialsProvider user =
                    new UsernamePasswordCredentialsProvider(
                            credentials.getUsername(),
                            credentials.getSecret());
            return command.setCredentialsProvider(user);
        } else {
            log.info("no credential is provided: {}", credentials);
            return command;
        }
    }


    private void checkOutBranch(File destPath, String branch) {
        try (Git git = Git.open(destPath)) {

            String currentBranch = git.getRepository().getBranch();
            if (currentBranch.equals(branch)) {
                log.info("already on branch: {}. will do a git pull", branch);
                PullResult pullResult = git.pull().call();
                log.debug("pull result: {}", pullResult);
                Preconditions.checkState(pullResult.isSuccessful());
                return;
            }


            List<Ref> refs = git.branchList().setListMode(
                    ListBranchCommand.ListMode.ALL).call();
            /* refs will have name like these:
            refs/heads/master,
            refs/heads/trans,
            refs/heads/zanata,
            refs/remotes/origin/HEAD,
            refs/remotes/origin/master,
            refs/remotes/origin/trans,
            refs/remotes/origin/zanata

            where the local branches are: master, trans, zanata
            remote branches are: master, trans, zanata
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

            // if local branch exists and we are now on a different branch,
            // we delete it first then re-checkout
            if (localBranchRef.isPresent()) {
                log.debug("deleting local branch {}", branch);
                git.branchDelete().setBranchNames(branch).call();
            }

            if (remoteBranchRef.isPresent()) {
                // if remote branch exists, we create a new local branch based on it.
                git.checkout()
                        .setCreateBranch(true)
                        .setForce(true).setName(branch)
                        .setStartPoint("origin/" + branch)
                        .setUpstreamMode(
                                CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
            } else {
                // If branch does not exists in remote, create new local branch based on master branch.
                git.checkout()
                        .setCreateBranch(true)
                        .setForce(true).setName(branch)
                        .setStartPoint("origin/master")
                        .setUpstreamMode(
                                CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
            }
            if (log.isDebugEnabled()) {
                log.debug("current branch is: {}",
                        git.getRepository().getBranch());
            }
        } catch (IOException | GitAPIException e) {
            throw new RepoSyncException(e);
        }
    }

    private static void doGitFetch(File workingDir) {
        try (Git git = Git.open(workingDir)) {
            log.info("doing git fetch");
            FetchResult result = git.fetch().call();
            log.info("git fetch result: {}", result.getMessages());
        } catch (IOException | GitAPIException e) {
            throw new RepoSyncException(e);
        }

    }

    @Override
    public void syncTranslationToRepo(SyncJobDetail jobDetail, Path workingDir) {
        UsernamePasswordCredential
                user =
                new UsernamePasswordCredential(
                        jobDetail.getSrcRepoUsername(),
                        jobDetail.getSrcRepoSecret());
        try (Git git = Git.open(workingDir.toFile())) {
            if (log.isDebugEnabled()) {
                log.debug("before syncing translation, current branch: {}",
                        git.getRepository().getBranch());
            }
            StatusCommand statusCommand = git.status();
            Status status = statusCommand.call();
            Set<String> uncommittedChanges = status.getUncommittedChanges();
            uncommittedChanges.addAll(status.getUntracked());
            if (!uncommittedChanges.isEmpty()) {
                log.info("uncommitted files in git repo: {}",
                        uncommittedChanges);
                AddCommand addCommand = git.add();
                addCommand.addFilepattern(".");
                addCommand.call();

                log.info("commit changed files");
                CommitCommand commitCommand = git.commit();
                commitCommand.setAuthor(commitAuthorName(),
                        commitAuthorEmail());
                commitCommand.setMessage(commitMessage(jobDetail.getZanataUsername()));
                RevCommit revCommit = commitCommand.call();

                log.info("push to remote repo");
                PushCommand pushCommand = git.push();
                setUserIfProvided(pushCommand, user);
                pushCommand.call();
            } else {
                log.info("nothing changed so nothing to do");
            }
        } catch (IOException e) {
            log.error("what the heck", e);
            throw new RepoSyncException(
                    "failed opening " + workingDir + " as git repo", e);
        } catch (GitAPIException e) {
            log.error("what the heck", e);
            throw new RepoSyncException(
                    "Failed committing translations into the repo", e);
        }
    }

    @Override
    public String supportedRepoType() {
        return GitPlugin.NAME;
    }
}
