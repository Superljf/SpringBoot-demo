package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.annotation.RequirePermission;
import com.xkcoding.swagger.annotation.WebLog;
import com.xkcoding.swagger.common.ApiResponse;
import com.xkcoding.swagger.common.DataType;
import com.xkcoding.swagger.common.ParamType;
import com.xkcoding.swagger.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户相关的增删改查操作")
@Slf4j
public class UserController {
    @GetMapping
    @RequirePermission("user:read")
    @WebLog(value = "根据用户名查询用户", logArgs = true, logResult = true, logTime = true)
    @Operation(summary = "根据用户名查询用户", description = "根据用户名参数查询用户信息")
    public ApiResponse<User> getByUserName(@Parameter(description = "用户名", example = "张三") String username) {
        log.info("多个参数用  @ApiImplicitParams");
        User user = new User();
        user.setId(1);
        user.setName(username);
        user.setJob("JAVA");
        return ApiResponse.<User>builder().code(200).message("操作成功").data(user).build();
    }

    @GetMapping("/{id}")
    @RequirePermission("user:read")
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询单个用户信息")
    public ApiResponse<User> get(@Parameter(description = "用户ID", example = "1") @PathVariable Integer id) {
        log.info("单个参数用  @ApiImplicitParam");
        User user = new User();
        user.setId(id);
        user.setName("u1");
        user.setJob("p1");
        return ApiResponse.<User>builder().code(200).message("操作成功").data(user).build();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public void delete(@Parameter(description = "用户ID", example = "1") @PathVariable Integer id) {
        log.info("单个参数用 ApiImplicitParam");
    }

    @PostMapping
    @RequirePermission("user:create")
    @WebLog(value = "添加新用户", logArgs = true, logResult = true, logTime = true)
    @Operation(summary = "添加用户", description = "创建新用户")
    public User post(@RequestBody User user) {
        log.info("如果是 POST PUT 这种带 @RequestBody 的可以不用写 @ApiImplicitParam");
        return user;
    }

    @PostMapping("/multipar")
    @RequirePermission("user:create")
    @Operation(summary = "批量添加用户", description = "接收用户列表并批量创建")
    public List<User> multipar(@RequestBody List<User> user) {
        log.info("如果是 POST PUT 这种带 @RequestBody 的可以不用写 @ApiImplicitParam");

        return user;
    }

    @PostMapping("/array")
    @RequirePermission("user:create")
    @Operation(summary = "数组方式添加用户", description = "接收用户数组并创建")
    public User[] array(@RequestBody User[] user) {
        log.info("如果是 POST PUT 这种带 @RequestBody 的可以不用写 @ApiImplicitParam");
        return user;
    }

    @PutMapping("/{id}")
    @RequirePermission("user:update")
    @Operation(summary = "更新用户", description = "根据ID更新用户信息")
    public void put(@Parameter(description = "用户ID", example = "1") @PathVariable Long id, @RequestBody User user) {
        log.info("如果你不想写 @ApiImplicitParam 那么 swagger 也会使用默认的参数名作为描述信息 ");
    }

    @PostMapping("/{id}/file")
    @RequirePermission("user:upload")
    @Operation(summary = "文件上传", description = "为指定用户上传文件")
    public String file(@Parameter(description = "用户ID", example = "1") @PathVariable Long id, 
                      @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {
        log.info(file.getContentType());
        log.info(file.getName());
        log.info(file.getOriginalFilename());
        return file.getOriginalFilename();
    }
}
