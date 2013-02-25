package org.medale.backup;

import java.io.InputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

public class Shell {
    public static void main(String[] arg) throws JSchException {

        SshChannelCreator channelCreator = SshChannelCreatorFactory.getSshChannelCreator();

        StringBuilder outputBuffer = new StringBuilder();

        try {
            ChannelExec channelExec = channelCreator.getChannelExec();
            channelExec.setCommand("ls -la");
            channelExec.connect();
            InputStream commandOutput = channelExec.getInputStream();
            int readByte = commandOutput.read();

            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = commandOutput.read();
            }

            channelExec.disconnect();
            System.out.println(outputBuffer.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
        channelCreator.closeSession();
    }

}