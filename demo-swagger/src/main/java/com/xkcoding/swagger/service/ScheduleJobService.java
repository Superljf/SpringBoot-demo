package com.xkcoding.swagger.service;

import com.xkcoding.swagger.entity.ScheduleJob;
import org.quartz.SchedulerException;

import java.util.List;

public interface ScheduleJobService {

    /**
     * 创建并启动定时任务
     *
     * @param scheduleJob 任务信息
     * @return 是否成功
     * @throws SchedulerException 调度异常
     */
    boolean createAndStartJob(ScheduleJob scheduleJob) throws SchedulerException;

    /**
     * 暂停定时任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组名
     * @return 是否成功
     * @throws SchedulerException 调度异常
     */
    boolean pauseJob(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 恢复定时任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组名
     * @return 是否成功
     * @throws SchedulerException 调度异常
     */
    boolean resumeJob(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 删除定时任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组名
     * @return 是否成功
     * @throws SchedulerException 调度异常
     */
    boolean deleteJob(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 修改定时任务的cron表达式
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组名
     * @param cronExpression 新的cron表达式
     * @return 是否成功
     * @throws SchedulerException 调度异常
     */
    boolean updateJobCron(String jobName, String jobGroup, String cronExpression) throws SchedulerException;

    /**
     * 立即执行一次定时任务
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组名
     * @return 是否成功
     * @throws SchedulerException 调度异常
     */
    boolean runJobNow(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 获取所有定时任务列表
     *
     * @return 任务列表
     * @throws SchedulerException 调度异常
     */
    List<ScheduleJob> getAllJobs() throws SchedulerException;

    /**
     * 根据任务名称和组名获取任务信息
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组名
     * @return 任务信息
     * @throws SchedulerException 调度异常
     */
    ScheduleJob getJobInfo(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 检查任务是否存在
     *
     * @param jobName 任务名称
     * @param jobGroup 任务组名
     * @return 是否存在
     * @throws SchedulerException 调度异常
     */
    boolean isJobExists(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 验证cron表达式是否有效
     *
     * @param cronExpression cron表达式
     * @return 是否有效
     */
    boolean isValidCronExpression(String cronExpression);

    /**
     * 获取可用的任务类列表
     *
     * @return 任务类列表
     */
    List<String> getAvailableJobClasses();
}
