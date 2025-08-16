package com.xkcoding.swagger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * <p>
 * Spring Boot Admin Server 配置
 * </p>
 * 
 * 注意：这是一个示例配置，实际使用时建议单独创建一个 Admin Server 项目
 * 如果需要启用，请：
 * 1. 在 pom.xml 中添加 spring-boot-admin-starter-server 依赖
 * 2. 在启动类上添加 @EnableAdminServer 注解
 * 3. 使用 --spring.profiles.active=admin-server 启动
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Configuration
@Profile("admin-server")
public class AdminServerConfig {
    
    // 如需启用 Admin Server，需要以下依赖：
    /*
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
        <version>2.7.10</version>
    </dependency>
    */
    
    // 启动类需要添加：@EnableAdminServer
    
    // 单独的 Admin Server application.yml 配置示例：
    /*
    server:
      port: 8090
    
    spring:
      application:
        name: admin-server
      boot:
        admin:
          ui:
            title: "Demo 应用监控中心"
            brand: "Spring Boot Admin"
    */
}
