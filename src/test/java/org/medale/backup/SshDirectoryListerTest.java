package org.medale.backup;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class SshDirectoryListerTest {

    @Test
    public void test() throws JSchException, IOException {
        SshChannelCreator channelCreator = SshChannelCreatorFactory.getSshChannelCreator();
        SshDirectoryLister lister = new SshDirectoryLister();
        lister.setSshChannelCreator(channelCreator);
        String listing = lister.getListing(".");
        System.out.println(listing);
        FileWriter writer = new FileWriter("listing.txt");
        IOUtils.write(listing, writer);
        IOUtils.closeQuietly(writer);
        channelCreator.closeSession();
    }

}
