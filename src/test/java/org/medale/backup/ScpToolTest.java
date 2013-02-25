package org.medale.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.jcraft.jsch.JSchException;

/**
 * -rw-r--r-- 1 bikramyogacolumbia bikramyogacolumbia 198 Dec 23 06:07
 * favicon.ico
 * 
 * filePermissions should be 0644 <br>
 * fileSizeInBytes should be 198
 */
public class ScpToolTest {

    @Test
    public void testScpFromSunnyDay() throws JSchException {
        String localLocation = "/tmp/favicon.ico";
        File localFile = new File(localLocation);
        if (localFile.exists()) {
            localFile.delete();
        }
        assertFalse(localFile.exists());
        ScpTool scpTool = new ScpTool();
        SshChannelCreator channelCreator = SshChannelCreatorFactory.getSshChannelCreator();
        scpTool.setSshChannelCreator(channelCreator);
        String remoteLocation = "~/favicon.ico";
        boolean success = scpTool.scpFileFrom(remoteLocation, localLocation);
        assertTrue(success);
        assertEquals("0644", scpTool.getFilePermissions());
        assertEquals(198, scpTool.getFileSizeInBytes());
        assertTrue(localFile.exists());
        assertTrue(localFile.length() > 0);
        channelCreator.closeSession();
    }

    @Test
    public void testScpFromBogusRemoteFile() throws JSchException {
        String localLocation = "/tmp/favicon.ico";
        File localFile = new File(localLocation);
        if (localFile.exists()) {
            localFile.delete();
        }
        assertFalse(localFile.exists());
        ScpTool scpTool = new ScpTool();
        SshChannelCreator channelCreator = SshChannelCreatorFactory.getSshChannelCreator();
        scpTool.setSshChannelCreator(channelCreator);
        String remoteLocation = "~/bogusBogus.php";
        boolean success = scpTool.scpFileFrom(remoteLocation, localLocation);
        assertFalse(success);
        channelCreator.closeSession();
    }

}
