package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.common.ApiResponse;
import com.xkcoding.swagger.entity.ScheduleJob;
import com.xkcoding.swagger.service.ScheduleJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Controller
@RequestMapping("/schedule")
@Tag(name = "定时任务管理", description = "定时任务的增删改查和状态管理")
public class ScheduleJobController {

    @Autowired
    private ScheduleJobService scheduleJobService;

    // ============================页面控制器============================

    /**
     * 任务管理首页
     */
    @GetMapping("/index")
    public String index(Model model) {
        try {
            List<ScheduleJob> jobs = scheduleJobService.getAllJobs();
            model.addAttribute("jobs", jobs);
            model.addAttribute("jobCount", jobs.size());
            
            // 统计任务状态
            long runningCount = jobs.stream().filter(job -> 
                    job.getStatus() != null && job.getStatus() == ScheduleJob.STATUS_NORMAL).count();
            long pausedCount = jobs.size() - runningCount;
            
            model.addAttribute("runningCount", runningCount);
            model.addAttribute("pausedCount", pausedCount);
            
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            model.addAttribute("error", "获取任务列表失败：" + e.getMessage());
        }
        
        return "schedule/index";
    }

    /**
     * 新增任务页面
     */
    @GetMapping("/add")
    public String addJobPage(Model model) {
        model.addAttribute("job", new ScheduleJob());
        model.addAttribute("jobClasses", scheduleJobService.getAvailableJobClasses());
        return "schedule/add";
    }

    /**
     * 编辑任务页面
     */
    @GetMapping("/edit/{jobName}/{jobGroup}")
    public String editJobPage(@PathVariable String jobName, @PathVariable String jobGroup, Model model) {
        try {
            ScheduleJob job = scheduleJobService.getJobInfo(jobName, jobGroup);
            if (job == null) {
                model.addAttribute("error", "任务不存在");
                return "redirect:/schedule/index";
            }
            
            model.addAttribute("job", job);
            model.addAttribute("jobClasses", scheduleJobService.getAvailableJobClasses());
            return "schedule/edit";
            
        } catch (Exception e) {
            log.error("获取任务信息失败", e);
            model.addAttribute("error", "获取任务信息失败：" + e.getMessage());
            return "redirect:/schedule/index";
        }
    }

    // ============================API接口============================

