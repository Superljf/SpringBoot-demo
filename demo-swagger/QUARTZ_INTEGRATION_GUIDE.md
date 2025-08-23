# Spring Boot Quartz 定时任务集成指南

## 🎯 概述

本项目集成了Quartz定时任务调度框架，提供完整的定时任务管理功能，包括任务的增删改查、状态管理和前端可视化操作界面。

## 📦 依赖配置

在`pom.xml`中添加以下依赖：

```xml
<!-- Quartz定时任务 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>

<!-- Thymeleaf模板引擎 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## ⚙️ 配置文件

在`application.yml`中配置Quartz：

```yaml
spring:
  # Quartz定时任务配置
  quartz:
    scheduler-name: demo-scheduler
    startup-delay: 10s
    auto-startup: true
    wait-for-jobs-to-complete-on-shutdown: true
    overwrite-existing-jobs: false
    job-store-type: jdbc
    jdbc:
      initialize-schema: embedded
    properties:
      org:
        quartz:
          scheduler:
            instanceName: demo-scheduler
            instanceId: AUTO
          jobStore:
            class: org.quartz.impl.jdbcjobstore.JobStoreTX
            driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
            tablePrefix: QRTZ_
            isClustered: false
            clusterCheckinInterval: 10000
            useProperties: false
          threadPool:
            class: org.quartz.simpl.SimpleThreadPool
            threadCount: 10
            threadPriority: 5
            threadsInheritContextClassLoaderOfInitializingThread: true

  # Thymeleaf模板配置
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    encoding: UTF-8
    mode: HTML
    cache: false
```

## 🏗️ 系统架构

```
定时任务管理系统
├── 实体层 (Entity)
│   └── ScheduleJob - 任务信息实体
├── 任务层 (Job)
│   ├── BaseJob - 任务基类
│   ├── SimpleJob - 简单任务示例
│   └── DataSyncJob - 数据同步任务示例
├── 服务层 (Service)
│   ├── ScheduleJobService - 任务管理接口
│   └── ScheduleJobServiceImpl - 任务管理实现
├── 控制层 (Controller)
│   └── ScheduleJobController - 任务管理API和页面
└── 前端页面 (Templates)
    ├── index.html - 任务列表页面
    ├── add.html - 新增任务页面
    └── edit.html - 编辑任务页面
```

## 🚀 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

### 2. 访问管理界面

打开浏览器访问：`http://localhost:8089/demo/schedule/index`

### 3. 访问API文档

Swagger文档：`http://localhost:8089/demo/swagger-ui/index.html`

## 📋 功能特性

### 核心功能

✅ **任务管理**
- 新增定时任务
- 删除定时任务  
- 暂停定时任务
- 恢复定时任务
- 修改任务Cron表达式
- 立即执行任务

✅ **任务查询**
- 任务列表查询
- 任务详情查询
- 任务状态监控

✅ **前端界面**
- 响应式设计
- Bootstrap美化
- 实时状态更新
- 操作友好提示

### 任务状态

| 状态 | 值 | 描述 |
|------|---|------|
| 正常 | 1 | 任务正常执行 |
| 暂停 | 0 | 任务已暂停 |

## 📱 界面展示

### 任务列表页面
- 📊 统计卡片：总任务数、运行中、已暂停
- 📋 任务列表：显示任务详情和状态
- 🎛️ 操作按钮：运行、暂停/恢复、编辑、删除

### 新增任务页面
- 📝 任务配置表单
- ✅ Cron表达式验证
- 📖 Cron帮助文档

### 编辑任务页面
- 📄 任务基本信息（只读）
- ✏️ Cron表达式修改
- 🎮 任务状态控制

## 🛠️ API接口

### 任务管理API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/schedule/api/jobs` | GET | 获取任务列表 |
| `/schedule/api/jobs/{name}/{group}` | GET | 获取任务详情 |
| `/schedule/api/jobs` | POST | 创建任务 |
| `/schedule/api/jobs/{name}/{group}/cron` | PUT | 更新Cron表达式 |
| `/schedule/api/jobs/{name}/{group}/pause` | PUT | 暂停任务 |
| `/schedule/api/jobs/{name}/{group}/resume` | PUT | 恢复任务 |
| `/schedule/api/jobs/{name}/{group}/run` | PUT | 立即执行 |
| `/schedule/api/jobs/{name}/{group}` | DELETE | 删除任务 |

