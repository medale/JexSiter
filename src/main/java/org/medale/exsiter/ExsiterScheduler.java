package org.medale.exsiter;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Reads $USER_HOME/.exsiter/application.conf backupCronFrequency property for
 * executing Main backup on the schedule indicated by that cron expression.
 * 
 * java -jar exister.jar (default executable in manifest)
 */
public class ExsiterScheduler {

    private static final Logger LOGGER = Logger
            .getLogger(ExsiterScheduler.class);

    private static final DateTimeFormatter MEDIUM_DATE_FORMATTER = DateTimeFormat
            .mediumDateTime();
    public static final String CONFIG_LOCATION = "configLocation";

    public static void main(final String[] args) throws SchedulerException,
            ParseException, IOException {
        final Options options = getOptions();
        final CommandLineParser parser = new GnuParser();
        final CommandLine line = parser.parse(options, args);
        final String configLocation = line.getOptionValue(CONFIG_LOCATION);
        final ApplicationConfiguration appConfig = getApplicationConfiguration(configLocation);
        appConfig.loadConfiguration();
        final Properties configuration = appConfig.getConfiguration();
        final String backupFrequencyCronExpression = configuration
                .getProperty(ApplicationConfiguration.PROP_BACKUP_CRON_FREQUENCY);
        Scheduler scheduler = null;
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            final CronExpression cronExpression = new CronExpression(
                    backupFrequencyCronExpression);
            final CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                    .cronSchedule(cronExpression);
            final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1").startNow()
                    .withSchedule(scheduleBuilder).build();
            final JobDetail job = JobBuilder.newJob(BackupJob.class)
                    .withIdentity("job1", "group1").build();
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
            if (LOGGER.isDebugEnabled()) {
                final long instant = System.currentTimeMillis();
                LOGGER.debug("Started run at "
                        + MEDIUM_DATE_FORMATTER.print(instant));
            }
        } catch (final Exception e) {
            LOGGER.error("Program terminated due to " + e, e);
        }
    }

    protected static Options getOptions() {
        final Options options = new Options();
        final boolean hasArgs = true;
        options.addOption(
                "c",
                CONFIG_LOCATION,
                hasArgs,
                "If set, overrides default config location of $USER_HOME/.exsiter/application.conf.");
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

    /**
     * Backup job invokes Main.main with no args invoking backup with default
     * dir $USER_HOME/.exsiter/application.conf
     */
    public static class BackupJob implements Job {
        @Override
        public void execute(final JobExecutionContext context)
                throws JobExecutionException {
            final String[] args = {};
            try {
                final long kickOffTime = System.currentTimeMillis();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Kicking off a new backup at "
                            + MEDIUM_DATE_FORMATTER.print(kickOffTime));
                }
                Main.main(args);
                final long completedTime = System.currentTimeMillis();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Backup timing: start="
                            + MEDIUM_DATE_FORMATTER.print(kickOffTime)
                            + "\tfinish="
                            + MEDIUM_DATE_FORMATTER.print(completedTime));

                }
            } catch (final Exception e) {
                throw new JobExecutionException(e);
            }
        }
    }

}
