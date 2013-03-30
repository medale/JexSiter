package org.medale.exsiter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Constants/protocol/comments usually verbatim from Jan Pechanec's excellent
 * post on scp protocol at
 * https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works.
 */
public final class SshProtocolUtils {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private SshProtocolUtils() {
    }

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
    public static void replyWithOK(final OutputStream out) throws IOException {
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
    public static int convertByteCharDigitToInt(final int byteCharDigit) {
        return byteCharDigit - CONVERT_BYTE_DIGIT_TO_INT_OFFSET;
    }

    public static void readExpectedByte(final InputStream in,
            final int expectedByte) throws IOException {
        final int actualByte = in.read();
        if (actualByte != expectedByte) {
            final String errMsg = "Scp protocol expected >>" + expectedByte
                    + "<<. Actually read >>" + actualByte + "<<.";
            throw new IOException(errMsg);
        }
    }

    /**
     * Parent directories in localFileLocation must already exist or IOException
     * is thrown.
     * 
     * @param in
     * @param localFileLocation
     * @param byteCount
     * @throws IOException
     */
    public static void copyByteCountBytesFromInputStreamToLocalFile(
            final InputStream in, final String localFileLocation,
            final long byteCount) throws IOException {
        FileOutputStream fout = null;
        final File localFile = new File(localFileLocation);
        final File parentDir = localFile.getParentFile();
        if (parentDir != null) {
            // null indicates no parent - would write to current dir
            if (!parentDir.exists()) {
                final String errMsg = "Unable to write to local file "
                        + localFileLocation
                        + " because parent directory does not exist.";
                throw new IOException(errMsg);
            }
        }
        try {
            fout = new FileOutputStream(new File(localFileLocation));
            copyByteCountBytesFromInputStreamToOutputStream(in, fout,
                    byteCount, DEFAULT_BUFFER_SIZE);
        } finally {
            IOUtils.closeQuietly(fout);
        }
    }

    public static void copyByteCountBytesFromInputStreamToOutputStream(
            final InputStream in, final OutputStream out, final long byteCount,
            final int bufferSize) throws IOException {
        final byte[] byteBuffer = new byte[bufferSize];
        long remainingBytesToCopy = byteCount;
        boolean validCopy = true;
        final int offset = 0;
        while (validCopy && (remainingBytesToCopy > 0)) {
            int length = 0;
            if (remainingBytesToCopy < bufferSize) {
                length = (int) remainingBytesToCopy;
            } else {
                length = bufferSize;
            }
            final int bytesReadCount = in.read(byteBuffer, offset, length);
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
