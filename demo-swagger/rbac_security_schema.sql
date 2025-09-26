-- =============================================
-- Spring Security RBAC权限模型数据库表结构（修复版）
-- 数据库：test0815
-- =============================================

-- 临时关闭外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 先删依赖表，再删主表，避免外键冲突
DROP TABLE IF EXISTS `t_role_permission`;
DROP TABLE IF EXISTS `t_user_role`;
DROP TABLE IF EXISTS `t_permission`;
DROP TABLE IF EXISTS `t_role`;
DROP TABLE IF EXISTS `t_user`;

-- 1. 用户表
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '用户姓名',
  `job` varchar(100) DEFAULT NULL COMMENT '工作岗位',
  `username` varchar(50) NOT NULL COMMENT '登录用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（加密后）',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '用户状态（1:正常 0:禁用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `t_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(200) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '角色状态（1:正常 0:禁用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
CREATE TABLE `t_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码',
  `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
  `type` tinyint(1) NOT NULL DEFAULT '3' COMMENT '权限类型（1:菜单 2:按钮 3:接口）',
  `path` varchar(200) DEFAULT NULL COMMENT '权限路径/资源路径',
  `method` varchar(20) DEFAULT NULL COMMENT '请求方法（GET,POST,PUT,DELETE等）',
  `parent_id` int(11) NOT NULL DEFAULT '0' COMMENT '父权限ID',
  `description` varchar(200) DEFAULT NULL COMMENT '权限描述',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '权限状态（1:正常 0:禁用）',
  `sort_order` int(11) NOT NULL DEFAULT '1' COMMENT '排序字段',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 用户角色关联表
CREATE TABLE `t_user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 角色权限关联表
CREATE TABLE `t_role_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  `permission_id` int(11) NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permission_role_id` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permission_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `t_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ================= 初始化数据 =================

-- 插入默认角色
INSERT INTO `t_role` (`role_code`, `role_name`, `description`) VALUES 
('ROLE_ADMIN', '系统管理员', '系统管理员，拥有所有权限'),
('ROLE_USER', '普通用户', '普通用户，拥有基本权限'),
('ROLE_GUEST', '访客', '访客用户，只有查看权限');

-- 插入默认权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `type`, `path`, `method`, `description`) VALUES 
('user:read', '查看用户', 3, '/user/**', 'GET', '查看用户信息'),
('user:create', '创建用户', 3, '/user', 'POST', '创建新用户'),
('user:update', '更新用户', 3, '/user/**', 'PUT', '更新用户信息'),
('user:delete', '删除用户', 3, '/user/**', 'DELETE', '删除用户'),
('user:upload', '用户文件上传', 3, '/user/*/file', 'POST', '用户文件上传'),
('db:read', '数据库查询', 3, '/db/**', 'GET', '数据库查询操作'),
('db:write', '数据库写入', 3, '/db/**', 'POST,PUT,DELETE', '数据库写入操作'),
('monitor:read', '监控查看', 3, '/monitor/**', 'GET', '查看系统监控信息'),
('actuator:read', 'Actuator查看', 3, '/actuator/**', 'GET', '查看Actuator端点'),
('log:read', '日志查看', 3, '/log-demo/**', 'GET', '查看日志演示'),
('schedule:read', '任务查看', 3, '/schedule/**', 'GET', '查看定时任务'),
('schedule:write', '任务管理', 3, '/schedule/**', 'POST,PUT,DELETE', '管理定时任务'),
('file:download', '文件下载', 3, '/file/download/**', 'GET', '下载文件权限'),
('file:list', '文件列表', 3, '/file/list', 'GET', '查看文件列表权限'),
('file:report', '报表下载', 3, '/file/download/report', 'POST', '生成并下载报表权限');

-- 插入默认用户（密码:123456, 已BCrypt加密）
INSERT INTO `t_user` (`name`, `job`, `username`, `password`, `email`, `phone`, `status`) VALUES 
('系统管理员', '管理员', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTY.5d7wx8VKa6SqhK7V4gy5b6u', 'admin@example.com', '13800138000', 1),
('普通用户', '开发工程师', 'user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTY.5d7wx8VKa6SqhK7V4gy5b6u', 'user@example.com', '13800138001', 1),
('访客用户', '访客', 'guest', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKTY.5d7wx8VKa6SqhK7V4gy5b6u', 'guest@example.com', '13800138002', 1);

-- 用户角色
INSERT INTO `t_user_role` (`user_id`, `role_id`, `create_by`) VALUES 
(1, 1, 'system'),
(2, 2, 'system'),
(3, 3, 'system');

-- 管理员角色拥有所有权限
INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `create_by`) 
SELECT 1, id, 'system' FROM t_permission;

-- 普通用户权限
INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `create_by`) VALUES 
(2, (SELECT id FROM t_permission WHERE permission_code = 'user:read'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'user:update'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'user:upload'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'db:read'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'monitor:read'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'log:read'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'schedule:read'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'file:download'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'file:list'), 'system'),
(2, (SELECT id FROM t_permission WHERE permission_code = 'file:report'), 'system');

-- 访客权限
INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `create_by`) VALUES 
(3, (SELECT id FROM t_permission WHERE permission_code = 'user:read'), 'system'),
(3, (SELECT id FROM t_permission WHERE permission_code = 'db:read'), 'system'),
(3, (SELECT id FROM t_permission WHERE permission_code = 'monitor:read'), 'system'),
(3, (SELECT id FROM t_permission WHERE permission_code = 'log:read'), 'system'),
(3, (SELECT id FROM t_permission WHERE permission_code = 'schedule:read'), 'system'),
(3, (SELECT id FROM t_permission WHERE permission_code = 'file:list'), 'system');

-- 重新开启外键检查
SET FOREIGN_KEY_CHECKS = 1;
