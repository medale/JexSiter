package org.medale.exsiter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;

/**
 * For background see
 * https://blogs.oracle.com/janp/entry/how_the_scp_protocol_works:
 * 
 * -f = source mode: the type of the message, mode, length and filename are
 * provided in plain text, followed by a new line:
 * 
 * Cmmmm[sp] (linux 4-char mode, e.g 0644) length filename\n there might be
 * multiple protocol messages before 0 (null byte) starts actual data transfer
 * 
 * Tmtime 0 atime 0\n - if -p option is used send 0 to indicate file was read
 * 
 */
public class ScpTool {

    private static final Logger LOGGER = Logger.getLogger(ScpTool.class);

    private static final String SCP_FROM_PREFIX = "scp -f ";
    private SshChannelCreator channelCreator;
    private String filePermissions;
    private long fileSizeInBytes;
    private String fileName;

    public void setSshChannelCreator(final SshChannelCreator channelCreator) {
        this.channelCreator = channelCreator;
    }

    public boolean scpFileFrom(final String remoteFileLocation,
            final String localFileLocation) {
        boolean fileSuccessfullyCopied = false;
        try {
            // exec 'scp -f remoteLocation'
            final String scpFromCommand = SCP_FROM_PREFIX + remoteFileLocation;
            final ChannelExec execChannel = this.channelCreator
                    .getChannelExec();
            execChannel.setCommand(scpFromCommand);

            // get I/O streams for remote scp
            final OutputStream out = execChannel.getOutputStream();
            final BufferedInputStream bin = new BufferedInputStream(
                    execChannel.getInputStream());

            execChannel.connect();

            SshProtocolUtils.replyWithOK(out);
            if (nextByteIndicatesSingleFileCopyMode(bin)) {
                this.filePermissions = readFilePermissions(bin);
                this.fileSizeInBytes = readFileSizeInBytes(bin);
                this.fileName = readFileName(bin);
                SshProtocolUtils.replyWithOK(out);
                SshProtocolUtils.copyByteCountBytesFromInputStreamToLocalFile(
                        bin, localFileLocation, this.fileSizeInBytes);
                SshProtocolUtils.readExpectedByte(bin,
                        SshProtocolUtils.NULL_BYTE);
                SshProtocolUtils.replyWithOK(out);
                fileSuccessfullyCopied = true;
            }
            execChannel.disconnect();
        } catch (final Exception e) {
            final String errMsg = "Unable to copy file " + remoteFileLocation
                    + " due to " + e;
            LOGGER.error(errMsg);
            fileSuccessfullyCopied = false;
        }
        return fileSuccessfullyCopied;
    }

    public String getFilePermissions() {
        return this.filePermissions;
    }

    public long getFileSizeInBytes() {
        return this.fileSizeInBytes;
    }

    public String getFileName() {
        return this.fileName;
    }

    protected boolean nextByteIndicatesSingleFileCopyMode(
            final BufferedInputStream bin) throws IOException {
        boolean singleFileCopyMode = false;
        if (bin != null) {
            final int nextByteRead = bin.read();
            if (nextByteRead == SshProtocolUtils.SINGLE_FILE_COPY) {
                singleFileCopyMode = true;
            } else if (nextByteRead == SshProtocolUtils.WARN) {
                // remote file not found (there might be other circumstances)
                singleFileCopyMode = false;
            }
        }
        return singleFileCopyMode;
    }

    protected String readFilePermissions(final BufferedInputStream bin)
            throws IOException {
        final StringBuilder filePermissions = new StringBuilder();
        final byte[] filePerms = new byte[4];
        final int bytesRead = bin.read(filePerms);
        if (bytesRead != filePerms.length) {
            final String errMsg = "Unable to read required number of bytes for file permissions.";
            throw new IOException(errMsg);
        }
        for (final byte permByte : filePerms) {
            final int permInt = SshProtocolUtils
                    .convertByteCharDigitToInt(permByte);
            filePermissions.append(permInt);
        }
        SshProtocolUtils.readExpectedByte(bin,
                SshProtocolUtils.END_OF_FIELD_MARKER);
        return filePermissions.toString();
    }

    protected long readFileSizeInBytes(final BufferedInputStream bin)
            throws IOException {
        long fileSizeInBytes = 0L;
        boolean validFileSize = true;
        boolean fileSizeTerminatorFound = false;
        while (validFileSize && !fileSizeTerminatorFound) {
            final int byteRead = bin.read();
            if (byteRead == SshProtocolUtils.EOF) {
                validFileSize = false;
            } else if (byteRead == SshProtocolUtils.END_OF_FIELD_MARKER) {
                fileSizeTerminatorFound = true;
            } else {
                fileSizeInBytes = fileSizeInBytes * 10;
                fileSizeInBytes += SshProtocolUtils
                        .convertByteCharDigitToInt(byteRead);
            }
        }
        if (!validFileSize) {
            final String errMsg = "Unable to read file size due to premature end of input stream.";
            throw new IOException(errMsg);
        }
        return fileSizeInBytes;
    }

    private String readFileName(final BufferedInputStream bin)
            throws IOException {
        final StringBuilder fileName = new StringBuilder();
        boolean validFileName = true;
        boolean fileNameTerminatorFound = false;
        while (validFileName && !fileNameTerminatorFound) {
            final int byteRead = bin.read();
            if (byteRead == SshProtocolUtils.EOF) {
                validFileName = false;
            } else if (byteRead == SshProtocolUtils.NEWLINE) {
                fileNameTerminatorFound = true;
            } else {
                fileName.append((char) byteRead);
            }
        }
        return fileName.toString();
    }

}
