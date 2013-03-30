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
 * Utility class stores and loads map containing filePathAndName
 * key/FilePathChecksumTriple values.
 * 
 */
public class MapStore {

    public static final int FILE_AND_PATH_NAME_INDEX = 0;
    public static final int MD5_HASH_INDEX = 1;

    public static void storeMap(final File mapFile,
            final Map<String, FileLocationMd5Pair> filePathAndNameToTripleMap)
            throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(mapFile);
            storeMap(writer, filePathAndNameToTripleMap);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public static void storeMap(final Writer writer,
            final Map<String, FileLocationMd5Pair> filePathAndNameToTripleMap) {
        CSVWriter csvWriter = null;
        try {
            // generate default , separator with " quote char, \n line separator
            csvWriter = new CSVWriter(writer);
            for (final Map.Entry<String, FileLocationMd5Pair> entry : filePathAndNameToTripleMap
                    .entrySet()) {
                final String filePathAndName = entry.getKey();
                final FileLocationMd5Pair triple = entry.getValue();
                csvWriter.writeNext(new String[] { filePathAndName,
                        triple.getMd5Hash() });
            }
        } finally {
            IOUtils.closeQuietly(csvWriter);
        }
    }

    /**
     * Load map containing filename and path as key and a triple of File name,
     * path and Checksum as value.
     * 
     * @param reader
     * @return
     * @throws IOException
     */
    public static Map<String, FileLocationMd5Pair> loadMap(
            final File csvMapFile) throws IOException {
        Map<String, FileLocationMd5Pair> filePathAndNameToTripleMap = null;
        Reader reader = null;
        try {
            reader = new FileReader(csvMapFile);
            filePathAndNameToTripleMap = loadMap(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return filePathAndNameToTripleMap;
    }

    /**
     * Load map containing filename and path as key and a triple of File name,
     * path and Checksum as value.
     * 
     * @param reader
     * @return
     * @throws IOException
     */
    public static Map<String, FileLocationMd5Pair> loadMap(
            final Reader reader) throws IOException {
        final Map<String, FileLocationMd5Pair> filePathAndNameToTripleMap = new HashMap<String, FileLocationMd5Pair>();
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(reader);
            final List<String[]> allEntries = csvReader.readAll();
            for (final String[] entry : allEntries) {
                final String filePathAndName = entry[FILE_AND_PATH_NAME_INDEX];
                final String md5Hash = entry[MD5_HASH_INDEX];
                final FileLocationMd5Pair triple = new FileLocationMd5Pair(
                        md5Hash, filePathAndName);
                filePathAndNameToTripleMap.put(filePathAndName, triple);
            }
        } finally {
            IOUtils.closeQuietly(csvReader);
        }
        return filePathAndNameToTripleMap;
    }

}
