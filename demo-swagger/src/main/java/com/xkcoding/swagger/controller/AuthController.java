package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.annotation.WebLog;
import com.xkcoding.swagger.common.ApiResponse;
import com.xkcoding.swagger.config.JwtTokenUtil;
import com.xkcoding.swagger.entity.JwtResponse;
import com.xkcoding.swagger.entity.LoginRequest;
import com.xkcoding.swagger.entity.User;
import com.xkcoding.swagger.service.SecurityUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 认证授权Controller
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证授权", description = "用户登录、注销、Token刷新等认证相关操作")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityUserService securityUserService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(AuthenticationManager authenticationManager,
                         SecurityUserService securityUserService,
                         JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.securityUserService = securityUserService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    @WebLog(value = "用户登录", logArgs = true, logResult = false, logTime = true)
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    public ApiResponse<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 设置安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            User user = (User) authentication.getPrincipal();

            // 生成JWT Token
            String accessToken = jwtTokenUtil.generateToken(user);
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

            // 构建响应
            JwtResponse.UserInfo userInfo = JwtResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles() != null ? 
                      user.getRoles().stream().map(role -> role.getRoleCode()).collect(Collectors.toList()) : 
                      null)
                .permissions(user.getPermissions() != null ? 
                           user.getPermissions().stream().map(perm -> perm.getPermissionCode()).collect(Collectors.toList()) : 
                           null)
                .loginTime(LocalDateTime.now())
                .build();

            JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(24) // 24小时
                .user(userInfo)
                .build();

            log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());

            return ApiResponse.<JwtResponse>builder()
                .code(200)
                .message("登录成功")
                .data(jwtResponse)
                .build();

        } catch (AuthenticationException e) {
            log.warn("用户登录失败: username={}, error={}", loginRequest.getUsername(), e.getMessage());
            return ApiResponse.<JwtResponse>builder()
                .code(401)
                .message("用户名或密码错误")
                .data(null)
                .build();
        } catch (Exception e) {
            log.error("登录过程中发生异常", e);
            return ApiResponse.<JwtResponse>builder()
                .code(500)
                .message("登录失败：" + e.getMessage())
                .data(null)
                .build();
        }
    }

    @PostMapping("/logout")
    @WebLog(value = "用户注销", logArgs = false, logResult = true, logTime = true)
    @Operation(summary = "用户注销", description = "用户注销，清除认证信息")
    public ApiResponse<String> logout(HttpServletRequest request) {
        try {
            // 获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                log.info("用户注销: username={}", username);
            }

            // 清除安全上下文
            SecurityContextHolder.clearContext();

            return ApiResponse.<String>builder()
                .code(200)
                .message("注销成功")
                .data("用户已成功注销")
                .build();

        } catch (Exception e) {
            log.error("注销过程中发生异常", e);
            return ApiResponse.<String>builder()
                .code(500)
                .message("注销失败：" + e.getMessage())
                .data(null)
                .build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新Token获取新的访问Token")
    public ApiResponse<JwtResponse> refreshToken(@Parameter(description = "刷新Token") @RequestParam String refreshToken) {
        try {
            if (!StringUtils.hasText(refreshToken)) {
                return ApiResponse.<JwtResponse>builder()
                    .code(400)
                    .message("刷新Token不能为空")
                    .data(null)
                    .build();
            }

            // 验证刷新Token
            if (!jwtTokenUtil.validateToken(refreshToken)) {
                return ApiResponse.<JwtResponse>builder()
                    .code(401)
                    .message("刷新Token无效或已过期")
                    .data(null)
                    .build();
            }

            // 从刷新Token中获取用户名
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            if (username == null) {
                return ApiResponse.<JwtResponse>builder()
                    .code(401)
                    .message("无法从刷新Token中获取用户信息")
                    .data(null)
                    .build();
            }

            // 获取用户信息
            User user = securityUserService.findByUsername(username);
            if (user == null || !user.isEnabled()) {
                return ApiResponse.<JwtResponse>builder()
                    .code(401)
                    .message("用户不存在或已被禁用")
                    .data(null)
                    .build();
            }

            // 生成新的访问Token
            String newAccessToken = jwtTokenUtil.generateToken(user);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

            // 构建响应
            JwtResponse.UserInfo userInfo = JwtResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles() != null ? 
                      user.getRoles().stream().map(role -> role.getRoleCode()).collect(Collectors.toList()) : 
                      null)
                .permissions(user.getPermissions() != null ? 
                           user.getPermissions().stream().map(perm -> perm.getPermissionCode()).collect(Collectors.toList()) : 
                           null)
                .loginTime(LocalDateTime.now())
                .build();

            JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(24)
                .user(userInfo)
                .build();

            log.info("Token刷新成功: username={}", username);

            return ApiResponse.<JwtResponse>builder()
                .code(200)
                .message("Token刷新成功")
                .data(jwtResponse)
                .build();

        } catch (Exception e) {
            log.error("Token刷新失败", e);
            return ApiResponse.<JwtResponse>builder()
                .code(500)
                .message("Token刷新失败：" + e.getMessage())
                .data(null)
                .build();
        }
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<JwtResponse.UserInfo> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.<JwtResponse.UserInfo>builder()
                    .code(401)
                    .message("用户未登录")
                    .data(null)
                    .build();
            }

            User user = (User) authentication.getPrincipal();
            JwtResponse.UserInfo userInfo = JwtResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles() != null ? 
                      user.getRoles().stream().map(role -> role.getRoleCode()).collect(Collectors.toList()) : 
                      null)
                .permissions(user.getPermissions() != null ? 
                           user.getPermissions().stream().map(perm -> perm.getPermissionCode()).collect(Collectors.toList()) : 
                           null)
                .build();

            return ApiResponse.<JwtResponse.UserInfo>builder()
                .code(200)
                .message("获取成功")
                .data(userInfo)
                .build();

        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return ApiResponse.<JwtResponse.UserInfo>builder()
                .code(500)
                .message("获取用户信息失败：" + e.getMessage())
                .data(null)
                .build();
        }
    }

    @GetMapping("/check")
    @Operation(summary = "检查Token有效性", description = "检查当前Token是否有效")
    public ApiResponse<String> checkToken(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader(JwtTokenUtil.HEADER_STRING);
            String token = jwtTokenUtil.resolveToken(bearerToken);

            if (!StringUtils.hasText(token)) {
                return ApiResponse.<String>builder()
                    .code(401)
                    .message("Token不存在")
                    .data(null)
                    .build();
            }

            if (!jwtTokenUtil.validateToken(token)) {
                return ApiResponse.<String>builder()
                    .code(401)
                    .message("Token无效或已过期")
                    .data(null)
                    .build();
            }

            long remainingTime = jwtTokenUtil.getTokenRemainingTime(token);
            String message = String.format("Token有效，剩余时间：%d分钟", remainingTime / 1000 / 60);

            return ApiResponse.<String>builder()
                .code(200)
                .message("Token有效")
                .data(message)
                .build();

        } catch (Exception e) {
            log.error("检查Token失败", e);
            return ApiResponse.<String>builder()
                .code(500)
                .message("检查Token失败：" + e.getMessage())
                .data(null)
                .build();
        }
    }
}