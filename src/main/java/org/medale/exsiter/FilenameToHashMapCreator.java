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
 * Converts into a map<filePathAndName, FilePathChecksumTriple>.
 * 
 */
public class FilenameToHashMapCreator {

    /**
     * Method connects to remote machine and executes
     * LIST_ALL_FILES_AND_THEIR_CHECKSUMS command. Returned output is parsed
     * into map of filePathAndName key, FilePathChecksumTriple.
     * 
     * @param channelCreator
     * @return
     * @throws JSchException
     * @throws IOException
     */
    public static Map<String, FilePathChecksumTriple> getFilenameToHashMap(
            SshChannelCreator channelCreator) throws JSchException, IOException {
        SshShellCommandExecutor commandExecutor = new SshShellCommandExecutor();
        commandExecutor.setSshChannelCreator(channelCreator);
        String command = SshShellCommandExecutor.LIST_ALL_FILES_AND_THEIR_CHECKSUMS;
        List<String> commandOutput = commandExecutor.getCommandOutput(command);
        Map<String, FilePathChecksumTriple> filenameToHashMap = getMap(commandOutput);
        return filenameToHashMap;
    }

    protected static Map<String, FilePathChecksumTriple> getMap(
            List<String> commandOutput) {
        Map<String, FilePathChecksumTriple> filenameToHashMap = new HashMap<String, FilePathChecksumTriple>();
        for (String spaceSeparatedMd5HashAndFileNameWithPath : commandOutput) {
            FilePathChecksumTriple triple = FilePathChecksumTriple
                    .getInstance(spaceSeparatedMd5HashAndFileNameWithPath);
            String filePathAndName = triple.getFilePathAndName();
            filenameToHashMap.put(filePathAndName, triple);
        }
        return filenameToHashMap;
    }
}
