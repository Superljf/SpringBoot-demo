package com.xkcoding.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * OpenAPI 3.0 配置
 * </p>
 *
 * SpringDoc OpenAPI 配置，替代传统的 Springfox Swagger
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // ✅ 用 Java 8 兼容的写法
        List<Server> servers = new ArrayList<Server>();
        servers.add(new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("本地开发环境"));

        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("Spring Boot Demo API")
                        .description("这是一个 Spring Boot 示例项目的 API 文档\n\n" +
                                "### 功能特性\n" +
                                "- 🚀 **用户管理**: 完整的用户增删改查操作\n" +
                                "- 🗄️ **数据库操作**: MySQL 连接与数据操作示例\n" +
                                "- 📝 **AOP 日志**: 基于切面的请求日志记录\n" +
                                "- 📊 **监控功能**: Spring Boot Admin + Actuator 监控\n" +
                                "- 📖 **API 文档**: SpringDoc OpenAPI 3.0 文档\n\n" +
                                "### 快速开始\n" +
                                "1. 测试连通性: `GET /db/ping`\n" +
                                "2. 查看用户列表: `GET /user`\n" +
                                "3. 体验日志记录: `GET /log-demo/simple`")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Demo Team")
                                .email("demo@example.com")
                                .url("https://github.com/demo"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
