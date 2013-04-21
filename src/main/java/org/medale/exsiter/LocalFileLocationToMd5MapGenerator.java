package org.medale.exsiter;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Generate relative local file name to its md5 hash map.
 */
public class LocalFileLocationToMd5MapGenerator {

    /** Used to make relative file path by replacing absolute directory */
    public static final String RELATIVE_PREFIX = "." + File.separator;

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
        final Collection<File> files = getAllReadableFilesFromStartDirectory(startDirectoryLocation);
        for (final Iterator<File> iterator = files.iterator(); iterator
                .hasNext();) {
            final File file = iterator.next();
            final String absoluteFileLocation = file.getAbsolutePath();
            final String relativeFileLocation = getFileLocationRelativeToStartDirectoryLocation(
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

    /**
     * Return a file location string that is relative to the start directory
     * from an absolute file location. So /home/test/.bashrc becomes ./.bashrc
     * if /home/test/ is the starting dir.
     * 
     * @param startDirectoryLocation
     * @param absoluteFileLocation
     * @return
     */
    protected static String getFileLocationRelativeToStartDirectoryLocation(
            String startDirectoryLocation, final String absoluteFileLocation) {
        if (!startDirectoryLocation.endsWith(File.separator)) {
            startDirectoryLocation = startDirectoryLocation + File.separator;
        }
        final String relativeFileLocation = absoluteFileLocation.replaceFirst(
                startDirectoryLocation, RELATIVE_PREFIX);
        return relativeFileLocation;
    }

    /**
     * Return list of all readable files under start directory.
     * 
     * @param startDirectoryLocation
     * @return
     */
    protected static Collection<File> getAllReadableFilesFromStartDirectory(
            final String startDirectoryLocation) {
        final File startDir = new File(startDirectoryLocation);
        // matches all files
        final IOFileFilter fileFilter = new ReadableFileFilter();
        // matches all subdirectories
        final IOFileFilter dirFilter = TrueFileFilter.INSTANCE;
        final Collection<File> files = FileUtils.listFiles(startDir,
                fileFilter, dirFilter);
        return files;
    }

    /**
     * Filter to only accept files that are readable to executing user.
     */
    public static class ReadableFileFilter implements IOFileFilter {

        @Override
        public boolean accept(final File file) {
            return file.canRead();
        }

        @Override
        public boolean accept(final File dir, final String name) {
            final File file = new File(dir, name);
            return file.canRead();
        }

    }
}
