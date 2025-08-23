package com.xkcoding.swagger.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * 角色验证注解
 * </p>
 * 
 * 用于标记需要特定角色才能访问的方法
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 所需的角色编码
     */
    String[] value() default {};

    /**
     * 角色关系类型（AND 或 OR）
     * true: 需要拥有所有角色（AND关系）
     * false: 只需拥有其中一个角色（OR关系）
     */
    boolean requireAll() default false;

    /**
     * 角色验证失败时的提示信息
     */
    String message() default "角色权限不足，访问被拒绝";
}