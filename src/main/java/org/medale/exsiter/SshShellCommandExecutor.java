package org.medale.exsiter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

/**
 * For example: LIST_ALL_FILES_AND_THEIR_CHECKSUMS
 * 
 * find . -type f | xargs md5sum
 * 
 * Sample output (128-bit md5 sum file w/ path):<br>
 * 56a329926a92460b9b6ac1377f610e48 ./web/newsletter/grip-it.jpg<br>
 * 4d6ccea2e6e506ee68b1a793c477e617 ./web/newsletter/handstand.jpg<br>
 */
public class SshShellCommandExecutor {

    private static final Logger LOGGER = Logger.getLogger(SshShellCommandExecutor.class);

    public static final String LIST_ALL_FILES_AND_THEIR_CHECKSUMS = "find . -type f | xargs md5sum";

    private SshChannelCreator channelCreator;

    public void setSshChannelCreator(SshChannelCreator channelCreator) {
        this.channelCreator = channelCreator;
    }

    /**
     * 
     * @param command
     * @return
     * @throws JSchException
     * @throws IOException
     */
    public List<String> getCommandOutput(String command) throws JSchException, IOException {
        List<String> lines = null;
        try {
            ChannelExec channelExec = channelCreator.getChannelExec();
            channelExec.setCommand(command);
            channelExec.connect();
            InputStream commandOutput = channelExec.getInputStream();
            lines = IOUtils.readLines(commandOutput);
            channelExec.disconnect();
        } catch (Exception e) {
            String errMsg = "Unable to execute command " + command + " due to " + e;
            LOGGER.error(errMsg);
            throw new JSchException(errMsg);
        }

        return lines;
    }
}
