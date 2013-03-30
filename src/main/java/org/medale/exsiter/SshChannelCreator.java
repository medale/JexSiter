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

    public void setConfigurationProperties(final Properties configProperties) {
        this.configProperties = configProperties;
    }

    public void initializeSession() throws JSchException {
        this.session = getSessionForCurrentConfigurationSettings();
    }

    public void openSession() throws JSchException {
        if (this.session != null) {
            if (this.session.isConnected()) {
                this.session.disconnect();
            }
            this.session.connect();
        } else {
            final String errMsg = "Session must be initialized before opening.";
            LOGGER.error(errMsg);
            throw new JSchException(errMsg);
        }
    }

    public ChannelExec getChannelExec() throws JSchException {
        final ChannelExec execChannel = (ChannelExec) getChannel(EXEC_CHANNEL_TYPE);
        return execChannel;
    }

    public ChannelShell getChannelShell() throws JSchException {
        final ChannelShell shellChannel = (ChannelShell) getChannel(SHELL_CHANNEL_TYPE);
        return shellChannel;
    }

    protected Channel getChannel(final String channelType) throws JSchException {
        Channel channel = null;
        if (isOpenSession()) {
            channel = this.session.openChannel(channelType);
        } else {
            final String errMsg = "JSch session must be open to get channel.";
            LOGGER.error(errMsg);
            throw new JSchException(errMsg);
        }
        return channel;
    }

    public void closeSession() {
        if (isOpenSession()) {
            this.session.disconnect();
        }
    }

    public boolean isOpenSession() {
        return (this.session != null) && this.session.isConnected();
    }

    protected Session getSessionForCurrentConfigurationSettings()
            throws JSchException {
        Session session = null;
        final JSch jsch = new JSch();
        final String privateKeyLocation = this.configProperties
                .getProperty(ApplicationConfiguration.PROP_PRIVATE_KEY_LOC);
        final String privateKeyPassphrase = this.configProperties
                .getProperty(ApplicationConfiguration.PROP_PRIVATE_KEY_PASS);
        jsch.addIdentity(privateKeyLocation, privateKeyPassphrase);
        final String knownHostsLocation = this.configProperties
                .getProperty(ApplicationConfiguration.PROP_KNOWN_HOSTS_LOC);
        jsch.setKnownHosts(knownHostsLocation);
        final String username = this.configProperties
                .getProperty(ApplicationConfiguration.PROP_USERNAME);
        final String hostname = this.configProperties
                .getProperty(ApplicationConfiguration.PROP_HOSTNAME);
        session = jsch.getSession(username, hostname);
        return session;
    }
}
