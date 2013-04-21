package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class MainTest {

    @Before
    public void setUp() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configuration = appConfig.getConfiguration();
        final String backupRoot = configuration
                .getProperty(ApplicationConfiguration.PROP_BACKUP_ROOT_DIR);
        final File backupRootDir = new File(backupRoot);
        if (backupRootDir.exists()) {
            FileUtils.deleteDirectory(backupRootDir);
        }
    }

    @Test
    public void testMainInit() throws IOException, ParseException,
            JSchException {

        final String[] args = { "-" + Main.CONFIG_LOCATION,
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION,
                "-" + Main.INITIALIZE };
        Main.main(args);
    }
}
