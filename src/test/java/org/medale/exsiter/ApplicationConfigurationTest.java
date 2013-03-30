package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

/**
 * Needs presence of $HOME_DIR/.exsiter/test/test-application.conf with
 * ApplicationConfiguration.PROP_BACKUP_ROOT_DIR = $HOME_DIR/exsiter-test
 */
public class ApplicationConfigurationTest {

    public static final String HOME_DIR = System.getProperty("user.home");

    private static final StringBuilder testConfigLocationBuilder = new StringBuilder();

    static {
        final String[] locationComponents = { HOME_DIR, File.separator,
                ApplicationConfiguration.DEFAULT_CONFIG_DIR, File.separator,
                "test", File.separator, "test-application.conf" };
        for (final String locationComponent : locationComponents) {
            testConfigLocationBuilder.append(locationComponent);
        }
    }

    public static final String TEST_CONFIG_LOCATION = testConfigLocationBuilder
            .toString();

    /**
     * Needs presence of $HOME_DIR/.exsiter/test/test-application.conf with
     * ApplicationConfiguration.PROP_BACKUP_ROOT_DIR = $HOME_DIR/exsiter-test
     */
    @Test
    public void testLoadConfiguration() throws IOException {

        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final String propertyVal = configProps
                .getProperty(ApplicationConfiguration.PROP_BACKUP_ROOT_DIR);
        final String expectedPropVal = HOME_DIR + File.separator
                + "exsiter-test";
        assertEquals(expectedPropVal, propertyVal);
    }
}
