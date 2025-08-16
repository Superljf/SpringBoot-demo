package com.xkcoding.swagger.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 分页响应实体
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "分页响应", description = "Page Response")
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @ApiModelProperty(value = "当前页码", required = true, example = "1")
    private Integer currentPage;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "每页大小", required = true, example = "10")
    private Integer pageSize;

    /**
     * 总记录数
     */
    @ApiModelProperty(value = "总记录数", required = true, example = "100")
    private Long totalCount;

    /**
     * 总页数
     */
    @ApiModelProperty(value = "总页数", required = true, example = "10")
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    @ApiModelProperty(value = "是否有下一页", required = true, example = "true")
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    @ApiModelProperty(value = "是否有上一页", required = true, example = "false")
    private Boolean hasPrevious;

    /**
     * 数据列表
     */
    @ApiModelProperty(value = "数据列表", required = true)
    private List<T> data;

    /**
     * 计算总页数
     */
    public static Integer calculateTotalPages(Long totalCount, Integer pageSize) {
        if (totalCount == null || totalCount == 0 || pageSize == null || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * 构建分页响应
     */
    public static <T> PageResponse<T> build(Integer currentPage, Integer pageSize, Long totalCount, List<T> data) {
        Integer totalPages = calculateTotalPages(totalCount, pageSize);
        
        return PageResponse.<T>builder()
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .hasNext(currentPage < totalPages)
                .hasPrevious(currentPage > 1)
                .data(data)
                .build();
    }
}