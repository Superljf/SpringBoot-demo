package com.xkcoding.swagger.service.impl;

import com.xkcoding.swagger.entity.ScheduleJob;
import com.xkcoding.swagger.service.ScheduleJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
public class ScheduleJobServiceImpl implements ScheduleJobService {

    @Autowired
    private Scheduler scheduler;

    @Override
    public boolean createAndStartJob(ScheduleJob scheduleJob) throws SchedulerException {
        if (scheduleJob == null || !StringUtils.hasText(scheduleJob.getJobName()) 
                || !StringUtils.hasText(scheduleJob.getJobGroup())
                || !StringUtils.hasText(scheduleJob.getJobClassName())
                || !StringUtils.hasText(scheduleJob.getCronExpression())) {
            throw new IllegalArgumentException("任务参数不完整");
        }

        // 验证cron表达式
        if (!isValidCronExpression(scheduleJob.getCronExpression())) {
            throw new IllegalArgumentException("Cron表达式格式不正确");
        }

        // 检查任务是否已存在
        if (isJobExists(scheduleJob.getJobName(), scheduleJob.getJobGroup())) {
            throw new SchedulerException("任务已存在：" + scheduleJob.getJobName());
        }

        try {
            // 构建JobDetail
            Class<?> jobClass = Class.forName(scheduleJob.getJobClassName());
            
            @SuppressWarnings("unchecked")
            Class<? extends Job> jobClazz = (Class<? extends Job>) jobClass;
            
            JobDetail jobDetail = JobBuilder.newJob(jobClazz)
                    .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                    .withDescription(scheduleJob.getDescription())
                    .build();

            // 设置任务参数
            if (StringUtils.hasText(scheduleJob.getJobData())) {
                jobDetail.getJobDataMap().put("jobData", scheduleJob.getJobData());
                // 解析JSON参数并设置到JobDataMap（简化处理）
                String[] pairs = scheduleJob.getJobData().split(",");
                for (String pair : pairs) {
                    if (pair.contains("=")) {
                        String[] kv = pair.split("=", 2);
                        if (kv.length == 2) {
                            jobDetail.getJobDataMap().put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
            }

            // 构建Trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(scheduleJob.getJobName() + "_trigger", scheduleJob.getJobGroup())
                    .withDescription(scheduleJob.getDescription())
                    .withSchedule(CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression()))
                    .build();

            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            // 如果状态为暂停，则暂停任务
            if (scheduleJob.getStatus() != null && scheduleJob.getStatus() == ScheduleJob.STATUS_PAUSED) {
                pauseJob(scheduleJob.getJobName(), scheduleJob.getJobGroup());
            }

            log.info("定时任务创建成功：{}.{}", scheduleJob.getJobGroup(), scheduleJob.getJobName());
            return true;

        } catch (ClassNotFoundException e) {
            throw new SchedulerException("任务类不存在：" + scheduleJob.getJobClassName(), e);
        } catch (Exception e) {
            throw new SchedulerException("创建任务失败", e);
        }
    }

    @Override
    public boolean pauseJob(String jobName, String jobGroup) throws SchedulerException {
        if (!StringUtils.hasText(jobName) || !StringUtils.hasText(jobGroup)) {
            throw new IllegalArgumentException("任务名称和组名不能为空");
        }

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("任务不存在：" + jobName);
        }

        scheduler.pauseJob(jobKey);
        log.info("任务暂停成功：{}.{}", jobGroup, jobName);
        return true;
    }

    @Override
    public boolean resumeJob(String jobName, String jobGroup) throws SchedulerException {
        if (!StringUtils.hasText(jobName) || !StringUtils.hasText(jobGroup)) {
            throw new IllegalArgumentException("任务名称和组名不能为空");
        }

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("任务不存在：" + jobName);
        }

        scheduler.resumeJob(jobKey);
        log.info("任务恢复成功：{}.{}", jobGroup, jobName);
        return true;
    }

    @Override
    public boolean deleteJob(String jobName, String jobGroup) throws SchedulerException {
        if (!StringUtils.hasText(jobName) || !StringUtils.hasText(jobGroup)) {
            throw new IllegalArgumentException("任务名称和组名不能为空");
        }

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("任务不存在：" + jobName);
        }

