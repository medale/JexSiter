package org.medale.exsiter;

import java.io.IOException;
import java.util.Properties;

import com.jcraft.jsch.JSchException;

/**
 * Utility class that loads an SshChannelCreator based on application
 * configuration.
 */
public class SshChannelCreatorFactory {

    /**
     * Configure SshChannelCreator from ${user.home}/.exsiter/application.conf
     * default location.
     * 
     * @return SshChannelCreator
     * @throws JSchException
     * @throws IOException
     */
    public static SshChannelCreator getDefaultSshChannelCreator()
            throws JSchException, IOException {
        ApplicationConfiguration appConfig = new ApplicationConfiguration();
        appConfig.loadConfiguration();
        Properties configProps = appConfig.getConfiguration();
        return SshChannelCreatorFactory.getSshChannelCreatorWithOpenSession(configProps);
    }

    /**
     * 
     * @param configPropertiesLocation
     * @return
     * @throws JSchException
     */
    public static SshChannelCreator getSshChannelCreatorWithOpenSession(Properties configProps)
            throws JSchException {
        SshChannelCreator channelCreator = new SshChannelCreator();
        channelCreator.setConfigurationProperties(configProps);
        channelCreator.initializeSession();
        channelCreator.openSession();
        return channelCreator;
    }

}
