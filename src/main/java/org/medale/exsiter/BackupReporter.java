package org.medale.exsiter;

import java.io.IOException;

/**
 * Implementer provides functionality to store information about added, modified
 * and deleted files in repo adjustor.
 */
public interface BackupReporter {

    String createReport(final String outputLocation, final String gitDateTag,
            final RepositoryAdjustor repoAdjustor) throws IOException;
}
