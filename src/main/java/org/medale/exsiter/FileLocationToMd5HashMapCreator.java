package org.medale.exsiter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.JSchException;

/**
 * Utility class that ties together logic retrieving a remote listing of all
 * files with their md5 hash sum:<br>
 * 56a329926a92460b9b6ac1377f610e48 ./web/newsletter/grip-it.jpg<br>
 * 
 * Converts into a map<fileLocation, FileLocationMd5Pair>.
 * 
 */
public class FileLocationToMd5HashMapCreator {

    /**
     * Method connects to remote machine and executes
     * LIST_ALL_FILES_AND_THEIR_CHECKSUMS command. Returned output is parsed
     * into map of fileLocation key,FileLocationMd5Pair value.
     * 
     * @param channelCreator
     * @return
     * @throws JSchException
     * @throws IOException
     */
    public static Map<String, FileLocationMd5Pair> getFileLocationToMd5HashMap(
            final SshChannelCreator channelCreator) throws JSchException,
            IOException {
        final SshShellCommandExecutor commandExecutor = new SshShellCommandExecutor();
        commandExecutor.setSshChannelCreator(channelCreator);
        final String command = SshShellCommandExecutor.LIST_ALL_FILES_AND_THEIR_CHECKSUMS;
        final List<String> commandOutput = commandExecutor
                .getCommandOutput(command);
        final Map<String, FileLocationMd5Pair> fileLocationToMd5HashMap = getMap(commandOutput);
        return fileLocationToMd5HashMap;
    }

    protected static Map<String, FileLocationMd5Pair> getMap(
            final List<String> commandOutput) {
        final Map<String, FileLocationMd5Pair> fileLocationToMd5HashMap = new HashMap<String, FileLocationMd5Pair>();
        for (final String spaceSeparatedMd5HashAndFileLocation : commandOutput) {
            final FileLocationMd5Pair pair = FileLocationMd5Pair
                    .getInstance(spaceSeparatedMd5HashAndFileLocation);
            final String fileLocation = pair.getFileLocation();
            fileLocationToMd5HashMap.put(fileLocation, pair);
        }
        return fileLocationToMd5HashMap;
    }
}
