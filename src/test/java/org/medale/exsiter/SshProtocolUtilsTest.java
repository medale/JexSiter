package org.medale.exsiter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class SshProtocolUtilsTest {

    @Test
    public void testConvertByteCharDigitToInt() {
        final char[] inputChars = { '0', '1', '9' };
        final int[] expectedInts = { 0, 1, 9 };
        for (int i = 0; i < expectedInts.length; i++) {
            final int input = inputChars[i];
            final int expected = expectedInts[i];
            final int actual = SshProtocolUtils
                    .convertByteCharDigitToInt(input);
            assertEquals(actual, expected);
        }
    }

    @Test
    public void testCopyByteCountBytesFromInputStreamToOutputStream()
            throws IOException {
        final int bufferSize = 16;
        final String input = "Eine alte dumme Gans hat Eier.";
        final byte[] inputBytes = input
                .getBytes(ExsiterConstants.DEFAULT_ENCODING);
        final ByteArrayInputStream in = new ByteArrayInputStream(inputBytes);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        SshProtocolUtils.copyByteCountBytesFromInputStreamToOutputStream(in,
                out, inputBytes.length, bufferSize);
        final byte[] actualOutput = out.toByteArray();
        assertEquals(inputBytes.length, actualOutput.length);
        assertArrayEquals(inputBytes, actualOutput);
    }

    @Test(expected = IOException.class)
    public void testCopyByteCountBytesFromInputStreamToLocalFileNoParentDir()
            throws IOException {
        final String fullContent = "content";
        final byte[] content = fullContent
                .getBytes(ExsiterConstants.DEFAULT_ENCODING);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final String totallyBogusLocalFileLocation = "./bogus/foo/bar/baz/foo.txt";
        final long abbreviatedCount = content.length - 1;
        SshProtocolUtils.copyByteCountBytesFromInputStreamToLocalFile(in,
                totallyBogusLocalFileLocation, abbreviatedCount);
    }

    @Test
    public void testCopyByteCountBytesFromInputStreamToLocalFileSunnyDay()
            throws IOException {
        final String fullContent = "content";
        final byte[] content = fullContent
                .getBytes(ExsiterConstants.DEFAULT_ENCODING);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final File localFile = File.createTempFile("copy", ".txt");
        final String localFileLocation = localFile.getAbsolutePath();
        final long abbreviatedCount = content.length - 1;
        SshProtocolUtils.copyByteCountBytesFromInputStreamToLocalFile(in,
                localFileLocation, abbreviatedCount);
        // did not write last letter
        final String expectedContent = "conten";
        final String actualContent = FileUtils.readFileToString(localFile);
        assertEquals(expectedContent, actualContent);
        localFile.delete();
    }
}
