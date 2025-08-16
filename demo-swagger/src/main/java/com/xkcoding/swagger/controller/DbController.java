package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.common.ApiResponse;
import com.xkcoding.swagger.common.PageResponse;
import com.xkcoding.swagger.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * MySQL 数据库连接与操作示例
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@RestController
@RequestMapping("/db")
@Api(tags = "Database", description = "MySQL 数据库操作示例", value = "MySQL 数据库操作示例")
public class DbController {
    private final JdbcTemplate jdbcTemplate;

    public DbController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    @ApiOperation(value = "数据库连通性检查", notes = "测试数据库连接是否正常")
    public ApiResponse<String> ping() {
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            String result = (one != null && one == 1) ? "数据库连接正常" : "数据库连接异常";
            return ApiResponse.<String>builder().code(200).message("操作成功").data(result).build();
        } catch (Exception e) {
            return ApiResponse.<String>builder().code(500).message("数据库连接失败: " + e.getMessage()).data(null).build();
        }
    }

    @GetMapping("/users")
    @ApiOperation(value = "查询用户列表", notes = "获取所有用户信息")
    public ApiResponse<List<User>> listUsers() {
        try {
            List<User> users = jdbcTemplate.query(
                    "SELECT id, name, job FROM t_user ORDER BY id DESC",
                    (rs, rowNum) -> new User(rs.getInt("id"), rs.getString("name"), rs.getString("job"))
            );
            return ApiResponse.<List<User>>builder().code(200).message("查询成功").data(users).build();
        } catch (Exception e) {
            return ApiResponse.<List<User>>builder().code(500).message("查询失败: " + e.getMessage()).data(null).build();
        }
    }

    @GetMapping("/users/{id}")
    @ApiOperation(value = "根据ID查询用户", notes = "根据用户ID查询单个用户信息")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "int", paramType = "path")
    public ApiResponse<User> getUserById(@PathVariable Integer id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT id, name, job FROM t_user WHERE id = ?",
                    (rs, rowNum) -> new User(rs.getInt("id"), rs.getString("name"), rs.getString("job")),
                    id
            );
            return ApiResponse.<User>builder().code(200).message("查询成功").data(user).build();
        } catch (Exception e) {
            return ApiResponse.<User>builder().code(404).message("用户不存在: " + e.getMessage()).data(null).build();
        }
    }

    @PostMapping("/users")
    @ApiOperation(value = "新增用户", notes = "添加新用户到数据库")
    public ApiResponse<User> addUser(@RequestBody User user) {
        try {
            final String sql = "INSERT INTO t_user(name, job) VALUES(?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getName());
                ps.setString(2, user.getJob());
                return ps;
            }, keyHolder);

            Number id = keyHolder.getKey();
            user.setId(id == null ? null : id.intValue());
            return ApiResponse.<User>builder().code(200).message("新增成功").data(user).build();
        } catch (Exception e) {
            return ApiResponse.<User>builder().code(500).message("新增失败: " + e.getMessage()).data(null).build();
        }
    }

    @PutMapping("/users/{id}")
    @ApiOperation(value = "更新用户", notes = "根据ID更新用户信息")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "int", paramType = "path")
    public ApiResponse<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        try {
            int affected = jdbcTemplate.update(
                    "UPDATE t_user SET name = ?, job = ? WHERE id = ?",
                    user.getName(), user.getJob(), id
            );

            if (affected > 0) {
                user.setId(id);
                return ApiResponse.<User>builder().code(200).message("更新成功").data(user).build();
            } else {
                return ApiResponse.<User>builder().code(404).message("用户不存在").data(null).build();
            }
        } catch (Exception e) {
            return ApiResponse.<User>builder().code(500).message("更新失败: " + e.getMessage()).data(null).build();
        }
    }

    @DeleteMapping("/users/{id}")
    @ApiOperation(value = "删除用户", notes = "根据ID删除用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "int", paramType = "path")
    public ApiResponse<String> deleteUser(@PathVariable Integer id) {
        try {
            int affected = jdbcTemplate.update("DELETE FROM t_user WHERE id = ?", id);

            if (affected > 0) {
                return ApiResponse.<String>builder().code(200).message("删除成功").data("用户ID: " + id).build();
            } else {
                return ApiResponse.<String>builder().code(404).message("用户不存在").data(null).build();
            }
        } catch (Exception e) {
            return ApiResponse.<String>builder().code(500).message("删除失败: " + e.getMessage()).data(null).build();
        }
    }

    @GetMapping("/count")
    @ApiOperation(value = "统计用户数量", notes = "获取用户总数")
    public ApiResponse<Integer> countUsers() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_user", Integer.class);
            return ApiResponse.<Integer>builder().code(200).message("统计成功").data(count).build();
        } catch (Exception e) {
            return ApiResponse.<Integer>builder().code(500).message("统计失败: " + e.getMessage()).data(null).build();
        }
    }

    @GetMapping("/users/page")
    @ApiOperation(value = "分页查询用户", notes = "支持分页、排序和关键字搜索的用户查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码（从1开始）", dataType = "int", paramType = "query", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "每页大小", dataType = "int", paramType = "query", defaultValue = "10"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键字（用户名或岗位）", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "sortBy", value = "排序字段（id/name/job）", dataType = "string", paramType = "query", defaultValue = "id"),
            @ApiImplicitParam(name = "sortDir", value = "排序方向（asc/desc）", dataType = "string", paramType = "query", defaultValue = "desc")
    })
    public ApiResponse<PageResponse<User>> getUsersWithPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            // 参数校验
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100; // 限制最大页面大小

            // 校验排序字段
            if (!"id".equals(sortBy) && !"name".equals(sortBy) && !"job".equals(sortBy)) {
                sortBy = "id";
            }

            // 校验排序方向
            if (!"asc".equals(sortDir) && !"desc".equals(sortDir)) {
                sortDir = "desc";
            }

            // 构建查询条件
            StringBuilder whereClause = new StringBuilder();
            Object[] params = new Object[0];

            if (keyword != null && !keyword.trim().isEmpty()) {
                whereClause.append(" WHERE (name LIKE ? OR job LIKE ?)");
                String likeKeyword = "%" + keyword.trim() + "%";
                params = new Object[]{likeKeyword, likeKeyword};
            }

            // 查询总数
            String countSql = "SELECT COUNT(*) FROM t_user" + whereClause.toString();
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, params);

            if (totalCount == 0) {
                PageResponse<User> pageResponse = PageResponse.build(page, size, 0L, new ArrayList<>());
                return ApiResponse.<PageResponse<User>>builder().code(200).message("查询成功").data(pageResponse).build();
            }
            // 分页查询数据
            String dataSql = String.format(
                    "SELECT id, name, job FROM t_user%s ORDER BY %s %s LIMIT ? OFFSET ?",
                    whereClause.toString(), sortBy, sortDir.toUpperCase()
            );

            // 计算偏移量
            int offset = (page - 1) * size;

            // 组装参数
            Object[] dataParams;
            if (params.length > 0) {
                dataParams = new Object[params.length + 2];
                System.arraycopy(params, 0, dataParams, 0, params.length);
                dataParams[params.length] = size;
                dataParams[params.length + 1] = offset;
            } else {
                dataParams = new Object[]{size, offset};
            }

            List<User> users = jdbcTemplate.query(dataSql,
                    (rs, rowNum) -> new User(rs.getInt("id"), rs.getString("name"), rs.getString("job")),
                    dataParams);

            // 构建分页响应
            PageResponse<User> pageResponse = PageResponse.build(page, size, totalCount, users);

            return ApiResponse.<PageResponse<User>>builder().code(200).message("查询成功").data(pageResponse).build();

        } catch (Exception e) {
            return ApiResponse.<PageResponse<User>>builder().code(500).message("查询失败: " + e.getMessage()).data(null).build();
        }
    }
}
