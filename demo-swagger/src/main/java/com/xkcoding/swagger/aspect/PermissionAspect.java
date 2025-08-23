package com.xkcoding.swagger.aspect;

import com.xkcoding.swagger.annotation.RequirePermission;
import com.xkcoding.swagger.annotation.RequireRole;
import com.xkcoding.swagger.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 权限验证切面
 * </p>
 * 
 * 拦截标记了权限验证注解的方法，进行权限检查
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Aspect
@Component
@Slf4j
public class PermissionAspect {

    /**
     * 拦截 @RequirePermission 注解
     */
    @Around("@annotation(com.xkcoding.swagger.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        
        if (requirePermission == null) {
            return joinPoint.proceed();
        }

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("用户未认证");
        }

        User currentUser = (User) authentication.getPrincipal();
        if (currentUser == null) {
            throw new AccessDeniedException("无法获取当前用户信息");
        }

        // 获取用户权限
        List<String> userPermissions = currentUser.getPermissions() != null ?
                currentUser.getPermissions().stream()
                        .map(perm -> perm.getPermissionCode())
                        .collect(Collectors.toList()) : 
                Arrays.asList();

        // 检查权限
        String[] requiredPermissions = requirePermission.value();
        if (requiredPermissions.length == 0) {
            log.warn("@RequirePermission 注解未指定权限码: {}.{}", 
                    method.getDeclaringClass().getSimpleName(), method.getName());
            return joinPoint.proceed();
        }

        boolean hasPermission;
        if (requirePermission.requireAll()) {
            // 需要拥有所有权限（AND关系）
            hasPermission = Arrays.stream(requiredPermissions)
                    .allMatch(userPermissions::contains);
        } else {
            // 只需拥有其中一个权限（OR关系）
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            log.warn("用户权限验证失败: userId={}, username={}, requiredPermissions={}, userPermissions={}", 
                    currentUser.getId(), currentUser.getUsername(), 
                    Arrays.toString(requiredPermissions), userPermissions);
            throw new AccessDeniedException(requirePermission.message());
        }

        log.debug("用户权限验证通过: userId={}, username={}, requiredPermissions={}", 
                currentUser.getId(), currentUser.getUsername(), Arrays.toString(requiredPermissions));

        return joinPoint.proceed();
    }

    /**
     * 拦截 @RequireRole 注解
     */
    @Around("@annotation(com.xkcoding.swagger.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        
        if (requireRole == null) {
            return joinPoint.proceed();
        }

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("用户未认证");
        }

        User currentUser = (User) authentication.getPrincipal();
        if (currentUser == null) {
            throw new AccessDeniedException("无法获取当前用户信息");
        }

        // 获取用户角色
        List<String> userRoles = currentUser.getRoles() != null ?
                currentUser.getRoles().stream()
                        .map(role -> role.getRoleCode())
                        .collect(Collectors.toList()) : 
                Arrays.asList();

        // 检查角色
        String[] requiredRoles = requireRole.value();
        if (requiredRoles.length == 0) {
            log.warn("@RequireRole 注解未指定角色码: {}.{}", 
                    method.getDeclaringClass().getSimpleName(), method.getName());
            return joinPoint.proceed();
        }

        boolean hasRole;
        if (requireRole.requireAll()) {
            // 需要拥有所有角色（AND关系）
            hasRole = Arrays.stream(requiredRoles)
                    .allMatch(userRoles::contains);
        } else {
            // 只需拥有其中一个角色（OR关系）
            hasRole = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);
        }

        if (!hasRole) {
            log.warn("用户角色验证失败: userId={}, username={}, requiredRoles={}, userRoles={}", 
                    currentUser.getId(), currentUser.getUsername(), 
                    Arrays.toString(requiredRoles), userRoles);
            throw new AccessDeniedException(requireRole.message());
        }

        log.debug("用户角色验证通过: userId={}, username={}, requiredRoles={}", 
                currentUser.getId(), currentUser.getUsername(), Arrays.toString(requiredRoles));

        return joinPoint.proceed();
    }

    /**
     * 同时处理类级别和方法级别的权限注解
     * 类级别的注解会被方法级别的注解覆盖
     */
    @Around("@within(com.xkcoding.swagger.annotation.RequirePermission) || @within(com.xkcoding.swagger.annotation.RequireRole)")
    public Object checkClassLevelAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();

        // 检查方法级别是否有注解，如果有则跳过类级别检查
        if (method.isAnnotationPresent(RequirePermission.class) || 
            method.isAnnotationPresent(RequireRole.class)) {
            return joinPoint.proceed();
        }

        // 检查类级别的权限注解
        RequirePermission classPermission = targetClass.getAnnotation(RequirePermission.class);
        if (classPermission != null) {
            return checkPermissionInternal(joinPoint, classPermission);
        }

        // 检查类级别的角色注解
        RequireRole classRole = targetClass.getAnnotation(RequireRole.class);
        if (classRole != null) {
            return checkRoleInternal(joinPoint, classRole);
        }

        return joinPoint.proceed();
    }

    /**
     * 内部权限检查方法
     */
    private Object checkPermissionInternal(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("用户未认证");
        }

        User currentUser = (User) authentication.getPrincipal();
        if (currentUser == null) {
            throw new AccessDeniedException("无法获取当前用户信息");
        }

        List<String> userPermissions = currentUser.getPermissions() != null ?
                currentUser.getPermissions().stream()
                        .map(perm -> perm.getPermissionCode())
                        .collect(Collectors.toList()) : 
                Arrays.asList();

        String[] requiredPermissions = requirePermission.value();
        boolean hasPermission;
        if (requirePermission.requireAll()) {
            hasPermission = Arrays.stream(requiredPermissions).allMatch(userPermissions::contains);
        } else {
            hasPermission = Arrays.stream(requiredPermissions).anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            throw new AccessDeniedException(requirePermission.message());
        }

        return joinPoint.proceed();
    }

    /**
     * 内部角色检查方法
     */
    private Object checkRoleInternal(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("用户未认证");
        }

        User currentUser = (User) authentication.getPrincipal();
        if (currentUser == null) {
            throw new AccessDeniedException("无法获取当前用户信息");
        }

        List<String> userRoles = currentUser.getRoles() != null ?
                currentUser.getRoles().stream()
                        .map(role -> role.getRoleCode())
                        .collect(Collectors.toList()) : 
                Arrays.asList();

        String[] requiredRoles = requireRole.value();
        boolean hasRole;
        if (requireRole.requireAll()) {
            hasRole = Arrays.stream(requiredRoles).allMatch(userRoles::contains);
        } else {
            hasRole = Arrays.stream(requiredRoles).anyMatch(userRoles::contains);
        }

        if (!hasRole) {
            throw new AccessDeniedException(requireRole.message());
        }

        return joinPoint.proceed();
    }
}