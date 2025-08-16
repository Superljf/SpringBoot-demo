package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.common.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 监控相关接口
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@RestController
@RequestMapping("/monitor")
@Api(tags = "Monitor", description = "应用监控相关接口", value = "应用监控相关接口")
public class MonitorController implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public MonitorController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        // 自定义健康检查逻辑
        try {
            // 检查数据库连接
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            // 检查用户表是否存在数据
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_user", Integer.class);
            
            return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("userCount", userCount)
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }

    @GetMapping("/status")
    @ApiOperation(value = "获取应用状态信息", notes = "返回应用的基本状态信息")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 应用基本信息
            status.put("application", "demo-swagger");
            status.put("status", "UP");
            status.put("timestamp", System.currentTimeMillis());
            
            // JVM 信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> jvm = new HashMap<>();
            jvm.put("totalMemory", runtime.totalMemory());
            jvm.put("freeMemory", runtime.freeMemory());
            jvm.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            jvm.put("maxMemory", runtime.maxMemory());
            jvm.put("processors", runtime.availableProcessors());
            status.put("jvm", jvm);
            
            // 数据库状态
            Integer dbStatus = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_user", Integer.class);
            Map<String, Object> database = new HashMap<>();
            database.put("status", dbStatus != null && dbStatus == 1 ? "UP" : "DOWN");
            database.put("userCount", userCount);
            status.put("database", database);
            
            // 系统信息
            Map<String, Object> system = new HashMap<>();
            system.put("osName", System.getProperty("os.name"));
            system.put("osVersion", System.getProperty("os.version"));
            system.put("javaVersion", System.getProperty("java.version"));
            system.put("javaVendor", System.getProperty("java.vendor"));
            status.put("system", system);
            
            return ApiResponse.<Map<String, Object>>builder()
                    .code(200)
                    .message("获取状态成功")
                    .data(status)
                    .build();
                    
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            return ApiResponse.<Map<String, Object>>builder()
                    .code(500)
                    .message("获取状态失败")
                    .data(status)
                    .build();
        }
    }

    @GetMapping("/metrics")
    @ApiOperation(value = "获取应用指标", notes = "返回应用的性能指标")
    public ApiResponse<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // HTTP 请求统计（简化版，实际可以通过 Micrometer 获取更详细的指标）
            metrics.put("timestamp", System.currentTimeMillis());
            
            // 数据库连接池状态（如果使用了连接池）
            Integer activeConnections = 1; // 简化示例
            metrics.put("database.connections.active", activeConnections);
            
            // 用户数据统计
            Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_user", Integer.class);
            metrics.put("business.users.total", totalUsers);
            
            // JVM 指标
            Runtime runtime = Runtime.getRuntime();
            metrics.put("jvm.memory.used", runtime.totalMemory() - runtime.freeMemory());
            metrics.put("jvm.memory.free", runtime.freeMemory());
            metrics.put("jvm.memory.total", runtime.totalMemory());
            metrics.put("jvm.memory.max", runtime.maxMemory());
            
            // 系统负载（简化）
            metrics.put("system.cpu.count", runtime.availableProcessors());
            
            return ApiResponse.<Map<String, Object>>builder()
                    .code(200)
                    .message("获取指标成功")
                    .data(metrics)
                    .build();
                    
        } catch (Exception e) {
            return ApiResponse.<Map<String, Object>>builder()
                    .code(500)
                    .message("获取指标失败: " + e.getMessage())
                    .data(metrics)
                    .build();
        }
    }
}
