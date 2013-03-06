package org.medale.exsiter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Loads application configuration from default
 * user.home/.exsiter/application.conf file and verifies that required
 * properties are set.
 */
public class ApplicationConfiguration {

    private static final Logger LOGGER = Logger
            .getLogger(ApplicationConfiguration.class);

    public static final String PROP_PRIVATE_KEY_LOC = "privateKeyLocation";
    public static final String PROP_PRIVATE_KEY_PASS = "privateKeyPassphrase";
    public static final String PROP_KNOWN_HOSTS_LOC = "knownHostsLocation";
    public static final String PROP_USERNAME = "username";
    public static final String PROP_HOSTNAME = "hostname";
    public static final String PROP_BACKUP_ROOT_DIR = "backupRootDir";

    public static final String[] REQUIRED_PROPERTIES = { PROP_PRIVATE_KEY_LOC,
            PROP_PRIVATE_KEY_PASS, PROP_KNOWN_HOSTS_LOC, PROP_USERNAME,
            PROP_HOSTNAME, PROP_BACKUP_ROOT_DIR };

    private String configurationLocation;
    private Properties configurationProperties;

    public ApplicationConfiguration() {
        String homeDir = System.getProperty("user.home");
        configurationLocation = homeDir + File.separator + ".exsiter"
                + File.separator + "application.conf";
    }

    public void loadConfiguration() throws IOException {
        this.configurationProperties = getConfigurationFromFile(configurationLocation);
        verfiyRequiredProperties(configurationProperties);
    }

    public Properties getConfiguration() {
        return configurationProperties;
    }

    protected Properties getConfigurationFromFile(String configurationLocation)
            throws IOException {
        Properties properties = new Properties();
        Reader propsReader = null;
        try {
            propsReader = new FileReader(configurationLocation);
            properties.load(propsReader);
        } catch (IOException e) {
            String errMsg = "Unable to read application properties from "
                    + configurationLocation + " due to " + e;
            LOGGER.error(errMsg, e);
            throw new IOException(errMsg, e);
        } finally {
            IOUtils.closeQuietly(propsReader);
        }
        return properties;
    }

    protected static void verfiyRequiredProperties(Properties properties)
            throws IOException {
        for (String propertyKey : REQUIRED_PROPERTIES) {
            String propertyValue = properties.getProperty(propertyKey);
            assertNotNull(propertyValue, propertyKey);
        }
    }

    protected static void assertNotNull(String property, String propertyKey)
            throws IOException {
        if (property == null) {
            String explanation = "Unable to configure SSH connection due to missing property "
                    + propertyKey;
            throw new IOException(explanation);
        }
    }

}
