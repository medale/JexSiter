package org.medale.backup;

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

    public void setSshChannelCreator(SshChannelCreator channelCreator) {
        this.channelCreator = channelCreator;
    }

    public boolean scpFileFrom(String remoteFileLocation, String localFileLocation) {
        boolean fileSuccessfullyCopied = false;
        try {
            // exec 'scp -f remoteLocation'
            String scpFromCommand = SCP_FROM_PREFIX + remoteFileLocation;
            ChannelExec execChannel = channelCreator.getChannelExec();
            execChannel.setCommand(scpFromCommand);

            // get I/O streams for remote scp
            OutputStream out = execChannel.getOutputStream();
            BufferedInputStream bin = new BufferedInputStream(execChannel.getInputStream());

            execChannel.connect();

            SshProtocolUtils.replyWithOK(out);
            if (nextByteIndicatesSingleFileCopyMode(bin)) {
                filePermissions = readFilePermissions(bin);
                fileSizeInBytes = readFileSizeInBytes(bin);
                fileName = readFileName(bin);
                SshProtocolUtils.replyWithOK(out);
                SshProtocolUtils.copyByteCountBytesFromInputStreamToLocalFile(bin, localFileLocation, fileSizeInBytes);
                SshProtocolUtils.readExpectedByte(bin, SshProtocolUtils.NULL_BYTE);
                SshProtocolUtils.replyWithOK(out);
                fileSuccessfullyCopied = true;
            }
            execChannel.disconnect();
        } catch (Exception e) {
            String errMsg = "Unable to copy file " + remoteFileLocation + " due to " + e;
            LOGGER.error(errMsg);
            fileSuccessfullyCopied = false;
        }
        return fileSuccessfullyCopied;
    }

    public String getFilePermissions() {
        return filePermissions;
    }

    public long getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    public String getFileName() {
        return fileName;
    }

    protected boolean nextByteIndicatesSingleFileCopyMode(BufferedInputStream bin) throws IOException {
        boolean singleFileCopyMode = false;
        if (bin != null) {
            int nextByteRead = bin.read();
            if (nextByteRead == SshProtocolUtils.SINGLE_FILE_COPY) {
                singleFileCopyMode = true;
            } else if (nextByteRead == SshProtocolUtils.WARN) {
                // remote file not found (there might be other circumstances)
                singleFileCopyMode = false;
            }
        }
        return singleFileCopyMode;
    }

    protected String readFilePermissions(BufferedInputStream bin) throws IOException {
        StringBuilder filePermissions = new StringBuilder();
        byte[] filePerms = new byte[4];
        bin.read(filePerms);
        for (byte permByte : filePerms) {
            int permInt = SshProtocolUtils.convertByteCharDigitToInt(permByte);
            filePermissions.append(permInt);
        }
        SshProtocolUtils.readExpectedByte(bin, SshProtocolUtils.END_OF_FIELD_MARKER);
        return filePermissions.toString();
    }

    protected long readFileSizeInBytes(BufferedInputStream bin) throws IOException {
        long fileSizeInBytes = 0L;
        boolean validFileSize = true;
        boolean fileSizeTerminatorFound = false;
        while (validFileSize && !fileSizeTerminatorFound) {
            int byteRead = bin.read();
            if (byteRead == SshProtocolUtils.EOF) {
                validFileSize = false;
            } else if (byteRead == SshProtocolUtils.END_OF_FIELD_MARKER) {
                fileSizeTerminatorFound = true;
            } else {
                fileSizeInBytes = fileSizeInBytes * 10;
                fileSizeInBytes += SshProtocolUtils.convertByteCharDigitToInt(byteRead);
            }
        }
        if (!validFileSize) {
            String errMsg = "Unable to read file size due to premature end of input stream.";
            throw new IOException(errMsg);
        }
        return fileSizeInBytes;
    }

    private String readFileName(BufferedInputStream bin) throws IOException {
        StringBuilder fileName = new StringBuilder();
        boolean validFileName = true;
        boolean fileNameTerminatorFound = false;
        while (validFileName && !fileNameTerminatorFound) {
            int byteRead = bin.read();
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
