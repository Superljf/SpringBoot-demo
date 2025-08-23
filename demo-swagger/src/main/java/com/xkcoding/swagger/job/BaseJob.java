package com.xkcoding.swagger.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
public abstract class BaseJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();
        String triggerName = context.getTrigger().getKey().getName();
        
        log.info("定时任务开始执行 - 任务名称：{}，任务组：{}，触发器：{}", 
                jobName, jobGroup, triggerName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行具体任务
            doExecute(context);
            
            long endTime = System.currentTimeMillis();
            log.info("定时任务执行成功 - 任务名称：{}，执行时间：{}ms", 
                    jobName, endTime - startTime);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("定时任务执行失败 - 任务名称：{}，执行时间：{}ms，错误信息：{}", 
                    jobName, endTime - startTime, e.getMessage(), e);
            
            // 抛出JobExecutionException，让Quartz知道任务执行失败
            throw new JobExecutionException(e);
        }
    }

    /**
     * 具体的任务执行逻辑
     * 子类需要实现此方法
     *
     * @param context 任务执行上下文
     * @throws Exception 执行异常
     */
    protected abstract void doExecute(JobExecutionContext context) throws Exception;

    /**
     * 获取任务参数
     *
     * @param context 任务执行上下文
     * @param key 参数key
     * @return 参数值
     */
    protected String getJobData(JobExecutionContext context, String key) {
        return context.getJobDetail().getJobDataMap().getString(key);
    }

    /**
     * 获取当前时间字符串
     *
     * @return 格式化的时间字符串
     */
    protected String getCurrentTimeString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
