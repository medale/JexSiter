package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.googlecode.jatl.Html;
import com.googlecode.jatl.Indenter;
import com.googlecode.jatl.SimpleIndenter;

/**
 * Creates an HTML page about backup (files added/modified/deleted).
 */
public class SimpleHtmlBackupReporter implements BackupReporter {

    public static final String EMPTY_STRING = "";
    public static final String REPORT_TITLE_PREFIX = "Backup Report as of ";
    public static final String GIT_TAG_PREFIX = "All changes tagged in Git version control under tag ";
    public static final String NO_FILES_REPORT = "No files to report";

    @Override
    public String createReport(final String outputLocation,
            final RepositoryAdjustor repoAdjustor) throws IOException {
        final File outputFile = new File(outputLocation);
        final long dateTimeEpoch = System.currentTimeMillis();
        final String htmlReport = getHtmlReport(repoAdjustor, dateTimeEpoch);
        FileUtils.writeStringToFile(outputFile, htmlReport);
        return htmlReport;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected String getHtmlReport(final RepositoryAdjustor repoAdjustor,
            final long dateTimeEpoch) {
        final StringWriter writer = new StringWriter();
        final Html html = new Html(writer);
        final Indenter indenter = getIndenterWithEmptyStringIndents();
        html.indent(indenter);
        html.html();
        final String title = getTitleWithDateTime(dateTimeEpoch);
        html.head().title().text(title).end(2);
        html.body();
        html.h1().text(title).end();
        final String gitDateTag = repoAdjustor.getGitDateTag();
        final String gitTagInfo = GIT_TAG_PREFIX + gitDateTag;
        html.text(gitTagInfo);

        final Set<String> fileLocationsToBeAdded = repoAdjustor
                .getFileLocationsToBeAdded();
        final Set<String> fileLocationsToBeModified = repoAdjustor
                .getFileLocationsToBeModified();
        final Set<String> fileLocationsToBeLocallyDeleted = repoAdjustor
                .getFileLocationsToBeLocallyDeleted();
        final List[] sortedLocations = { new ArrayList(fileLocationsToBeAdded),
                new ArrayList(fileLocationsToBeModified),
                new ArrayList(fileLocationsToBeLocallyDeleted) };
        for (final List list : sortedLocations) {
            Collections.sort(list);
        }
        final String[] fileSetNames = { "NewFiles", "ModifiedFiles",
                "DeletedFiles" };

        for (int i = 0; i < fileSetNames.length; i++) {
            final String fileSetName = fileSetNames[i];
            html.h1().text(fileSetName);
            html.end();
            final List locationSet = sortedLocations[i];
            final int numberOfLocations = locationSet.size();
            if (numberOfLocations > 0) {
                html.ol();
                for (final Object location : locationSet) {
                    html.li().text(location.toString()).end();
                }
                html.end();
            } else {
                html.text(NO_FILES_REPORT);
            }
        }
        html.endAll();
        return writer.getBuffer().toString();
    }

    protected String getTitleWithDateTime(final long dateTimeEpoch) {
        final String dateTime = ExsiterConstants.DATE_FORMATTER
                .print(dateTimeEpoch);
        final String title = REPORT_TITLE_PREFIX + dateTime;
        return title;
    }

    private Indenter getIndenterWithEmptyStringIndents() {
        final String startTagNewLine = EMPTY_STRING;
        final String startTagIndent = EMPTY_STRING;
        final String endTagNewLine = EMPTY_STRING;
        final String endTagIndent = EMPTY_STRING;
        final Indenter indenter = new SimpleIndenter(startTagNewLine,
                startTagIndent, endTagNewLine, endTagIndent);
        return indenter;
    }
}
