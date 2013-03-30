package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class MapStoreTest {

    private final List<String> inputList = Arrays.asList(
            "56a329926a92460b9b6ac1377f610e48  ./web/newsletter/grip-it.jpg",
            "4d6ccea2e6e506ee68b1a793c477e617  ./web/newsletter/handstand.jpg");

    @Test
    public void testStoreMapAndLoadMapWithFile() throws IOException {
        final File csvFile = getTestCsvFile();
        final Map<String, FileLocationMd5Pair> fileLocationToMd5HashMap = getFileLocationToMd5HashMap();
        MapStore.storeMap(csvFile, fileLocationToMd5HashMap);

        final Map<String, FileLocationMd5Pair> actualMap = MapStore
                .loadMap(csvFile);
        assertEquals(fileLocationToMd5HashMap, actualMap);
    }

    @Test
    public void testStoreMapAndLoadMapWithWriter() throws IOException {
        Writer writer = null;
        final File csvFile = getTestCsvFile();
        writer = new FileWriter(csvFile);
        final Map<String, FileLocationMd5Pair> fileLocationToMd5HashMap = getFileLocationToMd5HashMap();
        MapStore.storeMap(writer, fileLocationToMd5HashMap);

        Reader reader = null;
        reader = new FileReader(csvFile);
        final Map<String, FileLocationMd5Pair> actualMap = MapStore
                .loadMap(reader);
        assertEquals(fileLocationToMd5HashMap, actualMap);
    }

    @Test
    public void testCsvApi() throws IOException {
        final File csvFile = getTestCsvFile();
        final List<FileLocationMd5Pair> expectedPairs = getExpectedTriples();
        CSVWriter writer = null;
        try {
            // generate default , separator with " quote char
            writer = new CSVWriter(new FileWriter(csvFile));
            for (final FileLocationMd5Pair pair : expectedPairs) {
                writer.writeNext(new String[] { pair.getFileLocation(),
                        pair.getMd5Hash() });
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            final List<String[]> allEntries = reader.readAll();
            assertEquals(expectedPairs.size(), allEntries.size());
            for (int i = 0; i < expectedPairs.size(); i++) {
                final FileLocationMd5Pair expectedTriple = expectedPairs.get(i);
                final String[] currEntry = allEntries.get(i);
                final String filePathAndName = currEntry[0];
                final String md5Hash = currEntry[1];
                final FileLocationMd5Pair actualTriple = new FileLocationMd5Pair(
                        md5Hash, filePathAndName);
                assertEquals(expectedTriple, actualTriple);
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private List<FileLocationMd5Pair> getExpectedTriples() {
        final List<FileLocationMd5Pair> expectedTriples = new ArrayList<FileLocationMd5Pair>();
        for (final String input : this.inputList) {
            final FileLocationMd5Pair pair = FileLocationMd5Pair
                    .getInstance(input);
            expectedTriples.add(pair);
        }
        return expectedTriples;
    }

    private Map<String, FileLocationMd5Pair> getFileLocationToMd5HashMap() {
        final List<FileLocationMd5Pair> expectedTriples = getExpectedTriples();
        final Map<String, FileLocationMd5Pair> fileLocationToMd5HashMap = new HashMap<String, FileLocationMd5Pair>();
        for (final FileLocationMd5Pair pair : expectedTriples) {
            final String key = pair.getFileLocation();
            fileLocationToMd5HashMap.put(key, pair);
        }
        return fileLocationToMd5HashMap;
    }

    private File getTestCsvFile() throws IOException {
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final File testHome = new File(tmpDir, "test");
        if (testHome.exists()) {
            FileUtils.deleteDirectory(testHome);
        }
        final boolean creationSuccess = testHome.mkdirs();
        if (!creationSuccess) {
            throw new IOException("Unable to create "
                    + testHome.getAbsolutePath());
        }
        System.out.println("Created temp test dir in "
                + testHome.getAbsolutePath());
        final File csvFile = new File(testHome, "test.csv");
        return csvFile;
    }
}
