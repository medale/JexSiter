package org.medale.exsiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;

public class SimpleHtmlBackupReporterTest {

    private static final List<String> INPUT_LIST1 = Arrays.asList(
            "56a329926a92460b9b6ac1377f610e48  ./web/newsletter/grip-it.jpg",
            "4d6ccea2e6e506ee68b1a793c477e617  ./web/newsletter/handstand.jpg",
            "7d6ccea2e6e506ee68b1a793c477e613  ./web/bogus/bar.baz");

    @Test
    public void testGetHtmlReportWithModsAddsAndDeletes() {
        // start with exact same list but then adjust to simulate mod/add/delete
        final Map<String, String> localMap = getMap(INPUT_LIST1);
        final Map<String, String> remoteMap = getMap(INPUT_LIST1);

        // remote entry modified
        final String modLoc = "./web/newsletter/handstand.jpg";
        final String modMd5Hash = "123ccea2e6e506ee68b1a793c477e613";
        remoteMap.put(modLoc, modMd5Hash);

        // remote entry added
        final String addedLoc = "./newFile.txt";
        final String addedMd5 = "123ccea2e6e506ee68b1a793c477e642";
        remoteMap.put(addedLoc, addedMd5);

        // remote entry deleted
        final String deletedLoc = "./web/newsletter/grip-it.jpg";
        remoteMap.remove(deletedLoc);

        final RepositoryAdjustor adjustor = new RepositoryAdjustor();
        adjustor.setLocalFileLocationToMd5Map(localMap);
        adjustor.setRemoteFileLocationToMd5Map(remoteMap);
        adjustor.computeFileAdjustments();
        final String gitDateTag = "v2013Feb27-1";
        adjustor.setGitDateTag(gitDateTag);

        final SimpleHtmlBackupReporter reporter = new SimpleHtmlBackupReporter();
        final long dateTimeEpoch = ExsiterConstants.DATE_FORMATTER
                .parseMillis("Feb 27, 2013 7:00:00 PM");
        final String htmlReport = reporter.getHtmlReport(adjustor,
                dateTimeEpoch);
        System.out.println(htmlReport);
        final Document doc = Jsoup.parse(htmlReport);
        final Elements titleTags = doc.getElementsByTag("title");
        assertEquals(1, titleTags.size());
        final Element titleElem = titleTags.get(0);
        final String expectedTitle = reporter
                .getTitleWithDateTime(dateTimeEpoch);
        assertEquals(expectedTitle, titleElem.text());
        final Elements bodyElems = doc.getElementsByTag("body");
        assertEquals(1, bodyElems.size());
        final Element bodyElem = bodyElems.get(0);
        final List<Node> childNodes = bodyElem.childNodes();
        assertEquals(8, childNodes.size());
        final String[] fileSetNames = { "NewFiles", "ModifiedFiles",
                "DeletedFiles" };
        final String[] expectedFiles = { addedLoc, modLoc, deletedLoc };
        int h1NodeIndexForFileEntries = 2; // skip h1 title and git date
                                           // printout
        for (int i = 0; i < expectedFiles.length; i++) {
            final String expectedFile = expectedFiles[i];
            final String expectedFileSetName = fileSetNames[i];
            final Element h1Elem = (Element) childNodes
                    .get(h1NodeIndexForFileEntries);
            assertEquals(expectedFileSetName, h1Elem.text());
            final Element olElem = (Element) childNodes
                    .get(h1NodeIndexForFileEntries + 1);
            final Elements liElem = olElem.getElementsByTag("li");
            assertEquals(expectedFile, liElem.text());
            h1NodeIndexForFileEntries += 2;
        }
    }

    @Test
    public void testGetHtmlReportWithNoChanges() {
        final Map<String, String> localMap = getMap(INPUT_LIST1);
        final Map<String, String> remoteMap = getMap(INPUT_LIST1);

        final RepositoryAdjustor adjustor = new RepositoryAdjustor();
        adjustor.setLocalFileLocationToMd5Map(localMap);
        adjustor.setRemoteFileLocationToMd5Map(remoteMap);
        adjustor.computeFileAdjustments();

        final String gitDateTag = "v2013Feb27-1";
        adjustor.setGitDateTag(gitDateTag);

        final SimpleHtmlBackupReporter reporter = new SimpleHtmlBackupReporter();
        final long dateTimeEpoch = ExsiterConstants.DATE_FORMATTER
                .parseMillis("Feb 27, 2013 7:00:00 PM");
        final String htmlReport = reporter.getHtmlReport(adjustor,
                dateTimeEpoch);
        System.out.println(htmlReport);
        final Document doc = Jsoup.parse(htmlReport);
        final Elements titleTags = doc.getElementsByTag("title");
        assertEquals(1, titleTags.size());
        final Element titleElem = titleTags.get(0);
        final String expectedTitle = reporter
                .getTitleWithDateTime(dateTimeEpoch);
        assertEquals(expectedTitle, titleElem.text());
        final Elements bodyElems = doc.getElementsByTag("body");
        assertEquals(1, bodyElems.size());
        final Element bodyElem = bodyElems.get(0);
        final List<Node> childNodes = bodyElem.childNodes();
        assertEquals(8, childNodes.size());
        final String[] fileSetNames = { "NewFiles", "ModifiedFiles",
                "DeletedFiles" };

        int h1NodeIndexForFileEntries = 2; // skip h1 title and git date
                                           // printout
        for (int i = 0; i < fileSetNames.length; i++) {
            final String expectedFileSetName = fileSetNames[i];
            final Element h1Elem = (Element) childNodes
                    .get(h1NodeIndexForFileEntries);
            assertEquals(expectedFileSetName, h1Elem.text());
            final Node node = childNodes.get(h1NodeIndexForFileEntries + 1);
            assertTrue(node instanceof TextNode);
            final TextNode textNode = (TextNode) node;
            final String actualText = textNode.getWholeText();
            assertEquals(SimpleHtmlBackupReporter.NO_FILES_REPORT, actualText);
            h1NodeIndexForFileEntries += 2;
        }
    }

    private Map<String, String> getMap(final List<String> inputList) {
        final List<FileLocationMd5Pair> expectedFileLocations = getFileLocationMd5Hashes(inputList);
        final Map<String, String> fileLocationToMd5HashMap = new HashMap<String, String>();
        for (final FileLocationMd5Pair pair : expectedFileLocations) {
            final String key = pair.getFileLocation();
            final String value = pair.getMd5Hash();
            fileLocationToMd5HashMap.put(key, value);
        }
        return fileLocationToMd5HashMap;
    }

    private List<FileLocationMd5Pair> getFileLocationMd5Hashes(
            final List<String> inputList) {
        final List<FileLocationMd5Pair> expectedFileLocations = new ArrayList<FileLocationMd5Pair>();
        for (final String input : inputList) {
            final FileLocationMd5Pair pair = FileLocationMd5Pair
                    .getInstance(input);
            expectedFileLocations.add(pair);
        }
        return expectedFileLocations;
    }
}