        boolean result = scheduler.deleteJob(jobKey);
        if (result) {
            log.info("任务删除成功：{}.{}", jobGroup, jobName);
        }
        return result;
    }

    @Override
    public boolean updateJobCron(String jobName, String jobGroup, String cronExpression) throws SchedulerException {
        if (!StringUtils.hasText(jobName) || !StringUtils.hasText(jobGroup) 
                || !StringUtils.hasText(cronExpression)) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 验证cron表达式
        if (!isValidCronExpression(cronExpression)) {
            throw new IllegalArgumentException("Cron表达式格式不正确");
        }

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger", jobGroup);
        CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        
        if (oldTrigger == null) {
            throw new SchedulerException("任务触发器不存在：" + jobName);
        }

        // 创建新的触发器
        CronTrigger newTrigger = oldTrigger.getTriggerBuilder()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        // 重新调度
        scheduler.rescheduleJob(triggerKey, newTrigger);
        log.info("任务Cron表达式更新成功：{}.{}，新表达式：{}", jobGroup, jobName, cronExpression);
        return true;
    }

    @Override
    public boolean runJobNow(String jobName, String jobGroup) throws SchedulerException {
        if (!StringUtils.hasText(jobName) || !StringUtils.hasText(jobGroup)) {
            throw new IllegalArgumentException("任务名称和组名不能为空");
        }

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("任务不存在：" + jobName);
        }

        scheduler.triggerJob(jobKey);
        log.info("任务立即执行：{}.{}", jobGroup, jobName);
        return true;
    }

    @Override
    public List<ScheduleJob> getAllJobs() throws SchedulerException {
        List<ScheduleJob> jobList = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                
                ScheduleJob scheduleJob = new ScheduleJob();
                scheduleJob.setJobName(jobKey.getName());
                scheduleJob.setJobGroup(jobKey.getGroup());
                scheduleJob.setDescription(jobDetail.getDescription());
                scheduleJob.setJobClassName(jobDetail.getJobClass().getName());

                if (!triggers.isEmpty()) {
                    Trigger trigger = triggers.get(0);
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        scheduleJob.setCronExpression(cronTrigger.getCronExpression());
                    }

                    // 获取触发器状态
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    scheduleJob.setStatus(triggerState == Trigger.TriggerState.PAUSED ? 
                            ScheduleJob.STATUS_PAUSED : ScheduleJob.STATUS_NORMAL);

                    // 设置执行时间
                    Date nextFireTime = trigger.getNextFireTime();
                    if (nextFireTime != null) {
                        scheduleJob.setNextFireTime(LocalDateTime.ofInstant(
                                nextFireTime.toInstant(), ZoneId.systemDefault()));
                    }

                    Date prevFireTime = trigger.getPreviousFireTime();
                    if (prevFireTime != null) {
                        scheduleJob.setPrevFireTime(LocalDateTime.ofInstant(
                                prevFireTime.toInstant(), ZoneId.systemDefault()));
                    }
                }

                // 获取任务参数
                if (jobDetail.getJobDataMap().containsKey("jobData")) {
                    scheduleJob.setJobData(jobDetail.getJobDataMap().getString("jobData"));
                }

                jobList.add(scheduleJob);
            }
        }

        return jobList;
    }

    @Override
    public ScheduleJob getJobInfo(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        
        if (jobDetail == null) {
            return null;
        }

        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        
        ScheduleJob scheduleJob = new ScheduleJob();
        scheduleJob.setJobName(jobKey.getName());
        scheduleJob.setJobGroup(jobKey.getGroup());
        scheduleJob.setDescription(jobDetail.getDescription());
        scheduleJob.setJobClassName(jobDetail.getJobClass().getName());

        if (!triggers.isEmpty()) {
            Trigger trigger = triggers.get(0);
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                scheduleJob.setCronExpression(cronTrigger.getCronExpression());
            }

            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            scheduleJob.setStatus(triggerState == Trigger.TriggerState.PAUSED ? 
                    ScheduleJob.STATUS_PAUSED : ScheduleJob.STATUS_NORMAL);
        }

        if (jobDetail.getJobDataMap().containsKey("jobData")) {
            scheduleJob.setJobData(jobDetail.getJobDataMap().getString("jobData"));
        }

        return scheduleJob;
    }

    @Override
    public boolean isJobExists(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        return scheduler.checkExists(jobKey);
    }

    @Override
    public boolean isValidCronExpression(String cronExpression) {
        try {
            CronScheduleBuilder.cronSchedule(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getAvailableJobClasses() {
        return Arrays.asList(
                "com.xkcoding.swagger.job.SimpleJob",
                "com.xkcoding.swagger.job.DataSyncJob"
        );
    }
}
