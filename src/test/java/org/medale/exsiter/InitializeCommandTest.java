package org.medale.exsiter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class InitializeCommandTest {

    @Before
    public void setUp() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final String backupRootDirLocation = configProps
                .getProperty(ApplicationConfiguration.PROP_BACKUP_ROOT_DIR);
        final File backupRootDir = new File(backupRootDirLocation);
        if (backupRootDir.exists()) {
            FileUtils.deleteDirectory(backupRootDir);
        }
    }

    @Test
    public void testCreateBackupDirButThrowExceptionIfItAlreadyExists()
            throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();

        final InitializeCommand initCmd = new InitializeCommand();
        final File backupDir = initCmd
                .createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        assertTrue(backupDir.exists());
    }

    @Test(expected = IOException.class)
    public void testCreateBackupDirAlreadyExists() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final InitializeCommand initCmd = new InitializeCommand();
        initCmd.createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        initCmd.createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
    }

    @Test
    public void testCreateRemoteContentDir() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();

        final InitializeCommand initCmd = new InitializeCommand();
        final File backupDir = initCmd
                .createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        initCmd.createRemoteContentDir(backupDir);

        final File remoteContentDir = new File(backupDir,
                ExsiterConstants.REMOTE_CONTENT_DIR);
        assertTrue(remoteContentDir.exists());
    }

    @Test
    public void testInitGitRepo() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final InitializeCommand initCmd = new InitializeCommand();
        final File backupDir = initCmd
                .createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        initCmd.initGitRepo(backupDir);
        final File gitDir = new File(backupDir, ".git");
        assertTrue(gitDir.exists());
    }

}
