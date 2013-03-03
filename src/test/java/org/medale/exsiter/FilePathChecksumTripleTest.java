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
        String input = "56a329926a92460b9b6ac1377f610e48 ./web/newsletter/grip-it.jpg";
        String[] md5HashAndFileNameWithPath = FilePathChecksumTriple
                .splitSpaceSeparatedMd5HashAndFileNameWithPath(input);
        String md5Hash = md5HashAndFileNameWithPath[FilePathChecksumTriple.MD5_HASH_INDEX];
        String fileNameWithPath = md5HashAndFileNameWithPath[FilePathChecksumTriple.FILE_NAME_AND_PATH_INDEX];
        assertEquals("56a329926a92460b9b6ac1377f610e48", md5Hash);
        assertEquals("./web/newsletter/grip-it.jpg", fileNameWithPath);
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
        String input = expectedMd5Hash + " " + expectedPath + expectedFileName;
        FilePathChecksumTriple triple = FilePathChecksumTriple
                .getInstance(input);
        assertEquals(expectedMd5Hash, triple.getMd5Hash());
        assertEquals(expectedPath, triple.getFilePath());
        assertEquals(expectedFileName, triple.getFileName());
    }
}
