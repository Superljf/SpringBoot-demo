package com.xkcoding.swagger.config;

import com.xkcoding.swagger.common.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * RabbitMQ 配置类
 * </p>
 * 
 * 配置各种消息模式的交换机、队列和绑定关系
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    // ==================== 消息转换器配置 ====================

    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        
        // 设置发布确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送成功: correlationData({})", correlationData);
            } else {
                log.error("消息发送失败: correlationData({}), cause({})", correlationData, cause);
            }
        });
        
        // 设置返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息路由失败: exchange({}), route({}), replyCode({}), replyText({}), message({})",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(),
                    returned.getReplyText(), returned.getMessage());
        });
        
        return rabbitTemplate;
    }

    /**
     * 监听器容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        // 设置并发消费者数量
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(10);
        // 设置预取数量
        factory.setPrefetchCount(1);
        return factory;
    }

    // ==================== 直接模式配置 ====================

    /**
     * 直接交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstants.DIRECT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 直接队列
     */
    @Bean
    public Queue directQueue() {
        return QueueBuilder.durable(RabbitMQConstants.DIRECT_QUEUE)
                .build();
    }

    /**
     * 直接队列绑定
     */
    @Bean
    public Binding directBinding(Queue directQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(directQueue)
                .to(directExchange)
                .with(RabbitMQConstants.DIRECT_ROUTING_KEY);
    }

    // ==================== 分列模式配置 ====================

    /**
     * 分列交换机（广播）
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder.fanoutExchange(RabbitMQConstants.FANOUT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 分列队列1
     */
    @Bean
    public Queue fanoutQueue1() {
        return QueueBuilder.durable(RabbitMQConstants.FANOUT_QUEUE_1)
                .build();
    }

    /**
     * 分列队列2
     */
    @Bean
    public Queue fanoutQueue2() {
        return QueueBuilder.durable(RabbitMQConstants.FANOUT_QUEUE_2)
                .build();
    }

    /**
     * 分列队列1绑定
     */
    @Bean
    public Binding fanoutBinding1(Queue fanoutQueue1, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutQueue1).to(fanoutExchange);
    }

    /**
     * 分列队列2绑定
     */
    @Bean
    public Binding fanoutBinding2(Queue fanoutQueue2, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutQueue2).to(fanoutExchange);
    }

    // ==================== 主题模式配置 ====================

    /**
     * 主题交换机
     */
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.TOPIC_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 主题队列 - 用户相关
     */
    @Bean
    public Queue topicQueueUser() {
        return QueueBuilder.durable(RabbitMQConstants.TOPIC_QUEUE_USER)
                .build();
    }

    /**
     * 主题队列 - 订单相关
     */
    @Bean
    public Queue topicQueueOrder() {
        return QueueBuilder.durable(RabbitMQConstants.TOPIC_QUEUE_ORDER)
                .build();
    }

    /**
     * 主题队列 - 全部消息
     */
    @Bean
    public Queue topicQueueAll() {
        return QueueBuilder.durable(RabbitMQConstants.TOPIC_QUEUE_ALL)
                .build();
    }

    /**
     * 用户队列绑定 - 匹配 user.* 的路由键
     */
    @Bean
    public Binding topicBindingUser(Queue topicQueueUser, TopicExchange topicExchange) {
        return BindingBuilder.bind(topicQueueUser)
                .to(topicExchange)
                .with("user.*");
    }

    /**
     * 订单队列绑定 - 匹配 order.* 的路由键
     */
    @Bean
    public Binding topicBindingOrder(Queue topicQueueOrder, TopicExchange topicExchange) {
        return BindingBuilder.bind(topicQueueOrder)
                .to(topicExchange)
                .with("order.*");
    }

    /**
     * 全部队列绑定 - 匹配所有路由键
     */
    @Bean
    public Binding topicBindingAll(Queue topicQueueAll, TopicExchange topicExchange) {
        return BindingBuilder.bind(topicQueueAll)
                .to(topicExchange)
                .with("#");
    }

    // ==================== 延迟队列配置 ====================

    /**
     * 延迟交换机
     */
    @Bean
    public DirectExchange delayExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstants.DELAY_EXCHANGE)
                .durable(true)
                .delayed()  // 启用延迟功能（需要安装 rabbitmq-delayed-message-exchange 插件）
                .build();
    }

    /**
     * 延迟队列
     */
    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(RabbitMQConstants.DELAY_QUEUE)
                .build();
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayQueue)
                .to(delayExchange)
                .with(RabbitMQConstants.DELAY_ROUTING_KEY);
    }

    // ==================== 死信队列配置 ====================

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstants.DEAD_LETTER_QUEUE)
                .build();
    }
}