package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

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
        final File remoteContentDir = new File(backupDir,
                ExsiterConstants.REMOTE_CONTENT_DIR);
        assertTrue(remoteContentDir.exists());
    }

    @Test(expected = IOException.class)
    public void testCreateBackupDirAlreadyExists() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final InitializeCommand initCmd = new InitializeCommand();
        final File backupDir = initCmd
                .createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        initCmd.createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
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

    @Test
    public void testDownloadFilenameToHashMap() throws IOException,
            JSchException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final InitializeCommand initCmd = new InitializeCommand();
        final File backupDir = initCmd
                .createBackupDirButThrowExceptionIfItAlreadyExists(configProps);
        initCmd.initGitRepo(backupDir);
        initCmd.downloadFilenameToHashMap(configProps, backupDir);
        final File filenameToHashMapFile = new File(backupDir,
                ExsiterConstants.FILENAME_TO_HASH_MAP);
        assertTrue(filenameToHashMapFile.exists());
        final Map<String, FileLocationMd5Pair> map = MapStore
                .loadMap(filenameToHashMapFile);
        assertEquals(SshShellCommandExecutorTest.TOTAL_TEST_FILES, map.size());
    }

    @Test
    public void testPerformInitialFileDownload() {

    }

}
