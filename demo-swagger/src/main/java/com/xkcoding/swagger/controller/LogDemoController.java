package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.annotation.WebLog;
import com.xkcoding.swagger.common.ApiResponse;
import com.xkcoding.swagger.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * AOP 日志记录示例控制器
 * </p>
 * 
 * 此控制器演示了如何使用 @WebLog 注解记录请求日志
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@RestController
@RequestMapping("/log-demo")
@Tag(name = "AOP 日志示例", description = "AOP 切面日志记录功能演示")
@Slf4j
public class LogDemoController {

    @GetMapping("/simple")
    @WebLog(value = "简单GET请求示例", logArgs = true, logResult = true, logTime = true)
    @Operation(summary = "简单GET请求", description = "演示基本的日志记录功能")
    public ApiResponse<String> simpleGet() {
        return ApiResponse.<String>builder()
                .code(200)
                .message("简单GET请求成功")
                .data("Hello, AOP Logger!")
                .build();
    }

    @GetMapping("/with-params")
    @WebLog(value = "带参数的GET请求示例")
    @Operation(summary = "带参数的GET请求", description = "演示参数记录功能")
    public ApiResponse<Map<String, Object>> getWithParams(@Parameter(description = "用户名", example = "张三") @RequestParam String name, 
                                                         @Parameter(description = "年龄", example = "18") @RequestParam(defaultValue = "18") Integer age) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", age);
        result.put("message", "带参数请求成功");
        result.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("请求成功")
                .data(result)
                .build();
    }

    @PostMapping("/with-body")
    @WebLog(value = "POST请求示例，记录请求体", logArgs = true, logResult = true)
    @Operation(summary = "POST请求示例", description = "演示请求体记录功能")
    public ApiResponse<User> postWithBody(@RequestBody User user) {
        // 模拟业务处理
        if (user.getId() == null) {
            user.setId(999);
        }
        
        log.info("业务处理：收到用户信息 - {}", user);
        
        return ApiResponse.<User>builder()
                .code(200)
                .message("POST请求处理成功")
                .data(user)
                .build();
    }

    @GetMapping("/slow-method")
    @WebLog(value = "慢方法示例，测试执行时间记录", logTime = true)
    @Operation(summary = "慢方法示例", description = "演示执行时间记录功能")
    public ApiResponse<Map<String, Object>> slowMethod() {
        try {
            // 模拟耗时操作
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "慢方法执行完成");
        result.put("duration", "约2秒");
        result.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("慢方法执行成功")
                .data(result)
                .build();
    }

    @GetMapping("/error-demo")
    @WebLog(value = "异常处理示例", logException = true)
    @Operation(summary = "异常处理示例", description = "演示异常记录功能")
    public ApiResponse<String> errorDemo(@Parameter(description = "是否抛出异常", example = "false") @RequestParam(defaultValue = "false") Boolean throwError) {
        if (throwError) {
            throw new RuntimeException("这是一个测试异常，用于演示异常日志记录功能");
        }
        
        return ApiResponse.<String>builder()
                .code(200)
                .message("正常执行，没有异常")
                .data("一切正常")
                .build();
    }

    @PutMapping("/selective-log")
    @WebLog(value = "选择性日志记录示例", logArgs = false, logResult = false, logTime = true, logException = true)
    @Operation(summary = "选择性日志记录", description = "演示选择性记录功能（只记录时间和异常）")
    public ApiResponse<String> selectiveLog(@RequestBody Map<String, Object> data) {
        log.info("这是业务层的日志，参数: {}", data);
        
        return ApiResponse.<String>builder()
                .code(200)
                .message("选择性日志记录完成")
                .data("只记录了执行时间，不记录参数和返回值")
                .build();
    }

    @GetMapping("/no-log")
    @Operation(summary = "无日志记录", description = "此方法没有 @WebLog 注解，不会记录AOP日志")
    public ApiResponse<String> noLog() {
        log.info("这是普通的业务日志，不会被AOP拦截");
        
        return ApiResponse.<String>builder()
                .code(200)
                .message("此方法不记录AOP日志")
                .data("只有业务代码中的日志会输出")
                .build();
    }
}
