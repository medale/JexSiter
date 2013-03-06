package org.medale.exsiter;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshChannelCreator {

    private static final Logger LOGGER = Logger
            .getLogger(SshChannelCreator.class);

    private static final String EXEC_CHANNEL_TYPE = "exec";
    private static final String SHELL_CHANNEL_TYPE = "shell";

    private Properties configProperties;
    private Session session;

    public void setConfigurationProperties(Properties configProperties) {
        this.configProperties = configProperties;
    }

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

    protected Session getSessionForCurrentConfigurationSettings()
            throws JSchException {
        Session session = null;
        JSch jsch = new JSch();
        String privateKeyLocation = configProperties
                .getProperty(ApplicationConfiguration.PROP_PRIVATE_KEY_LOC);
        String privateKeyPassphrase = configProperties
                .getProperty(ApplicationConfiguration.PROP_PRIVATE_KEY_PASS);
        jsch.addIdentity(privateKeyLocation, privateKeyPassphrase);
        String knownHostsLocation = configProperties
                .getProperty(ApplicationConfiguration.PROP_KNOWN_HOSTS_LOC);
        jsch.setKnownHosts(knownHostsLocation);
        String username = configProperties
                .getProperty(ApplicationConfiguration.PROP_USERNAME);
        String hostname = configProperties
                .getProperty(ApplicationConfiguration.PROP_HOSTNAME);
        session = jsch.getSession(username, hostname);
        return session;
    }
}
