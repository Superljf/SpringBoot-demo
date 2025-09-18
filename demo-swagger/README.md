# Spring Boot Demo - Swagger + RabbitMQ 集成示例

> 这是一个完整的 Spring Boot 示例项目，主要演示了以下功能：
> - SpringDoc OpenAPI 3.0 自动生成 API 文档
> - Spring Security RBAC 权限管理
> - RabbitMQ 消息队列集成（直接队列、分列模式、主题模式、延迟队列）
> - AOP 日志记录
> - Spring Boot Admin 监控
> - Quartz 定时任务
> - Redis 缓存集成
>
> 启动项目，访问地址：
> - Swagger文档: http://localhost:8089/demo/swagger-ui.html
> - 应用监控: http://localhost:8089/demo/actuator

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+ (需要安装延迟消息插件)

### 安装依赖

```bash
# 启动 MySQL
sudo systemctl start mysql

# 启动 Redis
sudo systemctl start redis

# 启动 RabbitMQ
sudo systemctl start rabbitmq-server

# 安装 RabbitMQ 延迟消息插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

### 数据库初始化

执行项目根目录下的 `rbac_security_schema.sql` 文件初始化数据库表结构和测试数据。

### 启动应用

```bash
# 克隆项目
git clone [项目地址]

# 进入项目目录
cd demo-swagger

# 编译打包
mvn clean package

