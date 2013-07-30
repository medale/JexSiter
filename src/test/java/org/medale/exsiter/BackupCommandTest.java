package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class BackupCommandTest {

    @Test
    public void testGetRemoteFileNameToMd5Map() throws IOException,
            JSchException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);

        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final BackupCommand backupCmd = new BackupCommand();
        final File backupDir = ApplicationConfiguration
                .getExsiterBackupDirectory(configProps);

        final Map<String, String> remoteMap = backupCmd
                .getRemoteFileNameToMd5Map(configProps, backupDir);
        assertEquals(SshShellCommandExecutorTest.TOTAL_TEST_FILES,
                remoteMap.size());
    }

    @Test
    public void testExecute() throws Exception {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final File backupDirectory = ApplicationConfiguration
                .getExsiterBackupDirectory(configProps);

        if (backupDirectory.exists()) {
            FileUtils.deleteDirectory(backupDirectory);
            final InitializeCommand initCmd = new InitializeCommand();
            initCmd.execute(appConfig);
        }

        final BackupCommand backupCmd = new BackupCommand();
        backupCmd.execute(configProps);

    }
}