### 辅助API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/schedule/api/cron/validate` | GET | 验证Cron表达式 |
| `/schedule/api/job-classes` | GET | 获取可用任务类 |

## 🎯 创建自定义任务

### 1. 继承BaseJob类

```java
@Component
@Slf4j
public class MyCustomJob extends BaseJob {

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        // 获取任务参数
        String param = getJobData(context, "param");
        
        // 执行具体业务逻辑
        log.info("执行自定义任务，参数：{}", param);
        
        // 你的业务代码
        // ...
    }
}
```

### 2. 注册任务类

在`ScheduleJobServiceImpl.getAvailableJobClasses()`方法中添加：

```java
return Arrays.asList(
    "com.xkcoding.swagger.job.SimpleJob",
    "com.xkcoding.swagger.job.DataSyncJob",
    "com.xkcoding.swagger.job.MyCustomJob"  // 新增
);
```

## 📅 Cron表达式说明

### 格式
```
秒 分 时 日 月 周 [年]
```

### 常用示例
| 表达式 | 说明 |
|--------|------|
| `0 0/5 * * * ?` | 每5分钟执行 |
| `0 0 2 * * ?` | 每天凌晨2点执行 |
| `0 0 10,14,16 * * ?` | 每天10点、14点、16点执行 |
| `0 0/30 9-17 * * ?` | 工作时间内每30分钟执行 |
| `0 0 12 ? * WED` | 每周三中午12点执行 |
| `0 0 12 1 * ?` | 每月1号中午12点执行 |

### 特殊字符
- `*`：匹配任意值
- `?`：用于日和周字段，表示不指定值
- `-`：表示范围
- `,`：表示列表
- `/`：表示增量

## 🧪 测试示例

### 1. 创建简单任务

```bash
curl -X POST "http://localhost:8089/demo/schedule/api/jobs" \
-H "Content-Type: application/json" \
-d '{
  "jobName": "testJob",
  "jobGroup": "DEFAULT",
  "description": "测试任务",
  "jobClassName": "com.xkcoding.swagger.job.SimpleJob",
  "cronExpression": "0/10 * * * * ?",
  "jobData": "message=Hello Quartz",
  "status": 1
}'
```

### 2. 查询任务列表

```bash
curl -X GET "http://localhost:8089/demo/schedule/api/jobs"
```

### 3. 暂停任务

```bash
curl -X PUT "http://localhost:8089/demo/schedule/api/jobs/testJob/DEFAULT/pause"
```

### 4. 恢复任务

```bash
curl -X PUT "http://localhost:8089/demo/schedule/api/jobs/testJob/DEFAULT/resume"
```

### 5. 立即执行

```bash
curl -X PUT "http://localhost:8089/demo/schedule/api/jobs/testJob/DEFAULT/run"
```

### 6. 删除任务

```bash
curl -X DELETE "http://localhost:8089/demo/schedule/api/jobs/testJob/DEFAULT"
```

## 📊 数据库表说明

Quartz会自动创建以下数据库表：

- `QRTZ_JOB_DETAILS` - 任务详情
- `QRTZ_TRIGGERS` - 触发器信息
- `QRTZ_CRON_TRIGGERS` - Cron触发器
- `QRTZ_FIRED_TRIGGERS` - 已触发的触发器
- `QRTZ_PAUSED_TRIGGER_GRPS` - 暂停的触发器组
- `QRTZ_SCHEDULER_STATE` - 调度器状态
- `QRTZ_LOCKS` - 锁信息

## ⚠️ 注意事项

1. **任务类要求**：
   - 必须继承`BaseJob`或实现`Job`接口
   - 必须添加`@Component`注解
   - 要有无参构造函数

2. **Cron表达式**：
   - 使用6位或7位表达式
   - 注意时区问题
   - 避免过于频繁的执行

3. **任务参数**：
   - 使用简单的key=value格式
   - 复杂参数建议使用JSON

4. **异常处理**：
   - 任务中的异常会被记录
   - 重要任务需要添加重试机制

## 🎯 最佳实践

1. **任务命名**：
   - 使用有意义的名称
   - 按模块分组管理

2. **执行时间**：
   - 避免在高峰期执行重任务
   - 合理设置任务间隔

3. **监控日志**：
   - 关键节点记录日志
   - 监控任务执行状态

4. **数据备份**：
   - 定期备份Quartz表数据
   - 重要任务配置版本化管理

---

🎉 **Spring Boot Quartz集成完成！现在您可以轻松管理所有定时任务！**
