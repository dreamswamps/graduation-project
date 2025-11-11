-- 创建数据库
CREATE DATABASE IF NOT EXISTS `project` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `project`;

-- 1. 管理员基础信息表
CREATE TABLE IF NOT EXISTS `admin` (
  `id` int NOT NULL PRIMARY KEY,
  `name` varchar(50) DEFAULT NULL,
  `addtime` date DEFAULT NULL,
  `gender` varchar(10) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `avatar` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 管理员权限与登录信息表
CREATE TABLE IF NOT EXISTS `admin_auth` (
  `username` varchar(50) NOT NULL PRIMARY KEY,
  `pwd` varchar(100) DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  `admin_id` int NOT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 文章信息表
CREATE TABLE IF NOT EXISTS `article` (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `title` varchar(255) DEFAULT NULL,
  `img` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `content` longtext,
  `time` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `publisher_id` int NOT NULL,
  `publisher_name` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 业务申请记录表
CREATE TABLE IF NOT EXISTS `application` (
  `aid` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `type` varchar(45) NOT NULL,
  `apply_account` varchar(45) NOT NULL,
  `apply_time` datetime DEFAULT NULL,
  `approval` int DEFAULT '0',
  `approved_time` datetime DEFAULT NULL,
  `has_completed` int DEFAULT '0',
  `working` int DEFAULT '0',
  `worker` int DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  KEY `idx_account` (`apply_account`),
  KEY `idx_approval` (`approval`),
  KEY `idx_completed` (`has_completed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 待办事项表
CREATE TABLE IF NOT EXISTS `todolist` (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `uid` int NOT NULL,
  `title` varchar(45) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `rate` int DEFAULT NULL,
  `modify_time` datetime DEFAULT NULL,
  `index` int DEFAULT NULL,
  `tag_done` int DEFAULT '0',
  `tag_like` int DEFAULT '0',
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建默认管理员
INSERT INTO `admin` (`id`, `name`, `addtime`, `gender`, `phone`, `is_deleted`, `avatar`) 
VALUES (0, '管理员', CURDATE(), '男', NULL, 0, NULL);

-- 创建管理员登录凭证
-- 密码为 '123456' 
INSERT INTO `admin_auth` (`username`, `pwd`, `role`, `email`, `admin_id`, `is_deleted`) 
VALUES ('admin_0', '123456', 'Admin', NULL, 0, 0);