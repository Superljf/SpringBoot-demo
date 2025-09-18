package com.xkcoding.swagger.service.rabbitmq;

import com.xkcoding.swagger.common.RabbitMQConstants;
import com.xkcoding.swagger.entity.MessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 直接队列模式 - 生产者服务
 * </p>
 * 
 * Direct Exchange：根据路由键精确匹配到队列
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class DirectProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送直接消息
     *
     * @param message 消息内容
     */
    public void sendDirectMessage(String message) {
        try {
            MessageInfo messageInfo = new MessageInfo(message, RabbitMQConstants.MESSAGE_TYPE_DIRECT);
            messageInfo.setSender("DirectProducer");
            messageInfo.setRoutingKey(RabbitMQConstants.DIRECT_ROUTING_KEY);
            
            log.info("发送直接消息: {}", messageInfo);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.DIRECT_EXCHANGE,
                    RabbitMQConstants.DIRECT_ROUTING_KEY,
                    messageInfo
            );
            
            log.info("直接消息发送成功: messageId={}", messageInfo.getMessageId());
            
        } catch (Exception e) {
            log.error("直接消息发送失败: message={}, error={}", message, e.getMessage(), e);
            throw new RuntimeException("直接消息发送失败", e);
        }
    }

    /**
     * 发送带有自定义路由键的直接消息
     *
     * @param message    消息内容
     * @param routingKey 路由键
     */
    public void sendDirectMessage(String message, String routingKey) {
        try {
            MessageInfo messageInfo = new MessageInfo(message, RabbitMQConstants.MESSAGE_TYPE_DIRECT);
            messageInfo.setSender("DirectProducer");
            messageInfo.setRoutingKey(routingKey);
            
            log.info("发送自定义路由键直接消息: message={}, routingKey={}", message, routingKey);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.DIRECT_EXCHANGE,
                    routingKey,
                    messageInfo
            );
            
            log.info("自定义路由键直接消息发送成功: messageId={}, routingKey={}", 
                    messageInfo.getMessageId(), routingKey);
            
        } catch (Exception e) {
            log.error("自定义路由键直接消息发送失败: message={}, routingKey={}, error={}", 
                    message, routingKey, e.getMessage(), e);
            throw new RuntimeException("自定义路由键直接消息发送失败", e);
        }
    }

    /**
     * 发送复杂对象消息
     *
     * @param messageInfo 消息对象
     */
    public void sendDirectMessage(MessageInfo messageInfo) {
        try {
            messageInfo.setMessageType(RabbitMQConstants.MESSAGE_TYPE_DIRECT);
            messageInfo.setSender("DirectProducer");
            messageInfo.setCreateTime(LocalDateTime.now());
            
            if (messageInfo.getMessageId() == null) {
                messageInfo.setMessageId("MSG" + System.currentTimeMillis());
            }
            
            String routingKey = messageInfo.getRoutingKey() != null ? 
                    messageInfo.getRoutingKey() : RabbitMQConstants.DIRECT_ROUTING_KEY;
            
            log.info("发送复杂对象直接消息: {}", messageInfo);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.DIRECT_EXCHANGE,
                    routingKey,
                    messageInfo
            );
            
            log.info("复杂对象直接消息发送成功: messageId={}", messageInfo.getMessageId());
            
        } catch (Exception e) {
            log.error("复杂对象直接消息发送失败: messageInfo={}, error={}", 
                    messageInfo, e.getMessage(), e);
            throw new RuntimeException("复杂对象直接消息发送失败", e);
        }
    }
}