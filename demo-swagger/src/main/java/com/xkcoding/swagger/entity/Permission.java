package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 权限实体类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限实体")
public class Permission implements Serializable, GrantedAuthority {
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1")
    private Integer id;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码", example = "user:read")
    private String permissionCode;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称", example = "查看用户")
    private String permissionName;

    /**
     * 权限类型（1:菜单 2:按钮 3:接口）
     */
    @Schema(description = "权限类型", example = "3")
    private Integer type;

    /**
     * 权限路径/资源路径
     */
    @Schema(description = "权限路径", example = "/user/**")
    private String path;

    /**
     * 请求方法（GET,POST,PUT,DELETE等）
     */
    @Schema(description = "请求方法", example = "GET")
    private String method;

    /**
     * 父权限ID
     */
    @Schema(description = "父权限ID", example = "0")
    private Integer parentId;

    /**
     * 权限描述
     */
    @Schema(description = "权限描述", example = "用户查看权限")
    private String description;

    /**
     * 权限状态（1:正常 0:禁用）
     */
    @Schema(description = "权限状态", example = "1")
    private Integer status;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "1")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 实现 GrantedAuthority 接口
     */
    @Override
    public String getAuthority() {
        return permissionCode;
    }
}