package com.xkcoding.swagger.common;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "分页响应")
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer currentPage;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long totalCount;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "10")
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
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