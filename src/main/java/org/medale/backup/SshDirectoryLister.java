package org.medale.backup;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

/**
 * %M file permissions %s file size in bytes %T@ last mod time %p file name
 * 
 * find . -type f -printf '%M|%s|%T@|%p\n'
 */
public class SshDirectoryLister {

    private static final Logger LOGGER = Logger.getLogger(SshDirectoryLister.class);

    private SshChannelCreator channelCreator;

    public void setSshChannelCreator(SshChannelCreator channelCreator) {
        this.channelCreator = channelCreator;
    }

    /**
     * 
     * </pre>
     * 
     * @param relativeDir
     * @return
     * @throws JSchException
     * @throws IOException
     */
    public String getListing(String relativeDir) throws JSchException, IOException {
        StringBuilder builder = new StringBuilder();

        try {
            ChannelExec channelExec = channelCreator.getChannelExec();
            channelExec.setCommand("find . -type f -printf '%M|%s|%T@|%p\n'");
            // channelExec.setCommand("cd ~/" + relativeDir + " ; ls -la");
            channelExec.connect();
            InputStream commandOutput = channelExec.getInputStream();
            int readByte = commandOutput.read();

            while (readByte != -1) {
                builder.append((char) readByte);
                readByte = commandOutput.read();
            }

            channelExec.disconnect();
        } catch (Exception e) {
            String errMsg = "Unable to get directory listing for " + relativeDir + " due to " + e;
            LOGGER.error(errMsg);
            throw new JSchException(errMsg);
        }

        return builder.toString();
    }
}
