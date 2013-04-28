package org.medale.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ExsiterFileUtilsTest {

    // as user executing this test (i.e. readable files only)
    // cd /home/test
    // find . -type f | xargs md5sum | wc -l
    public static final int FILE_COUNT = 3423;

    @Test
    public void testDeleteFileAndEmptyParentDirs() throws IOException {
        final File tempDir = Files.createTempDir();
        final File stopDirectory = new File(tempDir, "foo");
        final File barParent = new File(stopDirectory, "bar");
        final File bazParent = new File(barParent, "baz");
        final File fileToDelete = new File(bazParent, "baz.txt");

        Files.createParentDirs(fileToDelete);
        Files.write(fileToDelete.getCanonicalPath(), fileToDelete,
                Charsets.UTF_8);

        // before
        assertTrue(fileToDelete.exists());
        assertTrue(bazParent.exists());
        assertTrue(barParent.exists());
        assertTrue(stopDirectory.exists());

        ExsiterFileUtils.deleteFileAndEmptyParentDirectoriesUntilStopDirectory(
                fileToDelete, stopDirectory);

        // after
        assertFalse(fileToDelete.exists());
        assertFalse(bazParent.exists());
        assertFalse(barParent.exists());
        assertTrue(stopDirectory.exists());

        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testGetAllReadableFilesFromStartDirectory() throws IOException {
        final String startDirectoryLocation = "/home/test/";
        final Collection<File> files = ExsiterFileUtils
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
            final String actualOutput = ExsiterFileUtils
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
        final String actualOutput = ExsiterFileUtils
                .getFileLocationRelativeToStartDirectoryLocation(
                        startDirectoryLocationWithNoEndingForwardSlash,
                        absoluteFileLocation);
        assertEquals(expectedOutput, actualOutput);
    }

}
