package com.xkcoding.swagger.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class DataSyncJob extends BaseJob {

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        String source = getJobData(context, "source");
        String target = getJobData(context, "target");
        
        log.info("DataSyncJob 开始执行 - 从 {} 同步到 {}，时间：{}", 
                source != null ? source : "默认源",
                target != null ? target : "默认目标",
                getCurrentTimeString());
        
        // 模拟数据同步过程
        for (int i = 1; i <= 5; i++) {
            log.info("正在同步第 {} 批数据...", i);
            Thread.sleep(1000);
        }
        
        log.info("DataSyncJob 执行完成 - 数据同步成功");
    }
}
