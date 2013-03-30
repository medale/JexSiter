package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileLocationMd5PairTest {

    @Test(expected = IllegalArgumentException.class)
    public void testSplitWithNull() {
        final String input = null;
        FileLocationMd5Pair.splitSpaceSeparatedMd5HashAndFileLocation(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSplitWithNoSpace() {
        final String input = "md5./foo/baz.bar";
        FileLocationMd5Pair.splitSpaceSeparatedMd5HashAndFileLocation(input);
    }

    @Test
    public void testSplitSunnyDay() {
        final String[] inputs = {
                "7455657be214e67f36009154d3653e1b  ./.bash_profile",
                "56a329926a92460b9b6ac1377f610e48  ./web/newsletter/grip-it.jpg" };
        final String[][] expectedResults = {
                { "7455657be214e67f36009154d3653e1b", "./.bash_profile" },
                { "56a329926a92460b9b6ac1377f610e48",
                        "./web/newsletter/grip-it.jpg" } };
        for (int i = 0; i < expectedResults.length; i++) {
            final String[] expectedResult = expectedResults[i];
            final String input = inputs[i];
            final String[] md5HashAndFileNameWithPath = FileLocationMd5Pair
                    .splitSpaceSeparatedMd5HashAndFileLocation(input);
            final String md5Hash = md5HashAndFileNameWithPath[FileLocationMd5Pair.MD5_HASH_INDEX];
            final String fileLocation = md5HashAndFileNameWithPath[FileLocationMd5Pair.FILE_LOCATION_INDEX];
            final String expectedMd5 = expectedResult[0];
            final String expectedFile = expectedResult[1];
            assertEquals(expectedMd5, md5Hash);
            assertEquals(expectedFile, fileLocation);
        }
    }

    @Test
    public void testGetInstance() {
        final String expectedMd5Hash = "56a329926a92460b9b6ac1377f610e48";
        final String expectedLocation = "./web/newsletter/grip-it.jpg";
        final String input = expectedMd5Hash + FileLocationMd5Pair.DOUBLE_SPACE
                + expectedLocation;
        final FileLocationMd5Pair pair = FileLocationMd5Pair.getInstance(input);
        assertEquals(expectedMd5Hash, pair.getMd5Hash());
        assertEquals(expectedLocation, pair.getFileLocation());
    }
}
