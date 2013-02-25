package org.medale.backup;

public class FileInfo {

    public static final String DIRECTORY = "d";
    public static final String FILE = "-";
    public static final String SEPARATOR = " ";

    private String unixFilePermissions;
    private String owner;
    private String group;
    private long size;
    private long lastFileModificationinGmtEpochMillis;
    private String filename;

    public boolean isDirectory() {
        return unixFilePermissionsStartsWith(DIRECTORY);
    }

    public boolean isFile() {
        return unixFilePermissionsStartsWith(FILE);
    }

    protected boolean unixFilePermissionsStartsWith(final String prefix) {
        return unixFilePermissions != null && unixFilePermissions.startsWith(prefix);
    }

    public String getUnixFilePermissions() {
        return unixFilePermissions;
    }

    public void setUnixFilePermissions(String unixFilePermissions) {
        this.unixFilePermissions = unixFilePermissions;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastFileModificationinGmtEpochMillis() {
        return lastFileModificationinGmtEpochMillis;
    }

    public void setLastFileModificationinGmtEpochMillis(long lastFileModificationinGmtEpochMillis) {
        this.lastFileModificationinGmtEpochMillis = lastFileModificationinGmtEpochMillis;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
