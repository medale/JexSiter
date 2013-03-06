package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;

/**
 *
 */
public class InitializeCommand {

    private static final Logger LOGGER = Logger
            .getLogger(InitializeCommand.class);

    public void execute() throws IOException {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.loadConfiguration();
        Properties configProps = appConfig.getConfiguration();

        File backupDir = createBackupDir(configProps);
        initGitRepo(backupDir);
        downloadFilenameToHashMap(configProps, backupDir);
        performInitialBulkFileDownload(configProps, backupDir);
    }

    private void initGitRepo(File backupDir) throws IOException {
        Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.initGitRepository(repo);
    }

    private void downloadFilenameToHashMap(Properties configProps,
            File backupDir) {
        File filenameToHashMap = new File(backupDir,
                ExsiterConstants.FILENAME_TO_HASH_MAP);
    }

    private void performInitialBulkFileDownload(Properties configProps,
            File backupDir) {
        String notImplemented = "Initial bulk download from target host to local backup dir is not implemented. Do scp -R via command line!";
        LOGGER.error(notImplemented);
        throw new UnsupportedOperationException(notImplemented);
    }

    public File createBackupDir(Properties configProps) throws IOException {
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
        return backupDir;
    }
}
