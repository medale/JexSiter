package org.medale.exsiter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Entry into exister functionality:<br>
 * java -jar jexsiter.jar org.medale.exsiter.Main -init<br>
 * java -jar jexsiter.jar org.medale.exsiter.Main -backup (this is default)<br>
 * 
 * Background on GnuParser:
 * http://www.mail-archive.com/commons-user@jakarta.apache.org/msg06288.html The
 * basic parser performs no interpretation of the command line tokens. GnuParser
 * supports 'ant' like options e.g. '-buildfile' and Java system properties e.g.
 * '-Dant.home=/dir'. PosixParser support 'tar' like options e.g. '-zxf' which
 * is burst into three options '-z', '-x' and '-f'.
 */
public class Main {

    public enum Functionality {
        INIT {
            @Override
            public String option() {
                return "init";
            }
        },
        BACKUP {
            @Override
            public String option() {
                return "backup";
            }
        };

        public abstract String option();
    };

    /**
     * @param args
     * @throws org.apache.commons.cli.ParseException
     */
    public static void main(String[] args) {
        Functionality functionalityToExecute = getFunctionalityToExecuteFromCommandLine(args);
        switch (functionalityToExecute) {
        case INIT:
            break;
        case BACKUP:
            break;
        }
    }

    protected static Functionality getFunctionalityToExecuteFromCommandLine(
            String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new GnuParser();
        Functionality functionalityToExecute = null;
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(Functionality.INIT.option())) {
                System.out.println("Initializing file/md5 hash map...");
                functionalityToExecute = Functionality.INIT;
            } else {
                System.out.println("Initiating backup...");
                functionalityToExecute = Functionality.BACKUP;
            }
        } catch (ParseException exp) {
            System.err.println("Unable to parse command line args due to "
                    + exp.getMessage());
        }
        return functionalityToExecute;
    }

    protected static Options getOptions() {
        Options options = new Options();
        boolean hasArgs = false;
        options.addOption(
                "i",
                Functionality.INIT.option(),
                hasArgs,
                "Initializes file name to hash map at $gitDir/exsiter-current/fileNameToHashMap.csv ");
        options.addOption(
                "b",
                Functionality.BACKUP.option(),
                hasArgs,
                "Performs incremental backup using file name to hash map at $gitDir/exsiter-current/fileNameToHashMap.csv");
        return options;
    }

}
