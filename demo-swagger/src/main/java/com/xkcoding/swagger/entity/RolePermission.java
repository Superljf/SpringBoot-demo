package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 角色权限关联实体类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色权限关联")
public class RolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Integer id;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "1")
    private Integer roleId;

    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1")
    private Integer permissionId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    @Schema(description = "创建者", example = "admin")
    private String createBy;
}