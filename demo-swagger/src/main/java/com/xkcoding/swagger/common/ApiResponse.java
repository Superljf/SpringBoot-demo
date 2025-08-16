package com.xkcoding.swagger.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 通用API接口返回
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-11-29 11:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用API接口返回")
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = -8987146499044811408L;
    /**
     * 通用返回状态
     */
    @Schema(description = "通用返回状态", example = "200")
    private Integer code;
    /**
     * 通用返回信息
     */
    @Schema(description = "通用返回信息", example = "操作成功")
    private String message;
    /**
     * 通用返回数据
     */
    @Schema(description = "通用返回数据")
    private T data;
}
