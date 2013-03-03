package org.medale.exsiter;

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
 */
public class MapStore {

    public static final int FILE_AND_PATH_NAME_INDEX = 0;
    public static final int MD5_HASH_INDEX = 1;

    public static void storeMap(Writer writer,
            Map<String, FilePathChecksumTriple> filePathAndNameToTripleMap) {
        CSVWriter csvWriter = null;
        try {
            // generate default , separator with " quote char, \n line separator
            csvWriter = new CSVWriter(writer);
            for (Map.Entry<String, FilePathChecksumTriple> entry : filePathAndNameToTripleMap
                    .entrySet()) {
                String filePathAndName = entry.getKey();
                FilePathChecksumTriple triple = entry.getValue();
                csvWriter.writeNext(new String[] { filePathAndName,
                        triple.getMd5Hash() });
            }
        } finally {
            IOUtils.closeQuietly(csvWriter);
        }
    }

    public static Map<String, FilePathChecksumTriple> loadMap(Reader reader)
            throws IOException {
        Map<String, FilePathChecksumTriple> filePathAndNameToTripleMap = new HashMap<String, FilePathChecksumTriple>();
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(reader);
            List<String[]> allEntries = csvReader.readAll();
            for (String[] entry : allEntries) {
                String filePathAndName = entry[FILE_AND_PATH_NAME_INDEX];
                String md5Hash = entry[MD5_HASH_INDEX];
                FilePathChecksumTriple triple = new FilePathChecksumTriple(
                        md5Hash, filePathAndName);
                filePathAndNameToTripleMap.put(filePathAndName, triple);
            }
        } finally {
            IOUtils.closeQuietly(csvReader);
        }
        return filePathAndNameToTripleMap;
    }

}
