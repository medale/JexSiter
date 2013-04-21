package org.medale.exsiter;

import org.apache.commons.lang.CharEncoding;

public class ExsiterConstants {

    /**
     * Note: Dirs are relative to application.conf gitDir property. Location of
     * git repo that holds exsiter backup
     */
    public static final String EXSITER_BACKUP_DIR = "exsiter-backup";

    /**
     * Dir under EXSITER_BACKUP_DIR that stores remote content
     */
    public static final String REMOTE_CONTENT_DIR = "remote-content-dir";

    /**
     * File under EXSITER_BACKUP_DIR that stores map of file name to its latest
     * md5 hash
     */
    public static final String REMOTE_FILE_NAME_TO_MD5_MAP = "remoteFileNameToMd5Map.csv";

    /**
     * File under EXSITER_BACKUP_DIR that stores map of local file name to its
     * md5 hash.
     */
    public static final String LOCAL_FILE_NAME_TO_MD5_MAP = "localFileNameToMd5Map.csv";

    /**
     * Default character encoding
     */
    public static final String DEFAULT_ENCODING = CharEncoding.UTF_8;

    /**
     * Command used to list remote/local files and their checksums
     */
    public static final String LIST_ALL_FILES_AND_THEIR_CHECKSUMS = "find . -type f | xargs md5sum";

}
