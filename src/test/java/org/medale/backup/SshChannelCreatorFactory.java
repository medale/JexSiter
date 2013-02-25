package org.medale.backup;

import org.junit.Ignore;

import com.jcraft.jsch.JSchException;

@Ignore
public class SshChannelCreatorFactory {

    public static SshChannelCreator getSshChannelCreator() throws JSchException {
        SshChannelCreator channelCreator = new SshChannelCreator();
        String homeDir = System.getProperty("user.home");
        String configPropertiesLocation = homeDir + "/.exsiter/application.conf";
        System.out.println("Loading conf from " + configPropertiesLocation);
        channelCreator.setConfigPropertiesLocation(configPropertiesLocation);
        channelCreator.initializeSession();
        channelCreator.openSession();
        return channelCreator;
    }

}
