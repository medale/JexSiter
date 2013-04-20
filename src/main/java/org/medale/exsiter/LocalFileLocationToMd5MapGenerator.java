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

public class LocalFileLocationToMd5MapGenerator {

    /** Used to make relative file path by replacing absolute directory */
    public static final String RELATIVE_PREFIX = "." + File.separator;

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

    protected static String getFileLocationRelativeToStartDirectoryLocation(
            final String startDirectoryLocation,
            final String absoluteFileLocation) {
        final String relativeFileLocation = absoluteFileLocation.replaceFirst(
                startDirectoryLocation, RELATIVE_PREFIX);
        return relativeFileLocation;
    }

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
