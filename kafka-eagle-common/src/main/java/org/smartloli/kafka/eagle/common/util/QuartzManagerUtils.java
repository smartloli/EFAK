/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmQueue;

/**
 * Used to schedule and send different types of alarm queue information. Such as
 * email, dingding, wechat etc.
 * 
 * @author smartloli.
 *
 *         Created by Oct 24, 2019
 */
public class QuartzManagerUtils {

	public static final String KE_JOB_GROUP_NAME = "KE_JOB_GROUP_NAME";
	private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

	/** Add new job. */
	public static void addJob(BaseJobContext jobContext, String jobName, Class<? extends Job> jobClass, String cron) {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, KE_JOB_GROUP_NAME).build();
			jobDetail.getJobDataMap().put(AlarmQueue.JOB_PARAMS, jobContext);
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
			triggerBuilder.withIdentity("ke_trigger_name_" + new Date().getTime(), "ke_trigger_group_" + new Date().getTime());
			triggerBuilder.startNow();
			triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
			CronTrigger trigger = (CronTrigger) triggerBuilder.build();
			sched.scheduleJob(jobDetail, trigger);
			if (!sched.isShutdown()) {
				sched.start();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Modify job. */
	public static void modifyJobTime(String jobName, String cron) {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			TriggerKey triggerKey = TriggerKey.triggerKey("ke_trigger_name_" + new Date().getTime(), "ke_trigger_group_" + new Date().getTime());
			CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
			if (trigger == null) {
				return;
			}

			String oldTime = trigger.getCronExpression();
			if (!oldTime.equalsIgnoreCase(cron)) {
				TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
				triggerBuilder.withIdentity("ke_trigger_name_" + new Date().getTime(), "ke_trigger_group_" + new Date().getTime());
				triggerBuilder.startNow();
				triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
				trigger = (CronTrigger) triggerBuilder.build();
				sched.rescheduleJob(triggerKey, trigger);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Remove job. */
	public static void removeJob(String jobName) {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			TriggerKey triggerKey = TriggerKey.triggerKey("ke_trigger_name_" + new Date().getTime(), "ke_trigger_group_" + new Date().getTime());
			sched.pauseTrigger(triggerKey);
			sched.unscheduleJob(triggerKey);
			sched.deleteJob(JobKey.jobKey(jobName, KE_JOB_GROUP_NAME));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Start all jobs. */
	public static void startJobs() {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			sched.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Shutdown all jobs. */
	public static void shutdownJobs() {
		try {
			Scheduler sched = schedulerFactory.getScheduler();
			if (!sched.isShutdown()) {
				sched.shutdown();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get quartz cron date. {@code delayMill} unit second, such 1 sec.
	 * 
	 */
	public static String getCron(final Date date, int delaySecond) {
		SimpleDateFormat sdf = new SimpleDateFormat("ss mm HH dd MM ? yyyy");
		String formatTimeStr = "";
		if (date != null) {
			try {
				formatTimeStr = sdf.format(sdf.parse(sdf.format(date.getTime() + delaySecond * 1000)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return formatTimeStr;
	}
}
