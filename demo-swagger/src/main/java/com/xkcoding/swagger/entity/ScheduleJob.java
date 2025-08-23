package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Schema(description = "定时任务")
public class ScheduleJob implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String jobId;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String jobName;

    /**
     * 任务组名
     */
    @Schema(description = "任务组名")
    private String jobGroup;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述")
    private String description;

    /**
     * 任务类名
     */
    @Schema(description = "任务类名")
    private String jobClassName;

    /**
     * cron表达式
     */
    @Schema(description = "cron表达式")
    private String cronExpression;

    /**
     * 任务状态：0-暂停，1-正常
     */
    @Schema(description = "任务状态", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 任务参数
     */
    @Schema(description = "任务参数")
    private String jobData;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 下次执行时间
     */
    @Schema(description = "下次执行时间")
    private LocalDateTime nextFireTime;

    /**
     * 上次执行时间
     */
    @Schema(description = "上次执行时间")
    private LocalDateTime prevFireTime;

    // 状态常量
    public static final int STATUS_PAUSED = 0;
    public static final int STATUS_NORMAL = 1;

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case STATUS_PAUSED:
                return "暂停";
            case STATUS_NORMAL:
                return "正常";
            default:
                return "未知";
        }
    }
}
