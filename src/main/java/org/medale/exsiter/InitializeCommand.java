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
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        execute(appConfig);
    }

    public void execute(ApplicationConfiguration appConfig) throws IOException,
            JSchException {
        appConfig.loadConfiguration();
        Properties configProps = appConfig.getConfiguration();

        LOGGER.info("Creating backup directory...");
        File backupDir = createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        LOGGER.info("Created backup directory at "
                + backupDir.getAbsolutePath());

        LOGGER.info("Initializing git repo in backup dir...");
        initGitRepo(backupDir);
        LOGGER.info("Initialized git repo in backup dir...");

        LOGGER.info("Downloading and storing file names and their md5 sums (this might take a while ~1min)...");
        downloadFilenameToHashMap(configProps, backupDir);

        LOGGER.info("Starting bulk file download...");
        performInitialBulkFileDownload(configProps, backupDir);

        LOGGER.info("Committing all changes to repo...");
        addAndCommitChanges(backupDir);
    }

    private void initGitRepo(File backupDir) throws IOException {
        Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.initGitRepository(repo);
    }

    private void downloadFilenameToHashMap(Properties configProps,
            File backupDir) throws JSchException, IOException {
        SshChannelCreator channelCreator = SshChannelCreatorFactory
                .getSshChannelCreator(configProps);
        Map<String, FilePathChecksumTriple> filenameToHashMap = FilenameToHashMapCreator
                .getFilenameToHashMap(channelCreator);
        channelCreator.closeSession();

        File filenameToHashMapFile = new File(backupDir,
                ExsiterConstants.FILENAME_TO_HASH_MAP);
        MapStore.storeMap(filenameToHashMapFile, filenameToHashMap);
    }

    private void performInitialBulkFileDownload(Properties configProps,
            File backupDir) {
        String notImplemented = "Initial bulk download from target host to local backup dir is not implemented. Do scp -R via command line!";
        LOGGER.error(notImplemented);
    }

    private void addAndCommitChanges(File backupDir) throws IOException {
        Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.addAllChanges(repo);
        String commitMessage = "Initial download of fileNameToHashMap and content.";
        GitShell.commitAllChanges(repo, commitMessage);
    }

    protected File createBackupDirButThrowExceptionIfItAlreadyExists(
            Properties configProps) throws IOException {
        String backupRootDirLocation = configProps
                .getProperty(ApplicationConfiguration.PROP_BACKUP_ROOT_DIR);
        File backupRootDir = new File(backupRootDirLocation);
        File backupDir = new File(backupRootDir,
                ExsiterConstants.EXSITER_BACKUP_DIR);
        if (backupDir.exists()) {
            String errMsg = backupDir.getAbsolutePath()
                    + " already exists. Unable to initialize new application - aborting...";
            LOGGER.fatal(errMsg);
            throw new IOException(errMsg);
        }
        backupDir.mkdirs();

        File remoteContentDir = new File(backupDir,
                ExsiterConstants.REMOTE_CONTENT_DIR);
        remoteContentDir.mkdir();

        return backupDir;
    }
}
