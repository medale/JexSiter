package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class RepositoryAdjustorTest {

    private static final List<String> INPUT_LIST1 = Arrays.asList(
            "56a329926a92460b9b6ac1377f610e48  ./web/newsletter/grip-it.jpg",
            "4d6ccea2e6e506ee68b1a793c477e617  ./web/newsletter/handstand.jpg",
            "7d6ccea2e6e506ee68b1a793c477e613  ./web/bogus/bar.baz");

    @Test
    public void testComputeFileLocationsToBeLocallyDeletedWithFilesToBeDeleted() {
        final Map<String, String> remoteMap = getMap(INPUT_LIST1);
        final Set<String> remoteFileLocations = remoteMap.keySet();
        final List<String> localList = INPUT_LIST1.subList(0, 1);
        final Map<String, String> localMap = getMap(localList);
        final Set<String> localFileLocations = localMap.keySet();
        final Set<String> locallyDeleted = RepositoryAdjustor
                .computeFileLocationsToBeLocallyDeleted(localFileLocations,
                        remoteFileLocations);
        final List<String> expectedLocallyDeleted = Arrays.asList(
                "./web/newsletter/handstand.jpg", "./web/bogus/bar.baz");
        assertEquals(locallyDeleted.size(), expectedLocallyDeleted.size());
        for (final String expected : expectedLocallyDeleted) {
            assertTrue(locallyDeleted.contains(expected));
        }
    }

    @Test
    public void testComputeFileLocationsToBeLocallyDeletedWithNoFilesToBeDeleted() {
        final Map<String, String> remoteMap = getMap(INPUT_LIST1);
        final Set<String> remoteFileLocations = remoteMap.keySet();
        final Map<String, String> localMap = remoteMap;
        final Set<String> localFileLocations = localMap.keySet();
        final Set<String> locallyDeleted = RepositoryAdjustor
                .computeFileLocationsToBeLocallyDeleted(localFileLocations,
                        remoteFileLocations);
        assertEquals(0, locallyDeleted.size());
    }

    @Test
    public void testComputeFileLocationsToBeAddedWithFilesToBeAdded() {
        final Map<String, String> localMap = getMap(INPUT_LIST1);
        final Set<String> localFileLocations = localMap.keySet();
        final List<String> remoteList = INPUT_LIST1.subList(0, 1);
        final Map<String, String> remoteMap = getMap(remoteList);
        final Set<String> remoteFileLocations = remoteMap.keySet();
        final Set<String> added = RepositoryAdjustor
                .computeFileLocationsToBeAdded(localFileLocations,
                        remoteFileLocations);
        final List<String> expectedAdded = Arrays.asList(
                "./web/newsletter/handstand.jpg", "./web/bogus/bar.baz");
        assertEquals(expectedAdded.size(), added.size());
        for (final String expected : expectedAdded) {
            assertTrue(added.contains(expected));
        }
    }

    @Test
    public void testComputeFileLocationsToBeModified() {
        final Map<String, String> localMap = getMap(INPUT_LIST1);
        final Map<String, String> remoteMap = getMap(INPUT_LIST1);
        final String modLoc = "./web/newsletter/handstand.jpg";
        final String modMd5Hash = "123ccea2e6e506ee68b1a793c477e613";
        remoteMap.put(modLoc, modMd5Hash);
        final Set<String> modified = RepositoryAdjustor
                .computeFileLocationsToBeModified(localMap, remoteMap);
        final List<String> expectedMod = Arrays.asList(modLoc);
        assertEquals(expectedMod.size(), modified.size());
        for (final String expected : expectedMod) {
            assertTrue(modified.contains(expected));
        }
    }

    @Test
    public void testComputeFileAdjustments() {
        final Map<String, String> localMap = getMap(INPUT_LIST1);
        final Map<String, String> remoteMap = getMap(INPUT_LIST1);

        // remote entry modified
        final String modLoc = "./web/newsletter/handstand.jpg";
        final String modMd5Hash = "123ccea2e6e506ee68b1a793c477e613";
        remoteMap.put(modLoc, modMd5Hash);

        // remote entry added
        final String addedLoc = "./newFile.txt";
        final String addedMd5 = "123ccea2e6e506ee68b1a793c477e642";
        remoteMap.put(addedLoc, addedMd5);

        // remote entry deleted
        final String deletedLoc = "./web/newsletter/grip-it.jpg";
        remoteMap.remove(deletedLoc);

        final RepositoryAdjustor adjustor = new RepositoryAdjustor();
        adjustor.setLocalFileLocationToMd5Map(localMap);
        adjustor.setRemoteFileLocationToMd5Map(remoteMap);
        adjustor.computeFileAdjustments();

        final Set<String> fileLocationsToBeModified = adjustor
                .getFileLocationsToBeModified();
        assertTrue(fileLocationsToBeModified.size() == 1);
        assertTrue(fileLocationsToBeModified.contains(modLoc));

        final Set<String> fileLocationsToBeAdded = adjustor
                .getFileLocationsToBeAdded();
        assertTrue(fileLocationsToBeAdded.size() == 1);
        assertTrue(fileLocationsToBeAdded.contains(addedLoc));

        final Set<String> fileLocationsToBeLocallyDeleted = adjustor
                .getFileLocationsToBeLocallyDeleted();
        assertTrue(fileLocationsToBeLocallyDeleted.size() == 1);
        assertTrue(fileLocationsToBeLocallyDeleted.contains(deletedLoc));
    }

    private Map<String, String> getMap(final List<String> inputList) {
        final List<FileLocationMd5Pair> expectedFileLocations = getFileLocationMd5Hashes(inputList);
        final Map<String, String> fileLocationToMd5HashMap = new HashMap<String, String>();
        for (final FileLocationMd5Pair pair : expectedFileLocations) {
            final String key = pair.getFileLocation();
            final String value = pair.getMd5Hash();
            fileLocationToMd5HashMap.put(key, value);
        }
        return fileLocationToMd5HashMap;
    }

    private List<FileLocationMd5Pair> getFileLocationMd5Hashes(
            final List<String> inputList) {
        final List<FileLocationMd5Pair> expectedFileLocations = new ArrayList<FileLocationMd5Pair>();
        for (final String input : inputList) {
            final FileLocationMd5Pair pair = FileLocationMd5Pair
                    .getInstance(input);
            expectedFileLocations.add(pair);
        }
        return expectedFileLocations;
    }

}
