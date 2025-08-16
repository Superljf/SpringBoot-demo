package com.xkcoding.swagger.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Web 请求日志记录注解
 * </p>
 * 
 * 用法：在需要记录日志的 Controller 方法上添加此注解
 * 
 * @author demo
 * @date Created in 2024-12-19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLog {
    
    /**
     * 日志描述
     */
    String value() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean logArgs() default true;
    
    /**
     * 是否记录返回结果
     */
    boolean logResult() default true;
    
    /**
     * 是否记录执行时间
     */
    boolean logTime() default true;
    
    /**
     * 是否记录异常信息
     */
    boolean logException() default true;
}
