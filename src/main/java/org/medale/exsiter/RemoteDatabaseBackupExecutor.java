package org.medale.exsiter;

import java.io.IOException;
import java.util.List;

import com.jcraft.jsch.JSchException;

/**
 * Utility class used to execute remote database backup script and checks for
 * successful execution.
 */
public class RemoteDatabaseBackupExecutor {

    /**
     * Execute database backup script on remote machine and return
     * success/failure output of command.
     * 
     * @param channelCreator
     * @return
     * @throws JSchException
     * @throws IOException
     */
    public static String executeRemoteDatabaseBackup(
            final SshChannelCreator channelCreator) throws JSchException,
            IOException {
        final SshShellCommandExecutor commandExecutor = new SshShellCommandExecutor();
        commandExecutor.setSshChannelCreator(channelCreator);
        final String command = ExsiterConstants.EXECUTE_DB_BACKUP;
        final List<String> commandOutput = commandExecutor
                .getCommandOutput(command);
        String output = null;
        if ((commandOutput != null) && (commandOutput.size() > 0)) {
            output = commandOutput.get(0);
        } else {
            output = ExsiterConstants.DB_BACKUP_NO_MSG;
        }
        return output;
    }
}
