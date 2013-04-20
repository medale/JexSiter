package org.medale.exsiter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Utility class stores and loads map containing filePathAndName key/md5sum
 * values.
 */
public class MapStore {

    public static final int FILE_AND_PATH_NAME_INDEX = 0;
    public static final int MD5_HASH_INDEX = 1;

    public static void storeMap(final File mapFile,
            final Map<String, String> filePathAndNameToMd5Map)
            throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(mapFile);
            storeMap(writer, filePathAndNameToMd5Map);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public static void storeMap(final Writer writer,
            final Map<String, String> filePathAndNameToMd5Map) {
        CSVWriter csvWriter = null;
        try {
            // generate default , separator with " quote char, \n line separator
            csvWriter = new CSVWriter(writer);
            for (final Map.Entry<String, String> entry : filePathAndNameToMd5Map
                    .entrySet()) {
                final String filePathAndName = entry.getKey();
                final String md5Hash = entry.getValue();
                csvWriter.writeNext(new String[] { filePathAndName, md5Hash });
            }
        } finally {
            IOUtils.closeQuietly(csvWriter);
        }
    }

    /**
     * Load map containing fileLocation as key and the file's MD5 Hash as value.
     * 
     * @param reader
     * @return
     * @throws IOException
     */
    public static Map<String, String> loadMap(final File csvMapFile)
            throws IOException {
        Map<String, String> filePathAndNameToMd5Map = null;
        Reader reader = null;
        try {
            reader = new FileReader(csvMapFile);
            filePathAndNameToMd5Map = loadMap(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return filePathAndNameToMd5Map;
    }

    /**
     * Load map containing fileLocation as key and the file's MD5 Hash as value.
     * 
     * @param reader
     * @return
     * @throws IOException
     */
    public static Map<String, String> loadMap(final Reader reader)
            throws IOException {
        final Map<String, String> filePathAndNameToMd5Map = new HashMap<String, String>();
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(reader);
            final List<String[]> allEntries = csvReader.readAll();
            for (final String[] entry : allEntries) {
                final String filePathAndName = entry[FILE_AND_PATH_NAME_INDEX];
                final String md5Hash = entry[MD5_HASH_INDEX];
                filePathAndNameToMd5Map.put(filePathAndName, md5Hash);
            }
        } finally {
            IOUtils.closeQuietly(csvReader);
        }
        return filePathAndNameToMd5Map;
    }

}
