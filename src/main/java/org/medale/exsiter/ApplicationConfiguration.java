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
public final class ApplicationConfiguration {

    private static final Logger LOGGER = Logger
            .getLogger(ApplicationConfiguration.class);

    public static final String DEFAULT_CONFIG_DIR = ".exsiter";

    public static final String PROP_PRIVATE_KEY_LOC = "privateKeyLocation";
    public static final String PROP_PRIVATE_KEY_PASS = "privateKeyPassphrase";
    public static final String PROP_KNOWN_HOSTS_LOC = "knownHostsLocation";
    public static final String PROP_USERNAME = "username";
    public static final String PROP_HOSTNAME = "hostname";
    public static final String PROP_BACKUP_ROOT_DIR = "backupRootDir";

    static final String[] REQUIRED_PROPERTIES = { PROP_PRIVATE_KEY_LOC,
            PROP_PRIVATE_KEY_PASS, PROP_KNOWN_HOSTS_LOC, PROP_USERNAME,
            PROP_HOSTNAME, PROP_BACKUP_ROOT_DIR };

    private final String configurationLocation;
    private Properties configurationProperties;

    /**
     * Loads application configuration from default
     * user.home/.exsiter/application.conf file.
     */
    public ApplicationConfiguration() {
        final String homeDir = System.getProperty("user.home");
        this.configurationLocation = homeDir + File.separator
                + DEFAULT_CONFIG_DIR + File.separator + "application.conf";
    }

    public ApplicationConfiguration(final String configurationLocation) {
        this.configurationLocation = configurationLocation;
    }

    /**
     * Loads config from current configurationLocation and verifies presence of
     * required props.
     * 
     * @throws IOException
     */
    public void loadConfiguration() throws IOException {
        this.configurationProperties = getConfigurationFromFile(this.configurationLocation);
        verfiyRequiredProperties(this.configurationProperties);
    }

    public String getConfigurationLocation() {
        return this.configurationLocation;
    }

    public Properties getConfiguration() {
        return this.configurationProperties;
    }

    public static File getExsiterBackupDirectory(final Properties configProps) {
        final String backupRootDirLocation = configProps
                .getProperty(ApplicationConfiguration.PROP_BACKUP_ROOT_DIR);
        final File backupRootDir = new File(backupRootDirLocation);

        final File backupDir = new File(backupRootDir,
                ExsiterConstants.EXSITER_BACKUP_DIR);
        return backupDir;
    }

    protected Properties getConfigurationFromFile(
            final String configurationLocation) throws IOException {
        final Properties properties = new Properties();
        Reader propsReader = null;
        try {
            propsReader = new FileReader(configurationLocation);
            properties.load(propsReader);
        } catch (final IOException e) {
            final String errMsg = "Unable to read application properties from "
                    + configurationLocation + " due to " + e;
            LOGGER.error(errMsg, e);
            throw new IOException(errMsg, e);
        } finally {
            IOUtils.closeQuietly(propsReader);
        }
        return properties;
    }

    protected static void verfiyRequiredProperties(final Properties properties)
            throws IOException {
        for (final String propertyKey : REQUIRED_PROPERTIES) {
            final String propertyValue = properties.getProperty(propertyKey);
            assertNotNull(propertyValue, propertyKey);
        }
    }

    protected static void assertNotNull(final String property,
            final String propertyKey) throws IOException {
        if (property == null) {
            final String explanation = "Unable to configure SSH connection due to missing property "
                    + propertyKey;
            throw new IOException(explanation);
        }
    }

}
