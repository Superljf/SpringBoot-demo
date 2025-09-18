# RabbitMQ 集成指南

本指南详细介绍了如何在Spring Boot项目中集成RabbitMQ，实现基于直接队列、分列模式、主题模式、延迟队列的消息发送和接收功能。

## 📋 功能概述

### 🎯 实现的消息模式

1. **直接队列模式 (Direct Exchange)**
   - 基于精确路由键匹配
   - 点对点消息传递
   - 支持自定义路由键

2. **分列模式 (Fanout Exchange)**
   - 广播消息到所有绑定队列
   - 忽略路由键
   - 适用于通知、日志等场景

3. **主题模式 (Topic Exchange)**
   - 基于路由键模式匹配
   - 支持通配符 (*：匹配一个单词，#：匹配零个或多个单词)
   - 灵活的消息路由

4. **延迟队列模式 (Delay Exchange)**
   - 支持消息延迟发送
   - 基于RabbitMQ延迟插件
   - 适用于定时任务、超时处理等场景

## 🏗️ 技术架构

### 依赖配置

```xml
<!-- RabbitMQ -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- RabbitMQ 延迟插件支持 -->
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit</artifactId>
</dependency>
```

### 配置信息

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    virtual-host: /
    username: guest
    password: guest
    connection-timeout: 15000
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
      receive-timeout: 5000
      reply-timeout: 5000
    listener:
      simple:
        acknowledge-mode: manual
        concurrency: 1
        max-concurrency: 10
        prefetch: 1
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
```

## 📦 核心组件

### 1. 配置类

- **RabbitMQConfig.java**: 定义交换机、队列和绑定关系
- **消息转换器**: 支持JSON格式消息
- **发布确认**: 保证消息可靠投递

### 2. 消息实体

- **MessageInfo.java**: 统一的消息实体类
- **RabbitMQConstants.java**: 消息队列常量定义

### 3. 生产者服务

- **DirectProducerService**: 直接队列生产者
- **FanoutProducerService**: 分列模式生产者
- **TopicProducerService**: 主题模式生产者
- **DelayProducerService**: 延迟队列生产者

### 4. 消费者服务

- **DirectConsumerService**: 直接队列消费者
- **FanoutConsumerService**: 分列模式消费者
- **TopicConsumerService**: 主题模式消费者
- **DelayConsumerService**: 延迟队列消费者

### 5. REST API控制器

- **RabbitMQController**: 提供完整的REST API接口

## 🚀 使用示例

### 1. 直接队列模式

```java
// 发送直接消息
@PostMapping("/rabbitmq/direct/send")
public ApiResponse<String> sendDirectMessage(@RequestParam String message) {
    directProducerService.sendDirectMessage(message);
    return ApiResponse.success("消息发送成功");
}

// 消费直接消息
@RabbitListener(queues = RabbitMQConstants.DIRECT_QUEUE)
public void handleDirectMessage(MessageInfo messageInfo, Message message, Channel channel) {
    // 处理消息逻辑
    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
}
```

### 2. 分列模式

```java
// 发送广播消息
fanoutProducerService.sendFanoutMessage("这是一条广播消息");

// 多个队列同时接收
@RabbitListener(queues = RabbitMQConstants.FANOUT_QUEUE_1)
public void handleFanoutMessage1(MessageInfo messageInfo) {
    // 队列1处理逻辑 - 日志记录
}

@RabbitListener(queues = RabbitMQConstants.FANOUT_QUEUE_2)
public void handleFanoutMessage2(MessageInfo messageInfo) {
    // 队列2处理逻辑 - 统计分析
}
```

### 3. 主题模式

```java
// 发送主题消息
topicProducerService.sendUserEmailMessage("用户邮件内容");        // 路由键: user.email.send
topicProducerService.sendOrderCreateMessage("订单创建通知");      // 路由键: order.create.notify

// 消费主题消息
@RabbitListener(queues = RabbitMQConstants.TOPIC_QUEUE_USER)     // 监听 user.*
@RabbitListener(queues = RabbitMQConstants.TOPIC_QUEUE_ORDER)    // 监听 order.*
@RabbitListener(queues = RabbitMQConstants.TOPIC_QUEUE_ALL)      // 监听 #
```

### 4. 延迟队列

```java
// 发送延迟消息
delayProducerService.sendDelayMessage("延迟消息", 5000L);          // 5秒后处理
delayProducerService.sendTaskReminderMessage("会议提醒", "重要会议即将开始", 30000L);
delayProducerService.sendOrderTimeoutCancelMessage("ORD001", 1800000L);  // 30分钟后检查订单

// 消费延迟消息
@RabbitListener(queues = RabbitMQConstants.DELAY_QUEUE)
public void handleDelayMessage(MessageInfo messageInfo) {
    // 延迟消息处理逻辑
    if (messageInfo.getContent().contains("任务提醒")) {
        handleTaskReminder(messageInfo);
    } else if (messageInfo.getContent().contains("订单超时")) {
        handleOrderTimeout(messageInfo);
    }
}
```

## 🔧 API接口说明

### 直接队列API

- `POST /rabbitmq/direct/send`: 发送直接消息
- `POST /rabbitmq/direct/send-with-routing-key`: 发送自定义路由键消息
- `POST /rabbitmq/direct/send-object`: 发送复杂对象消息

### 分列模式API

- `POST /rabbitmq/fanout/send`: 发送广播消息
- `POST /rabbitmq/fanout/send-notification`: 发送通知广播消息

### 主题模式API

- `POST /rabbitmq/topic/send-user-email`: 发送用户邮件消息
- `POST /rabbitmq/topic/send-user-sms`: 发送用户短信消息
- `POST /rabbitmq/topic/send-order-create`: 发送订单创建消息
- `POST /rabbitmq/topic/send-custom`: 发送自定义主题消息
- `POST /rabbitmq/topic/send-user-notification`: 发送用户通知消息

### 延迟队列API

- `POST /rabbitmq/delay/send`: 发送延迟消息
- `POST /rabbitmq/delay/send-5s`: 发送5秒延迟消息
- `POST /rabbitmq/delay/send-task-reminder`: 发送任务提醒延迟消息
- `POST /rabbitmq/delay/send-order-timeout`: 发送订单超时取消延迟消息

### 测试API

- `POST /rabbitmq/test/send-all-types`: 测试所有消息模式
- `GET /rabbitmq/info`: 获取RabbitMQ配置信息

## 📊 交换机和队列配置

### 交换机列表

| 交换机名称 | 类型 | 说明 |
|---------|------|------|
| demo.direct.exchange | Direct | 直接交换机 |
| demo.fanout.exchange | Fanout | 分列交换机 |
| demo.topic.exchange | Topic | 主题交换机 |
| demo.delay.exchange | x-delayed-message | 延迟交换机 |

### 队列列表

| 队列名称 | 绑定交换机 | 路由键/模式 | 说明 |
|---------|-----------|------------|------|
| demo.direct.queue | demo.direct.exchange | demo.direct | 直接队列 |
| demo.fanout.queue.1 | demo.fanout.exchange | - | 分列队列1（日志服务） |
| demo.fanout.queue.2 | demo.fanout.exchange | - | 分列队列2（统计服务） |
| demo.topic.queue.user | demo.topic.exchange | user.* | 用户相关队列 |
| demo.topic.queue.order | demo.topic.exchange | order.* | 订单相关队列 |
| demo.topic.queue.all | demo.topic.exchange | # | 全部消息队列 |
| demo.delay.queue | demo.delay.exchange | demo.delay | 延迟队列 |

## ⚠️ 注意事项

### 1. 环境要求

- **RabbitMQ服务器**: 3.8.0+
- **延迟插件**: 需要安装 `rabbitmq-delayed-message-exchange` 插件
- **Java版本**: 1.8+
- **Spring Boot版本**: 2.7.0+

### 2. 插件安装

```bash
# 下载延迟消息插件
wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.10.2/rabbitmq_delayed_message_exchange-3.10.2.ez

# 复制插件到RabbitMQ插件目录
cp rabbitmq_delayed_message_exchange-3.10.2.ez $RABBITMQ_HOME/plugins/

# 启用插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 重启RabbitMQ
systemctl restart rabbitmq-server
```

### 3. 权限配置

所有API接口都配置了相应的权限控制：

- `rabbitmq:direct:send`: 直接队列发送权限
- `rabbitmq:fanout:send`: 分列模式发送权限
- `rabbitmq:topic:send`: 主题模式发送权限
- `rabbitmq:delay:send`: 延迟队列发送权限
- `rabbitmq:test:send`: 测试权限（仅管理员）
- `rabbitmq:info:read`: 信息查看权限

### 4. 消息确认机制

- **生产者确认**: 通过 `publisher-confirm-type: correlated` 配置
- **消费者确认**: 使用手动确认模式 `acknowledge-mode: manual`
- **消息重试**: 支持失败重试，最大重试3次

### 5. 错误处理

- **发送失败**: 记录错误日志并抛出运行时异常
- **消费失败**: 拒绝消息并重新排队
- **连接断开**: 自动重连机制

## 🎯 最佳实践

### 1. 消息设计

- 使用统一的 `MessageInfo` 实体类
- 设置合适的消息ID、时间戳等元数据
- 合理设计路由键命名规范

### 2. 性能优化

- 设置合适的预取数量 (`prefetch: 1`)
- 控制并发消费者数量
- 使用连接池和缓存

### 3. 监控和运维

- 启用Spring Boot Actuator监控
- 记录详细的消息处理日志
- 设置适当的超时和重试策略

### 4. 安全考虑

- 配置访问权限控制
- 使用HTTPS传输
- 定期更新RabbitMQ版本

## 🔍 故障排查

### 常见问题

1. **连接失败**: 检查RabbitMQ服务状态和网络连通性
2. **消息丢失**: 确认生产者确认和消费者手动确认配置
3. **延迟消息不生效**: 检查延迟插件是否正确安装
4. **权限错误**: 确认用户具有相应的操作权限

### 日志查看

```bash
# 查看RabbitMQ日志
tail -f /var/log/rabbitmq/rabbit@hostname.log

# 查看应用日志
tail -f logs/demo-swagger.log
```

---

## 🎉 总结

本RabbitMQ集成实现了完整的消息队列功能，支持四种主要的消息模式，提供了丰富的API接口和完善的错误处理机制。通过合理的配置和使用，可以满足大部分业务场景的消息处理需求。

**主要特点:**
- ✅ 支持四种消息模式
- ✅ 完整的REST API接口
- ✅ 权限控制和安全机制
- ✅ 消息确认和重试机制
- ✅ 详细的文档和示例
- ✅ 易于扩展和维护