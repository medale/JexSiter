package org.medale.exsiter;

/**
 * Assumes Unix paths, i.e. directories separated by /.
 */
public class FilePathChecksumTriple {

    protected static final int MD5_HASH_INDEX = 0;
    protected static final int FILE_NAME_AND_PATH_INDEX = 1;
    protected static final String SPACE = " ";

    protected static final int FILE_PATH_INDEX = 0;
    protected static final int FILE_NAME_INDEX = 1;
    protected static final String EMPTY_STRING = "";
    protected static final String FORWARD_SLASH = "/";

    private String md5Hash;
    private String filePath;
    private String fileName;

    public FilePathChecksumTriple(String md5Hash, String filePath,
            String fileName) {
        this.md5Hash = md5Hash;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public static FilePathChecksumTriple getInstance(
            String spaceSeparatedMd5HashAndFileNameWithPath) {
        String[] md5HashAndFileNameWithPath = splitSpaceSeparatedMd5HashAndFileNameWithPath(spaceSeparatedMd5HashAndFileNameWithPath);
        String md5Hash = md5HashAndFileNameWithPath[MD5_HASH_INDEX];
        String fileNameWithPath = md5HashAndFileNameWithPath[FILE_NAME_AND_PATH_INDEX];
        String[] filePathAndName = geFilePathAndFileName(fileNameWithPath);
        String filePath = filePathAndName[FILE_PATH_INDEX];
        String fileName = filePathAndName[FILE_NAME_INDEX];
        return new FilePathChecksumTriple(md5Hash, filePath, fileName);
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    protected static String[] splitSpaceSeparatedMd5HashAndFileNameWithPath(
            String spaceSeparatedMd5HashAndFileNameWithPath) {
        if (spaceSeparatedMd5HashAndFileNameWithPath == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        String[] md5HashAndFileNameWithPath = spaceSeparatedMd5HashAndFileNameWithPath
                .split(SPACE);
        if (md5HashAndFileNameWithPath.length != 2) {
            throw new IllegalArgumentException(
                    "Unable to parse md5hash and filepath/name from >>"
                            + spaceSeparatedMd5HashAndFileNameWithPath + "<<");
        }
        return md5HashAndFileNameWithPath;
    }

    protected static String[] geFilePathAndFileName(String fileNameWithPath) {
        String[] filePathAndName = new String[2];
        int lastForwardSlashIndex = fileNameWithPath.lastIndexOf(FORWARD_SLASH);
        if (wasLastForwardSlashFound(lastForwardSlashIndex)) {
            int beginIndexInclusive = 0;
            int endIndexExclusive = lastForwardSlashIndex + 1;
            filePathAndName[FILE_PATH_INDEX] = fileNameWithPath.substring(
                    beginIndexInclusive, endIndexExclusive);
            if (lastForwardSlashIndex < fileNameWithPath.length()) {
                beginIndexInclusive = lastForwardSlashIndex + 1;
                endIndexExclusive = fileNameWithPath.length();
                filePathAndName[FILE_NAME_INDEX] = fileNameWithPath.substring(
                        beginIndexInclusive, endIndexExclusive);
            }
        } else {
            filePathAndName[FILE_NAME_INDEX] = fileNameWithPath;
            filePathAndName[FILE_PATH_INDEX] = EMPTY_STRING;
        }
        return filePathAndName;
    }

    private static boolean wasLastForwardSlashFound(int lastForwardSlash) {
        return lastForwardSlash != -1;
    }

}
