package com.xkcoding.swagger.service;

import com.xkcoding.swagger.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserCacheService {

    // 模拟数据库数据
    private static final List<User> MOCK_USERS = new ArrayList<>();

    static {
        User user1 = new User();
        user1.setId(1);
        user1.setName("张三");
        user1.setJob("前端开发");
        MOCK_USERS.add(user1);
        
        User user2 = new User();
        user2.setId(2);
        user2.setName("李四");
        user2.setJob("后端开发");
        MOCK_USERS.add(user2);
        
        User user3 = new User();
        user3.setId(3);
        user3.setName("王五");
        user3.setJob("全栈开发");
        MOCK_USERS.add(user3);
        
        User user4 = new User();
        user4.setId(4);
        user4.setName("赵六");
        user4.setJob("产品经理");
        MOCK_USERS.add(user4);
        
        User user5 = new User();
        user5.setId(5);
        user5.setName("钱七");
        user5.setJob("UI设计师");
        MOCK_USERS.add(user5);
    }

    /**
     * 根据ID查询用户（带缓存）
     * @Cacheable: 如果缓存中有数据，直接返回缓存数据；如果没有，执行方法并将结果放入缓存
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Cacheable(value = "userCache", key = "#id", unless = "#result == null")
    public User findById(Integer id) {
        log.info("执行数据库查询，用户ID: {}", id);
        
        // 模拟数据库查询延迟
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Optional<User> user = MOCK_USERS.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();

        if (user.isPresent()) {
            log.info("找到用户: {}", user.get());
            return user.get();
        } else {
            log.info("用户不存在，ID: {}", id);
            return null;
        }
    }

    /**
     * 查询所有用户（带缓存）
     * 使用固定的key
     *
     * @return 所有用户列表
     */
    @Cacheable(value = "userCache", key = "'all_users'")
    public List<User> findAll() {
        log.info("执行数据库查询，获取所有用户");
        
        // 模拟数据库查询延迟
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("返回 {} 个用户", MOCK_USERS.size());
        return new ArrayList<>(MOCK_USERS);
    }

    /**
     * 根据工作岗位查询用户（带缓存）
     * 使用复合key
     *
     * @param job 工作岗位
     * @return 用户列表
     */
    @Cacheable(value = "userCache", key = "'job:' + #job")
    public List<User> findByJob(String job) {
        log.info("执行数据库查询，根据工作岗位查询: {}", job);
        
        // 模拟数据库查询延迟
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<User> result = MOCK_USERS.stream()
                .filter(user -> user.getJob().contains(job))
                .collect(Collectors.toList());

        log.info("找到 {} 个用户，工作岗位包含: {}", result.size(), job);
        return result;
    }

    /**
     * 更新用户信息
     * @CachePut: 无论缓存中是否有数据，都执行方法并更新缓存
     *
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @CachePut(value = "userCache", key = "#user.id")
    public User updateUser(User user) {
        log.info("更新用户信息: {}", user);
        
        // 模拟数据库更新
        Optional<User> existingUser = MOCK_USERS.stream()
                .filter(u -> u.getId().equals(user.getId()))
                .findFirst();

        if (existingUser.isPresent()) {
            User existing = existingUser.get();
            existing.setName(user.getName());
            existing.setJob(user.getJob());
            log.info("用户信息更新成功: {}", existing);
            
            // 清除相关缓存
            clearRelatedCache();
            
            return existing;
        } else {
            log.warn("用户不存在，无法更新，ID: {}", user.getId());
            return null;
        }
    }

    /**
     * 保存新用户
     * @CachePut: 将新用户放入缓存
     *
     * @param user 用户信息
     * @return 保存后的用户信息
     */
    @CachePut(value = "userCache", key = "#result.id", condition = "#result != null")
    public User saveUser(User user) {
        log.info("保存新用户: {}", user);
        
        // 生成新ID
        int newId = MOCK_USERS.stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;
        
        user.setId(newId);
        MOCK_USERS.add(user);
        
        log.info("用户保存成功: {}", user);
        
        // 清除相关缓存
        clearRelatedCache();
        
        return user;
    }

    /**
     * 删除用户
     * @CacheEvict: 删除指定的缓存
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    @CacheEvict(value = "userCache", key = "#id")
    public boolean deleteUser(Integer id) {
        log.info("删除用户，ID: {}", id);
        
        boolean removed = MOCK_USERS.removeIf(user -> user.getId().equals(id));
        
        if (removed) {
            log.info("用户删除成功，ID: {}", id);
            // 清除相关缓存
            clearRelatedCache();
        } else {
            log.warn("用户不存在，删除失败，ID: {}", id);
        }
        
        return removed;
    }

    /**
     * 清除所有用户缓存
     * @CacheEvict: allEntries = true 清除指定缓存的所有条目
     */
    @CacheEvict(value = "userCache", allEntries = true)
    public void clearAllCache() {
        log.info("清除所有用户缓存");
    }

    /**
     * 清除相关缓存
     * 清除all_users和job相关的缓存
     */
    @CacheEvict(value = "userCache", key = "'all_users'")
    public void clearAllUsersCache() {
        log.info("清除所有用户列表缓存");
    }

    /**
     * 清除相关缓存（私有方法）
     */
    private void clearRelatedCache() {
        // 注意：由于是私有方法，@CacheEvict注解不会生效
        // 这里只是演示，实际应该通过其他方式清除缓存
        log.info("需要清除相关缓存：all_users 和 job 相关缓存");
    }

    /**
     * 批量查询用户（演示条件缓存）
     *
     * @param ids 用户ID列表
     * @return 用户列表
     */
    @Cacheable(value = "userCache", key = "'batch:' + #ids.toString()", condition = "#ids.size() <= 10")
    public List<User> findByIds(List<Integer> ids) {
        log.info("批量查询用户，IDs: {}", ids);
        
        // 模拟数据库查询延迟
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<User> result = MOCK_USERS.stream()
                .filter(user -> ids.contains(user.getId()))
                .collect(Collectors.toList());

        log.info("批量查询结果：{} 个用户", result.size());
        return result;
    }

    /**
     * 获取用户统计信息（演示复杂缓存key）
     *
     * @return 统计信息
     */
    @Cacheable(value = "dataCache", key = "'user_stats'")
    public String getUserStats() {
        log.info("计算用户统计信息");
        
        // 模拟复杂计算
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long total = MOCK_USERS.size();
        long devCount = MOCK_USERS.stream()
                .filter(user -> user.getJob().contains("开发"))
                .count();

        String stats = String.format("总用户数: %d, 开发人员: %d", total, devCount);
        log.info("统计信息: {}", stats);
        
        return stats;
    }
}
