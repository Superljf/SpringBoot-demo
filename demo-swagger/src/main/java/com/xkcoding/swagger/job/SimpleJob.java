package com.xkcoding.swagger.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SimpleJob extends BaseJob {

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        String message = getJobData(context, "message");
        if (message == null) {
            message = "Hello Quartz!";
        }
        
        log.info("SimpleJob 正在执行 - 消息：{}，当前时间：{}", message, getCurrentTimeString());
        
        // 模拟任务执行
        Thread.sleep(2000);
        
        log.info("SimpleJob 执行完成");
    }
}
