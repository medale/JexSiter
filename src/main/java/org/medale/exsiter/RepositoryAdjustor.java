package org.medale.exsiter;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 */
public class RepositoryAdjustor {

    private Map<String, FileLocationMd5Pair> previousFileLocationToMd5HashMap;
    private Map<String, FileLocationMd5Pair> currentFileLocationToMd5HashMap;
    private Set<String> fileLocationsToBeLocallyDeleted;
    private Set<String> fileLocationsToBeModified;
    private Set<String> fileLocationsToBeAdded;

    public void setPreviouslyDownloadedFilenameToHashMap(
            final Map<String, FileLocationMd5Pair> previousFileLocationToMd5HashMap) {
        this.previousFileLocationToMd5HashMap = previousFileLocationToMd5HashMap;
    }

    public void setCurrentFilenameToHashMap(
            final Map<String, FileLocationMd5Pair> currentFileLocationToMd5HashMap) {
        this.currentFileLocationToMd5HashMap = currentFileLocationToMd5HashMap;
    }

    /**
     * Based on current and previous map determine which files need to be
     * locally deleted, modified or added.
     */
    public void computeFileAdjustments() {
        final Set<String> currentFileLocations = this.currentFileLocationToMd5HashMap
                .keySet();
        final Set<String> previousFileLocations = this.previousFileLocationToMd5HashMap
                .keySet();
        this.fileLocationsToBeLocallyDeleted = computeFileLocationsToBeLocallyDeleted(
                currentFileLocations, previousFileLocations);
        this.fileLocationsToBeAdded = computeFileLocationsToBeAdded(
                currentFileLocations, previousFileLocations);
        this.fileLocationsToBeModified = computeFileLocationsToBeModified(
                this.currentFileLocationToMd5HashMap,
                this.previousFileLocationToMd5HashMap);
    }

    protected static Set<String> computeFileLocationsToBeModified(
            final Map<String, FileLocationMd5Pair> currentFileLocationToMd5HashMap,
            final Map<String, FileLocationMd5Pair> previousFileLocationToMd5HashMap) {
        final Set<String> fileLocationsToBeModified = new HashSet<String>();
        for (final Entry<String, FileLocationMd5Pair> currEntry : currentFileLocationToMd5HashMap
                .entrySet()) {
            final String currFileLocationKey = currEntry.getKey();
            if (previousFileLocationToMd5HashMap
                    .containsKey(currFileLocationKey)) {
                final FileLocationMd5Pair currPair = currEntry.getValue();
                final FileLocationMd5Pair prevPair = previousFileLocationToMd5HashMap
                        .get(currFileLocationKey);
                final String currMd5Hash = currPair.getMd5Hash();
                final String prevMd5Hash = prevPair.getMd5Hash();
                if (!currMd5Hash.equals(prevMd5Hash)) {
                    fileLocationsToBeModified.add(currFileLocationKey);
                }
            }
        }
        return fileLocationsToBeModified;
    }

    protected static Set<String> computeFileLocationsToBeLocallyDeleted(
            final Set<String> currentFileLocations,
            final Set<String> previousFileLocations) {
        final Set<String> fileLocationsToBeLocallyDeleted = new HashSet<String>(
                previousFileLocations);
        fileLocationsToBeLocallyDeleted.removeAll(currentFileLocations);
        return fileLocationsToBeLocallyDeleted;
    }

    protected static Set<String> computeFileLocationsToBeAdded(
            final Set<String> currentFileLocations,
            final Set<String> previousFileLocations) {
        final Set<String> fileLocationsToBeAdded = new HashSet<String>(
                currentFileLocations);
        fileLocationsToBeAdded.removeAll(previousFileLocations);
        return fileLocationsToBeAdded;
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

}
