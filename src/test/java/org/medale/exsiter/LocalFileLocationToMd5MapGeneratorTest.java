package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

public class LocalFileLocationToMd5MapGeneratorTest {

    // as user executing this test (i.e. readable files only)
    // cd /home/test
    // find . -type f | xargs md5sum | wc -l
    private static final int FILE_COUNT = 3423;

    @Test
    public void testGetAllReadableFilesFromStartDirectory() throws IOException {
        final String startDirectoryLocation = "/home/test/";
        final Collection<File> files = LocalFileLocationToMd5MapGenerator
                .getAllReadableFilesFromStartDirectory(startDirectoryLocation);
        assertTrue(files.size() > 0);
        int fileCount = 0;
        for (final Iterator<File> iterator = files.iterator(); iterator
                .hasNext();) {
            final File file = iterator.next();
            if (file.canRead()) {
                assertTrue(file.exists() && file.isFile());
                fileCount++;
            }
        }
        assertEquals(FILE_COUNT, fileCount);
    }

    @Test
    public void testGetFileLocationRelativeToStartDirectoryLocation() {
        final String startDirectoryLocation = "/home/test/";
        final String[] testInputs = { startDirectoryLocation + ".bashrc",
                startDirectoryLocation + "web/newsletter/grip-it.jpg" };
        final String[] exprectedOutputs = { "./.bashrc",
                "./web/newsletter/grip-it.jpg" };
        for (int i = 0; i < exprectedOutputs.length; i++) {
            final String absoluteFileLocation = testInputs[i];
            final String expectedOutput = exprectedOutputs[i];
            final String actualOutput = LocalFileLocationToMd5MapGenerator
                    .getFileLocationRelativeToStartDirectoryLocation(
                            startDirectoryLocation, absoluteFileLocation);
            assertEquals(expectedOutput, actualOutput);
        }
    }

    @Test
    public void testGetFileLocationRelativeToStartDirectoryLocationWithNonstandardStart() {
        final String startDirectoryLocationWithNoEndingForwardSlash = "/home/test";
        final String absoluteFileLocation = "/home/test/web/newsletter/grip-it.jpg";
        final String expectedOutput = "./web/newsletter/grip-it.jpg";
        final String actualOutput = LocalFileLocationToMd5MapGenerator
                .getFileLocationRelativeToStartDirectoryLocation(
                        startDirectoryLocationWithNoEndingForwardSlash,
                        absoluteFileLocation);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testGetMd5Hash() throws Exception {
        final String[] fileNames = { "/home/test/web/newsletterArt/grip.jpg",
                "/home/test/web/newsletterArt/EAGLE.jpg" };
        final String[] expectedHashes = { "56a329926a92460b9b6ac1377f610e47",
                "2a3e4fd62f085573625216a9c247c0f1" };
        for (int i = 0; i < expectedHashes.length; i++) {
            final String expectedHash = expectedHashes[i];
            final String inputFile = fileNames[i];
            final File file = new File(inputFile);
            final String md5Hash = LocalFileLocationToMd5MapGenerator
                    .getMd5Hash(file);
            assertEquals(md5Hash, expectedHash);
        }
    }

    @Test
    public void testGetFileLocationMd5Pairs() throws Exception {
        final String[] fileNames = { "./web/newsletterArt/grip.jpg",
                "./web/newsletterArt/EAGLE.jpg" };
        final String[] expectedHashes = { "56a329926a92460b9b6ac1377f610e47",
                "2a3e4fd62f085573625216a9c247c0f1" };
        final String startDirectoryLocation = "/home/test/";
        final Map<String, String> map = LocalFileLocationToMd5MapGenerator
                .getFileLocationToMd5Map(startDirectoryLocation);
        assertTrue(map.size() > 0);
        for (int i = 0; i < expectedHashes.length; i++) {
            final String fileName = fileNames[i];
            final String expectedHash = expectedHashes[i];
            assertTrue(map.containsKey(fileName));
            assertEquals(expectedHash, map.get(fileName));
        }
    }
}
