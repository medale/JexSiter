package org.medale.exsiter;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * TODO: org.quartz.scheduler.skipUpdateCheck: true
 * StdSchedulerFactory.PROP_SCHED_SKIP_UPDATE_CHECK
 */
public class ExsiterScheduler {

    public static void main(final String[] args) {

        try {
            final Scheduler scheduler = StdSchedulerFactory
                    .getDefaultScheduler();
            scheduler.start();
            scheduler.shutdown();
        } catch (final SchedulerException se) {
            se.printStackTrace();
        }
    }
}
