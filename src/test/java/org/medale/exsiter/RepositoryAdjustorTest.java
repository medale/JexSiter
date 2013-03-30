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
        final Map<String, FileLocationMd5Pair> previousMap = getMap(INPUT_LIST1);
        final Set<String> previousFileLocations = previousMap.keySet();
        final List<String> currentList = INPUT_LIST1.subList(0, 1);
        final Map<String, FileLocationMd5Pair> currentMap = getMap(currentList);
        final Set<String> currentFileLocations = currentMap.keySet();
        final Set<String> locallyDeleted = RepositoryAdjustor
                .computeFileLocationsToBeLocallyDeleted(currentFileLocations,
                        previousFileLocations);
        final List<String> expectedLocallyDeleted = Arrays.asList(
                "./web/newsletter/handstand.jpg", "./web/bogus/bar.baz");
        assertEquals(locallyDeleted.size(), expectedLocallyDeleted.size());
        for (final String expected : expectedLocallyDeleted) {
            assertTrue(locallyDeleted.contains(expected));
        }
    }

    @Test
    public void testComputeFileLocationsToBeLocallyDeletedWithNoFilesToBeDeleted() {
        final Map<String, FileLocationMd5Pair> previousMap = getMap(INPUT_LIST1);
        final Set<String> previousFileLocations = previousMap.keySet();
        final Map<String, FileLocationMd5Pair> currentMap = previousMap;
        final Set<String> currentFileLocations = currentMap.keySet();
        final Set<String> locallyDeleted = RepositoryAdjustor
                .computeFileLocationsToBeLocallyDeleted(currentFileLocations,
                        previousFileLocations);
        assertEquals(0, locallyDeleted.size());
    }

    @Test
    public void testComputeFileLocationsToBeAddedWithFilesToBeAdded() {
        final Map<String, FileLocationMd5Pair> currentMap = getMap(INPUT_LIST1);
        final Set<String> currentFileLocations = currentMap.keySet();
        final List<String> previousList = INPUT_LIST1.subList(0, 1);
        final Map<String, FileLocationMd5Pair> previousMap = getMap(previousList);
        final Set<String> previousFileLocations = previousMap.keySet();
        final Set<String> added = RepositoryAdjustor
                .computeFileLocationsToBeAdded(currentFileLocations,
                        previousFileLocations);
        final List<String> expectedAdded = Arrays.asList(
                "./web/newsletter/handstand.jpg", "./web/bogus/bar.baz");
        assertEquals(expectedAdded.size(), added.size());
        for (final String expected : expectedAdded) {
            assertTrue(added.contains(expected));
        }
    }

    @Test
    public void testComputeFileLocationsToBeModified() {
        final Map<String, FileLocationMd5Pair> currentMap = getMap(INPUT_LIST1);
        final Map<String, FileLocationMd5Pair> previousMap = getMap(INPUT_LIST1);
        final String modLoc = "./web/newsletter/handstand.jpg";
        final FileLocationMd5Pair modPair = currentMap.get(modLoc);
        final String newMd5Hash = "123ccea2e6e506ee68b1a793c477e613";
        modPair.setMd5Hash(newMd5Hash);
        final Set<String> modified = RepositoryAdjustor
                .computeFileLocationsToBeModified(currentMap, previousMap);
        final List<String> expectedMod = Arrays.asList(modLoc);
        assertEquals(expectedMod.size(), modified.size());
        for (final String expected : expectedMod) {
            assertTrue(modified.contains(expected));
        }
    }

    private Map<String, FileLocationMd5Pair> getMap(final List<String> inputList) {
        final List<FileLocationMd5Pair> expectedFileLocations = getFileLocationMd5Hashes(inputList);
        final Map<String, FileLocationMd5Pair> fileLocationToMd5HashMap = new HashMap<String, FileLocationMd5Pair>();
        for (final FileLocationMd5Pair pair : expectedFileLocations) {
            final String key = pair.getFileLocation();
            fileLocationToMd5HashMap.put(key, pair);
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
