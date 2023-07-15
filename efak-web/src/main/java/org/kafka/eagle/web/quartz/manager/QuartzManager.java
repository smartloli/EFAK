/**
 * QuartzManager.java
 * <p>
 * Copyright 2023 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.web.quartz.manager;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.JobConstans;
import org.kafka.eagle.web.quartz.pojo.JobDetails;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/14 22:32
 * @Version: 3.4.0
 */
@Component
@Slf4j
public class QuartzManager {

    @Autowired
    private Scheduler sched;

    /**
     * Add or update job.
     *
     * @param jobClass
     * @param jobName
     * @param jobGroupName
     * @param jobCron
     */
    public Boolean addOrUpdateJob(Class<? extends QuartzJobBean> jobClass, String jobName, String jobGroupName, String jobCron) {
        Boolean status = false;
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            if (trigger == null) {
                addJob(jobClass, jobName, jobGroupName, jobCron);
            } else {
                if (trigger.getCronExpression().equals(jobCron)) {
                    return true;
                }
                updateJob(jobName, jobGroupName, jobCron);
            }
            status = true;
        } catch (SchedulerException e) {
            log.error("Add or update job has error, msg is {}", e);
        }
        return status;
    }

    /**
     * Add a job
     *
     * @param jobClass
     * @param jobName
     * @param jobGroupName
     * @param jobCron      (0/5 * * * * ?)
     */
    public void addJob(Class<? extends QuartzJobBean> jobClass, String jobName, String jobGroupName, String jobCron) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroupName)
                    .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobCron)).startNow().build();

            sched.scheduleJob(jobDetail, trigger);
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (SchedulerException e) {
            log.error("Add a job has error, msg is {}", e);
        }
    }

    public void addJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, int jobTime) {
        addJob(jobClass, jobName, jobGroupName, jobTime, -1);
    }

    public void addJob(Class<? extends Job> jobClass, String jobName, String jobGroupName, int jobTime, int jobTimes) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName)// 任务名称和组构成任务key
                    .build();
            Trigger trigger;
            if (jobTimes < 0) {
                trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroupName)
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1).withIntervalInSeconds(jobTime))
                        .startNow().build();
            } else {
                trigger = TriggerBuilder
                        .newTrigger().withIdentity(jobName, jobGroupName).withSchedule(SimpleScheduleBuilder
                                .repeatSecondlyForever(1).withIntervalInSeconds(jobTime).withRepeatCount(jobTimes))
                        .startNow().build();
            }
            sched.scheduleJob(jobDetail, trigger);
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (SchedulerException e) {
            log.error("Add a job has error, msg is {}", e);
        }
    }

    public void updateJob(String jobName, String jobGroupName, String jobTime) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobTime)).build();
            sched.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            log.error("Update job has error, msg is {}", e);
        }
    }

    public Boolean deleteJob(String jobName, String jobGroupName) {
        Boolean status = false;
        try {
            sched.pauseTrigger(TriggerKey.triggerKey(jobName, jobGroupName));
            sched.unscheduleJob(TriggerKey.triggerKey(jobName, jobGroupName));
            sched.deleteJob(new JobKey(jobName, jobGroupName));
            status = true;
        } catch (Exception e) {
            log.error("Delete job has error, msg is {}", e);
        }
        return status;
    }

    public Boolean pauseJob(String jobName, String jobGroupName) {
        Boolean status = false;
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            sched.pauseJob(jobKey);
            status = true;
        } catch (SchedulerException e) {
            log.error("Pause job has error, msg is {}", e);
        }
        return status;
    }

    public Boolean resumeJob(String jobName, String jobGroupName) {
        Boolean status = false;
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            sched.resumeJob(jobKey);
            status = false;
        } catch (SchedulerException e) {
            log.error("Resume job has error, msg is {}", e);
        }
        return status;
    }

    public void runAJobNow(String jobName, String jobGroupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            sched.triggerJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.error("Run a job has error, msg is {}", e);
        }
    }

    public PageInfo<JobDetails> queryAllJobBean(int pageNum, int pageSize, String search) {
        PageHelper.startPage(pageNum, pageSize);
        List<JobDetails> jobList = new ArrayList<>();
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = sched.getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = sched.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    JobDetails jobDetails = new JobDetails();
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        jobDetails.setCronExpression(cronTrigger.getCronExpression());
                        jobDetails.setTimeZone(cronTrigger.getTimeZone().toZoneId().toString());
                    }
                    jobDetails.setTriggerGroupName(trigger.getKey().getName());
                    jobDetails.setTriggerName(trigger.getKey().getGroup());
                    jobDetails.setJobGroupName(jobKey.getGroup());
                    jobDetails.setJobName(jobKey.getName());
                    jobDetails.setStartTime(trigger.getStartTime());
                    jobDetails.setJobClassName(sched.getJobDetail(jobKey).getJobClass().getName());
                    jobDetails.setNextFireTime(trigger.getNextFireTime());
                    jobDetails.setPreviousFireTime(trigger.getPreviousFireTime());
                    jobDetails.setStatus(sched.getTriggerState(trigger.getKey()).name());
                    if (StrUtil.isNotBlank(search)) {
                        if (JobConstans.JOBS.containsKey(jobKey.getName()) && JobConstans.JOBS.get(jobKey.getName()).contains(search)) {
                            jobList.add(jobDetails);
                        }
                    }
                    if (StrUtil.isBlank(search)) {
                        jobList.add(jobDetails);
                    }
                }
            }
        } catch (SchedulerException e) {
            log.error("Get all job has error, msg is {}", e);
        }
        return new PageInfo<>(jobList);
    }

    public List<String> getAllJobNames() {
        List<String> jobNames = new ArrayList<>();
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = sched.getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                jobNames.add(JobConstans.JOBS.get(jobKey.getName()));
            }
        } catch (SchedulerException e) {
            log.error("Find a job has error, msg is {}", e);
        }
        return jobNames;

    }

    public List<Map<String, Object>> queryAllJob() {
        List<Map<String, Object>> jobList = null;
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = sched.getJobKeys(matcher);
            jobList = new ArrayList<>();
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = sched.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("jobName", jobKey.getName());
                    map.put("jobGroupName", jobKey.getGroup());
                    map.put("description", "trigger:" + trigger.getKey());
                    Trigger.TriggerState triggerState = sched.getTriggerState(trigger.getKey());
                    map.put("jobStatus", triggerState.name());
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                        map.put("jobTime", cronExpression);
                    }
                    jobList.add(map);
                }
            }
        } catch (SchedulerException e) {
            log.error("Query all job has error, msg is {}", e);
        }
        return jobList;
    }

    public List<Map<String, Object>> queryRunJon() {
        List<Map<String, Object>> jobList = null;
        try {
            List<JobExecutionContext> executingJobs = sched.getCurrentlyExecutingJobs();
            jobList = new ArrayList<>(executingJobs.size());
            for (JobExecutionContext executingJob : executingJobs) {
                Map<String, Object> map = new HashMap<>();
                JobDetail jobDetail = executingJob.getJobDetail();
                JobKey jobKey = jobDetail.getKey();
                Trigger trigger = executingJob.getTrigger();
                map.put("jobName", jobKey.getName());
                map.put("jobGroupName", jobKey.getGroup());
                map.put("description", "trigger:" + trigger.getKey());
                Trigger.TriggerState triggerState = sched.getTriggerState(trigger.getKey());
                map.put("jobStatus", triggerState.name());
                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    map.put("jobTime", cronExpression);
                }
                jobList.add(map);
            }
        } catch (SchedulerException e) {
            log.error("Query run job has error, msg is {}", e);
        }
        return jobList;
    }
}
