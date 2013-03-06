package org.medale.exsiter;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

public class ApplicationConfigurationTest {

    // Note: Test requires presence of user.home/.exsiter/application.conf with
    // all required properties
    @Test
    public void testLoadConfiguration() throws IOException {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.loadConfiguration();
        Properties configProps = appConfig.getConfiguration();
        String property = configProps
                .getProperty(ApplicationConfiguration.PROP_BACKUP_ROOT_DIR);
        assertTrue(property.endsWith("exsiter"));
    }
}
