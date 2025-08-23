# Spring Boot 集成 Spring Security RBAC 权限管理 - 快速开始

## 📋 项目概述

本项目演示了如何在 Spring Boot 2.7.10 中集成 Spring Security，实现基于 RBAC（Role-Based Access Control）权限模型的完整权限管理系统。

### ✨ 核心特性

- 🔐 **JWT Token 认证**：无状态的 Token 认证机制
- 🎭 **RBAC 权限模型**：用户-角色-权限三级权限控制
- 🛡️ **AOP 权限验证**：基于注解的细粒度权限控制
- 📝 **请求日志记录**：完整的 API 访问日志
- 🔍 **API 文档集成**：SpringDoc OpenAPI 3.0 支持
- 🧪 **完整测试覆盖**：单元测试和集成测试

## 🚀 快速开始

### 1. 环境准备

确保您的开发环境满足以下要求：

```bash
# 必需环境
- JDK 1.8+
- Maven 3.x
- MySQL 8.x
- 推荐 IDE: IntelliJ IDEA
```

### 2. 数据库初始化

**步骤 1：创建数据库**
```sql
CREATE DATABASE test0815 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**步骤 2：执行初始化脚本**
```bash
# 执行项目根目录下的 SQL 脚本
mysql -u root -p test0815 < rbac_security_schema.sql
```

该脚本会创建：
- 👤 用户表（t_user）
- 🎭 角色表（t_role）
- 🔑 权限表（t_permission）
- 🔗 用户角色关联表（t_user_role）
- 🔗 角色权限关联表（t_role_permission）

**默认用户账号：**
| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | ROLE_ADMIN | 系统管理员，拥有所有权限 |
| user | 123456 | ROLE_USER | 普通用户，拥有基本权限 |
| guest | 123456 | ROLE_GUEST | 访客用户，只有查看权限 |

### 3. 配置文件

确认 `application.yml` 中的数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test0815?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

# JWT 配置
jwt:
  secret: demo-swagger-security-jwt-secret-key-2024-very-long-secret
  expiration: 24  # 访问令牌过期时间（小时）
  refresh-expiration: 7  # 刷新令牌过期时间（天）
```

### 4. 启动应用

```bash
# 方式一：使用 Maven
mvn spring-boot:run

# 方式二：编译后运行
mvn clean package
java -jar target/demo-swagger.jar
```

启动成功后，应用运行在：http://localhost:8089/demo

## 🔧 API 使用指南

### 认证相关接口

#### 1. 用户登录
```bash
POST /demo/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456",
  "rememberMe": false
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 24,
    "user": {
      "id": 1,
      "username": "admin",
      "name": "系统管理员",
      "email": "admin@example.com",
      "roles": ["ROLE_ADMIN"],
      "permissions": ["user:read", "user:create", "user:update", "user:delete", "..."],
      "loginTime": "2024-12-19T14:30:00"
    }
  }
}
```

#### 2. 获取当前用户信息
```bash
GET /demo/auth/me
Authorization: Bearer {your-jwt-token}
```

#### 3. 刷新Token
```bash
POST /demo/auth/refresh?refreshToken={your-refresh-token}
```

#### 4. 用户注销
```bash
POST /demo/auth/logout
Authorization: Bearer {your-jwt-token}
```

### 受保护的接口示例

#### 查看用户列表（需要 user:read 权限）
```bash
GET /demo/user
Authorization: Bearer {your-jwt-token}
```

#### 创建用户（需要 user:create 权限）
```bash
POST /demo/user
Authorization: Bearer {your-jwt-token}
Content-Type: application/json

{
  "name": "新用户",
  "job": "开发工程师",
  "username": "newuser",
  "password": "123456",
  "email": "newuser@example.com"
}
```

## 🔒 权限控制说明

### 权限模型

本系统采用 RBAC（基于角色的访问控制）模型：

```
用户 (User) ←→ 角色 (Role) ←→ 权限 (Permission)
```

### 预定义权限

