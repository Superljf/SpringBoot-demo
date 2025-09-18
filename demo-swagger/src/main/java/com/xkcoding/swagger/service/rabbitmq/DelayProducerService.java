package com.xkcoding.swagger.service.rabbitmq;

import com.xkcoding.swagger.common.RabbitMQConstants;
import com.xkcoding.swagger.entity.MessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 延迟队列 - 生产者服务
 * </p>
 * 
 * 实现消息的延迟发送功能，支持两种方式：
 * 1. 使用RabbitMQ延迟插件 (rabbitmq-delayed-message-exchange)
 * 2. 使用TTL + 死信队列实现延迟
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class DelayProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送延迟消息（使用延迟插件）
     *
     * @param message   消息内容
     * @param delayTime 延迟时间（毫秒）
     */
    public void sendDelayMessage(String message, long delayTime) {
        try {
            MessageInfo messageInfo = new MessageInfo(message, RabbitMQConstants.MESSAGE_TYPE_DELAY);
            messageInfo.setSender("DelayProducer");
            messageInfo.setRoutingKey(RabbitMQConstants.DELAY_ROUTING_KEY);
            messageInfo.setDelayTime(delayTime);
            
            log.info("发送延迟消息: message={}, delayTime={}ms", message, delayTime);
            
            // 使用延迟插件发送消息
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.DELAY_EXCHANGE,
                    RabbitMQConstants.DELAY_ROUTING_KEY,
                    messageInfo,
                    createDelayMessagePostProcessor(delayTime)
            );
            
            log.info("延迟消息发送成功: messageId={}, delayTime={}ms, expectedDeliveryTime={}", 
                    messageInfo.getMessageId(), delayTime, 
                    LocalDateTime.now().plusNanos(delayTime * 1_000_000));
            
        } catch (Exception e) {
            log.error("延迟消息发送失败: message={}, delayTime={}ms, error={}", 
                    message, delayTime, e.getMessage(), e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }

    /**
     * 发送预定义延迟时间的消息
     */
    public void sendDelay5SecondsMessage(String message) {
        sendDelayMessage(message, RabbitMQConstants.DELAY_5_SECONDS);
    }

    public void sendDelay30SecondsMessage(String message) {
        sendDelayMessage(message, RabbitMQConstants.DELAY_30_SECONDS);
    }

    public void sendDelay1MinuteMessage(String message) {
        sendDelayMessage(message, RabbitMQConstants.DELAY_1_MINUTE);
    }

    public void sendDelay5MinutesMessage(String message) {
        sendDelayMessage(message, RabbitMQConstants.DELAY_5_MINUTES);
    }

    /**
     * 发送复杂对象延迟消息
     *
     * @param messageInfo 消息对象
     */
    public void sendDelayMessage(MessageInfo messageInfo) {
        try {
            messageInfo.setMessageType(RabbitMQConstants.MESSAGE_TYPE_DELAY);
            messageInfo.setSender("DelayProducer");
            messageInfo.setCreateTime(LocalDateTime.now());
            
            if (messageInfo.getMessageId() == null) {
                messageInfo.setMessageId("MSG" + System.currentTimeMillis());
            }
            
            String routingKey = messageInfo.getRoutingKey() != null ? 
                    messageInfo.getRoutingKey() : RabbitMQConstants.DELAY_ROUTING_KEY;
            
            Long delayTime = messageInfo.getDelayTime();
            if (delayTime == null || delayTime <= 0) {
                delayTime = RabbitMQConstants.DELAY_5_SECONDS; // 默认5秒延迟
                messageInfo.setDelayTime(delayTime);
            }
            
            log.info("发送复杂对象延迟消息: {}", messageInfo);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.DELAY_EXCHANGE,
                    routingKey,
                    messageInfo,
                    createDelayMessagePostProcessor(delayTime)
            );
            
            log.info("复杂对象延迟消息发送成功: messageId={}, delayTime={}ms", 
                    messageInfo.getMessageId(), delayTime);
            
        } catch (Exception e) {
            log.error("复杂对象延迟消息发送失败: messageInfo={}, error={}", 
                    messageInfo, e.getMessage(), e);
            throw new RuntimeException("复杂对象延迟消息发送失败", e);
        }
    }

    /**
     * 发送定时任务提醒消息
     *
     * @param taskName    任务名称
     * @param reminderText 提醒内容
     * @param delayTime   延迟时间（毫秒）
     */
    public void sendTaskReminderMessage(String taskName, String reminderText, long delayTime) {
        try {
            String content = String.format("任务提醒: %s - %s", taskName, reminderText);
            
            MessageInfo messageInfo = new MessageInfo(content, RabbitMQConstants.MESSAGE_TYPE_DELAY);
            messageInfo.setSender("TaskReminderProducer");
            messageInfo.setRoutingKey(RabbitMQConstants.DELAY_ROUTING_KEY);
            messageInfo.setDelayTime(delayTime);
            
            // 设置额外数据
            TaskReminderData extraData = new TaskReminderData(taskName, reminderText, 
                    LocalDateTime.now(), LocalDateTime.now().plusNanos(delayTime * 1_000_000));
            messageInfo.setExtraData(extraData);
            
            log.info("发送任务提醒延迟消息: taskName={}, delayTime={}ms", taskName, delayTime);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.DELAY_EXCHANGE,
                    RabbitMQConstants.DELAY_ROUTING_KEY,
                    messageInfo,
                    createDelayMessagePostProcessor(delayTime)
            );
            
            log.info("任务提醒延迟消息发送成功: messageId={}, taskName={}, delayTime={}ms", 
                    messageInfo.getMessageId(), taskName, delayTime);
            
        } catch (Exception e) {
            log.error("任务提醒延迟消息发送失败: taskName={}, reminderText={}, delayTime={}ms, error={}", 
                    taskName, reminderText, delayTime, e.getMessage(), e);
            throw new RuntimeException("任务提醒延迟消息发送失败", e);
        }
    }

    /**
     * 发送订单超时取消消息
     *
     * @param orderId     订单ID
     * @param timeoutTime 超时时间（毫秒）
     */
    public void sendOrderTimeoutCancelMessage(String orderId, long timeoutTime) {
        try {
            String content = String.format("订单超时自动取消检查: 订单ID=%s", orderId);
            
            MessageInfo messageInfo = new MessageInfo(content, RabbitMQConstants.MESSAGE_TYPE_DELAY);
            messageInfo.setSender("OrderTimeoutProducer");
            messageInfo.setRoutingKey(RabbitMQConstants.DELAY_ROUTING_KEY);
            messageInfo.setDelayTime(timeoutTime);
            
            // 设置额外数据
            OrderTimeoutData extraData = new OrderTimeoutData(orderId, 
                    LocalDateTime.now(), LocalDateTime.now().plusNanos(timeoutTime * 1_000_000));
            messageInfo.setExtraData(extraData);
            
            log.info("发送订单超时取消延迟消息: orderId={}, timeoutTime={}ms", orderId, timeoutTime);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.DELAY_EXCHANGE,
                    RabbitMQConstants.DELAY_ROUTING_KEY,
                    messageInfo,
                    createDelayMessagePostProcessor(timeoutTime)
            );
            
            log.info("订单超时取消延迟消息发送成功: messageId={}, orderId={}, timeoutTime={}ms", 
                    messageInfo.getMessageId(), orderId, timeoutTime);
            
        } catch (Exception e) {
            log.error("订单超时取消延迟消息发送失败: orderId={}, timeoutTime={}ms, error={}", 
                    orderId, timeoutTime, e.getMessage(), e);
            throw new RuntimeException("订单超时取消延迟消息发送失败", e);
        }
    }

    /**
     * 创建延迟消息后处理器
     *
     * @param delayTime 延迟时间（毫秒）
     * @return 消息后处理器
     */
    private MessagePostProcessor createDelayMessagePostProcessor(long delayTime) {
        return message -> {
            // 设置延迟时间（使用 x-delay 头）
            message.getMessageProperties().setDelay((int) delayTime);
            return message;
        };
    }

    /**
     * 任务提醒数据内部类
     */
    public static class TaskReminderData {
        private String taskName;
        private String reminderText;
        private LocalDateTime createTime;
        private LocalDateTime expectedExecuteTime;

        public TaskReminderData(String taskName, String reminderText, 
                               LocalDateTime createTime, LocalDateTime expectedExecuteTime) {
            this.taskName = taskName;
            this.reminderText = reminderText;
            this.createTime = createTime;
            this.expectedExecuteTime = expectedExecuteTime;
        }

        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        public String getReminderText() { return reminderText; }
        public void setReminderText(String reminderText) { this.reminderText = reminderText; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public LocalDateTime getExpectedExecuteTime() { return expectedExecuteTime; }
        public void setExpectedExecuteTime(LocalDateTime expectedExecuteTime) { this.expectedExecuteTime = expectedExecuteTime; }

        @Override
        public String toString() {
            return "TaskReminderData{taskName='" + taskName + "', reminderText='" + reminderText + 
                   "', createTime=" + createTime + ", expectedExecuteTime=" + expectedExecuteTime + "}";
        }
    }

    /**
     * 订单超时数据内部类
     */
    public static class OrderTimeoutData {
        private String orderId;
        private LocalDateTime createTime;
        private LocalDateTime timeoutTime;

        public OrderTimeoutData(String orderId, LocalDateTime createTime, LocalDateTime timeoutTime) {
            this.orderId = orderId;
            this.createTime = createTime;
            this.timeoutTime = timeoutTime;
        }

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public LocalDateTime getTimeoutTime() { return timeoutTime; }
        public void setTimeoutTime(LocalDateTime timeoutTime) { this.timeoutTime = timeoutTime; }

        @Override
        public String toString() {
            return "OrderTimeoutData{orderId='" + orderId + "', createTime=" + createTime + 
                   ", timeoutTime=" + timeoutTime + "}";
        }
    }
}