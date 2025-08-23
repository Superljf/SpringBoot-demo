package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 角色实体类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色实体")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "1")
    private Integer id;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String roleCode;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "系统管理员，拥有所有权限")
    private String description;

    /**
     * 角色状态（1:正常 0:禁用）
     */
    @Schema(description = "角色状态", example = "1")
    private Integer status;

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
     * 角色权限列表
     */
    @Schema(description = "角色权限列表")
    private List<Permission> permissions;
}