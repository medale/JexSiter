package org.medale.exsiter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

@Ignore
public class SshShellCommandExecutorTest {

    @Test
    public void test() throws JSchException, IOException {
        SshChannelCreator channelCreator = SshChannelCreatorFactory.getDefaultSshChannelCreator();
        SshShellCommandExecutor commandExecutor = new SshShellCommandExecutor();
        commandExecutor.setSshChannelCreator(channelCreator);
        List<String> results = commandExecutor
                .getCommandOutput(SshShellCommandExecutor.LIST_ALL_FILES_AND_THEIR_CHECKSUMS);
        FileWriter writer = null;
        try {
            writer = new FileWriter("/tmp/results.txt");
            IOUtils.writeLines(results, "\n", writer);
        } finally {
            IOUtils.closeQuietly(writer);
            channelCreator.closeSession();
        }
    }

}
