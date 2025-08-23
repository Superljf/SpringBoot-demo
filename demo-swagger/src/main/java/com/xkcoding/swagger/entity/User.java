package com.xkcoding.swagger.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体")
public class User implements Serializable, UserDetails {
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
    
    /**
     * 用户名（用于登录）
     */
    @Schema(description = "登录用户名", example = "admin")
    private String username;
    
    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;
    
    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    /**
     * 用户状态（1:正常 0:禁用）
     */
    @Schema(description = "用户状态", example = "1")
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
     * 用户角色列表
     */
    @Schema(description = "用户角色列表")
    private List<Role> roles;
    
    /**
     * 用户权限列表（通过角色获取）
     */
    @Schema(description = "用户权限列表")
    private List<Permission> permissions;

    // ==== 以下是UserDetails接口的实现 ====
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
