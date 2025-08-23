package com.xkcoding.swagger.service.impl;

import com.xkcoding.swagger.entity.Permission;
import com.xkcoding.swagger.entity.Role;
import com.xkcoding.swagger.entity.User;
import com.xkcoding.swagger.service.SecurityUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * Spring Security 用户服务实现类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class SecurityUserServiceImpl implements SecurityUserService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public SecurityUserServiceImpl(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user;
    }

    @Override
    public User findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }

        try {
            // 查询用户基本信息
            String userSql = "SELECT id, name, job, username, password, email, phone, status, create_time, update_time " +
                           "FROM t_user WHERE username = ? AND status = 1";
            
            List<User> users = jdbcTemplate.query(userSql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setJob(rs.getString("job"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setStatus(rs.getInt("status"));
                user.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
                user.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
                return user;
            }, username);

            if (users.isEmpty()) {
                return null;
            }

            User user = users.get(0);
            
            // 查询用户角色
            List<Role> roles = getUserRoles(user.getId());
            user.setRoles(roles);
            
            // 查询用户权限
            List<Permission> permissions = getUserPermissions(user.getId());
            user.setPermissions(permissions);
            
            return user;
        } catch (Exception e) {
            log.error("查询用户信息失败: username={}", username, e);
            return null;
        }
    }

    @Override
    public User findByUserId(Integer userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 查询用户基本信息
            String userSql = "SELECT id, name, job, username, password, email, phone, status, create_time, update_time " +
                           "FROM t_user WHERE id = ?";
            
            List<User> users = jdbcTemplate.query(userSql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setJob(rs.getString("job"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setStatus(rs.getInt("status"));
                user.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
                user.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
                return user;
            }, userId);

            if (users.isEmpty()) {
                return null;
            }

            User user = users.get(0);
            
            // 查询用户角色
            List<Role> roles = getUserRoles(user.getId());
            user.setRoles(roles);
            
            // 查询用户权限
            List<Permission> permissions = getUserPermissions(user.getId());
            user.setPermissions(permissions);
            
            return user;
        } catch (Exception e) {
            log.error("查询用户信息失败: userId={}", userId, e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        try {
            // 加密密码
            if (StringUtils.hasText(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            String sql = "INSERT INTO t_user (name, job, username, password, email, phone, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getName());
                ps.setString(2, user.getJob());
                ps.setString(3, user.getUsername());
                ps.setString(4, user.getPassword());
                ps.setString(5, user.getEmail());
                ps.setString(6, user.getPhone());
                ps.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                user.setId(key.intValue());
                return findByUserId(user.getId());
            }
            return null;
        } catch (Exception e) {
            log.error("创建用户失败", e);
            throw new RuntimeException("创建用户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        try {
            StringBuilder sql = new StringBuilder("UPDATE t_user SET ");
            Object[] params = new Object[6];
            int paramIndex = 0;

            sql.append("name = ?, job = ?, email = ?, phone = ?, update_time = CURRENT_TIMESTAMP");
            params[paramIndex++] = user.getName();
            params[paramIndex++] = user.getJob();
            params[paramIndex++] = user.getEmail();
            params[paramIndex++] = user.getPhone();

            // 如果提供了新密码，则更新密码
            if (StringUtils.hasText(user.getPassword())) {
                sql.append(", password = ?");
                params[paramIndex++] = passwordEncoder.encode(user.getPassword());
            }

            if (user.getStatus() != null) {
                sql.append(", status = ?");
                params[paramIndex++] = user.getStatus();
            }

            sql.append(" WHERE id = ?");
            params[paramIndex] = user.getId();

            // 调整参数数组大小
            Object[] finalParams = new Object[paramIndex + 1];
            System.arraycopy(params, 0, finalParams, 0, paramIndex + 1);

            int affected = jdbcTemplate.update(sql.toString(), finalParams);
            if (affected > 0) {
                return findByUserId(user.getId());
            }
            return null;
        } catch (Exception e) {
            log.error("更新用户失败", e);
            throw new RuntimeException("更新用户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Integer userId) {
        try {
            // 删除用户角色关联
            jdbcTemplate.update("DELETE FROM t_user_role WHERE user_id = ?", userId);
            
            // 删除用户
            int affected = jdbcTemplate.update("DELETE FROM t_user WHERE id = ?", userId);
            return affected > 0;
        } catch (Exception e) {
            log.error("删除用户失败: userId={}", userId, e);
            throw new RuntimeException("删除用户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Integer userId, List<Integer> roleIds) {
        try {
            // 先删除现有角色关联
            jdbcTemplate.update("DELETE FROM t_user_role WHERE user_id = ?", userId);
            
            // 添加新的角色关联
            if (roleIds != null && !roleIds.isEmpty()) {
                String sql = "INSERT INTO t_user_role (user_id, role_id, create_by) VALUES (?, ?, ?)";
                for (Integer roleId : roleIds) {
                    jdbcTemplate.update(sql, userId, roleId, "system");
                }
            }
            return true;
        } catch (Exception e) {
            log.error("分配角色失败: userId={}, roleIds={}", userId, roleIds, e);
            throw new RuntimeException("分配角色失败: " + e.getMessage());
        }
    }

    @Override
    public boolean hasPermission(Integer userId, String permissionCode) {
        try {
            String sql = "SELECT COUNT(1) FROM t_user u " +
                        "JOIN t_user_role ur ON u.id = ur.user_id " +
                        "JOIN t_role_permission rp ON ur.role_id = rp.role_id " +
                        "JOIN t_permission p ON rp.permission_id = p.id " +
                        "WHERE u.id = ? AND p.permission_code = ? AND u.status = 1 AND p.status = 1";
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, permissionCode);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, permissionCode={}", userId, permissionCode, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeUserStatus(Integer userId, Integer status) {
        try {
            String sql = "UPDATE t_user SET status = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?";
            int affected = jdbcTemplate.update(sql, status, userId);
            return affected > 0;
        } catch (Exception e) {
            log.error("修改用户状态失败: userId={}, status={}", userId, status, e);
            throw new RuntimeException("修改用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户角色列表
     */
    private List<Role> getUserRoles(Integer userId) {
        String sql = "SELECT r.id, r.role_code, r.role_name, r.description, r.status, r.create_time, r.update_time " +
                    "FROM t_role r " +
                    "JOIN t_user_role ur ON r.id = ur.role_id " +
                    "WHERE ur.user_id = ? AND r.status = 1";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Role role = new Role();
            role.setId(rs.getInt("id"));
            role.setRoleCode(rs.getString("role_code"));
            role.setRoleName(rs.getString("role_name"));
            role.setDescription(rs.getString("description"));
            role.setStatus(rs.getInt("status"));
            role.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            role.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
            return role;
        }, userId);
    }

    /**
     * 获取用户权限列表
     */
    private List<Permission> getUserPermissions(Integer userId) {
        String sql = "SELECT DISTINCT p.id, p.permission_code, p.permission_name, p.type, p.path, p.method, " +
                    "p.parent_id, p.description, p.status, p.sort_order, p.create_time, p.update_time " +
                    "FROM t_permission p " +
                    "JOIN t_role_permission rp ON p.id = rp.permission_id " +
                    "JOIN t_user_role ur ON rp.role_id = ur.role_id " +
                    "WHERE ur.user_id = ? AND p.status = 1 " +
                    "ORDER BY p.sort_order, p.id";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Permission permission = new Permission();
            permission.setId(rs.getInt("id"));
            permission.setPermissionCode(rs.getString("permission_code"));
            permission.setPermissionName(rs.getString("permission_name"));
            permission.setType(rs.getInt("type"));
            permission.setPath(rs.getString("path"));
            permission.setMethod(rs.getString("method"));
            permission.setParentId(rs.getInt("parent_id"));
            permission.setDescription(rs.getString("description"));
            permission.setStatus(rs.getInt("status"));
            permission.setSortOrder(rs.getInt("sort_order"));
            permission.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            permission.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
            return permission;
        }, userId);
    }
}