# 启动应用
java -jar target/demo-swagger.jar
# 或者
mvn spring-boot:run
```

## 📋 功能模块

## 📋 功能模块

### 1. API 文档模块 (SpringDoc OpenAPI 3.0)
- 自动生成 API 文档
- Swagger UI 界面
- 支持接口测试

### 2. 安全认证模块 (Spring Security + JWT)
- RBAC 权限模型
- JWT Token 认证
- 方法级权限控制
- 自定义权限注解

### 3. RabbitMQ 消息队列模块 🆕
- **直接队列模式**: 基于精确路由键匹配
- **分列模式**: 广播消息到所有绑定队列
- **主题模式**: 支持通配符的路由键匹配
- **延迟队列**: 支持消息延迟发送
- 消息确认机制
- 失败重试机制

### 4. AOP 日志模块
- Web 请求日志记录
- 权限验证切面
- 执行时间统计

### 5. 监控模块 (Spring Boot Admin + Actuator)
- 应用健康状态监控
- 系统指标收集
- JVM 监控

### 6. 定时任务模块 (Quartz)
- 动态创建任务
- 任务管理接口
- Cron 表达式支持

### 7. 缓存模块 (Redis + Spring Cache)
- 基于 Redis 的数据缓存
- 多种缓存策略
- 条件缓存和批量操作

### 8. 数据库操作模块 (MySQL + JDBC)
- CRUD 操作示例
- 分页查询
- 数据统计

## 🛠️ 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.10 | 主框架 |
| Spring Security | 5.x | 安全框架 |
| SpringDoc OpenAPI | 1.7.0 | API文档 |
| RabbitMQ | 3.8+ | 消息队列 |
| MySQL | 8.0.33 | 主数据库 |
| Redis | 6.0+ | 缓存数据库 |
| Quartz | - | 定时任务 |
| JWT | 0.11.5 | Token认证 |
| Lombok | - | 代码简化 |

## 📚 API 接口文档

### 认证相关
- `POST /auth/login` - 用户登录
- `POST /auth/refresh` - 刷新Token
- `GET /auth/info` - 获取用户信息

### RabbitMQ 消息队列 🆕

#### 直接队列模式
- `POST /rabbitmq/direct/send` - 发送直接消息
- `POST /rabbitmq/direct/send-with-routing-key` - 发送自定义路由键消息
- `POST /rabbitmq/direct/send-object` - 发送复杂对象消息

#### 分列模式
- `POST /rabbitmq/fanout/send` - 发送广播消息
- `POST /rabbitmq/fanout/send-notification` - 发送通知广播消息

#### 主题模式
- `POST /rabbitmq/topic/send-user-email` - 发送用户邮件消息
- `POST /rabbitmq/topic/send-user-sms` - 发送用户短信消息
- `POST /rabbitmq/topic/send-order-create` - 发送订单创建消息
- `POST /rabbitmq/topic/send-custom` - 发送自定义主题消息

#### 延迟队列
- `POST /rabbitmq/delay/send` - 发送延迟消息
- `POST /rabbitmq/delay/send-5s` - 发锁5秒延迟消息
- `POST /rabbitmq/delay/send-task-reminder` - 发送任务提醒延迟消息
- `POST /rabbitmq/delay/send-order-timeout` - 发送订单超时取消延迟消息

#### 测试接口
- `POST /rabbitmq/test/send-all-types` - 测试所有消息模式
- `GET /rabbitmq/info` - 获取RabbitMQ配置信息

### 数据库操作
- `GET /db/ping` - 数据库连通性检查
- `GET /db/users` - 查询用户列表
- `POST /db/users` - 新增用户
- `PUT /db/users/{id}` - 更新用户
- `DELETE /db/users/{id}` - 删除用户

### 定时任务管理
- `GET /schedule/jobs` - 获取任务列表
- `POST /schedule/jobs` - 创建定时任务
- `PUT /schedule/jobs/{id}/start` - 启动任务
- `PUT /schedule/jobs/{id}/pause` - 暂停任务

### 监控接口
- `GET /monitor/health` - 应用健康状态
- `GET /monitor/info` - 应用信息
- `GET /actuator/**` - Actuator监控端点

## 📁 项目结构

```
src/main/java/com/xkcoding/swagger/
├── annotation/          # 自定义注解
│   ├── RequirePermission.java
│   ├── RequireRole.java
│   └── WebLog.java
├── aspect/             # AOP切面
│   ├── PermissionAspect.java
│   └── WebLogAspect.java
├── common/             # 公共类
│   ├── ApiResponse.java
│   ├── PageResponse.java
│   └── RabbitMQConstants.java 🆕
├── config/             # 配置类
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   ├── RabbitMQConfig.java 🆕
│   └── JwtTokenUtil.java
├── controller/         # 控制器
│   ├── AuthController.java
│   ├── RabbitMQController.java 🆕
│   ├── DbController.java
│   └── UserController.java
├── entity/             # 实体类
│   ├── User.java
│   ├── MessageInfo.java 🆕
│   └── Role.java
├── service/            # 业务服务
│   ├── UserCacheService.java
│   └── rabbitmq/ 🆕
│       ├── DirectProducerService.java
│       ├── DirectConsumerService.java
│       ├── FanoutProducerService.java
│       ├── FanoutConsumerService.java
│       ├── TopicProducerService.java
│       ├── TopicConsumerService.java
│       ├── DelayProducerService.java
│       └── DelayConsumerService.java
└── job/                # 定时任务
    ├── BaseJob.java
    └── SimpleJob.java
```

## 📝 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test0815
    username: root
    password: 123456
```

### RabbitMQ配置 🆕
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### JWT配置
```yaml
jwt:
  secret: demo-swagger-security-jwt-secret-key-2024
  expiration: 24
  refresh-expiration: 7
```

## 🚀 快速体验 RabbitMQ 功能 🆕

### 1. 启动项目后访问 Swagger 文档
```
http://localhost:8089/demo/swagger-ui.html
```

### 2. 先登录获取 Token
```bash
curl -X POST "http://localhost:8089/demo/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456"}'
```

### 3. 使用 Token 测试 RabbitMQ 接口
```bash
# 测试直接消息
curl -X POST "http://localhost:8089/demo/rabbitmq/direct/send?message=测试直接消息" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试广播消息
curl -X POST "http://localhost:8089/demo/rabbitmq/fanout/send?message=测试广播消息" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试主题消息
curl -X POST "http://localhost:8089/demo/rabbitmq/topic/send-user-email?message=测试用户邮件" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试延迟消息
curl -X POST "http://localhost:8089/demo/rabbitmq/delay/send-5s?message=测试5秒延迟消息" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 一次性测试所有模式
curl -X POST "http://localhost:8089/demo/rabbitmq/test/send-all-types?message=全部测试" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. 查看控制台日志
在应用启动后，你将在控制台看到各种消息的发送和消费日志。

## 📚 详细文档

- [RabbitMQ 集成指南](RABBITMQ_INTEGRATION_GUIDE.md) 🆕
- [Spring Security RBAC 指南](SPRING_SECURITY_RBAC_GUIDE.md)
- [SpringDoc 迁移指南](SPRINGDOC_MIGRATION_GUIDE.md)
- [AOP 日志教程](AOP_LOG_TUTORIAL.md)
- [Quartz 集成指南](QUARTZ_INTEGRATION_GUIDE.md)
- [Redis 集成指南](REDIS_INTEGRATION_GUIDE.md)

## ⚠️ 注意事项

### RabbitMQ 延迟插件 🆕
延迟消息功能需要安装 `rabbitmq-delayed-message-exchange` 插件：

```bash
# 下载插件
wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.10.2/rabbitmq_delayed_message_exchange-3.10.2.ez

# 复制到插件目录
cp rabbitmq_delayed_message_exchange-3.10.2.ez $RABBITMQ_HOME/plugins/

# 启用插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange

# 重启 RabbitMQ
systemctl restart rabbitmq-server
```

### 默认用户账号
- **管理员**: admin / 123456 (拥有所有权限)
- **普通用户**: user / 123456 (基本权限)
- **访客**: guest / 123456 (只读权限)

## 📦 打包部署

```bash
# Maven 打包
mvn clean package -DskipTests

# Docker 构建
docker build -t demo-swagger:latest .

# Docker 运行
docker run -d -p 8089:8089 --name demo-swagger demo-swagger:latest
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📝 许可证

MIT License

---

**新增功能**: 🆕 表示新增的 RabbitMQ 相关功能

# 附录: 原始文档

以下是原始的 Spring Boot Swagger 集成示例文档：

---

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <artifactId>spring-boot-demo-swagger</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>spring-boot-demo-swagger</name>
    <description>Demo project for Spring Boot</description>

    <parent>
        <groupId>com.xkcoding</groupId>
        <artifactId>spring-boot-demo</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
        <swagger.version>2.9.2</swagger.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>${swagger.version}</version>
        </dependency>

        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>${swagger.version}</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <finalName>spring-boot-demo-swagger</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

## Swagger2Config.java

```java

@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xkcoding.swagger.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("spring-boot-demo")
                .description("这是一个简单的 Swagger API 演示")
                .version("1.0.0-SNAPSHOT")
                .build();
    }

}
```

## UserController.java

> 主要演示API层的注解。

```java

@RestController
@RequestMapping("/user")
@Api(tags = "1.0.0-SNAPSHOT", description = "用户管理", value = "用户管理")
@Slf4j
public class UserController {
    @GetMapping
    @ApiOperation(value = "条件查询（DONE）", notes = "备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户名", dataType = DataType.STRING, paramType = ParamType.QUERY, defaultValue = "xxx")})
    public ApiResponse<User> getByUserName(String username) {
        log.info("多个参数用  @ApiImplicitParams");
        return ApiResponse.<User>builder().code(200)
                .message("操作成功")
                .data(new User(1, username, "JAVA"))
                .build();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "主键查询（DONE）", notes = "备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "用户编号", dataType = DataType.INT, paramType = ParamType.PATH)})
    public ApiResponse<User> get(@PathVariable Integer id) {
        log.info("单个参数用  @ApiImplicitParam");
        return ApiResponse.<User>builder().code(200)
                .message("操作成功")
                .data(new User(id, "u1", "p1"))
                .build();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户（DONE）", notes = "备注")
    @ApiImplicitParam(name = "id", value = "用户编号", dataType = DataType.INT, paramType = ParamType.PATH)
    public void delete(@PathVariable Integer id) {
        log.info("单个参数用 ApiImplicitParam");
    }

    @PostMapping
    @ApiOperation(value = "添加用户（DONE）")
    public User post(@RequestBody User user) {
        log.info("如果是 POST PUT 这种带 @RequestBody 的可以不用写 @ApiImplicitParam");
        return user;
    }

    @PostMapping("/multipar")
    @ApiOperation(value = "添加用户（DONE）")
    public List<User> multipar(@RequestBody List<User> user) {
        log.info("如果是 POST PUT 这种带 @RequestBody 的可以不用写 @ApiImplicitParam");

        return user;
    }

    @PostMapping("/array")
    @ApiOperation(value = "添加用户（DONE）")
    public User[] array(@RequestBody User[] user) {
        log.info("如果是 POST PUT 这种带 @RequestBody 的可以不用写 @ApiImplicitParam");
        return user;
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户（DONE）")
    public void put(@PathVariable Long id, @RequestBody User user) {
        log.info("如果你不想写 @ApiImplicitParam 那么 swagger 也会使用默认的参数名作为描述信息 ");
    }

    @PostMapping("/{id}/file")
    @ApiOperation(value = "文件上传（DONE）")
    public String file(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        log.info(file.getContentType());
        log.info(file.getName());
        log.info(file.getOriginalFilename());
        return file.getOriginalFilename();
    }
}
```

## ApiResponse.java

> 主要演示了 实体类 的注解。

```java

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "通用PI接口返回", description = "Common Api Response")
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = -8987146499044811408L;
    /**
     * 通用返回状态
     */
    @ApiModelProperty(value = "通用返回状态", required = true)
    private Integer code;
    /**
     * 通用返回信息
     */
    @ApiModelProperty(value = "通用返回信息", required = true)
    private String message;
    /**
     * 通用返回数据
     */
    @ApiModelProperty(value = "通用返回数据", required = true)
    private T data;
}
```

## 参考

1. swagger 官方网站：https://swagger.io/

2. swagger 官方文档：https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Getting-started

3. swagger 常用注解：https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations



测试示例
访问 http://localhost:8089/demo/swagger-ui.html#/ 测试以下场景：
基本分页：GET /demo/db/users/page?page=1&size=5
搜索 + 分页：GET /demo/db/users/page?keyword=开发&page=1&size=10
排序 + 分页：GET /demo/db/users/page?sortBy=name&sortDir=asc&page=1&size=10
综合查询：GET /demo/db/users/page?keyword=前端&sortBy=id&sortDir=desc&page=1&size=5


# 健康检查
curl http://localhost:8089/demo/actuator/health

# 应用信息
curl http://localhost:8089/demo/actuator/info

# 指标概览
curl http://localhost:8089/demo/actuator/metrics

# 自定义状态接口
curl http://localhost:8089/demo/monitor/status


Controller 层：接收 HTTP 请求，返回响应。
    
Service 层：处理业务逻辑。

DAO/Repository 层：操作数据库。

Entity/Model 层：实体类，对应数据库表。

Database：最终的数据存储。