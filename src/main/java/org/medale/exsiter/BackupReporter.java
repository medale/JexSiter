package org.medale.exsiter;

import java.io.IOException;

/**
 * Implementer provides functionality to store information about added, modified
 * and deleted files in repo adjustor.
 */
public interface BackupReporter {

    void createReport(final String outputLocation,
            final RepositoryAdjustor repoAdjustor) throws IOException;
}
