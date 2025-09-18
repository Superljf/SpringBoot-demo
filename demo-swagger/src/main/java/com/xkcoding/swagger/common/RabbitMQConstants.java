package com.xkcoding.swagger.common;

/**
 * <p>
 * RabbitMQ 消息队列常量
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
public class RabbitMQConstants {

    // ==================== 交换机名称 ====================
    
    /**
     * 直接交换机
     */
    public static final String DIRECT_EXCHANGE = "demo.direct.exchange";
    
    /**
     * 分列交换机（广播）
     */
    public static final String FANOUT_EXCHANGE = "demo.fanout.exchange";
    
    /**
     * 主题交换机
     */
    public static final String TOPIC_EXCHANGE = "demo.topic.exchange";
    
    /**
     * 延迟交换机
     */
    public static final String DELAY_EXCHANGE = "demo.delay.exchange";

    // ==================== 队列名称 ====================
    
    /**
     * 直接队列
     */
    public static final String DIRECT_QUEUE = "demo.direct.queue";
    
    /**
     * 分列队列1
     */
    public static final String FANOUT_QUEUE_1 = "demo.fanout.queue.1";
    
    /**
     * 分列队列2
     */
    public static final String FANOUT_QUEUE_2 = "demo.fanout.queue.2";
    
    /**
     * 主题队列 - 用户相关
     */
    public static final String TOPIC_QUEUE_USER = "demo.topic.queue.user";
    
    /**
     * 主题队列 - 订单相关
     */
    public static final String TOPIC_QUEUE_ORDER = "demo.topic.queue.order";
    
    /**
     * 主题队列 - 全部消息
     */
    public static final String TOPIC_QUEUE_ALL = "demo.topic.queue.all";
    
    /**
     * 延迟队列
     */
    public static final String DELAY_QUEUE = "demo.delay.queue";
    
    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE = "demo.dead.letter.queue";

    // ==================== 路由键 ====================
    
    /**
     * 直接路由键
     */
    public static final String DIRECT_ROUTING_KEY = "demo.direct";
    
    /**
     * 主题路由键 - 用户邮件
     */
    public static final String TOPIC_ROUTING_KEY_USER_EMAIL = "user.email.send";
    
    /**
     * 主题路由键 - 用户短信
     */
    public static final String TOPIC_ROUTING_KEY_USER_SMS = "user.sms.send";
    
    /**
     * 主题路由键 - 订单创建
     */
    public static final String TOPIC_ROUTING_KEY_ORDER_CREATE = "order.create.notify";
    
    /**
     * 主题路由键 - 订单支付
     */
    public static final String TOPIC_ROUTING_KEY_ORDER_PAYMENT = "order.payment.notify";
    
    /**
     * 延迟路由键
     */
    public static final String DELAY_ROUTING_KEY = "demo.delay";

    // ==================== 消息类型 ====================
    
    /**
     * 直接消息
     */
    public static final String MESSAGE_TYPE_DIRECT = "DIRECT";
    
    /**
     * 分列消息
     */
    public static final String MESSAGE_TYPE_FANOUT = "FANOUT";
    
    /**
     * 主题消息
     */
    public static final String MESSAGE_TYPE_TOPIC = "TOPIC";
    
    /**
     * 延迟消息
     */
    public static final String MESSAGE_TYPE_DELAY = "DELAY";

    // ==================== 延迟时间 (毫秒) ====================
    
    /**
     * 5秒延迟
     */
    public static final long DELAY_5_SECONDS = 5 * 1000L;
    
    /**
     * 30秒延迟
     */
    public static final long DELAY_30_SECONDS = 30 * 1000L;
    
    /**
     * 1分钟延迟
     */
    public static final long DELAY_1_MINUTE = 60 * 1000L;
    
    /**
     * 5分钟延迟
     */
    public static final long DELAY_5_MINUTES = 5 * 60 * 1000L;
}