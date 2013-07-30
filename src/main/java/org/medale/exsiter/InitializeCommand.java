package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;

import com.jcraft.jsch.JSchException;

/**
 * Used to initialize Exsiter storage directories and Git repository.
 */
public class InitializeCommand {

    private static final Logger LOGGER = Logger
            .getLogger(InitializeCommand.class);

    public void execute(final ApplicationConfiguration appConfig)
            throws IOException, JSchException {
        LOGGER.info("Executing initialize command with app config based on "
                + appConfig.getConfigurationLocation());
        final Properties configProps = appConfig.getConfiguration();

        LOGGER.info("Creating backup directory...");
        final File backupDir = createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        LOGGER.info("Created backup directory at "
                + backupDir.getAbsolutePath());

        final File remoteContentDir = createRemoteContentDir(backupDir);
        LOGGER.info("Created remote content dir at "
                + remoteContentDir.getAbsolutePath());

        LOGGER.info("Initializing git repo in backup dir...");
        initGitRepo(backupDir);
        LOGGER.info("Initialized git repo in backup dir...");

        performInitialFileDownload(configProps, backupDir);

        LOGGER.info("Committing all changes to repo...");
        addAndCommitChanges(backupDir);
    }

    protected void initGitRepo(final File backupDir) throws IOException {
        final Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.initGitRepository(repo);
    }

    protected void performInitialFileDownload(final Properties configProps,
            final File backupDir) {
        final String notImplemented = "Initial file download from target host to local backup dir is not implemented. Do scp -R via command line!";
        LOGGER.info(notImplemented);
    }

    protected void addAndCommitChanges(final File backupDir) throws IOException {
        final Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.addAllChanges(repo);
        final String commitMessage = "Initial creation of repository.";
        GitShell.commitAllChanges(repo, commitMessage);
    }

    /**
     * Creates backup root dir containing backup dir with remote content dir.
     * Returns backup dir where backups will be made (subdir of backup root
     * dir).
     * 
     * @param configProps
     * @return backup dir
     * @throws IOException
     */
    protected File createBackupDirButThrowExceptionIfItAlreadyExists(
            final Properties configProps) throws IOException {
        final File backupDir = ApplicationConfiguration
                .getExsiterBackupDirectory(configProps);
        if (backupDir.exists()) {
            final String errMsg = backupDir.getAbsolutePath()
                    + " already exists. Unable to initialize new application - aborting...";
            LOGGER.fatal(errMsg);
            throw new IOException(errMsg);
        }
        final boolean backupDirCreated = backupDir.mkdirs();
        checkIfDirCreationWasSuccessful(backupDirCreated, backupDir);

        return backupDir;
    }

    protected void checkIfDirCreationWasSuccessful(
            final boolean creationSuccess, final File dir) throws IOException {
        if (!creationSuccess) {
            throw new IOException("Unable to create " + dir.getAbsolutePath());
        }
    }

    /**
     * Create directory to store remote content (i.e. files that came off remote
     * server)
     * 
     * @param backupDir
     * @return
     * @throws IOException
     */
    protected File createRemoteContentDir(final File backupDir)
            throws IOException {
        // remote-content-dir inside of exsiter-backup dir
        // location of remote content, i.e. not metadata map
        final File remoteContentDir = new File(backupDir,
                ExsiterConstants.REMOTE_CONTENT_DIR);
        final boolean remotedContentDirCreated = remoteContentDir.mkdir();
        checkIfDirCreationWasSuccessful(remotedContentDirCreated,
                remoteContentDir);
        return remoteContentDir;
    }
}
