package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.annotation.RequirePermission;
import com.xkcoding.swagger.annotation.WebLog;
import com.xkcoding.swagger.common.ApiResponse;
import com.xkcoding.swagger.common.RabbitMQConstants;
import com.xkcoding.swagger.entity.MessageInfo;
import com.xkcoding.swagger.service.rabbitmq.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * <p>
 * RabbitMQ 消息队列控制器
 * </p>
 * 
 * 提供各种消息模式的REST API接口
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@RestController
@RequestMapping("/rabbitmq")
@Tag(name = "RabbitMQ消息队列", description = "RabbitMQ各种消息模式的API接口")
public class RabbitMQController {

    @Autowired
    private DirectProducerService directProducerService;

    @Autowired
    private FanoutProducerService fanoutProducerService;

    @Autowired
    private TopicProducerService topicProducerService;

    @Autowired
    private DelayProducerService delayProducerService;

    // ==================== 直接队列模式 API ====================

    @PostMapping("/direct/send")
    @WebLog("发送直接队列消息")
    @RequirePermission("rabbitmq:direct:send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "发送直接队列消息", description = "发送消息到直接队列，基于精确路由键匹配")
    public ApiResponse<String> sendDirectMessage(
            @Parameter(description = "消息内容", example = "这是一条直接队列消息")
            @RequestParam @NotBlank String message) {
        try {
            directProducerService.sendDirectMessage(message);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("直接队列消息发送成功")
                    .data("消息已发送到直接队列")
                    .build();
        } catch (Exception e) {
            log.error("发送直接队列消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("直接队列消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/direct/send-with-routing-key")
    @WebLog("发送自定义路由键直接队列消息")
    @RequirePermission("rabbitmq:direct:send")
    @Operation(summary = "发送自定义路由键直接队列消息", description = "发送消息到直接队列，使用自定义路由键")
    public ApiResponse<String> sendDirectMessageWithRoutingKey(
            @Parameter(description = "消息内容", example = "这是一条自定义路由键消息")
            @RequestParam @NotBlank String message,
            @Parameter(description = "路由键", example = "custom.routing.key")
            @RequestParam @NotBlank String routingKey) {
        try {
            directProducerService.sendDirectMessage(message, routingKey);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("自定义路由键直接队列消息发送成功")
                    .data("消息已发送到直接队列，路由键: " + routingKey)
                    .build();
        } catch (Exception e) {
            log.error("发送自定义路由键直接队列消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("自定义路由键直接队列消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/direct/send-object")
    @WebLog("发送复杂对象直接队列消息")
    @RequirePermission("rabbitmq:direct:send")
    @Operation(summary = "发送复杂对象直接队列消息", description = "发送复杂消息对象到直接队列")
    public ApiResponse<String> sendDirectObjectMessage(@RequestBody @Valid MessageInfo messageInfo) {
        try {
            directProducerService.sendDirectMessage(messageInfo);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("复杂对象直接队列消息发送成功")
                    .data("消息ID: " + messageInfo.getMessageId())
                    .build();
        } catch (Exception e) {
            log.error("发送复杂对象直接队列消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("复杂对象直接队列消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    // ==================== 分列模式 API ====================

    @PostMapping("/fanout/send")
    @WebLog("发送广播消息")
    @RequirePermission("rabbitmq:fanout:send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "发送广播消息", description = "发送消息到分列交换机，广播到所有绑定队列")
    public ApiResponse<String> sendFanoutMessage(
            @Parameter(description = "消息内容", example = "这是一条广播消息")
            @RequestParam @NotBlank String message) {
        try {
            fanoutProducerService.sendFanoutMessage(message);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("广播消息发送成功")
                    .data("消息已广播到所有绑定队列")
                    .build();
        } catch (Exception e) {
            log.error("发送广播消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("广播消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/fanout/send-notification")
    @WebLog("发送通知广播消息")
    @RequirePermission("rabbitmq:fanout:send")
    @Operation(summary = "发送通知广播消息", description = "发送通知类型的广播消息")
    public ApiResponse<String> sendNotificationMessage(
            @Parameter(description = "通知标题", example = "系统维护通知")
            @RequestParam @NotBlank String title,
            @Parameter(description = "通知内容", example = "系统将于今晚22:00-24:00进行维护")
            @RequestParam @NotBlank String content,
            @Parameter(description = "通知级别", example = "INFO")
            @RequestParam @NotBlank String level) {
        try {
            fanoutProducerService.sendNotificationMessage(title, content, level);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("通知广播消息发送成功")
                    .data("通知已广播: " + title)
                    .build();
        } catch (Exception e) {
            log.error("发送通知广播消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("通知广播消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    // ==================== 主题模式 API ====================

    @PostMapping("/topic/send-user-email")
    @WebLog("发送用户邮件主题消息")
    @RequirePermission("rabbitmq:topic:send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "发送用户邮件主题消息", description = "发送用户邮件相关的主题消息")
    public ApiResponse<String> sendUserEmailMessage(
            @Parameter(description = "邮件内容", example = "欢迎注册我们的系统")
            @RequestParam @NotBlank String message) {
        try {
            topicProducerService.sendUserEmailMessage(message);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("用户邮件主题消息发送成功")
                    .data("邮件消息已发送，路由键: " + RabbitMQConstants.TOPIC_ROUTING_KEY_USER_EMAIL)
                    .build();
        } catch (Exception e) {
            log.error("发送用户邮件主题消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("用户邮件主题消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/topic/send-user-sms")
    @WebLog("发送用户短信主题消息")
    @RequirePermission("rabbitmq:topic:send")
    @Operation(summary = "发送用户短信主题消息", description = "发送用户短信相关的主题消息")
    public ApiResponse<String> sendUserSmsMessage(
            @Parameter(description = "短信内容", example = "您的验证码是: 123456")
            @RequestParam @NotBlank String message) {
        try {
            topicProducerService.sendUserSmsMessage(message);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("用户短信主题消息发送成功")
                    .data("短信消息已发送，路由键: " + RabbitMQConstants.TOPIC_ROUTING_KEY_USER_SMS)
                    .build();
        } catch (Exception e) {
            log.error("发送用户短信主题消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("用户短信主题消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/topic/send-order-create")
    @WebLog("发送订单创建主题消息")
    @RequirePermission("rabbitmq:topic:send")
    @Operation(summary = "发送订单创建主题消息", description = "发送订单创建相关的主题消息")
    public ApiResponse<String> sendOrderCreateMessage(
            @Parameter(description = "订单信息", example = "订单创建成功，订单号: ORD001")
            @RequestParam @NotBlank String message) {
        try {
            topicProducerService.sendOrderCreateMessage(message);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("订单创建主题消息发送成功")
                    .data("订单创建消息已发送，路由键: " + RabbitMQConstants.TOPIC_ROUTING_KEY_ORDER_CREATE)
                    .build();
        } catch (Exception e) {
            log.error("发送订单创建主题消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("订单创建主题消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/topic/send-custom")
    @WebLog("发送自定义主题消息")
    @RequirePermission("rabbitmq:topic:send")
    @Operation(summary = "发送自定义主题消息", description = "发送自定义路由键的主题消息")
    public ApiResponse<String> sendCustomTopicMessage(
            @Parameter(description = "消息内容", example = "自定义主题消息")
            @RequestParam @NotBlank String message,
            @Parameter(description = "路由键", example = "custom.topic.key")
            @RequestParam @NotBlank String routingKey) {
        try {
            topicProducerService.sendTopicMessage(message, routingKey);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("自定义主题消息发送成功")
                    .data("消息已发送，路由键: " + routingKey)
                    .build();
        } catch (Exception e) {
            log.error("发送自定义主题消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("自定义主题消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/topic/send-user-notification")
    @WebLog("发送用户通知消息")
    @RequirePermission("rabbitmq:topic:send")
    @Operation(summary = "发送用户通知消息", description = "发送用户通知相关的主题消息")
    public ApiResponse<String> sendUserNotification(
            @Parameter(description = "用户ID", example = "user001")
            @RequestParam @NotBlank String userId,
            @Parameter(description = "通知类型", example = "email")
            @RequestParam @NotBlank String notifyType,
            @Parameter(description = "通知内容", example = "您有新的消息")
            @RequestParam @NotBlank String content) {
        try {
            topicProducerService.sendUserNotification(userId, notifyType, content);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("用户通知消息发送成功")
                    .data("用户通知已发送: " + userId + " - " + notifyType)
                    .build();
        } catch (Exception e) {
            log.error("发送用户通知消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("用户通知消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    // ==================== 延迟队列 API ====================

    @PostMapping("/delay/send")
    @WebLog("发送延迟消息")
    @RequirePermission("rabbitmq:delay:send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "发送延迟消息", description = "发送指定延迟时间的消息")
    public ApiResponse<String> sendDelayMessage(
            @Parameter(description = "消息内容", example = "这是一条延迟消息")
            @RequestParam @NotBlank String message,
            @Parameter(description = "延迟时间（毫秒）", example = "5000")
            @RequestParam @NotNull @Positive Long delayTime) {
        try {
            delayProducerService.sendDelayMessage(message, delayTime);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("延迟消息发送成功")
                    .data("消息将在 " + delayTime + "ms 后被处理")
                    .build();
        } catch (Exception e) {
            log.error("发送延迟消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("延迟消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/delay/send-5s")
    @WebLog("发送5秒延迟消息")
    @RequirePermission("rabbitmq:delay:send")
    @Operation(summary = "发送5秒延迟消息", description = "发送5秒后处理的延迟消息")
    public ApiResponse<String> sendDelay5SecondsMessage(
            @Parameter(description = "消息内容", example = "这是一条5秒延迟消息")
            @RequestParam @NotBlank String message) {
        try {
            delayProducerService.sendDelay5SecondsMessage(message);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("5秒延迟消息发送成功")
                    .data("消息将在5秒后被处理")
                    .build();
        } catch (Exception e) {
            log.error("发送5秒延迟消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("5秒延迟消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/delay/send-task-reminder")
    @WebLog("发送任务提醒延迟消息")
    @RequirePermission("rabbitmq:delay:send")
    @Operation(summary = "发送任务提醒延迟消息", description = "发送任务提醒类型的延迟消息")
    public ApiResponse<String> sendTaskReminderMessage(
            @Parameter(description = "任务名称", example = "会议提醒")
            @RequestParam @NotBlank String taskName,
            @Parameter(description = "提醒内容", example = "您有一个重要会议即将开始")
            @RequestParam @NotBlank String reminderText,
            @Parameter(description = "延迟时间（毫秒）", example = "30000")
            @RequestParam @NotNull @Positive Long delayTime) {
        try {
            delayProducerService.sendTaskReminderMessage(taskName, reminderText, delayTime);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("任务提醒延迟消息发送成功")
                    .data("任务提醒将在 " + delayTime + "ms 后发送: " + taskName)
                    .build();
        } catch (Exception e) {
            log.error("发送任务提醒延迟消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("任务提醒延迟消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @PostMapping("/delay/send-order-timeout")
    @WebLog("发送订单超时取消延迟消息")
    @RequirePermission("rabbitmq:delay:send")
    @Operation(summary = "发送订单超时取消延迟消息", description = "发送订单超时自动取消的延迟消息")
    public ApiResponse<String> sendOrderTimeoutCancelMessage(
            @Parameter(description = "订单ID", example = "ORD202401001")
            @RequestParam @NotBlank String orderId,
            @Parameter(description = "超时时间（毫秒）", example = "1800000")
            @RequestParam @NotNull @Positive Long timeoutTime) {
        try {
            delayProducerService.sendOrderTimeoutCancelMessage(orderId, timeoutTime);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("订单超时取消延迟消息发送成功")
                    .data("订单 " + orderId + " 将在 " + timeoutTime + "ms 后检查超时状态")
                    .build();
        } catch (Exception e) {
            log.error("发送订单超时取消延迟消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("订单超时取消延迟消息发送失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    // ==================== 通用测试 API ====================

    @PostMapping("/test/send-all-types")
    @WebLog("测试发送所有类型消息")
    @RequirePermission("rabbitmq:test:send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "测试发送所有类型消息", description = "一次性测试所有消息模式")
    public ApiResponse<String> testSendAllTypes(
            @Parameter(description = "测试消息内容", example = "测试消息")
            @RequestParam(defaultValue = "测试消息") String message) {
        try {
            String timestamp = LocalDateTime.now().toString();
            
            // 发送直接消息
            directProducerService.sendDirectMessage(message + " - 直接消息 - " + timestamp);
            
            // 发送广播消息
            fanoutProducerService.sendFanoutMessage(message + " - 广播消息 - " + timestamp);
            
            // 发送主题消息
            topicProducerService.sendUserEmailMessage(message + " - 用户邮件主题消息 - " + timestamp);
            topicProducerService.sendOrderCreateMessage(message + " - 订单创建主题消息 - " + timestamp);
            
            // 发送延迟消息
            delayProducerService.sendDelay5SecondsMessage(message + " - 5秒延迟消息 - " + timestamp);
            
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("所有类型消息发送成功")
                    .data("已发送直接、广播、主题、延迟等各种类型消息")
                    .build();
        } catch (Exception e) {
            log.error("测试发送所有类型消息失败: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("测试发送消息失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }

    @GetMapping("/info")
    @WebLog("获取RabbitMQ配置信息")
    @RequirePermission("rabbitmq:info:read")
    @Operation(summary = "获取RabbitMQ配置信息", description = "获取当前RabbitMQ配置和队列信息")
    public ApiResponse<Object> getRabbitMQInfo() {
        try {
            // 构建配置信息
            Object info = new Object() {
                public final String directExchange = RabbitMQConstants.DIRECT_EXCHANGE;
                public final String fanoutExchange = RabbitMQConstants.FANOUT_EXCHANGE;
                public final String topicExchange = RabbitMQConstants.TOPIC_EXCHANGE;
                public final String delayExchange = RabbitMQConstants.DELAY_EXCHANGE;
                public final String[] queues = {
                        RabbitMQConstants.DIRECT_QUEUE,
                        RabbitMQConstants.FANOUT_QUEUE_1,
                        RabbitMQConstants.FANOUT_QUEUE_2,
                        RabbitMQConstants.TOPIC_QUEUE_USER,
                        RabbitMQConstants.TOPIC_QUEUE_ORDER,
                        RabbitMQConstants.TOPIC_QUEUE_ALL,
                        RabbitMQConstants.DELAY_QUEUE
                };
                public final String status = "正常运行";
                public final String description = "RabbitMQ集成了直接、分列、主题、延迟等四种消息模式";
            };
            
            return ApiResponse.builder()
                    .code(200)
                    .message("获取RabbitMQ配置信息成功")
                    .data(info)
                    .build();
        } catch (Exception e) {
            log.error("获取RabbitMQ配置信息失败: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message("获取RabbitMQ配置信息失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
}