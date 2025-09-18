package com.xkcoding.swagger.service.rabbitmq;

import com.rabbitmq.client.Channel;
import com.xkcoding.swagger.common.RabbitMQConstants;
import com.xkcoding.swagger.entity.MessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 延迟队列 - 消费者服务
 * </p>
 * 
 * 监听延迟队列，处理延迟消息
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class DelayConsumerService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 监听延迟队列消息
     *
     * @param messageInfo 消息对象
     * @param message     原始消息
     * @param channel     消息通道
     */
    @RabbitListener(queues = RabbitMQConstants.DELAY_QUEUE)
    public void handleDelayMessage(MessageInfo messageInfo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        LocalDateTime receiveTime = LocalDateTime.now();
        
        try {
            log.info("接收到延迟消息: receiveTime={}, messageInfo={}", 
                    receiveTime.format(FORMATTER), messageInfo);
            
            // 计算实际延迟时间
            LocalDateTime createTime = messageInfo.getCreateTime();
            Long expectedDelayTime = messageInfo.getDelayTime();
            
            if (createTime != null && expectedDelayTime != null) {
                long actualDelayTime = java.time.Duration.between(createTime, receiveTime).toMillis();
                long delayDifference = Math.abs(actualDelayTime - expectedDelayTime);
                
                log.info("延迟消息时间统计: messageId={}, 期望延迟={}ms, 实际延迟={}ms, 误差={}ms", 
                        messageInfo.getMessageId(), expectedDelayTime, actualDelayTime, delayDifference);
            }
            
            // 处理延迟消息
            processDelayMessage(messageInfo, receiveTime);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("延迟消息处理成功: messageId={}, deliveryTag={}, processTime={}", 
                    messageInfo.getMessageId(), deliveryTag, LocalDateTime.now().format(FORMATTER));
            
        } catch (Exception e) {
            log.error("延迟消息处理失败: messageInfo={}, deliveryTag={}, receiveTime={}, error={}", 
                    messageInfo, deliveryTag, receiveTime.format(FORMATTER), e.getMessage(), e);
            
            try {
                // 处理失败，拒绝消息并重新排队
                channel.basicNack(deliveryTag, false, true);
                log.info("延迟消息已重新排队: deliveryTag={}", deliveryTag);
            } catch (IOException ioException) {
                log.error("延迟消息确认失败: deliveryTag={}, error={}", 
                        deliveryTag, ioException.getMessage(), ioException);
            }
        }
    }

    /**
     * 处理延迟消息的业务逻辑
     *
     * @param messageInfo 消息对象
     * @param receiveTime 接收时间
     */
    private void processDelayMessage(MessageInfo messageInfo, LocalDateTime receiveTime) {
        // 模拟消息处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        messageInfo.setReceiver("DelayConsumer");
        
        log.info("开始处理延迟消息业务逻辑: messageId={}, content={}, sender={}, createTime={}, receiveTime={}", 
                messageInfo.getMessageId(), 
                messageInfo.getContent(), 
                messageInfo.getSender(),
                messageInfo.getCreateTime() != null ? messageInfo.getCreateTime().format(FORMATTER) : "null",
                receiveTime.format(FORMATTER));
        
        // 根据消息内容判断业务类型
        String content = messageInfo.getContent();
        if (content != null) {
            if (content.contains("任务提醒")) {
                handleTaskReminderMessage(messageInfo, receiveTime);
            } else if (content.contains("订单超时")) {
                handleOrderTimeoutMessage(messageInfo, receiveTime);
            } else if (content.contains("定时")) {
                handleScheduleMessage(messageInfo, receiveTime);
            } else {
                handleGenericDelayMessage(messageInfo, receiveTime);
            }
        } else {
            handleGenericDelayMessage(messageInfo, receiveTime);
        }
        
        log.info("延迟消息业务处理完成: messageId={}, processCompleteTime={}", 
                messageInfo.getMessageId(), LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 处理任务提醒类型的延迟消息
     *
     * @param messageInfo 消息对象
     * @param receiveTime 接收时间
     */
    private void handleTaskReminderMessage(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("处理任务提醒延迟消息: {}", messageInfo.getContent());
        
        // 解析额外数据
        Object extraData = messageInfo.getExtraData();
        if (extraData != null) {
            log.info("任务提醒额外数据: {}", extraData);
        }
        
        // 模拟发送任务提醒通知
        sendTaskReminderNotification(messageInfo, receiveTime);
        
        log.info("任务提醒处理完成: messageId={}", messageInfo.getMessageId());
    }

    /**
     * 处理订单超时类型的延迟消息
     *
     * @param messageInfo 消息对象
     * @param receiveTime 接收时间
     */
    private void handleOrderTimeoutMessage(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("处理订单超时延迟消息: {}", messageInfo.getContent());
        
        // 解析额外数据
        Object extraData = messageInfo.getExtraData();
        if (extraData != null) {
            log.info("订单超时额外数据: {}", extraData);
        }
        
        // 模拟检查订单状态并处理超时逻辑
        checkAndHandleOrderTimeout(messageInfo, receiveTime);
        
        log.info("订单超时处理完成: messageId={}", messageInfo.getMessageId());
    }

    /**
     * 处理定时类型的延迟消息
     *
     * @param messageInfo 消息对象
     * @param receiveTime 接收时间
     */
    private void handleScheduleMessage(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("处理定时延迟消息: {}", messageInfo.getContent());
        
        // 模拟执行定时任务
        executeScheduledTask(messageInfo, receiveTime);
        
        log.info("定时任务处理完成: messageId={}", messageInfo.getMessageId());
    }

    /**
     * 处理通用类型的延迟消息
     *
     * @param messageInfo 消息对象
     * @param receiveTime 接收时间
     */
    private void handleGenericDelayMessage(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("处理通用延迟消息: {}", messageInfo.getContent());
        
        // 通用延迟消息处理逻辑
        processGenericDelayLogic(messageInfo, receiveTime);
        
        log.info("通用延迟消息处理完成: messageId={}", messageInfo.getMessageId());
    }

    // ==================== 具体业务处理方法 ====================

    /**
     * 发送任务提醒通知
     */
    private void sendTaskReminderNotification(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("发送任务提醒通知: messageId={}, content={}, receiveTime={}", 
                messageInfo.getMessageId(), messageInfo.getContent(), receiveTime.format(FORMATTER));
        
        // 这里可以集成邮件、短信、推送等通知服务
        // 例如：
        // emailService.sendTaskReminder(...)
        // smsService.sendTaskReminder(...)
        // pushService.sendTaskReminder(...)
    }

    /**
     * 检查并处理订单超时
     */
    private void checkAndHandleOrderTimeout(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("检查订单超时状态: messageId={}, content={}, receiveTime={}", 
                messageInfo.getMessageId(), messageInfo.getContent(), receiveTime.format(FORMATTER));
        
        // 这里可以实现订单超时处理逻辑
        // 例如：
        // 1. 查询订单当前状态
        // 2. 如果订单未支付，则自动取消订单
        // 3. 释放库存
        // 4. 发送取消通知
        
        // 模拟处理逻辑
        String content = messageInfo.getContent();
        if (content.contains("订单ID=")) {
            String orderId = extractOrderIdFromContent(content);
            log.info("处理订单超时: orderId={}, 执行自动取消逻辑", orderId);
            
            // 模拟订单状态检查和取消
            simulateOrderCancellation(orderId, receiveTime);
        }
    }

    /**
     * 执行定时任务
     */
    private void executeScheduledTask(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("执行定时任务: messageId={}, content={}, executeTime={}", 
                messageInfo.getMessageId(), messageInfo.getContent(), receiveTime.format(FORMATTER));
        
        // 这里可以实现具体的定时任务逻辑
        // 例如：
        // 1. 数据清理任务
        // 2. 报表生成任务
        // 3. 系统监控任务
        // 4. 缓存更新任务
    }

    /**
     * 处理通用延迟逻辑
     */
    private void processGenericDelayLogic(MessageInfo messageInfo, LocalDateTime receiveTime) {
        log.info("执行通用延迟逻辑: messageId={}, content={}, executeTime={}", 
                messageInfo.getMessageId(), messageInfo.getContent(), receiveTime.format(FORMATTER));
        
        // 通用延迟处理逻辑
        // 可以根据具体业务需求实现
    }

    /**
     * 从消息内容中提取订单ID
     */
    private String extractOrderIdFromContent(String content) {
        // 简单的字符串解析，实际项目中可能需要更复杂的解析逻辑
        int startIndex = content.indexOf("订单ID=") + 4;
        int endIndex = content.indexOf(" ", startIndex);
        if (endIndex == -1) {
            endIndex = content.length();
        }
        return content.substring(startIndex, endIndex);
    }

    /**
     * 模拟订单取消逻辑
     */
    private void simulateOrderCancellation(String orderId, LocalDateTime cancelTime) {
        log.info("模拟订单取消逻辑: orderId={}, cancelTime={}", orderId, cancelTime.format(FORMATTER));
        
        // 这里可以实现实际的订单取消逻辑
        // 例如：
        // 1. 更新订单状态为已取消
        // 2. 释放商品库存
        // 3. 如果有预扣款项，执行退款
        // 4. 发送订单取消通知给用户
        // 5. 记录取消原因（超时自动取消）
        
        log.info("订单自动取消完成: orderId={}", orderId);
    }
}