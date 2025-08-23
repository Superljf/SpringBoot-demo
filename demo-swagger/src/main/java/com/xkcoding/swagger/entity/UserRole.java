package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户角色关联实体类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户角色关联")
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Integer id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "1")
    private Integer roleId;

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