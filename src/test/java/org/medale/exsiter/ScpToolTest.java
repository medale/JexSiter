package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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
    public void testScpFromSunnyDay() throws JSchException, IOException {
        final String localLocation = "/tmp/favicon.ico";
        final File localFile = new File(localLocation);
        if (localFile.exists()) {
            final boolean deleted = localFile.delete();
            if (!deleted) {
                throw new IOException("Unable to delete " + localFile);
            }
        }
        assertFalse(localFile.exists());
        final ScpTool scpTool = new ScpTool();
        final SshChannelCreator channelCreator = SshChannelCreatorFactory
                .getDefaultSshChannelCreator();
        scpTool.setSshChannelCreator(channelCreator);
        final String remoteLocation = "~/favicon.ico";
        final boolean success = scpTool.scpFileFrom(remoteLocation,
                localLocation);
        assertTrue(success);
        assertEquals("0644", scpTool.getFilePermissions());
        assertEquals(198, scpTool.getFileSizeInBytes());
        assertTrue(localFile.exists());
        assertTrue(localFile.length() > 0);
        channelCreator.closeSession();
    }

    @Test
    public void testScpFromBogusRemoteFile() throws JSchException, IOException {
        final String localLocation = "/tmp/favicon.ico";
        final File localFile = new File(localLocation);
        if (localFile.exists()) {
            final boolean deleted = localFile.delete();
            if (!deleted) {
                throw new IOException("Unable to delete " + localFile);
            }
        }
        assertFalse(localFile.exists());
        final ScpTool scpTool = new ScpTool();
        final SshChannelCreator channelCreator = SshChannelCreatorFactory
                .getDefaultSshChannelCreator();
        scpTool.setSshChannelCreator(channelCreator);
        final String remoteLocation = "~/bogusBogus.php";
        final boolean success = scpTool.scpFileFrom(remoteLocation,
                localLocation);
        assertFalse(success);
        channelCreator.closeSession();
    }

}
