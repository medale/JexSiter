package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class SshShellCommandExecutorTest {

    // find . -type f | xargs md5sum | wc -l
    public static final int TOTAL_TEST_FILES = 3413;

    @Test
    public void testGetFilesAndChecksums() throws JSchException, IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final SshChannelCreator channelCreator = SshChannelCreatorFactory
                .getSshChannelCreator(configProps);
        final SshShellCommandExecutor commandExecutor = new SshShellCommandExecutor();
        commandExecutor.setSshChannelCreator(channelCreator);
        final List<String> results = commandExecutor
                .getCommandOutput(SshShellCommandExecutor.LIST_ALL_FILES_AND_THEIR_CHECKSUMS);
        assertEquals(TOTAL_TEST_FILES, results.size());
        FileWriter writer = null;
        try {
            writer = new FileWriter("/tmp/test-results.txt");
            IOUtils.writeLines(results, "\n", writer);
        } finally {
            IOUtils.closeQuietly(writer);
            channelCreator.closeSession();
        }
    }

}
