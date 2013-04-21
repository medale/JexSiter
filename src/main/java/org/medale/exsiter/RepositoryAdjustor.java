package org.medale.exsiter;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

    public Set<String> getFileLocationsToBeLocallyDeleted() {
        return this.fileLocationsToBeLocallyDeleted;
    }

    public Set<String> getFileLocationsToBeModified() {
        return this.fileLocationsToBeModified;
    }

    public Set<String> getFileLocationsToBeAdded() {
        return this.fileLocationsToBeAdded;
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
