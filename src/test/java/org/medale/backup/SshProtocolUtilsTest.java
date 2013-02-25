package org.medale.backup;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class SshProtocolUtilsTest {

    @Test
    public void testConvertByteCharDigitToInt() {
        char[] inputChars = { '0', '1', '9' };
        int[] expectedInts = { 0, 1, 9 };
        for (int i = 0; i < expectedInts.length; i++) {
            int input = inputChars[i];
            int expected = expectedInts[i];
            int actual = SshProtocolUtils.convertByteCharDigitToInt(input);
            assertEquals(actual, expected);
        }
    }

    @Test
    public void testCopyByteCountBytesFromInputStreamToOutputStream() throws IOException {
        int bufferSize = 16;
        String input = "Eine alte dumme Gans hat Eier.";
        byte[] inputBytes = input.getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(inputBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SshProtocolUtils.copyByteCountBytesFromInputStreamToOutputStream(in, out, inputBytes.length, bufferSize);
        byte[] actualOutput = out.toByteArray();
        assertEquals(inputBytes.length, actualOutput.length);
        assertArrayEquals(inputBytes, actualOutput);
    }

}
