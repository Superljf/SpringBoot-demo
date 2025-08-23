package com.xkcoding.swagger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>
 * 密码编码器配置类
 * </p>
 * 
 * 将 PasswordEncoder 独立配置，避免与 SecurityConfig 产生循环依赖
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Configuration
public class PasswordConfig {

    /**
     * 密码编码器
     * 使用 BCrypt 算法进行密码加密
     * 
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}