| 权限编码 | 权限名称 | 描述 |
|----------|----------|------|
| user:read | 查看用户 | 查看用户信息 |
| user:create | 创建用户 | 创建新用户 |
| user:update | 更新用户 | 更新用户信息 |
| user:delete | 删除用户 | 删除用户 |
| user:upload | 用户文件上传 | 用户文件上传 |
| db:read | 数据库查询 | 数据库查询操作 |
| db:write | 数据库写入 | 数据库写入操作 |
| monitor:read | 监控查看 | 查看系统监控信息 |
| actuator:read | Actuator查看 | 查看Actuator端点 |

### 权限验证方式

#### 1. Spring Security 原生注解
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAuthority('user:read')")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('user:delete')")
```

#### 2. 自定义权限注解
```java
@RequirePermission("user:read")
@RequireRole("ROLE_ADMIN")
@RequirePermission(value = {"user:read", "user:update"}, requireAll = false) // OR关系
@RequirePermission(value = {"user:read", "user:update"}, requireAll = true)  // AND关系
```

## 🎯 核心组件说明

### 1. JWT Token 工具类
- **文件位置**：`com.xkcoding.swagger.config.JwtTokenUtil`
- **功能**：Token 生成、验证、解析、刷新
- **配置**：支持自定义密钥和过期时间

### 2. 权限验证切面
- **文件位置**：`com.xkcoding.swagger.aspect.PermissionAspect`
- **功能**：拦截标记权限注解的方法，进行权限验证
- **支持**：类级别和方法级别注解，AND/OR 逻辑关系

### 3. 用户认证服务
- **文件位置**：`com.xkcoding.swagger.service.impl.SecurityUserServiceImpl`
- **功能**：用户认证、权限查询、用户管理
- **集成**：Spring Security UserDetailsService

### 4. 安全配置类
- **文件位置**：`com.xkcoding.swagger.config.SecurityConfig`
- **功能**：Security 核心配置、URL 权限配置、过滤器链
- **特性**：无状态会话、CORS 支持、异常处理

## 📊 监控和管理

### 1. API 文档
访问地址：http://localhost:8089/demo/swagger-ui.html

### 2. 应用监控
- 健康检查：http://localhost:8089/demo/actuator/health
- 应用信息：http://localhost:8089/demo/actuator/info
- 所有端点：http://localhost:8089/demo/actuator

### 3. 日志记录
使用 `@WebLog` 注解自动记录接口访问日志：
```java
@WebLog(value = "用户登录", logArgs = true, logResult = false, logTime = true)
```

## 🧪 测试指南

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=SecurityIntegrationTest
```

### 手动测试步骤

1. **登录获取Token**
2. **使用Token访问受保护资源**
3. **测试不同角色的权限边界**
4. **验证Token过期和刷新机制**

## 🔧 常见问题

### Q1: Token 验证失败
**解决方案**：
- 检查 JWT 密钥配置
- 确认 Token 格式正确（Bearer + 空格 + Token）
- 验证 Token 是否过期

### Q2: 权限验证不生效
**解决方案**：
- 确认用户已正确分配角色和权限
- 检查权限注解是否正确使用
- 验证 AOP 切面是否正常工作

### Q3: 数据库连接失败
**解决方案**：
- 检查数据库连接配置
- 确认数据库服务已启动
- 验证用户名密码正确性

## 📚 扩展开发

### 添加新权限

1. **在权限表中插入新权限**：
```sql
INSERT INTO t_permission (permission_code, permission_name, description) 
VALUES ('custom:action', '自定义操作', '自定义功能权限');
```

2. **为角色分配权限**：
```sql
INSERT INTO t_role_permission (role_id, permission_id, create_by) 
VALUES (1, LAST_INSERT_ID(), 'system');
```

3. **在代码中使用权限**：
```java
@RequirePermission("custom:action")
public void customAction() {
    // 业务逻辑
}
```

### 自定义权限验证逻辑

继承 `PermissionAspect` 或创建新的切面类来实现自定义权限验证逻辑。

## 📞 技术支持

如有问题，请：
1. 查看应用日志获取详细错误信息
2. 参考 API 文档了解接口规范
3. 运行测试用例验证功能正确性

---

🎉 **恭喜！您已成功集成了 Spring Security RBAC 权限管理系统！**