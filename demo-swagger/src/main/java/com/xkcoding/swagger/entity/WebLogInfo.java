package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * Web 请求日志信息实体
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Web请求日志信息")
public class WebLogInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID（用于链路追踪）
     */
    @Schema(description = "请求ID", example = "req-123456789")
    private String requestId;

    /**
     * 请求时间
     */
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    @Schema(description = "请求方法", example = "GET")
    private String method;

    /**
     * 请求URL
     */
    @Schema(description = "请求URL", example = "/demo/user/1")
    private String url;

    /**
     * 请求URI
     */
    @Schema(description = "请求URI", example = "/user/1")
    private String uri;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String params;

    /**
     * 请求体
     */
    @Schema(description = "请求体")
    private String requestBody;

    /**
     * 客户端IP
     */
    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    /**
     * User-Agent
     */
    @Schema(description = "User-Agent")
    private String userAgent;

    /**
     * 目标类名
     */
    @Schema(description = "目标类名", example = "UserController")
    private String className;

    /**
     * 目标方法名
     */
    @Schema(description = "目标方法名", example = "getUserById")
    private String methodName;

    /**
     * 方法描述
     */
    @Schema(description = "方法描述", example = "根据ID查询用户")
    private String description;

    /**
     * 执行时间（毫秒）
     */
    @Schema(description = "执行时间（毫秒）", example = "150")
    private Long executeTime;

    /**
     * 返回结果
     */
    @Schema(description = "返回结果")
    private String result;

    /**
     * 异常信息
     */
    @Schema(description = "异常信息")
    private String exceptionMsg;

    /**
     * 执行状态（SUCCESS/ERROR）
     */
    @Schema(description = "执行状态", example = "SUCCESS")
    private String status;
}
