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
 * 主题模式 - 消费者服务
 * </p>
 * 
 * 监听主题队列，处理主题消息
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class TopicConsumerService {

    /**
     * 监听用户相关队列消息 (路由键模式: user.*)
     *
     * @param messageInfo 消息对象
     * @param message     原始消息
     * @param channel     消息通道
     */
    @RabbitListener(queues = RabbitMQConstants.TOPIC_QUEUE_USER)
    public void handleUserTopicMessage(MessageInfo messageInfo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        
        try {
            log.info("【用户服务】接收到主题消息: routingKey={}, messageInfo={}", routingKey, messageInfo);
            
            // 根据路由键处理不同的用户消息
            processUserMessage(messageInfo, routingKey);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("【用户服务】主题消息处理成功: messageId={}, routingKey={}, deliveryTag={}", 
                    messageInfo.getMessageId(), routingKey, deliveryTag);
            
        } catch (Exception e) {
            log.error("【用户服务】主题消息处理失败: messageInfo={}, routingKey={}, deliveryTag={}, error={}", 
                    messageInfo, routingKey, deliveryTag, e.getMessage(), e);
            
            try {
                // 处理失败，拒绝消息并重新排队
                channel.basicNack(deliveryTag, false, true);
                log.info("【用户服务】消息已重新排队: deliveryTag={}", deliveryTag);
            } catch (IOException ioException) {
                log.error("【用户服务】消息确认失败: deliveryTag={}, error={}", 
                        deliveryTag, ioException.getMessage(), ioException);
            }
        }
    }

    /**
     * 监听订单相关队列消息 (路由键模式: order.*)
     *
     * @param messageInfo 消息对象
     * @param message     原始消息
     * @param channel     消息通道
     */
    @RabbitListener(queues = RabbitMQConstants.TOPIC_QUEUE_ORDER)
    public void handleOrderTopicMessage(MessageInfo messageInfo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        
        try {
            log.info("【订单服务】接收到主题消息: routingKey={}, messageInfo={}", routingKey, messageInfo);
            
            // 根据路由键处理不同的订单消息
            processOrderMessage(messageInfo, routingKey);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("【订单服务】主题消息处理成功: messageId={}, routingKey={}, deliveryTag={}", 
                    messageInfo.getMessageId(), routingKey, deliveryTag);
            
        } catch (Exception e) {
            log.error("【订单服务】主题消息处理失败: messageInfo={}, routingKey={}, deliveryTag={}, error={}", 
                    messageInfo, routingKey, deliveryTag, e.getMessage(), e);
            
            try {
                // 处理失败，拒绝消息并重新排队
                channel.basicNack(deliveryTag, false, true);
                log.info("【订单服务】消息已重新排队: deliveryTag={}", deliveryTag);
            } catch (IOException ioException) {
                log.error("【订单服务】消息确认失败: deliveryTag={}, error={}", 
                        deliveryTag, ioException.getMessage(), ioException);
            }
        }
    }

    /**
     * 监听全部消息队列 (路由键模式: #，匹配所有消息)
     *
     * @param messageInfo 消息对象
     * @param message     原始消息
     * @param channel     消息通道
     */
    @RabbitListener(queues = RabbitMQConstants.TOPIC_QUEUE_ALL)
    public void handleAllTopicMessage(MessageInfo messageInfo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        
        try {
            log.info("【监控服务】接收到全部主题消息: routingKey={}, messageInfo={}", routingKey, messageInfo);
            
            // 监控和记录所有消息
            monitorAllMessages(messageInfo, routingKey);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("【监控服务】主题消息处理成功: messageId={}, routingKey={}, deliveryTag={}", 
                    messageInfo.getMessageId(), routingKey, deliveryTag);
            
        } catch (Exception e) {
            log.error("【监控服务】主题消息处理失败: messageInfo={}, routingKey={}, deliveryTag={}, error={}", 
                    messageInfo, routingKey, deliveryTag, e.getMessage(), e);
            
            try {
                // 处理失败，拒绝消息并重新排队
                channel.basicNack(deliveryTag, false, true);
                log.info("【监控服务】消息已重新排队: deliveryTag={}", deliveryTag);
            } catch (IOException ioException) {
                log.error("【监控服务】消息确认失败: deliveryTag={}, error={}", 
                        deliveryTag, ioException.getMessage(), ioException);
            }
        }
    }

    /**
     * 处理用户相关消息
     *
     * @param messageInfo 消息对象
     * @param routingKey  路由键
     */
    private void processUserMessage(MessageInfo messageInfo, String routingKey) {
        // 模拟消息处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        messageInfo.setReceiver("UserService");
        
        // 根据路由键处理不同的用户消息
        if (routingKey.contains("email")) {
            handleUserEmailMessage(messageInfo);
        } else if (routingKey.contains("sms")) {
            handleUserSmsMessage(messageInfo);
        } else {
            handleGenericUserMessage(messageInfo, routingKey);
        }
        
        log.info("【用户服务】消息处理完成: messageId={}, routingKey={}, processTime={}", 
                messageInfo.getMessageId(), routingKey, LocalDateTime.now());
    }

    /**
     * 处理订单相关消息
     *
     * @param messageInfo 消息对象
     * @param routingKey  路由键
     */
    private void processOrderMessage(MessageInfo messageInfo, String routingKey) {
        // 模拟消息处理时间
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        messageInfo.setReceiver("OrderService");
        
        // 根据路由键处理不同的订单消息
        if (routingKey.contains("create")) {
            handleOrderCreateMessage(messageInfo);
        } else if (routingKey.contains("payment")) {
            handleOrderPaymentMessage(messageInfo);
        } else if (routingKey.contains("cancel")) {
            handleOrderCancelMessage(messageInfo);
        } else if (routingKey.contains("complete")) {
            handleOrderCompleteMessage(messageInfo);
        } else {
            handleGenericOrderMessage(messageInfo, routingKey);
        }
        
        log.info("【订单服务】消息处理完成: messageId={}, routingKey={}, processTime={}", 
                messageInfo.getMessageId(), routingKey, LocalDateTime.now());
    }

    /**
     * 监控所有消息
     *
     * @param messageInfo 消息对象
     * @param routingKey  路由键
     */
    private void monitorAllMessages(MessageInfo messageInfo, String routingKey) {
        // 模拟消息处理时间
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        messageInfo.setReceiver("MonitorService");
        
        // 记录消息监控信息
        log.info("【监控记录】消息类型: {}, 发送者: {}, 路由键: {}, 创建时间: {}", 
                messageInfo.getMessageType(),
                messageInfo.getSender(),
                routingKey,
                messageInfo.getCreateTime());
        
        // 模拟消息统计
        updateMessageMetrics(routingKey, messageInfo);
        
        log.info("【监控服务】消息监控完成: messageId={}, routingKey={}, monitorTime={}", 
                messageInfo.getMessageId(), routingKey, LocalDateTime.now());
    }

    // ==================== 具体业务处理方法 ====================

    private void handleUserEmailMessage(MessageInfo messageInfo) {
        log.info("处理用户邮件消息: {}", messageInfo.getContent());
        // 这里可以集成邮件发送服务
    }

    private void handleUserSmsMessage(MessageInfo messageInfo) {
        log.info("处理用户短信消息: {}", messageInfo.getContent());
        // 这里可以集成短信发送服务
    }

    private void handleGenericUserMessage(MessageInfo messageInfo, String routingKey) {
        log.info("处理通用用户消息: routingKey={}, content={}", routingKey, messageInfo.getContent());
    }

    private void handleOrderCreateMessage(MessageInfo messageInfo) {
        log.info("处理订单创建消息: {}", messageInfo.getContent());
        // 这里可以处理订单创建后的相关业务
    }

    private void handleOrderPaymentMessage(MessageInfo messageInfo) {
        log.info("处理订单支付消息: {}", messageInfo.getContent());
        // 这里可以处理订单支付后的相关业务
    }

    private void handleOrderCancelMessage(MessageInfo messageInfo) {
        log.info("处理订单取消消息: {}", messageInfo.getContent());
        // 这里可以处理订单取消后的相关业务
    }

    private void handleOrderCompleteMessage(MessageInfo messageInfo) {
        log.info("处理订单完成消息: {}", messageInfo.getContent());
        // 这里可以处理订单完成后的相关业务
    }

    private void handleGenericOrderMessage(MessageInfo messageInfo, String routingKey) {
        log.info("处理通用订单消息: routingKey={}, content={}", routingKey, messageInfo.getContent());
    }

    private void updateMessageMetrics(String routingKey, MessageInfo messageInfo) {
        // 模拟更新消息指标
        log.info("更新消息指标 - 路由键: {}, 消息类型: {}, 发送者: {}", 
                routingKey, messageInfo.getMessageType(), messageInfo.getSender());
    }
}