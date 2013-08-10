package org.medale.exsiter;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

/**
 * Entry into exister functionality:<br>
 * Unless-init option is specified we will attempt to run backup. Args in
 * parenthesis are optional.
 * 
 * <ol>
 * <li>Initialize: java -classpath jexsiter.jar org.medale.exsiter.Main -init
 * (-configLocation) or java -jar jexsiter-0.9-SNAPSHOT.jar -init
 * <li>Backup: java -classpath jexsiter.jar org.medale.exsiter.Main
 * (-configLocation) $absoluteConfigLocation</li>
 * </ol>
 * 
 * Background on GnuParser:
 * http://www.mail-archive.com/commons-user@jakarta.apache.org/msg06288.html The
 * basic parser performs no interpretation of the command line tokens. GnuParser
 * supports 'ant' like options e.g. '-buildfile' and Java system properties e.g.
 * '-Dant.home=/dir'. PosixParser support 'tar' like options e.g. '-zxf' which
 * is burst into three options '-z', '-x' and '-f'.
 */
public class Main {

    public static final String CONFIG_LOCATION = "configLocation";
    public static final String INITIALIZE = "init";

    public static void main(final String[] args) throws Exception {
        final Options options = getOptions();
        final CommandLineParser parser = new GnuParser();
        final CommandLine line = parser.parse(options, args);
        final String configLocation = line.getOptionValue(CONFIG_LOCATION);
        final ApplicationConfiguration appConfig = getApplicationConfiguration(configLocation);
        appConfig.loadConfiguration();
        if (line.hasOption(INITIALIZE)) {
            final InitializeCommand initCmd = new InitializeCommand();
            initCmd.execute(appConfig);
        } else {
            final SimpleHtmlBackupReporter reporter = new SimpleHtmlBackupReporter();
            final BackupCommand backupCmd = new BackupCommand();
            backupCmd.setBackupReporter(reporter);
            final Properties configProps = appConfig.getConfiguration();
            backupCmd.execute(configProps);
        }
    }

    protected static Options getOptions() {
        final Options options = new Options();
        boolean hasArgs = true;
        options.addOption(
                "c",
                CONFIG_LOCATION,
                hasArgs,
                "If set, overrides default config location of $USER_HOME/.exsiter/application.conf.");
        hasArgs = false;
        options.addOption("i", INITIALIZE, hasArgs,
                "Initializes local Exsiter file store and Git repository.");
        return options;
    }

    protected static ApplicationConfiguration getApplicationConfiguration(
            final String configLocation) {
        ApplicationConfiguration appConfig = null;
        if (configLocation == null) {
            appConfig = new ApplicationConfiguration();
        } else {
            appConfig = new ApplicationConfiguration(configLocation);
        }
        return appConfig;
    }

}
