package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static final String GIT_DIR = ".git";
    public static final String ADD_ALL_PATTERN = ".";
    public static final String TAG_PREFIX = "v";

    public static String getDateTag(Date date) {
        String datePattern = "yyyyMMMdd";
        SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        return formatter.format(date);
    }

    public static Repository getGitRepository(File repoDir) throws IOException {
        File gitHome = new File(repoDir, GIT_DIR);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(gitHome).build();
        return repo;
    }

    public static void initGitRepository(Repository repo) throws IOException {
        repo.create();
    }

    public static void addAllChanges(Repository repo) throws IOException {
        Git git = new Git(repo);
        AddCommand addCommand = git.add();
        try {
            addCommand.addFilepattern(ADD_ALL_PATTERN).call();
        } catch (Exception e) {
            String errMsg = "Unable to commit changes due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    public static void commitAllChanges(Repository repo, String commitMessage)
            throws IOException {
        Git git = new Git(repo);
        CommitCommand commitCommand = git.commit();
        try {
            commitCommand.setMessage(commitMessage).call();
        } catch (Exception e) {
            String errMsg = "Unable to commit changes due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    public static Status getStatus(Repository repo) throws IOException {
        Git git = new Git(repo);
        StatusCommand statusCommand = git.status();
        Status status = null;
        try {
            status = statusCommand.call();
        } catch (Exception e) {
            String errMsg = "Unable to execute status command due to " + e;
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
    public static void createNewTag(Repository repo, String tagName)
            throws IOException {
        Git git = new Git(repo);
        TagCommand tagCommand = git.tag();
        try {
            tagCommand.setName(tagName).call();
        } catch (Exception e) {
            String errMsg = "Unable to tag repo due to " + e;
            throw new IOException(errMsg, e);
        }
    }

    public static void cloneRepoDir(File repoDir, File cloneParentDir,
            String cloneDirLocation) throws IOException {
        CloneCommand cloneCommand = Git.cloneRepository();
        String repoDirUri = "file:///" + repoDir.getAbsolutePath();
        cloneCommand.setURI(repoDirUri);
        File cloneHome = new File(cloneParentDir, cloneDirLocation);
        try {
            cloneCommand.setDirectory(cloneHome).call();
        } catch (Exception e) {
            String errMsg = "Unable to clone repo due to " + e;
            throw new IOException(errMsg, e);
        }
    }

}
