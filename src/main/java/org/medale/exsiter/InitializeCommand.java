package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;

import com.jcraft.jsch.JSchException;

/**
 *
 */
public class InitializeCommand {

    private static final Logger LOGGER = Logger
            .getLogger(InitializeCommand.class);

    public void execute() throws IOException, JSchException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration();
        execute(appConfig);
    }

    public void execute(final ApplicationConfiguration appConfig)
            throws IOException, JSchException {
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();

        LOGGER.info("Creating backup directory...");
        final File backupDir = createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        LOGGER.info("Created backup directory at "
                + backupDir.getAbsolutePath());

        LOGGER.info("Initializing git repo in backup dir...");
        initGitRepo(backupDir);
        LOGGER.info("Initialized git repo in backup dir...");

        LOGGER.info("Downloading and storing file names and their md5 sums (this might take a while ~1min)...");
        downloadFilenameToHashMap(configProps, backupDir);

        LOGGER.info("Starting file download...");
        performInitialFileDownload(configProps, backupDir);

        LOGGER.info("Committing all changes to repo...");
        addAndCommitChanges(backupDir);
    }

    protected void initGitRepo(final File backupDir) throws IOException {
        final Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.initGitRepository(repo);
    }

    protected void downloadFilenameToHashMap(final Properties configProps,
            final File backupDir) throws JSchException, IOException {
        final SshChannelCreator channelCreator = SshChannelCreatorFactory
                .getSshChannelCreator(configProps);
        final Map<String, String> filenameToHashMap = RemoteFileLocationToMd5MapGenerator
                .getFileLocationToMd5Map(channelCreator);
        channelCreator.closeSession();

        final File filenameToHashMapFile = new File(backupDir,
                ExsiterConstants.FILENAME_TO_HASH_MAP);
        MapStore.storeMap(filenameToHashMapFile, filenameToHashMap);
    }

    protected void performInitialFileDownload(final Properties configProps,
            final File backupDir) {
        final String notImplemented = "Initial file download from target host to local backup dir is not implemented. Do scp -R via command line!";
        LOGGER.error(notImplemented);
    }

    protected void addAndCommitChanges(final File backupDir) throws IOException {
        final Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.addAllChanges(repo);
        final String commitMessage = "Initial download of fileNameToHashMap and content.";
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
        final String backupRootDirLocation = configProps
                .getProperty(ApplicationConfiguration.PROP_BACKUP_ROOT_DIR);
        final File backupRootDir = new File(backupRootDirLocation);
        // create exsiter-backup dir under root dir
        final File backupDir = new File(backupRootDir,
                ExsiterConstants.EXSITER_BACKUP_DIR);
        if (backupDir.exists()) {
            final String errMsg = backupDir.getAbsolutePath()
                    + " already exists. Unable to initialize new application - aborting...";
            LOGGER.fatal(errMsg);
            throw new IOException(errMsg);
        }
        final boolean backupDirCreated = backupDir.mkdirs();
        checkIfDirCreationWasSuccessful(backupDirCreated, backupDir);

        // remote-content-dir inside of exsiter-backup dir
        // location of remote content, i.e. not metadata map
        final File remoteContentDir = new File(backupDir,
                ExsiterConstants.REMOTE_CONTENT_DIR);
        final boolean remotedContentDirCreated = remoteContentDir.mkdir();
        checkIfDirCreationWasSuccessful(remotedContentDirCreated,
                remoteContentDir);

        return backupDir;
    }

    protected void checkIfDirCreationWasSuccessful(
            final boolean creationSuccess, final File dir) throws IOException {
        if (!creationSuccess) {
            throw new IOException("Unable to create " + dir.getAbsolutePath());
        }
    }
}
