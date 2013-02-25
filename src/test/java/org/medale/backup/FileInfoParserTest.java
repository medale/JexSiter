package org.medale.backup;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class FileInfoParserTest {

    private static final String ROOT_DIR_RESOURCE = "/directoryListings/rootDirListing.txt";
    private static final String FILE_LINE = "-rw-r--r-- 1 bikramyogacolumbia bikramyogacolumbia   426 Feb  4  2012 index.php";
    private static final String DIR_LINE = "drwxr-xr-x 2 bikramyogacolumbia bikramyogacolumbia    25 Jun  4  2012 bycDocs";

    @Test
    public void testParseUnixLongFileListingLine() throws ParseException {
        FileInfo fileInfo = FileInfoParser.parseUnixLongFileListingLine(DIR_LINE);
        assertNotNull(fileInfo);
        assertTrue(fileInfo.isDirectory());
    }

    @Test
    public void test() throws Exception {
        String unixLongFileListing = getLongFileListing(ROOT_DIR_RESOURCE);
        List<FileInfo> fileInfoList = FileInfoParser.parseUnixLongFileListing(unixLongFileListing);
    }

    private String getLongFileListing(String resource) throws IOException {
        InputStream input = getInputStream(resource);
        String listing = IOUtils.toString(input);
        return listing;
    }

    private InputStream getInputStream(String resource) {
        InputStream input = getClass().getResourceAsStream(resource);
        return input;
    }

}
