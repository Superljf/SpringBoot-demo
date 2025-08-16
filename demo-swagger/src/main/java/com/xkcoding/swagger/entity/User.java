package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 用户实体
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-11-29 11:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体")
public class User implements Serializable {
    private static final long serialVersionUID = 5057954049311281252L;
    /**
     * 主键id
     */
    @Schema(description = "主键id", example = "1")
    private Integer id;
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "张三")
    private String name;
    /**
     * 工作岗位
     */
    @Schema(description = "工作岗位", example = "前端开发")
    private String job;
}
