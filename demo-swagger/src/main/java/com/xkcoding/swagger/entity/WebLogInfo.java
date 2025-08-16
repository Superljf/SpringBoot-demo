package com.xkcoding.swagger.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "Web请求日志信息", description = "Web Log Info")
public class WebLogInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID（用于链路追踪）
     */
    @ApiModelProperty(value = "请求ID", example = "req-123456789")
    private String requestId;

    /**
     * 请求时间
     */
    @ApiModelProperty(value = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    @ApiModelProperty(value = "请求方法", example = "GET")
    private String method;

    /**
     * 请求URL
     */
    @ApiModelProperty(value = "请求URL", example = "/demo/user/1")
    private String url;

    /**
     * 请求URI
     */
    @ApiModelProperty(value = "请求URI", example = "/user/1")
    private String uri;

    /**
     * 请求参数
     */
    @ApiModelProperty(value = "请求参数")
    private String params;

    /**
     * 请求体
     */
    @ApiModelProperty(value = "请求体")
    private String requestBody;

    /**
     * 客户端IP
     */
    @ApiModelProperty(value = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    /**
     * User-Agent
     */
    @ApiModelProperty(value = "User-Agent")
    private String userAgent;

    /**
     * 目标类名
     */
    @ApiModelProperty(value = "目标类名", example = "UserController")
    private String className;

    /**
     * 目标方法名
     */
    @ApiModelProperty(value = "目标方法名", example = "getUserById")
    private String methodName;

    /**
     * 方法描述
     */
    @ApiModelProperty(value = "方法描述", example = "根据ID查询用户")
    private String description;

    /**
     * 执行时间（毫秒）
     */
    @ApiModelProperty(value = "执行时间（毫秒）", example = "150")
    private Long executeTime;

    /**
     * 返回结果
     */
    @ApiModelProperty(value = "返回结果")
    private String result;

    /**
     * 异常信息
     */
    @ApiModelProperty(value = "异常信息")
    private String exceptionMsg;

    /**
     * 执行状态（SUCCESS/ERROR）
     */
    @ApiModelProperty(value = "执行状态", example = "SUCCESS")
    private String status;
}
