package org.medale.backup;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshChannelCreator {

    private static final Logger LOGGER = Logger.getLogger(SshChannelCreator.class);

    public static final String PROP_PRIVATE_KEY_LOC = "privateKeyLocation";
    public static final String PROP_PRIVATE_KEY_PASS = "privateKeyPassphrase";
    public static final String PROP_KNOWN_HOSTS_LOC = "knownHostsLocation";
    public static final String PROP_USERNAME = "username";
    public static final String PROP_HOSTNAME = "hostname";

    private static final String[] REQUIRED_PROPERTIES = { PROP_PRIVATE_KEY_LOC, PROP_PRIVATE_KEY_PASS,
            PROP_KNOWN_HOSTS_LOC, PROP_USERNAME, PROP_HOSTNAME };

    private static final String EXEC_CHANNEL_TYPE = "exec";
    private static final String SHELL_CHANNEL_TYPE = "shell";

    private Properties configProperties;
    private String configPropertiesLocation;
    private Session session;

    public void initializeSession() throws JSchException {
        session = getSessionForCurrentConfigurationSettings();
    }

    public void openSession() throws JSchException {
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
            session.connect();
        } else {
            String errMsg = "Session must be initialized before opening.";
            LOGGER.error(errMsg);
            throw new JSchException(errMsg);
        }
    }

    public ChannelExec getChannelExec() throws JSchException {
        ChannelExec execChannel = (ChannelExec) getChannel(EXEC_CHANNEL_TYPE);
        return execChannel;
    }

    public ChannelShell getChannelShell() throws JSchException {
        ChannelShell shellChannel = (ChannelShell) getChannel(SHELL_CHANNEL_TYPE);
        return shellChannel;
    }

    protected Channel getChannel(String channelType) throws JSchException {
        Channel channel = null;
        if (isOpenSession()) {
            channel = session.openChannel(channelType);
        } else {
            String errMsg = "JSch session must be open to get channel.";
            LOGGER.error(errMsg);
            throw new JSchException(errMsg);
        }
        return channel;
    }

    public void closeSession() {
        if (isOpenSession()) {
            session.disconnect();
        }
    }

    public boolean isOpenSession() {
        return session != null && session.isConnected();
    }

    protected Session getSessionForCurrentConfigurationSettings() throws JSchException {
        configProperties = initializeSettingsFromConfigFile();
        Session session = null;
        JSch jsch = new JSch();
        String privateKeyLocation = configProperties.getProperty(PROP_PRIVATE_KEY_LOC);
        String privateKeyPassphrase = configProperties.getProperty(PROP_PRIVATE_KEY_PASS);
        jsch.addIdentity(privateKeyLocation, privateKeyPassphrase);
        String knownHostsLocation = configProperties.getProperty(PROP_KNOWN_HOSTS_LOC);
        jsch.setKnownHosts(knownHostsLocation);
        String username = configProperties.getProperty(PROP_USERNAME);
        String hostname = configProperties.getProperty(PROP_HOSTNAME);
        session = jsch.getSession(username, hostname);
        return session;
    }

    protected Properties initializeSettingsFromConfigFile() {
        Properties properties = null;
        if (configPropertiesLocation != null) {
            properties = new Properties();
            Reader propsReader = null;
            try {
                propsReader = new FileReader(configPropertiesLocation);
                properties.load(propsReader);
                verfiyRequiredProperties(properties);

            } catch (IOException e) {
                String errMsg = "Unable to read ssh properties from " + configPropertiesLocation + " due to " + e;
                LOGGER.error(errMsg, e);
            } finally {
                IOUtils.closeQuietly(propsReader);
            }
        }
        return properties;
    }

    private void verfiyRequiredProperties(Properties properties) throws IOException {
        for (String propertyKey : REQUIRED_PROPERTIES) {
            String propertyValue = properties.getProperty(propertyKey);
            assertNotNull(propertyValue, propertyKey);
        }
    }

    protected void assertNotNull(String property, String propertyKey) throws IOException {
        if (property == null) {
            String explanation = "Unable to configure SSH connection due to missing property " + propertyKey;
            throw new IOException(explanation);
        }
    }

    public void setConfigPropertiesLocation(String configPropertiesLocation) {
        this.configPropertiesLocation = configPropertiesLocation;
    }
}
