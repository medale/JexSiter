package org.medale.io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

/**
 * File utilities for deleting files and its empty parent directories, getting
 * relative file paths according to starting directory, and listing all readable
 * files in a directory.
 */
public class ExsiterFileUtils {

    private static final Logger LOGGER = Logger.getLogger(ExsiterFileUtils.class);

    /** Used to make relative file path by replacing absolute directory */
    public static final String RELATIVE_PREFIX = "." + File.separator;

    /**
     * Delete a file and empty parent directories until stop directory is
     * reached.
     * 
     * @param fileToDelete
     * @param stopDirectory
     * @throws IOException
     */
    public static void deleteFileAndEmptyParentDirectoriesUntilStopDirectory(
            final File fileToDelete, final File stopDirectory)
            throws IOException {
        if (fileToDelete.exists()) {
            final File parentDir = fileToDelete.getParentFile();
            final boolean deleted = fileToDelete.delete();
            if (deleted) {
                deleteEmptyParentDirectoriesUntilStopDirectory(parentDir,
                        stopDirectory);
            }
        }
    }

    /**
     * Delete empty parent directories starting at parentDir until
     * 
     * @param parentDir
     * @param stopDirectory
     * @throws IOException
     */
    public static void deleteEmptyParentDirectoriesUntilStopDirectory(
            File parentDir, final File stopDirectory) throws IOException {
        boolean continueDeletingParentDir = true;
        while (continueDeletingParentDir) {
            continueDeletingParentDir = stopIfStopDirectoryWasReached(
                    parentDir, stopDirectory);
            if (continueDeletingParentDir) {
                if (isParentDirectoryEmpty(parentDir) && parentDir.canWrite()) {
                    parentDir.delete();
                    if (LOGGER.isDebugEnabled()) {
                        final String msg = "Deleted "
                                + parentDir.getAbsolutePath();
                        LOGGER.debug(msg);
                    }
                    parentDir = parentDir.getParentFile();
                    if (parentDir == null) {
                        continueDeletingParentDir = false;
                    }
                } else {
                    continueDeletingParentDir = false;
                }
            }
        }
    }

    protected static boolean stopIfStopDirectoryWasReached(
            final File currentDirectory, final File stopDirectory)
            throws IOException {
        boolean continueDelete = true;
        if (currentDirectory.equals(stopDirectory)) {
            if (LOGGER.isDebugEnabled()) {
                final String msg = "Reached stop directory "
                        + stopDirectory.getCanonicalPath();
                LOGGER.debug(msg);
            }
            continueDelete = false;
        }
        return continueDelete;
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
    public static String getFileLocationRelativeToStartDirectoryLocation(
            String startDirectoryLocation, final String absoluteFileLocation) {
        if (!startDirectoryLocation.endsWith(File.separator)) {
            startDirectoryLocation = startDirectoryLocation + File.separator;
        }
        final String relativeFileLocation = absoluteFileLocation.replaceFirst(
                startDirectoryLocation, RELATIVE_PREFIX);
        return relativeFileLocation;
    }

    protected static boolean isParentDirectoryEmpty(final File parentDir) {
        final File[] parentFiles = parentDir.listFiles();
        return parentFiles.length == 0;
    }

    /**
     * Return list of all readable files under start directory.
     * 
     * @param startDirectoryLocation
     * @return
     */
    public static Collection<File> getAllReadableFilesFromStartDirectory(
            final String startDirectoryLocation) {
        final File startDir = new File(startDirectoryLocation);
        if (!startDir.exists()) {
            final String errMsg = "Directory >>" + startDir.getAbsolutePath()
                    + "<< did not exist!";
            throw new IllegalArgumentException(errMsg);
        }
        // matches all files
        final IOFileFilter fileFilter = new ReadableFileFilter();
        // matches all subdirectories
        final IOFileFilter dirFilter = TrueFileFilter.INSTANCE;
        final Collection<File> files = org.apache.commons.io.FileUtils
                .listFiles(startDir, fileFilter, dirFilter);
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
