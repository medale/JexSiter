package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class RemoteDatabaseBackupExecutorTest {

    private static final Logger LOGGER = Logger
            .getLogger(RemoteDatabaseBackupExecutorTest.class);

    @Test
    public void testExecuteRemoteDatabaseBackup() throws IOException,
            JSchException {
        if (isMysqldRunning()) {
            final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                    ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
            appConfig.loadConfiguration();
            final Properties configProps = appConfig.getConfiguration();
            final SshChannelCreator channelCreator = SshChannelCreatorFactory
                    .getSshChannelCreatorWithOpenSession(configProps);
            final String output = RemoteDatabaseBackupExecutor
                    .executeRemoteDatabaseBackup(channelCreator);
            assertEquals(ExsiterConstants.DB_BACKUP_SUCCESS, output);
        }
    }

    private boolean isMysqldRunning() {
        boolean isRunning = false;
        String line = null;
        try {
            final Process p = Runtime.getRuntime().exec("pidof mysqld");
            final BufferedReader input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            line = input.readLine();
            if (line != null) {
                final int mysqldPid = Integer.parseInt(line);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Mysqld running with pid " + mysqldPid);
                }
                isRunning = true;
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to determine whether mysqld was running due to "
                    + e);
        }
        return isRunning;
    }

}
