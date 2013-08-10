package org.medale.exsiter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
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
    public static final String REPORT_TITLE = "Backup Report";

    @Override
    public void createReport(final String outputLocation,
            final RepositoryAdjustor repoAdjustor) throws IOException {
        final File outputFile = new File(outputLocation);
        final String htmlReport = getHtmlReport(repoAdjustor);
        FileUtils.writeStringToFile(outputFile, htmlReport);
    }

    @SuppressWarnings("rawtypes")
    protected String getHtmlReport(final RepositoryAdjustor repoAdjustor) {
        final StringWriter writer = new StringWriter();
        final Html html = new Html(writer);
        final Indenter indenter = getIndenterWithEmptyStringIndents();
        html.indent(indenter);
        html.html();
        html.head().title().text(REPORT_TITLE).end(2);
        html.body();
        final Set<String> fileLocationsToBeAdded = repoAdjustor
                .getFileLocationsToBeAdded();
        final Set<String> fileLocationsToBeModified = repoAdjustor
                .getFileLocationsToBeModified();
        final Set<String> fileLocationsToBeLocallyDeleted = repoAdjustor
                .getFileLocationsToBeLocallyDeleted();
        final Set[] locationSets = { fileLocationsToBeAdded,
                fileLocationsToBeModified, fileLocationsToBeLocallyDeleted };
        final String[] fileSetNames = { "NewFiles", "ModifiedFiles",
                "DeletedFiles" };
        for (int i = 0; i < fileSetNames.length; i++) {
            final String fileSetName = fileSetNames[i];
            html.h1().text(fileSetName);
            html.end();
            html.ol();
            final Set locationSet = locationSets[i];
            for (final Object location : locationSet) {
                html.li().text(location.toString()).end();
            }
            html.end();
        }
        html.endAll();
        return writer.getBuffer().toString();
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
