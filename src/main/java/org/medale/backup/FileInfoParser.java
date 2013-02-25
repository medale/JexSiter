package org.medale.backup;

import java.text.ParseException;
import java.util.List;

/**
 *
 */
public class FileInfoParser {

    private static final int UNIX_PERMISSIONS = 0;

    public static List<FileInfo> parseUnixLongFileListing(String unixLongFileListing) {
        return null;
    }

    public static FileInfo parseUnixLongFileListingLine(String fileListingLine) throws ParseException {
        FileInfo fileInfo = new FileInfo();
        if (fileListingLine != null) {
            String[] infoParts = fileListingLine.split(FileInfo.SEPARATOR);
        }
        return fileInfo;
    }

}
