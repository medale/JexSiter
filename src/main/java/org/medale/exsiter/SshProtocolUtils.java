package org.medale.exsiter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Constants/protocol/comments usually verbatim from Jan Pechanec's excellent
 * post on scp protocol at
 * https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works
 */
public class SshProtocolUtils {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * 
     * Cmmmm length filename a single file copy, mmmmm is mode. Example: C0644
     * 299 myFile.txt
     */
    public static final char SINGLE_FILE_COPY = 'C';
    public static final char END_OF_FIELD_MARKER = ' ';
    /**
     * Offset used to convert a byte character representing a digit into an
     * integer.
     */
    public static final char CONVERT_BYTE_DIGIT_TO_INT_OFFSET = '0';

    public static final int OK = 0;
    public static final int WARN = 1;
    public static final int FATAL = 2;
    public static final int EOF = -1;

    public static final byte NULL_BYTE = 0;

    public static final char NEWLINE = '\n';

    /**
     * Sends OK (0/null byte) to output.
     * 
     * @param out
     * @throws IOException
     */
    public static void replyWithOK(OutputStream out) throws IOException {
        out.write(OK);
        out.flush();
    }

    /**
     * Scp protocol uses ascii char representations of digits as bytes to send
     * for example file permissions and file length. This converts one of those
     * byte char digits into an integer.
     * 
     * @param byteCharDigit
     * @return
     */
    public static int convertByteCharDigitToInt(int byteCharDigit) {
        return byteCharDigit - CONVERT_BYTE_DIGIT_TO_INT_OFFSET;
    }

    public static void readExpectedByte(InputStream in, int expectedByte) throws IOException {
        int actualByte = in.read();
        if (actualByte != expectedByte) {
            String errMsg = "Scp protocol expected >>" + expectedByte + "<<. Actually read >>" + actualByte + "<<.";
            throw new IOException(errMsg);
        }
    }

    public static void copyByteCountBytesFromInputStreamToLocalFile(BufferedInputStream bin, String localFileLocation,
            long byteCount) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(localFileLocation));
            copyByteCountBytesFromInputStreamToOutputStream(bin, fos, byteCount, DEFAULT_BUFFER_SIZE);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    public static void copyByteCountBytesFromInputStreamToOutputStream(InputStream in, OutputStream out,
            long byteCount, int bufferSize) throws IOException {
        byte[] byteBuffer = new byte[bufferSize];
        long remainingBytesToCopy = byteCount;
        boolean validCopy = true;
        int offset = 0;
        while (validCopy && (remainingBytesToCopy > 0)) {
            int length = 0;
            if (remainingBytesToCopy < bufferSize) {
                length = (int) remainingBytesToCopy;
            } else {
                length = bufferSize;
            }
            int bytesReadCount = in.read(byteBuffer, offset, length);
            if (bytesReadCount == SshProtocolUtils.EOF) {
                // file should be terminated with null byte not EOF!
                validCopy = false;
            } else {
                remainingBytesToCopy -= bytesReadCount;
                out.write(byteBuffer, offset, bytesReadCount);
            }
        }
    }

}
