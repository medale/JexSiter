package org.medale.exsiter;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.medale.io.ExsiterFileUtils;

/**
 * Generate relative local file name to its md5 hash map.
 */
public class LocalFileLocationToMd5MapGenerator {

    /**
     * Create a map of file location relative to startDirectoryLocation and the
     * md5 hash of that file for all user readable files under
     * startDirectoryLocation.
     * 
     * @param startDirectoryLocation
     * @return
     * @throws Exception
     */
    public static final Map<String, String> getFileLocationToMd5Map(
            final String startDirectoryLocation) throws Exception {
        final Map<String, String> fileLocationToMd5Map = new HashMap<String, String>();
        final Collection<File> files = ExsiterFileUtils
                .getAllReadableFilesFromStartDirectory(startDirectoryLocation);
        for (final Iterator<File> iterator = files.iterator(); iterator
                .hasNext();) {
            final File file = iterator.next();
            final String absoluteFileLocation = file.getAbsolutePath();
            final String relativeFileLocation = ExsiterFileUtils
                    .getFileLocationRelativeToStartDirectoryLocation(
                            startDirectoryLocation, absoluteFileLocation);
            final String md5Hash = getMd5Hash(file);
            fileLocationToMd5Map.put(relativeFileLocation, md5Hash);
        }
        return fileLocationToMd5Map;
    }

    protected static String getMd5Hash(final File file) throws Exception {
        final byte[] data = FileUtils.readFileToByteArray(file);
        final String md5Hash = DigestUtils.md5Hex(data);
        return md5Hash;
    }
}
