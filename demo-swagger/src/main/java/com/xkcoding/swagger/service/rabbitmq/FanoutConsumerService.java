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

/**
 * <p>
 * 分列模式 - 消费者服务
 * </p>
 * 
 * 监听分列队列，处理广播消息
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class FanoutConsumerService {

    /**
     * 监听分列队列1的消息
     *
     * @param messageInfo 消息对象
     * @param message     原始消息
     * @param channel     消息通道
     */
    @RabbitListener(queues = RabbitMQConstants.FANOUT_QUEUE_1)
    public void handleFanoutMessage1(MessageInfo messageInfo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("【队列1】接收到广播消息: {}", messageInfo);
            
            // 模拟队列1的特定处理逻辑
            processFanoutMessage1(messageInfo);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("【队列1】广播消息处理成功: messageId={}, deliveryTag={}", 
                    messageInfo.getMessageId(), deliveryTag);
            
        } catch (Exception e) {
            log.error("【队列1】广播消息处理失败: messageInfo={}, deliveryTag={}, error={}", 
                    messageInfo, deliveryTag, e.getMessage(), e);
            
            try {
                // 处理失败，拒绝消息并重新排队
                channel.basicNack(deliveryTag, false, true);
                log.info("【队列1】消息已重新排队: deliveryTag={}", deliveryTag);
            } catch (IOException ioException) {
                log.error("【队列1】消息确认失败: deliveryTag={}, error={}", 
                        deliveryTag, ioException.getMessage(), ioException);
            }
        }
    }

    /**
     * 监听分列队列2的消息
     *
     * @param messageInfo 消息对象
     * @param message     原始消息
     * @param channel     消息通道
     */
    @RabbitListener(queues = RabbitMQConstants.FANOUT_QUEUE_2)
    public void handleFanoutMessage2(MessageInfo messageInfo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("【队列2】接收到广播消息: {}", messageInfo);
            
            // 模拟队列2的特定处理逻辑
            processFanoutMessage2(messageInfo);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("【队列2】广播消息处理成功: messageId={}, deliveryTag={}", 
                    messageInfo.getMessageId(), deliveryTag);
            
        } catch (Exception e) {
            log.error("【队列2】广播消息处理失败: messageInfo={}, deliveryTag={}, error={}", 
                    messageInfo, deliveryTag, e.getMessage(), e);
            
            try {
                // 处理失败，拒绝消息并重新排队
                channel.basicNack(deliveryTag, false, true);
                log.info("【队列2】消息已重新排队: deliveryTag={}", deliveryTag);
            } catch (IOException ioException) {
                log.error("【队列2】消息确认失败: deliveryTag={}, error={}", 
                        deliveryTag, ioException.getMessage(), ioException);
            }
        }
    }

    /**
     * 处理队列1的广播消息 - 日志记录服务
     *
     * @param messageInfo 消息对象
     */
    private void processFanoutMessage1(MessageInfo messageInfo) {
        // 模拟消息处理时间
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("【日志记录服务】处理广播消息: messageId={}, content={}, sender={}", 
                messageInfo.getMessageId(), 
                messageInfo.getContent(), 
                messageInfo.getSender());
        
        // 设置处理者信息
        messageInfo.setReceiver("LogService");
        
        // 模拟日志记录业务
        saveToLogSystem(messageInfo);
        
        log.info("【日志记录服务】广播消息处理完成: messageId={}, processTime={}", 
                messageInfo.getMessageId(), LocalDateTime.now());
    }

    /**
     * 处理队列2的广播消息 - 统计分析服务
     *
     * @param messageInfo 消息对象
     */
    private void processFanoutMessage2(MessageInfo messageInfo) {
        // 模拟消息处理时间
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("【统计分析服务】处理广播消息: messageId={}, content={}, sender={}", 
                messageInfo.getMessageId(), 
                messageInfo.getContent(), 
                messageInfo.getSender());
        
        // 设置处理者信息
        messageInfo.setReceiver("StatisticsService");
        
        // 模拟统计分析业务
        updateStatistics(messageInfo);
        
        log.info("【统计分析服务】广播消息处理完成: messageId={}, processTime={}", 
                messageInfo.getMessageId(), LocalDateTime.now());
    }

    /**
     * 模拟日志记录业务
     *
     * @param messageInfo 消息对象
     */
    private void saveToLogSystem(MessageInfo messageInfo) {
        // 这里可以实现将消息保存到日志系统的逻辑
        log.info("保存消息到日志系统: messageId={}, messageType={}, createTime={}", 
                messageInfo.getMessageId(), 
                messageInfo.getMessageType(),
                messageInfo.getCreateTime());
        
        // 根据消息内容分类记录
        String content = messageInfo.getContent();
        if (content != null) {
            if (content.contains("错误") || content.contains("异常")) {
                log.warn("记录错误日志: {}", content);
            } else if (content.contains("警告")) {
                log.warn("记录警告日志: {}", content);
            } else {
                log.info("记录信息日志: {}", content);
            }
        }
    }

    /**
     * 模拟统计分析业务
     *
     * @param messageInfo 消息对象
     */
    private void updateStatistics(MessageInfo messageInfo) {
        // 这里可以实现消息统计分析的逻辑
        log.info("更新消息统计数据: messageId={}, messageType={}, sender={}", 
                messageInfo.getMessageId(), 
                messageInfo.getMessageType(),
                messageInfo.getSender());
        
        // 模拟统计计数
        String messageType = messageInfo.getMessageType();
        log.info("消息类型统计 - {}: +1", messageType);
        
        // 模拟发送者统计
        String sender = messageInfo.getSender();
        if (sender != null) {
            log.info("发送者统计 - {}: +1", sender);
        }
        
        // 模拟时间统计
        log.info("消息处理时间统计 - 当前时间: {}", LocalDateTime.now());
    }
}