package org.medale.exsiter;

public class ExsiterConstants {

    /**
     * Note: Dirs are relative to application.conf gitDir property. Location of
     * git repo that holds exsiter backup
     */
    public static final String EXSITER_BACKUP_DIR = "exsiter-backup";

    /**
     * Dir under EXSITER_BACKUP_DIR that stores web content
     */
    public static final String WEB_DIR = "target-web-dir";

    /**
     * File under EXSITER_BACKUP_DIR that stores map of filename to its latest
     * md5 hash
     */
    public static final String FILENAME_TO_HASH_MAP = "fileNameToHashMap.csv";

}
