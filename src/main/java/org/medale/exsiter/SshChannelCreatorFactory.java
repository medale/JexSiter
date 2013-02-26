package org.medale.exsiter;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;

/**
 * Utility class that loads an SshChannelCreator based on properties file.
 */
public class SshChannelCreatorFactory {

    private static final Logger LOGGER = Logger.getLogger(SshChannelCreatorFactory.class);

    /**
     * Configure SshChannelCreator from ${user.home}/.exsiter/application.conf
     * default location.
     * 
     * @return SshChannelCreator
     * @throws JSchException
     */
    public static SshChannelCreator getDefaultSshChannelCreator() throws JSchException {
        String homeDir = System.getProperty("user.home");
        String configPropertiesLocation = homeDir + "/.exsiter/application.conf";
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading conf from " + configPropertiesLocation);
        }
        return SshChannelCreatorFactory.getSshChannelCreator(configPropertiesLocation);
    }

    /**
     * 
     * @param configPropertiesLocation
     * @return
     * @throws JSchException
     */
    public static SshChannelCreator getSshChannelCreator(String configPropertiesLocation) throws JSchException {
        SshChannelCreator channelCreator = new SshChannelCreator();
        channelCreator.setConfigPropertiesLocation(configPropertiesLocation);
        channelCreator.initializeSession();
        channelCreator.openSession();
        return channelCreator;
    }

}
