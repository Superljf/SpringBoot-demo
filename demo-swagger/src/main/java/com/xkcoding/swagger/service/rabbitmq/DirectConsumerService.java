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
 * 直接队列模式 - 消费者服务
 * </p>
 * 
 * 监听直接队列，处理直接消息
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class DirectConsumerService {

    /**
     * 监听直接队列消息
     *
     * @param messageInfo 消息对象
     * @param message     原始消息
     * @param channel     消息通道
     */
    @RabbitListener(queues = RabbitMQConstants.DIRECT_QUEUE)
    public void handleDirectMessage(MessageInfo messageInfo, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("接收到直接队列消息: {}", messageInfo);
            
            // 模拟消息处理
            processDirectMessage(messageInfo);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("直接队列消息处理成功: messageId={}, deliveryTag={}", 
                    messageInfo.getMessageId(), deliveryTag);
            
        } catch (Exception e) {
            log.error("直接队列消息处理失败: messageInfo={}, deliveryTag={}, error={}", 
                    messageInfo, deliveryTag, e.getMessage(), e);
            
            try {
                // 处理失败，拒绝消息并重新排队
                channel.basicNack(deliveryTag, false, true);
                log.info("消息已重新排队: deliveryTag={}", deliveryTag);
            } catch (IOException ioException) {
                log.error("消息确认失败: deliveryTag={}, error={}", 
                        deliveryTag, ioException.getMessage(), ioException);
            }
        }
    }

    /**
     * 处理直接消息的业务逻辑
     *
     * @param messageInfo 消息对象
     */
    private void processDirectMessage(MessageInfo messageInfo) {
        // 模拟消息处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("处理直接消息业务逻辑: messageId={}, content={}, sender={}, createTime={}", 
                messageInfo.getMessageId(), 
                messageInfo.getContent(), 
                messageInfo.getSender(),
                messageInfo.getCreateTime());
        
        // 设置处理时间
        messageInfo.setReceiver("DirectConsumer");
        
        // 在这里可以添加具体的业务处理逻辑
        // 例如：保存到数据库、调用其他服务等
        handleBusinessLogic(messageInfo);
    }

    /**
     * 具体的业务处理逻辑
     *
     * @param messageInfo 消息对象
     */
    private void handleBusinessLogic(MessageInfo messageInfo) {
        // 根据消息内容执行不同的业务逻辑
        String content = messageInfo.getContent();
        
        if (content != null) {
            if (content.contains("用户")) {
                log.info("处理用户相关业务: {}", content);
                // 用户相关业务处理
            } else if (content.contains("订单")) {
                log.info("处理订单相关业务: {}", content);
                // 订单相关业务处理
            } else if (content.contains("邮件")) {
                log.info("处理邮件发送业务: {}", content);
                // 邮件发送业务处理
            } else {
                log.info("处理通用业务: {}", content);
                // 通用业务处理
            }
        }
        
        log.info("直接消息业务处理完成: messageId={}, processTime={}", 
                messageInfo.getMessageId(), LocalDateTime.now());
    }
}