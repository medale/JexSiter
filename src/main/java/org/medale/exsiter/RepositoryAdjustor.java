package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.medale.io.ExsiterFileUtils;

import com.google.common.io.Files;
import com.jcraft.jsch.JSchException;

/**
 * Determines adjustments that need to be made to the local repository vis a vis
 * the remote file location/md5 hashes. Some local files need to be deleted
 * (they don't exist anymore on remote repo), some local files have different
 * md5 hashes (remote files were changed, i.e. we need to download the latest
 * remote version), some remote files were added (they don't exist in local
 * repo).
 */
public class RepositoryAdjustor {

    private Map<String, String> localFileLocationToMd5Map;
    private Map<String, String> remoteFileLocationToMd5Map;
    private Set<String> fileLocationsToBeLocallyDeleted;
    private Set<String> fileLocationsToBeModified;
    private Set<String> fileLocationsToBeAdded;

    public void setLocalFileLocationToMd5Map(
            final Map<String, String> localFileLocationToMd5Map) {
        this.localFileLocationToMd5Map = localFileLocationToMd5Map;
    }

    public void setRemoteFileLocationToMd5Map(
            final Map<String, String> remoteFileLocationToMd5Map) {
        this.remoteFileLocationToMd5Map = remoteFileLocationToMd5Map;
    }

    /**
     * Based on current and previous map determine which files need to be
     * locally deleted, modified or added.
     */
    public void computeFileAdjustments() {
        final Set<String> currentFileLocations = this.remoteFileLocationToMd5Map
                .keySet();
        final Set<String> previousFileLocations = this.localFileLocationToMd5Map
                .keySet();
        this.fileLocationsToBeLocallyDeleted = computeFileLocationsToBeLocallyDeleted(
                currentFileLocations, previousFileLocations);
        this.fileLocationsToBeAdded = computeFileLocationsToBeAdded(
                currentFileLocations, previousFileLocations);
        this.fileLocationsToBeModified = computeFileLocationsToBeModified(
                this.remoteFileLocationToMd5Map, this.localFileLocationToMd5Map);
    }

    /**
     * Change local repository according to the file adjustments computed via
     * computeFileAdjustments method. Performs necessary deletes and downloads
     * added and modified files from remote repo. Adds, commits and tags
     * resulting changes in local repo.
     * 
     * @param backupDir
     * @param configProps
     * @throws IOException
     * @throws JSchException
     */
    public void executeFileAdjustments(final File backupDir,
            final Properties configProps) throws IOException, JSchException {
        final File remoteContentDir = new File(backupDir,
                ExsiterConstants.REMOTE_CONTENT_DIR);

        deleteLocalFiles(remoteContentDir, this.fileLocationsToBeLocallyDeleted);
        downloadAndStoreRemoteAddedAndModifiedFiles(configProps,
                this.fileLocationsToBeAdded, this.fileLocationsToBeModified,
                remoteContentDir);

        final Date backupDate = new Date();
        addCommitAndTagChangesInGitRepo(backupDir, backupDate);
    }

    public Set<String> getFileLocationsToBeLocallyDeleted() {
        return this.fileLocationsToBeLocallyDeleted;
    }

    public Set<String> getFileLocationsToBeModified() {
        return this.fileLocationsToBeModified;
    }

    public Set<String> getFileLocationsToBeAdded() {
        return this.fileLocationsToBeAdded;
    }

    protected void deleteLocalFiles(final File remoteContentDir,
            final Set<String> fileLocationsToBeLocallyDeleted)
            throws IOException {
        for (final String fileLocation : fileLocationsToBeLocallyDeleted) {
            final File fileToDelete = new File(remoteContentDir, fileLocation);
            ExsiterFileUtils
                    .deleteFileAndEmptyParentDirectoriesUntilStopDirectory(
                            fileToDelete, remoteContentDir);
        }
    }

    protected void downloadAndStoreRemoteAddedAndModifiedFiles(
            final Properties configProps,
            final Set<String> fileLocationsToBeAdded,
            final Set<String> fileLocationsToBeModified,
            final File remoteContentDir) throws JSchException, IOException {
        final SshChannelCreator channelCreator = SshChannelCreatorFactory
                .getSshChannelCreator(configProps);
        final ScpTool scpTool = new ScpTool();
        scpTool.setSshChannelCreator(channelCreator);
        scpFiles(scpTool, remoteContentDir, fileLocationsToBeAdded);
        scpFiles(scpTool, remoteContentDir, fileLocationsToBeModified);
    }

    protected void scpFiles(final ScpTool scpTool, final File remoteContentDir,
            final Set<String> fileLocations) throws IOException {
        for (final String fileLocation : fileLocations) {
            final File localFile = new File(remoteContentDir, fileLocation);
            Files.createParentDirs(localFile);
            final String localFileLocation = localFile.getCanonicalPath();
            scpTool.scpFileFrom(fileLocation, localFileLocation);
        }
    }

    protected void addCommitAndTagChangesInGitRepo(final File backupDir,
            final Date date) throws IOException {
        final Repository repo = GitShell.getGitRepository(backupDir);
        GitShell.addAllChanges(repo);
        final String dateTag = GitShell.getDateTag(date);
        final String commitMessage = "Backup commits for " + dateTag;
        GitShell.commitAllChanges(repo, commitMessage);
        GitShell.createNewTag(repo, dateTag);
    }

    /**
     * Remote md5 hash is different from local md5 hash.
     * 
     * @param localFileLocationToMd5Map
     * @param remoteFileLocationToMd5Map
     * @return
     */
    protected static Set<String> computeFileLocationsToBeModified(
            final Map<String, String> localFileLocationToMd5Map,
            final Map<String, String> remoteFileLocationToMd5Map) {
        final Set<String> fileLocationsToBeModified = new HashSet<String>();
        for (final Entry<String, String> localEntry : localFileLocationToMd5Map
                .entrySet()) {
            final String localFileLocationKey = localEntry.getKey();
            if (remoteFileLocationToMd5Map.containsKey(localFileLocationKey)) {
                final String localMd5Hash = localEntry.getValue();
                final String remoteMd5Hash = remoteFileLocationToMd5Map
                        .get(localFileLocationKey);
                if (!localMd5Hash.equals(remoteMd5Hash)) {
                    fileLocationsToBeModified.add(localFileLocationKey);
                }
            }
        }
        return fileLocationsToBeModified;
    }

    /**
     * Files don't exist anymore in remote location.
     * 
     * @param currentFileLocations
     * @param previousFileLocations
     * @return
     */
    protected static Set<String> computeFileLocationsToBeLocallyDeleted(
            final Set<String> currentFileLocations,
            final Set<String> previousFileLocations) {
        final Set<String> fileLocationsToBeLocallyDeleted = new HashSet<String>(
                previousFileLocations);
        fileLocationsToBeLocallyDeleted.removeAll(currentFileLocations);
        return fileLocationsToBeLocallyDeleted;
    }

    /**
     * New files in remote location that don't exist locally.
     * 
     * @param currentFileLocations
     * @param previousFileLocations
     * @return
     */
    protected static Set<String> computeFileLocationsToBeAdded(
            final Set<String> currentFileLocations,
            final Set<String> previousFileLocations) {
        final Set<String> fileLocationsToBeAdded = new HashSet<String>(
                currentFileLocations);
        fileLocationsToBeAdded.removeAll(previousFileLocations);
        return fileLocationsToBeAdded;
    }
}
