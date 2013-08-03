package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitShell {

    private static final Logger LOGGER = Logger.getLogger(GitShell.class);

    public static final String GIT_DIR = ".git";
    public static final String ADD_ALL_PATTERN = ".";
    public static final String TAG_PREFIX = "v";

    public static String getDateTag(final Date date) {
        final String datePattern = "yyyyMMMdd";
        final SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        return formatter.format(date);
    }

    public static Repository getGitRepository(final File repoDir)
            throws IOException {
        final File gitHome = new File(repoDir, GIT_DIR);
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Repository repo = builder.setGitDir(gitHome).build();
        return repo;
    }

    public static void initGitRepository(final Repository repo)
            throws IOException {
        repo.create();
    }

    public static void addAllChanges(final Repository repo) throws IOException {
        final Git git = new Git(repo);
        if (LOGGER.isDebugEnabled()) {
            logCurrentStatus(git);
        }
        addNewAndModifiedFiles(git);
        addRemovedFiles(git);
    }

    @SuppressWarnings("rawtypes")
    private static void logCurrentStatus(final Git git) throws IOException {
        final StatusCommand statusCommand = git.status();
        try {
            final Status status = statusCommand.call();
            final String[] statusFileSetNames = { "added", "changed",
                    "missing", "modified", "removed", "untracked" };
            final Set[] statusFileSets = { status.getAdded(),
                    status.getChanged(), status.getMissing(),
                    status.getModified(), status.getRemoved(),
                    status.getUntracked() };
            for (int i = 0; i < statusFileSets.length; i++) {
                final String setName = statusFileSetNames[i];
                final Set fileSet = statusFileSets[i];
                LOGGER.debug("Set of " + setName + " files: " + fileSet);
            }
        } catch (final Exception e) {
            final String errMsg = "Unable to display status due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    private static void addNewAndModifiedFiles(final Git git)
            throws IOException {
        final AddCommand addCommand = git.add();
        try {
            // add new files/modifications
            addCommand.addFilepattern(ADD_ALL_PATTERN).call();
        } catch (final Exception e) {
            final String errMsg = "Unable to commit changes due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    private static void addRemovedFiles(final Git git) throws IOException {
        final AddCommand addCommand = git.add();
        try {
            // add removed files
            final boolean update = true;
            addCommand.addFilepattern(ADD_ALL_PATTERN).setUpdate(update).call();

        } catch (final Exception e) {
            final String errMsg = "Unable to commit changes due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    public static void commitAllChanges(final Repository repo,
            final String commitMessage) throws IOException {
        final Git git = new Git(repo);
        final CommitCommand commitCommand = git.commit();
        try {
            commitCommand.setMessage(commitMessage).call();
        } catch (final Exception e) {
            final String errMsg = "Unable to commit changes due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    public static Status getStatus(final Repository repo) throws IOException {
        final Git git = new Git(repo);
        final StatusCommand statusCommand = git.status();
        Status status = null;
        try {
            status = statusCommand.call();
        } catch (final Exception e) {
            final String errMsg = "Unable to execute status command due to "
                    + e;
            throw new IOException(errMsg, e);
        }
        return status;
    }

    /**
     * http://stackoverflow.com/questions/791959/how-to-use-git-to-download-a-
     * particular-tag<br>
     * 
     * git tag -l lists all tags<br>
     * git checkout tags/[tag name]<br>
     * Create tar from specifc tag:<br>
     * git archive --format=tar --remote=[hostname]:[path to repo] [tag name] >
     * tagged.tar<br>
     * 
     * @param repo
     * @param tagName
     * @throws IOException
     */
    public static void createNewTag(final Repository repo, final String tagName)
            throws IOException {
        final Git git = new Git(repo);
        final TagCommand tagCommand = git.tag();
        try {
            tagCommand.setName(tagName).call();
        } catch (final Exception e) {
            final String errMsg = "Unable to tag repo due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    public static void cloneRepoDir(final File repoDir,
            final File cloneParentDir, final String cloneDirLocation)
            throws IOException {
        final CloneCommand cloneCommand = Git.cloneRepository();
        final String repoDirUri = "file:///" + repoDir.getAbsolutePath();
        cloneCommand.setURI(repoDirUri);
        final File cloneHome = new File(cloneParentDir, cloneDirLocation);
        try {
            cloneCommand.setDirectory(cloneHome).call();
        } catch (final Exception e) {
            final String errMsg = "Unable to clone repo due to " + e;
            throw new IOException(errMsg, e);
        }
    }

}
