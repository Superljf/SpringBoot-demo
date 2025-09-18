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
 * 主题模式 - 生产者服务
 * </p>
 * 
 * Topic Exchange：根据路由键模式匹配，支持通配符 (*：匹配一个单词，#：匹配零个或多个单词)
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class TopicProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送用户邮件主题消息
     *
     * @param message 消息内容
     */
    public void sendUserEmailMessage(String message) {
        sendTopicMessage(message, RabbitMQConstants.TOPIC_ROUTING_KEY_USER_EMAIL);
    }

    /**
     * 发送用户短信主题消息
     *
     * @param message 消息内容
     */
    public void sendUserSmsMessage(String message) {
        sendTopicMessage(message, RabbitMQConstants.TOPIC_ROUTING_KEY_USER_SMS);
    }

    /**
     * 发送订单创建主题消息
     *
     * @param message 消息内容
     */
    public void sendOrderCreateMessage(String message) {
        sendTopicMessage(message, RabbitMQConstants.TOPIC_ROUTING_KEY_ORDER_CREATE);
    }

    /**
     * 发送订单支付主题消息
     *
     * @param message 消息内容
     */
    public void sendOrderPaymentMessage(String message) {
        sendTopicMessage(message, RabbitMQConstants.TOPIC_ROUTING_KEY_ORDER_PAYMENT);
    }

    /**
     * 发送自定义路由键的主题消息
     *
     * @param message    消息内容
     * @param routingKey 路由键
     */
    public void sendTopicMessage(String message, String routingKey) {
        try {
            MessageInfo messageInfo = new MessageInfo(message, RabbitMQConstants.MESSAGE_TYPE_TOPIC);
            messageInfo.setSender("TopicProducer");
            messageInfo.setRoutingKey(routingKey);
            
            log.info("发送主题消息: message={}, routingKey={}", message, routingKey);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.TOPIC_EXCHANGE,
                    routingKey,
                    messageInfo
            );
            
            log.info("主题消息发送成功: messageId={}, routingKey={}", 
                    messageInfo.getMessageId(), routingKey);
            
        } catch (Exception e) {
            log.error("主题消息发送失败: message={}, routingKey={}, error={}", 
                    message, routingKey, e.getMessage(), e);
            throw new RuntimeException("主题消息发送失败", e);
        }
    }

    /**
     * 发送复杂对象主题消息
     *
     * @param messageInfo 消息对象
     */
    public void sendTopicMessage(MessageInfo messageInfo) {
        try {
            messageInfo.setMessageType(RabbitMQConstants.MESSAGE_TYPE_TOPIC);
            messageInfo.setSender("TopicProducer");
            messageInfo.setCreateTime(LocalDateTime.now());
            
            if (messageInfo.getMessageId() == null) {
                messageInfo.setMessageId("MSG" + System.currentTimeMillis());
            }
            
            String routingKey = messageInfo.getRoutingKey();
            if (routingKey == null || routingKey.isEmpty()) {
                throw new IllegalArgumentException("主题模式必须提供路由键");
            }
            
            log.info("发送复杂对象主题消息: {}", messageInfo);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.TOPIC_EXCHANGE,
                    routingKey,
                    messageInfo
            );
            
            log.info("复杂对象主题消息发送成功: messageId={}, routingKey={}", 
                    messageInfo.getMessageId(), routingKey);
            
        } catch (Exception e) {
            log.error("复杂对象主题消息发送失败: messageInfo={}, error={}", 
                    messageInfo, e.getMessage(), e);
            throw new RuntimeException("复杂对象主题消息发送失败", e);
        }
    }

    /**
     * 发送用户通知消息
     *
     * @param userId      用户ID
     * @param notifyType  通知类型 (email, sms, push)
     * @param content     通知内容
     */
    public void sendUserNotification(String userId, String notifyType, String content) {
        try {
            // 构建路由键: user.{notifyType}.{action}
            String routingKey = String.format("user.%s.send", notifyType);
            
            MessageInfo messageInfo = new MessageInfo(content, RabbitMQConstants.MESSAGE_TYPE_TOPIC);
            messageInfo.setSender("UserNotificationProducer");
            messageInfo.setRoutingKey(routingKey);
            messageInfo.setReceiver(userId);
            
            // 设置额外数据
            UserNotificationData extraData = new UserNotificationData(userId, notifyType, "send");
            messageInfo.setExtraData(extraData);
            
            log.info("发送用户通知消息: userId={}, notifyType={}, routingKey={}", 
                    userId, notifyType, routingKey);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.TOPIC_EXCHANGE,
                    routingKey,
                    messageInfo
            );
            
            log.info("用户通知消息发送成功: messageId={}, userId={}, notifyType={}", 
                    messageInfo.getMessageId(), userId, notifyType);
            
        } catch (Exception e) {
            log.error("用户通知消息发送失败: userId={}, notifyType={}, content={}, error={}", 
                    userId, notifyType, content, e.getMessage(), e);
            throw new RuntimeException("用户通知消息发送失败", e);
        }
    }

    /**
     * 发送订单相关消息
     *
     * @param orderId    订单ID
     * @param action     操作类型 (create, payment, cancel, complete)
     * @param content    消息内容
     */
    public void sendOrderMessage(String orderId, String action, String content) {
        try {
            // 构建路由键: order.{action}.notify
            String routingKey = String.format("order.%s.notify", action);
            
            MessageInfo messageInfo = new MessageInfo(content, RabbitMQConstants.MESSAGE_TYPE_TOPIC);
            messageInfo.setSender("OrderProducer");
            messageInfo.setRoutingKey(routingKey);
            
            // 设置额外数据
            OrderData extraData = new OrderData(orderId, action);
            messageInfo.setExtraData(extraData);
            
            log.info("发送订单消息: orderId={}, action={}, routingKey={}", 
                    orderId, action, routingKey);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.TOPIC_EXCHANGE,
                    routingKey,
                    messageInfo
            );
            
            log.info("订单消息发送成功: messageId={}, orderId={}, action={}", 
                    messageInfo.getMessageId(), orderId, action);
            
        } catch (Exception e) {
            log.error("订单消息发送失败: orderId={}, action={}, content={}, error={}", 
                    orderId, action, content, e.getMessage(), e);
            throw new RuntimeException("订单消息发送失败", e);
        }
    }

    /**
     * 用户通知数据内部类
     */
    public static class UserNotificationData {
        private String userId;
        private String notifyType;
        private String action;

        public UserNotificationData(String userId, String notifyType, String action) {
            this.userId = userId;
            this.notifyType = notifyType;
            this.action = action;
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getNotifyType() { return notifyType; }
        public void setNotifyType(String notifyType) { this.notifyType = notifyType; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        @Override
        public String toString() {
            return "UserNotificationData{userId='" + userId + "', notifyType='" + notifyType + "', action='" + action + "'}";
        }
    }

    /**
     * 订单数据内部类
     */
    public static class OrderData {
        private String orderId;
        private String action;

        public OrderData(String orderId, String action) {
            this.orderId = orderId;
            this.action = action;
        }

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        @Override
        public String toString() {
            return "OrderData{orderId='" + orderId + "', action='" + action + "'}";
        }
    }
}