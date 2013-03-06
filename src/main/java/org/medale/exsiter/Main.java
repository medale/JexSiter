package org.medale.exsiter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.jcraft.jsch.JSchException;

/**
 * Entry into exister functionality:<br>
 * java -jar jexsiter.jar org.medale.exsiter.Main -init<br>
 * java -jar jexsiter.jar org.medale.exsiter.Main -backup (this is default)<br>
 * 
 * Running either option with -test will execute functionality in test mode<br>
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
        },
        TEST {
            @Override
            public String option() {
                return "test";
            }
        };

        public abstract String option();
    };

    /**
     * @param args
     * @throws IOException
     * @throws JSchException
     * @throws org.apache.commons.cli.ParseException
     */
    public static void main(String[] args) throws IOException, JSchException {
        Set<Functionality> functionalityToExecute = getFunctionalityToExecuteFromCommandLine(args);
        if (functionalityToExecute.contains(Functionality.TEST)) {
            functionalityToExecute.remove(Functionality.TEST);
        } else {

        }

    }

    protected static void execute(Functionality functionality, boolean testMode)
            throws IOException, JSchException {
        switch (functionality) {
        case INIT:
            InitializeCommand initCommand = new InitializeCommand();
            initCommand.execute();
            break;
        case BACKUP:
            BackupCommand backupCommand = new BackupCommand();
            backupCommand.execute();
            break;
        default:
            throw new IllegalArgumentException("Unhandled functionality "
                    + functionality);
        }
    }

    protected static Set<Functionality> getFunctionalityToExecuteFromCommandLine(
            String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new GnuParser();
        Set<Functionality> functionalityToExecute = new HashSet<Functionality>();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(Functionality.INIT.option())) {
                System.out.println("Initializing file/md5 hash map...");
                functionalityToExecute.add(Functionality.INIT);
            } else if (line.hasOption(Functionality.BACKUP.option())) {
                System.out.println("Initiating backup...");
                functionalityToExecute.add(Functionality.BACKUP);
            } else if (line.hasOption(Functionality.TEST.option())) {
                System.out.println("Executing in test mode...");
                functionalityToExecute.add(Functionality.TEST);
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
