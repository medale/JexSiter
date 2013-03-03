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

    private List<String> inputList = Arrays.asList(
            "56a329926a92460b9b6ac1377f610e48 ./web/newsletter/grip-it.jpg",
            "4d6ccea2e6e506ee68b1a793c477e617 ./web/newsletter/handstand.jpg");

    @Test
    public void testStoreMapAndLoadMap() throws IOException {
        Writer writer = null;
        File csvFile = getTestCsvFile();
        writer = new FileWriter(csvFile);
        Map<String, FilePathChecksumTriple> filePathAndNameToTripleMap = getTriplesMap();
        MapStore.storeMap(writer, filePathAndNameToTripleMap);

        Reader reader = null;
        reader = new FileReader(csvFile);
        Map<String, FilePathChecksumTriple> actualMap = MapStore
                .loadMap(reader);
        assertEquals(filePathAndNameToTripleMap, actualMap);
    }

    @Test
    public void testCsvApi() throws IOException {
        File csvFile = getTestCsvFile();
        List<FilePathChecksumTriple> expectedTriples = getExpectedTriples();
        CSVWriter writer = null;
        try {
            // generate default , separator with " quote char
            writer = new CSVWriter(new FileWriter(csvFile));
            for (FilePathChecksumTriple triple : expectedTriples) {
                writer.writeNext(new String[] { triple.getFilePathAndName(),
                        triple.getMd5Hash() });
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            List<String[]> allEntries = reader.readAll();
            assertEquals(expectedTriples.size(), allEntries.size());
            for (int i = 0; i < expectedTriples.size(); i++) {
                FilePathChecksumTriple expectedTriple = expectedTriples.get(i);
                String[] currEntry = allEntries.get(i);
                String filePathAndName = currEntry[0];
                String md5Hash = currEntry[1];
                FilePathChecksumTriple actualTriple = new FilePathChecksumTriple(
                        md5Hash, filePathAndName);
                assertEquals(expectedTriple, actualTriple);
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private List<FilePathChecksumTriple> getExpectedTriples() {
        List<FilePathChecksumTriple> expectedTriples = new ArrayList<FilePathChecksumTriple>();
        for (String input : inputList) {
            FilePathChecksumTriple triple = FilePathChecksumTriple
                    .getInstance(input);
            expectedTriples.add(triple);
        }
        return expectedTriples;
    }

    private Map<String, FilePathChecksumTriple> getTriplesMap() {
        List<FilePathChecksumTriple> expectedTriples = getExpectedTriples();
        Map<String, FilePathChecksumTriple> filePathAndNameToTripleMap = new HashMap<String, FilePathChecksumTriple>();
        for (FilePathChecksumTriple triple : expectedTriples) {
            String key = triple.getFilePathAndName();
            filePathAndNameToTripleMap.put(key, triple);
        }
        return filePathAndNameToTripleMap;
    }

    private File getTestCsvFile() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File testHome = new File(tmpDir, "test");
        if (testHome.exists()) {
            FileUtils.deleteDirectory(testHome);
        }
        testHome.mkdirs();
        System.out.println("Created temp test dir in "
                + testHome.getAbsolutePath());
        File csvFile = new File(testHome, "test.csv");
        return csvFile;
    }
}