    /**
     * 获取所有定时任务列表
     */
    @GetMapping("/api/jobs")
    @ResponseBody
    @Operation(summary = "获取任务列表", description = "获取所有定时任务的列表")
    public ApiResponse<List<ScheduleJob>> getAllJobs() {
        try {
            List<ScheduleJob> jobs = scheduleJobService.getAllJobs();
            return ApiResponse.success(jobs);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return ApiResponse.error("获取任务列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个任务信息
     */
    @GetMapping("/api/jobs/{jobName}/{jobGroup}")
    @ResponseBody
    @Operation(summary = "获取任务详情", description = "根据任务名称和组名获取任务详细信息")
    public ApiResponse<ScheduleJob> getJobInfo(
            @Parameter(description = "任务名称") @PathVariable String jobName,
            @Parameter(description = "任务组名") @PathVariable String jobGroup) {
        try {
            ScheduleJob job = scheduleJobService.getJobInfo(jobName, jobGroup);
            if (job == null) {
                return ApiResponse.error("任务不存在");
            }
            return ApiResponse.success(job);
        } catch (Exception e) {
            log.error("获取任务信息失败", e);
            return ApiResponse.error("获取任务信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建定时任务
     */
    @PostMapping("/api/jobs")
    @ResponseBody
    @Operation(summary = "创建任务", description = "创建并启动新的定时任务")
    public ApiResponse<String> createJob(@RequestBody ScheduleJob scheduleJob) {
        try {
            // 参数验证
            if (scheduleJob.getJobName() == null || scheduleJob.getJobName().trim().isEmpty()) {
                return ApiResponse.error("任务名称不能为空");
            }
            if (scheduleJob.getJobGroup() == null || scheduleJob.getJobGroup().trim().isEmpty()) {
                scheduleJob.setJobGroup("DEFAULT");
            }
            if (scheduleJob.getJobClassName() == null || scheduleJob.getJobClassName().trim().isEmpty()) {
                return ApiResponse.error("任务类名不能为空");
            }
            if (scheduleJob.getCronExpression() == null || scheduleJob.getCronExpression().trim().isEmpty()) {
                return ApiResponse.error("Cron表达式不能为空");
            }

            // 验证Cron表达式
            if (!scheduleJobService.isValidCronExpression(scheduleJob.getCronExpression())) {
                return ApiResponse.error("Cron表达式格式不正确");
            }

            // 检查任务是否已存在
            if (scheduleJobService.isJobExists(scheduleJob.getJobName(), scheduleJob.getJobGroup())) {
                return ApiResponse.error("任务已存在");
            }

            // 设置默认值
            if (scheduleJob.getStatus() == null) {
                scheduleJob.setStatus(ScheduleJob.STATUS_NORMAL);
            }
            scheduleJob.setCreateTime(LocalDateTime.now());
            scheduleJob.setUpdateTime(LocalDateTime.now());

            boolean success = scheduleJobService.createAndStartJob(scheduleJob);
            if (success) {
                return ApiResponse.success("任务创建成功");
            } else {
                return ApiResponse.error("任务创建失败");
            }

        } catch (SchedulerException e) {
            log.error("创建任务失败", e);
            return ApiResponse.error("创建任务失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("创建任务异常", e);
            return ApiResponse.error("创建任务异常：" + e.getMessage());
        }
    }

    /**
     * 更新任务的Cron表达式
     */
    @PutMapping("/api/jobs/{jobName}/{jobGroup}/cron")
    @ResponseBody
    @Operation(summary = "更新Cron表达式", description = "修改定时任务的执行时间")
    public ApiResponse<String> updateJobCron(
            @Parameter(description = "任务名称") @PathVariable String jobName,
            @Parameter(description = "任务组名") @PathVariable String jobGroup,
            @Parameter(description = "新的Cron表达式") @RequestParam String cronExpression) {
        try {
            if (cronExpression == null || cronExpression.trim().isEmpty()) {
                return ApiResponse.error("Cron表达式不能为空");
            }

            if (!scheduleJobService.isValidCronExpression(cronExpression)) {
                return ApiResponse.error("Cron表达式格式不正确");
            }

            boolean success = scheduleJobService.updateJobCron(jobName, jobGroup, cronExpression);
            if (success) {
                return ApiResponse.success("Cron表达式更新成功");
            } else {
                return ApiResponse.error("Cron表达式更新失败");
            }

        } catch (SchedulerException e) {
            log.error("更新Cron表达式失败", e);
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 暂停定时任务
     */
    @PutMapping("/api/jobs/{jobName}/{jobGroup}/pause")
    @ResponseBody
    @Operation(summary = "暂停任务", description = "暂停指定的定时任务")
    public ApiResponse<String> pauseJob(
            @Parameter(description = "任务名称") @PathVariable String jobName,
            @Parameter(description = "任务组名") @PathVariable String jobGroup) {
        try {
            boolean success = scheduleJobService.pauseJob(jobName, jobGroup);
            if (success) {
                return ApiResponse.success("任务暂停成功");
            } else {
                return ApiResponse.error("任务暂停失败");
            }
        } catch (SchedulerException e) {
            log.error("暂停任务失败", e);
            return ApiResponse.error("暂停失败：" + e.getMessage());
        }
    }

    /**
     * 恢复定时任务
     */
    @PutMapping("/api/jobs/{jobName}/{jobGroup}/resume")
    @ResponseBody
    @Operation(summary = "恢复任务", description = "恢复已暂停的定时任务")
    public ApiResponse<String> resumeJob(
            @Parameter(description = "任务名称") @PathVariable String jobName,
            @Parameter(description = "任务组名") @PathVariable String jobGroup) {
        try {
            boolean success = scheduleJobService.resumeJob(jobName, jobGroup);
            if (success) {
                return ApiResponse.success("任务恢复成功");
            } else {
                return ApiResponse.error("任务恢复失败");
            }
        } catch (SchedulerException e) {
            log.error("恢复任务失败", e);
            return ApiResponse.error("恢复失败：" + e.getMessage());
        }
    }

    /**
     * 立即执行任务
     */
    @PutMapping("/api/jobs/{jobName}/{jobGroup}/run")
    @ResponseBody
    @Operation(summary = "立即执行", description = "立即执行一次定时任务")
    public ApiResponse<String> runJobNow(
            @Parameter(description = "任务名称") @PathVariable String jobName,
            @Parameter(description = "任务组名") @PathVariable String jobGroup) {
        try {
            boolean success = scheduleJobService.runJobNow(jobName, jobGroup);
            if (success) {
                return ApiResponse.success("任务执行成功");
            } else {
                return ApiResponse.error("任务执行失败");
            }
        } catch (SchedulerException e) {
            log.error("执行任务失败", e);
            return ApiResponse.error("执行失败：" + e.getMessage());
        }
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/api/jobs/{jobName}/{jobGroup}")
    @ResponseBody
    @Operation(summary = "删除任务", description = "删除指定的定时任务")
    public ApiResponse<String> deleteJob(
            @Parameter(description = "任务名称") @PathVariable String jobName,
            @Parameter(description = "任务组名") @PathVariable String jobGroup) {
        try {
            boolean success = scheduleJobService.deleteJob(jobName, jobGroup);
            if (success) {
                return ApiResponse.success("任务删除成功");
            } else {
                return ApiResponse.error("任务删除失败");
            }
        } catch (SchedulerException e) {
            log.error("删除任务失败", e);
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 验证Cron表达式
     */
    @GetMapping("/api/cron/validate")
    @ResponseBody
    @Operation(summary = "验证Cron表达式", description = "验证Cron表达式是否有效")
    public ApiResponse<String> validateCronExpression(
            @Parameter(description = "Cron表达式") @RequestParam String cronExpression) {
        try {
            boolean isValid = scheduleJobService.isValidCronExpression(cronExpression);
            if (isValid) {
                return ApiResponse.success("Cron表达式有效");
            } else {
                return ApiResponse.error("Cron表达式无效");
            }
        } catch (Exception e) {
            return ApiResponse.error("验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用的任务类列表
     */
    @GetMapping("/api/job-classes")
    @ResponseBody
    @Operation(summary = "获取任务类列表", description = "获取系统中可用的任务类列表")
    public ApiResponse<List<String>> getAvailableJobClasses() {
        try {
            List<String> jobClasses = scheduleJobService.getAvailableJobClasses();
            return ApiResponse.success(jobClasses);
        } catch (Exception e) {
            log.error("获取任务类列表失败", e);
            return ApiResponse.error("获取任务类列表失败：" + e.getMessage());
        }
    }
}
