package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.medale.mail.MailSender;

import com.jcraft.jsch.JSchException;

/**
 * Backup command used to compute difference between local file store and remote
 * location (files added, modified, deleted) and then makes and records the
 * adjustments needed to make local file store equal remote location. Backed by
 * Git repository and changes are tagged.
 */
public class BackupCommand {
    private static final Logger LOGGER = Logger.getLogger(BackupCommand.class);

    private BackupReporter backupReporter;

    public void execute(final Properties configProps) throws Exception {
        LOGGER.info("Executing Exsiter execute command...");

        final File backupDir = ApplicationConfiguration
                .getExsiterBackupDirectory(configProps);

        if (!backupDir.exists()) {
            final String errMsg = "Unable to execute backup command. Must first execute initialize command to set up proper backup directory.";
            LOGGER.error(errMsg);
            throw new IllegalStateException(errMsg);
        }

        // Step 0 - execute remote db backup
        createRemoteDatabaseBackup(configProps);

        // Step 1 - get and store current remote file locations/md5 hashes
        final Map<String, String> remoteFileLocationToMd5Map = getAndStoreCurrentRemoteFileLocationsAndMd5s(
                configProps, backupDir);

        // Step 2 - get and store current local file locations/md5 hashes
        final Map<String, String> localFileLocationToMd5Map = getAndStoreCurrentLocalFileLocationsAndMd5s(backupDir);

        // Step 3
        final RepositoryAdjustor repoAdjustor = new RepositoryAdjustor();
        repoAdjustor.setLocalFileLocationToMd5Map(localFileLocationToMd5Map);
        repoAdjustor.setRemoteFileLocationToMd5Map(remoteFileLocationToMd5Map);
        repoAdjustor.computeFileAdjustments();

        final Set<String> fileLocationsToBeModified = repoAdjustor
                .getFileLocationsToBeModified();
        final Set<String> fileLocationsToBeAdded = repoAdjustor
                .getFileLocationsToBeAdded();
        final Set<String> fileLocationsToBeLocallyDeleted = repoAdjustor
                .getFileLocationsToBeLocallyDeleted();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Mod: " + fileLocationsToBeModified.size());
            LOGGER.info("Added: " + fileLocationsToBeAdded.size());
            LOGGER.info("Delete local: "
                    + fileLocationsToBeLocallyDeleted.size());
        }

        repoAdjustor.executeFileAdjustments(backupDir, configProps);

        final File reportFile = new File(backupDir,
                ExsiterConstants.BACKUP_REPORT);
        final String report = this.backupReporter.createReport(
                reportFile.getAbsolutePath(), repoAdjustor);

        // Step 4 mail report
        final MailSender sender = new MailSender();
        sender.configure(configProps);
        if (sender.isEnabled()) {
            sender.addReport(report);
            final boolean sent = sender.send();
            if (LOGGER.isDebugEnabled()) {
                String msg = null;
                if (sent) {
                    msg = "Successfully sent report via email.";
                } else {
                    msg = "Unable to send report via email.";
                }
                LOGGER.debug(msg);
            }
        } else {
            LOGGER.debug("Sending report via email was not enabled. smtp.username not set.");
        }
    }

    protected void createRemoteDatabaseBackup(final Properties configProps) {
        SshChannelCreator channelCreator = null;
        try {
            channelCreator = SshChannelCreatorFactory
                    .getSshChannelCreatorWithOpenSession(configProps);
            final String output = RemoteDatabaseBackupExecutor
                    .executeRemoteDatabaseBackup(channelCreator);
            if (!output.equals(ExsiterConstants.DB_BACKUP_SUCCESS)) {
                LOGGER.error("Error while executing remote database backup: "
                        + output);
            } else {
                LOGGER.info("Successfully executed remote database backup command.");
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to execute remote database backup due to " + e);
        } finally {
            if (channelCreator != null) {
                channelCreator.closeSession();
            }
        }

    }

    private Map<String, String> getAndStoreCurrentLocalFileLocationsAndMd5s(
            final File backupDir) throws Exception, IOException {
        LOGGER.info("Computing current backup file locations and their md5 hash sums to determine differences...");
        final File remoteContentDir = new File(backupDir,
                ExsiterConstants.REMOTE_CONTENT_DIR);
        final String startDirectoryLocation = remoteContentDir
                .getAbsolutePath();
        final Map<String, String> localFileLocationToMd5Map = LocalFileLocationToMd5MapGenerator
                .getFileLocationToMd5Map(startDirectoryLocation);
        final String mapFileName = ExsiterConstants.LOCAL_FILE_NAME_TO_MD5_MAP;
        storeFileNameToMd5Map(backupDir, mapFileName, localFileLocationToMd5Map);
        return localFileLocationToMd5Map;
    }

    protected Map<String, String> getAndStoreCurrentRemoteFileLocationsAndMd5s(
            final Properties configProps, final File backupDir)
            throws JSchException, IOException {
        LOGGER.info("Retrieving remote file locations and their md5 hash sums...");
        final Map<String, String> remoteFileLocationToMd5Map = getRemoteFileNameToMd5Map(
                configProps, backupDir);
        final String mapFileName = ExsiterConstants.REMOTE_FILE_NAME_TO_MD5_MAP;
        storeFileNameToMd5Map(backupDir, mapFileName,
                remoteFileLocationToMd5Map);
        return remoteFileLocationToMd5Map;
    }

    protected Map<String, String> getRemoteFileNameToMd5Map(
            final Properties configProps, final File backupDir)
            throws JSchException, IOException {
        SshChannelCreator channelCreator = null;
        Map<String, String> fileNameToMd5Map = null;
        try {
            channelCreator = SshChannelCreatorFactory
                    .getSshChannelCreatorWithOpenSession(configProps);
            fileNameToMd5Map = RemoteFileLocationToMd5MapGenerator
                    .getFileLocationToMd5Map(channelCreator);
        } finally {
            if (channelCreator != null) {
                channelCreator.closeSession();
            }
        }
        return fileNameToMd5Map;
    }

    protected void storeFileNameToMd5Map(final File backupDir,
            final String mapFileName, final Map<String, String> fileNameToMd5Map)
            throws IOException {
        final File filenameToHashMapFile = new File(backupDir, mapFileName);
        MapStore.storeMap(filenameToHashMapFile, fileNameToMd5Map);
    }

    public BackupReporter getBackupReporter() {
        return this.backupReporter;
    }

    public void setBackupReporter(final BackupReporter backupReporter) {
        this.backupReporter = backupReporter;
    }

}
