package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FilePathChecksumTripleTest {

    @Test(expected = IllegalArgumentException.class)
    public void testSplitWithNull() {
        String input = null;
        FilePathChecksumTriple
                .splitSpaceSeparatedMd5HashAndFileNameWithPath(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSplitWithNoSpace() {
        String input = "md5./foo/baz.bar";
        FilePathChecksumTriple
                .splitSpaceSeparatedMd5HashAndFileNameWithPath(input);
    }

    @Test
    public void testSplitSunnyDay() {
        String[] inputs = {
                "7455657be214e67f36009154d3653e1b  ./.bash_profile",
                "56a329926a92460b9b6ac1377f610e48  ./web/newsletter/grip-it.jpg" };
        String[][] expectedResults = {
                { "7455657be214e67f36009154d3653e1b", "./.bash_profile" },
                { "56a329926a92460b9b6ac1377f610e48",
                        "./web/newsletter/grip-it.jpg" } };
        for (int i = 0; i < expectedResults.length; i++) {
            String[] expectedResult = expectedResults[i];
            String input = inputs[i];
            String[] md5HashAndFileNameWithPath = FilePathChecksumTriple
                    .splitSpaceSeparatedMd5HashAndFileNameWithPath(input);
            String md5Hash = md5HashAndFileNameWithPath[FilePathChecksumTriple.MD5_HASH_INDEX];
            String fileNameWithPath = md5HashAndFileNameWithPath[FilePathChecksumTriple.FILE_NAME_AND_PATH_INDEX];
            String expectedMd5 = expectedResult[0];
            String expectedFile = expectedResult[1];
            assertEquals(expectedMd5, md5Hash);
            assertEquals(expectedFile, fileNameWithPath);
        }
    }

    @Test
    public void testGetFileNameAndFilePathSunnyDay() {
        // even root entries look like this: ./bogus.php
        String path = "./web/newsletter/";
        String fileName = "grip-it.jpg";
        String fileNameWithPath = path + fileName;
        String[] results = FilePathChecksumTriple
                .geFilePathAndFileName(fileNameWithPath);
        assertEquals(fileName, results[FilePathChecksumTriple.FILE_NAME_INDEX]);
        assertEquals(path, results[FilePathChecksumTriple.FILE_PATH_INDEX]);
    }

    @Test
    public void testGetFileNameAndFilePathEdgeCases() {
        String[][] pathFileNamePairs = { { "./", "bogus.xml" },
                { "", "hello.txt" }, { "./foo/bar/", "" } };
        for (int i = 0; i < pathFileNamePairs.length; i++) {
            String[] pair = pathFileNamePairs[i];
            String path = pair[0];
            String fileName = pair[1];
            String fileNameWithPath = path + fileName;
            String[] results = FilePathChecksumTriple
                    .geFilePathAndFileName(fileNameWithPath);
            assertEquals(fileName,
                    results[FilePathChecksumTriple.FILE_NAME_INDEX]);
            assertEquals(path, results[FilePathChecksumTriple.FILE_PATH_INDEX]);
        }
    }

    @Test
    public void testGetInstance() {
        String expectedMd5Hash = "56a329926a92460b9b6ac1377f610e48";
        String expectedPath = "./web/newsletter/";
        String expectedFileName = "grip-it.jpg";
        String input = expectedMd5Hash + FilePathChecksumTriple.DOUBLE_SPACE
                + expectedPath + expectedFileName;
        FilePathChecksumTriple triple = FilePathChecksumTriple
                .getInstance(input);
        assertEquals(expectedMd5Hash, triple.getMd5Hash());
        assertEquals(expectedPath, triple.getFilePath());
        assertEquals(expectedFileName, triple.getFileName());
    }
}
