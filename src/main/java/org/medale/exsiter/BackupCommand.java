package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;

public class BackupCommand {
    private static final Logger LOGGER = Logger.getLogger(BackupCommand.class);

    public void execute(final Properties configProps) throws Exception {
        LOGGER.info("Executing Exsiter execute command...");

        final File backupDir = ApplicationConfiguration
                .getExsiterBackupDirectory(configProps);

        if (!backupDir.exists()) {
            final String errMsg = "Unable to execute backup command. Must first execute initialize command to set up proper backup directory.";
            LOGGER.error(errMsg);
            throw new IllegalStateException(errMsg);
        }

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

        System.out.println("Mod: " + fileLocationsToBeModified.size());
        System.out.println("Added: " + fileLocationsToBeAdded.size());
        System.out.println("Delete local: " + fileLocationsToBeLocallyDeleted);

        repoAdjustor.executeFileAdjustments(backupDir, configProps);
    }

    private Map<String, String> getAndStoreCurrentLocalFileLocationsAndMd5s(
            final File backupDir) throws Exception, IOException {
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
        final SshChannelCreator channelCreator = SshChannelCreatorFactory
                .getSshChannelCreator(configProps);
        final Map<String, String> fileNameToMd5Map = RemoteFileLocationToMd5MapGenerator
                .getFileLocationToMd5Map(channelCreator);
        channelCreator.closeSession();
        return fileNameToMd5Map;
    }

    protected void storeFileNameToMd5Map(final File backupDir,
            final String mapFileName, final Map<String, String> fileNameToMd5Map)
            throws IOException {
        final File filenameToHashMapFile = new File(backupDir, mapFileName);
        MapStore.storeMap(filenameToHashMapFile, fileNameToMd5Map);
    }

}
