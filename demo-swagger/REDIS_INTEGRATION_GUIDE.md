# Spring Boot Redis 集成指南

## 🎯 概述

本项目集成了Redis缓存功能，提供完整的数据操作和缓存解决方案。

## 📦 依赖配置

在`pom.xml`中添加以下依赖：

```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Redis连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>

<!-- 缓存支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

## ⚙️ 配置文件

在`application.yml`中配置Redis连接：

```yaml
spring:
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-wait: -1ms
        max-idle: 8
        min-idle: 0

  # 缓存配置
  cache:
    type: redis
    cache-names: 
      - userCache
      - dataCache
      - tempCache
    redis:
      time-to-live: 600000  # 10分钟
      cache-null-values: true
      key-prefix: "demo:cache:"
      use-key-prefix: true
```

## 🔧 核心组件

### 1. Redis配置类
`RedisConfig.java` - 配置RedisTemplate和缓存管理器

### 2. Redis服务类
`RedisService.java` - 提供Redis基础操作方法：
- String操作（set、get、incr、decr）
- Hash操作（hset、hget、hmset）
- List操作（lpush、rpush、lrange）
- Set操作（sadd、smembers）
- ZSet操作（zadd、zrange）

### 3. 缓存服务类
`UserCacheService.java` - 演示Spring Cache注解使用：
- `@Cacheable` - 查询缓存
- `@CachePut` - 更新缓存
- `@CacheEvict` - 清除缓存

## 🚀 快速开始

### 1. 启动Redis服务

```bash
# 使用Docker
docker run -d --name redis -p 6379:6379 redis:latest

# 或使用本地Redis
redis-server
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 访问API文档

打开浏览器访问：`http://localhost:8089/demo/swagger-ui/index.html`

## 📋 API接口

### Redis基础操作

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/redis/string/set` | POST | 设置字符串值 |
| `/api/redis/string/get` | GET | 获取字符串值 |
| `/api/redis/hash/set` | POST | 设置Hash字段 |
| `/api/redis/list/push` | POST | 向List添加元素 |
| `/api/redis/set/add` | POST | 向Set添加元素 |

### 缓存操作

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/redis/cache/user/{id}` | GET | 查询用户（带缓存） |
| `/api/redis/cache/users` | GET | 查询所有用户（带缓存） |
| `/api/redis/cache/users` | POST | 保存用户并缓存 |
| `/api/redis/cache/users` | PUT | 更新用户和缓存 |
| `/api/redis/cache/users/{id}` | DELETE | 删除用户和缓存 |

## 🧪 测试示例

### 基础操作测试

```bash
# 设置字符串值
curl -X POST "http://localhost:8089/demo/api/redis/string/set?key=test&value=hello"

# 获取字符串值
curl -X GET "http://localhost:8089/demo/api/redis/string/get?key=test"

# 计数器递增
curl -X GET "http://localhost:8089/demo/api/redis/string/incr?key=counter&delta=1"
```

### 缓存测试

```bash
# 第一次查询（从"数据库"查询，较慢）
curl -X GET "http://localhost:8089/demo/api/redis/cache/user/1"

# 第二次查询（从缓存查询，很快）
curl -X GET "http://localhost:8089/demo/api/redis/cache/user/1"
```

## 📊 性能对比

| 操作 | 数据库查询 | Redis缓存 | 性能提升 |
|------|-----------|----------|----------|
| 单用户查询 | ~1000ms | ~5ms | 200倍 |
| 全用户查询 | ~2000ms | ~8ms | 250倍 |

## 🔍 缓存注解说明

### @Cacheable
如果缓存中有数据，直接返回；没有则执行方法并缓存结果。

```java
@Cacheable(value = "userCache", key = "#id")
public User findById(Integer id) {
    // 数据库查询
    return user;
}
```

### @CachePut
总是执行方法，并将结果放入缓存。

```java
@CachePut(value = "userCache", key = "#user.id")
public User updateUser(User user) {
    // 更新逻辑
    return updatedUser;
}
```

### @CacheEvict
删除指定的缓存。

```java
@CacheEvict(value = "userCache", key = "#id")
public boolean deleteUser(Integer id) {
    // 删除逻辑
    return true;
}
```

## 🛠️ 监控调试

### 查看Redis数据

```bash
# 连接Redis客户端
redis-cli

# 查看所有键
KEYS *

# 查看缓存键
KEYS demo:cache:*

# 获取缓存值
GET "demo:cache:userCache::1"
```

### 应用日志

应用会输出缓存操作日志：
```
INFO - 执行数据库查询，用户ID: 1
INFO - 查询耗时: 1001ms
INFO - 找到用户: User(id=1, name=张三, job=前端开发)
```

## ⚠️ 注意事项

1. **Redis依赖**：确保Redis服务正常运行
2. **缓存一致性**：更新数据时及时清除相关缓存
3. **内存管理**：设置合理的过期时间
4. **序列化**：注意对象序列化兼容性

## 🎯 使用建议

1. **适合缓存的数据**：
   - 查询频率高的数据
   - 计算复杂的结果
   - 相对稳定的数据

2. **Key命名规范**：
   - 使用有意义的前缀
   - 保持Key的唯一性
   - 如：`user:profile:123`

3. **过期时间策略**：
   - 热点数据：较长过期时间
   - 临时数据：较短过期时间
   - 敏感数据：及时清除

## 🚀 扩展功能

基于当前集成，可进一步实现：
- 分布式锁
- 消息队列
- 限流器
- 会话存储
- 计数器

---

Redis集成完成！现在可以享受高效的缓存功能！🎉
