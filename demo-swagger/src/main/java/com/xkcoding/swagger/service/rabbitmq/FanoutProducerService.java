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
 * 分列模式 - 生产者服务
 * </p>
 * 
 * Fanout Exchange：广播模式，忽略路由键，将消息发送到所有绑定的队列
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class FanoutProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送广播消息（分列模式）
     *
     * @param message 消息内容
     */
    public void sendFanoutMessage(String message) {
        try {
            MessageInfo messageInfo = new MessageInfo(message, RabbitMQConstants.MESSAGE_TYPE_FANOUT);
            messageInfo.setSender("FanoutProducer");
            messageInfo.setRoutingKey(""); // Fanout模式忽略路由键
            
            log.info("发送广播消息: {}", messageInfo);
            
            // Fanout Exchange会忽略路由键，将消息广播到所有绑定的队列
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.FANOUT_EXCHANGE,
                    "", // 路由键为空，Fanout模式不需要路由键
                    messageInfo
            );
            
            log.info("广播消息发送成功: messageId={}", messageInfo.getMessageId());
            
        } catch (Exception e) {
            log.error("广播消息发送失败: message={}, error={}", message, e.getMessage(), e);
            throw new RuntimeException("广播消息发送失败", e);
        }
    }

    /**
     * 发送复杂对象广播消息
     *
     * @param messageInfo 消息对象
     */
    public void sendFanoutMessage(MessageInfo messageInfo) {
        try {
            messageInfo.setMessageType(RabbitMQConstants.MESSAGE_TYPE_FANOUT);
            messageInfo.setSender("FanoutProducer");
            messageInfo.setCreateTime(LocalDateTime.now());
            messageInfo.setRoutingKey(""); // Fanout模式忽略路由键
            
            if (messageInfo.getMessageId() == null) {
                messageInfo.setMessageId("MSG" + System.currentTimeMillis());
            }
            
            log.info("发送复杂对象广播消息: {}", messageInfo);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.FANOUT_EXCHANGE,
                    "", // 路由键为空
                    messageInfo
            );
            
            log.info("复杂对象广播消息发送成功: messageId={}", messageInfo.getMessageId());
            
        } catch (Exception e) {
            log.error("复杂对象广播消息发送失败: messageInfo={}, error={}", 
                    messageInfo, e.getMessage(), e);
            throw new RuntimeException("复杂对象广播消息发送失败", e);
        }
    }

    /**
     * 发送通知类型的广播消息
     *
     * @param title   通知标题
     * @param content 通知内容
     * @param level   通知级别 (INFO, WARN, ERROR)
     */
    public void sendNotificationMessage(String title, String content, String level) {
        try {
            MessageInfo messageInfo = new MessageInfo(content, RabbitMQConstants.MESSAGE_TYPE_FANOUT);
            messageInfo.setSender("NotificationProducer");
            messageInfo.setRoutingKey("");
            
            // 设置额外数据
            messageInfo.setExtraData(new NotificationData(title, level));
            
            log.info("发送通知广播消息: title={}, content={}, level={}", title, content, level);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.FANOUT_EXCHANGE,
                    "",
                    messageInfo
            );
            
            log.info("通知广播消息发送成功: messageId={}, title={}", 
                    messageInfo.getMessageId(), title);
            
        } catch (Exception e) {
            log.error("通知广播消息发送失败: title={}, content={}, level={}, error={}", 
                    title, content, level, e.getMessage(), e);
            throw new RuntimeException("通知广播消息发送失败", e);
        }
    }

    /**
     * 通知数据内部类
     */
    public static class NotificationData {
        private String title;
        private String level;

        public NotificationData(String title, String level) {
            this.title = title;
            this.level = level;
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        @Override
        public String toString() {
            return "NotificationData{title='" + title + "', level='" + level + "'}";
        }
    }
}