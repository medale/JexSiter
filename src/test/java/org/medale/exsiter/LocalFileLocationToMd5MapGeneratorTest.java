package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Test;

public class LocalFileLocationToMd5MapGeneratorTest {

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
