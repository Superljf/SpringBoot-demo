package com.xkcoding.swagger.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * 权限验证注解
 * </p>
 * 
 * 用于标记需要特定权限才能访问的方法
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 所需的权限编码
     */
    String[] value() default {};

    /**
     * 权限关系类型（AND 或 OR）
     * true: 需要拥有所有权限（AND关系）
     * false: 只需拥有其中一个权限（OR关系）
     */
    boolean requireAll() default false;

    /**
     * 权限验证失败时的提示信息
     */
    String message() default "权限不足，访问被拒绝";
}