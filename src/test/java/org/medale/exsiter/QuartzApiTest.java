package org.medale.exsiter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzApiTest {

    private static final Logger LOGGER = Logger.getLogger(QuartzApiTest.class);
    private static int invocationCount;

    @Test
    public void testCronExpression() throws ParseException {
        final String lastFridayOfMonthAt10 = "0 0 10 ? * 6L";
        final CronExpression cronExpression = new CronExpression(
                lastFridayOfMonthAt10);
        System.out.println(cronExpression.getExpressionSummary());
        final DateTime now = new DateTime();
        final int hourOfDay = 10;
        final int minuteOfHour = 0;
        final int secondOfMinute = 0;
        final int millisOfSecond = 0;
        DateTime lastFridayAt10 = now
                .dayOfMonth()
                .withMaximumValue()
                .withDayOfWeek(DateTimeConstants.FRIDAY)
                .withTime(hourOfDay, minuteOfHour, secondOfMinute,
                        millisOfSecond);
        if (lastFridayAt10.getMonthOfYear() != now.getMonthOfYear()) {
            lastFridayAt10 = lastFridayAt10.minusDays(7);
        }
        final boolean satisfiedBy = cronExpression.isSatisfiedBy(lastFridayAt10
                .toDate());
        assertTrue(satisfiedBy);

        final DateTime todayAtNoon = now.withTime(12, minuteOfHour,
                secondOfMinute, millisOfSecond);
        final boolean satisfiedBy2 = cronExpression.isSatisfiedBy(todayAtNoon
                .toDate());
        assertFalse(satisfiedBy2);
    }

    @Test
    public void testCronTrigger() throws ParseException, InterruptedException {
        try {
            final Scheduler scheduler = StdSchedulerFactory
                    .getDefaultScheduler();
            scheduler.start();
            final String everySecond = "* * * * * ?";
            final CronExpression cronExpression = new CronExpression(
                    everySecond);
            final CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                    .cronSchedule(cronExpression);
            final Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1").startNow()
                    .withSchedule(scheduleBuilder).build();
            invocationCount = 0;
            final JobDetail job = JobBuilder.newJob(SimpleJob.class)
                    .withIdentity("job1", "group1").build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(3000);
            assertTrue(invocationCount > 0);
            scheduler.shutdown();
        } catch (final SchedulerException se) {
            LOGGER.error(se);
        }
    }

    public static class SimpleJob implements Job {

        @Override
        public void execute(final JobExecutionContext context)
                throws JobExecutionException {
            invocationCount++;
            final JobKey jobKey = context.getJobDetail().getKey();
            LOGGER.info("SimpleJob: " + jobKey + " executing invocation count "
                    + invocationCount);
        }

    }

}
