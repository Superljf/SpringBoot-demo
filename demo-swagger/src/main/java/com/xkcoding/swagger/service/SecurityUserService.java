package com.xkcoding.swagger.service;

import com.xkcoding.swagger.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * <p>
 * Spring Security 用户服务接口
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
public interface SecurityUserService extends UserDetailsService {

    /**
     * 根据用户名查询用户信息（包含角色和权限）
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 根据用户ID查询用户信息（包含角色和权限）
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User findByUserId(Integer userId);

    /**
     * 创建新用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    User createUser(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新后的用户
     */
    User updateUser(User user);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Integer userId);

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否分配成功
     */
    boolean assignRoles(Integer userId, List<Integer> roleIds);

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Integer userId, String permissionCode);

    /**
     * 启用/禁用用户
     *
     * @param userId 用户ID
     * @param status 状态（1:启用 0:禁用）
     * @return 是否操作成功
     */
    boolean changeUserStatus(Integer userId, Integer status);
}