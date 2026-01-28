-- =============================================
-- 项目：wuzhi AI 聊天系统
-- 数据库建表语句
-- 生成时间：2026-01-26
-- 逻辑删除字段：data_deleted (1=已删除, 0=未删除)
-- 所有ID字段使用varchar(64)存储UUID（无连字符）
-- 不使用物理外键，通过应用层保证数据完整性
-- =============================================

-- 用户模块
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `data_user` (
    `id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password` VARCHAR(128) NOT NULL COMMENT '密码',
    `name` VARCHAR(50) NOT NULL COMMENT '用户姓名',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户操作记录表
CREATE TABLE IF NOT EXISTS `record_user` (
    `id` VARCHAR(64) NOT NULL COMMENT '记录ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `record_type` VARCHAR(50) NOT NULL COMMENT '记录类型（login/refresh_token）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_record_type` (`record_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作记录表';

-- 聊天模块
-- =============================================

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `data_conversation` (
    `id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记（兼容原有逻辑）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `data_chat` (
    `id` VARCHAR(64) NOT NULL COMMENT '消息ID',
    `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `type` VARCHAR(50) NOT NULL COMMENT '消息类型（USER/AI）',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记（兼容原有逻辑）',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 模型配置选项表
CREATE TABLE IF NOT EXISTS `data_model_option` (
    `code` VARCHAR(64) NOT NULL COMMENT '模型代码',
    `name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`code`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型选项表';

-- 企业管理模块
-- =============================================

-- 公司信息表
CREATE TABLE IF NOT EXISTS `data_company` (
    `id` VARCHAR(64) NOT NULL COMMENT '公司ID',
    `short_name` VARCHAR(50) DEFAULT NULL COMMENT '公司简称',
    `name` VARCHAR(100) NOT NULL COMMENT '公司全称',
    `description` TEXT COMMENT '公司描述',
    `credit_code` VARCHAR(50) DEFAULT NULL COMMENT '统一社会信用代码',
    `legal_representative` VARCHAR(50) DEFAULT NULL COMMENT '法人代表',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `industry` VARCHAR(50) NOT NULL COMMENT '所属行业',
    `status` VARCHAR(20) NOT NULL COMMENT '状态（active/inactive）',
    `approve_status` VARCHAR(20) NOT NULL COMMENT '认证状态',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_approve_status` (`approve_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司信息表';

-- 公司组织架构节点表
CREATE TABLE IF NOT EXISTS `node_company` (
    `id` VARCHAR(64) NOT NULL COMMENT '节点ID',
    `parent_id` VARCHAR(64) DEFAULT NULL COMMENT '父节点ID',
    `company_id` VARCHAR(64) NOT NULL COMMENT '公司ID',
    `level` INT NOT NULL COMMENT '节点层级',
    `target_id` VARCHAR(64) NOT NULL COMMENT '目标实体ID',
    `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `type` VARCHAR(50) NOT NULL COMMENT '节点类型（department/employee）',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司组织架构节点表';

-- 公司操作记录表
CREATE TABLE IF NOT EXISTS `record_company` (
    `id` VARCHAR(64) NOT NULL COMMENT '记录ID',
    `company_id` VARCHAR(64) NOT NULL COMMENT '公司ID',
    `type` VARCHAR(50) NOT NULL COMMENT '操作对象类型',
    `operate_type` VARCHAR(50) NOT NULL COMMENT '操作类型（add/update/delete）',
    `target_id` VARCHAR(64) NOT NULL COMMENT '目标ID',
    `message` TEXT COMMENT '操作详情',
    `operator_user_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `operate_time` DATETIME NOT NULL COMMENT '操作时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_type` (`type`),
    KEY `idx_operate_type` (`operate_type`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司操作记录表';

-- 部门表
CREATE TABLE IF NOT EXISTS `data_department` (
    `id` VARCHAR(64) NOT NULL COMMENT '部门ID',
    `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `description` TEXT COMMENT '部门描述',
    `company_id` VARCHAR(64) NOT NULL COMMENT '所属公司ID',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `operator_emp_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 员工表
CREATE TABLE IF NOT EXISTS `data_employee` (
    `id` VARCHAR(64) NOT NULL COMMENT '员工ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
    `company_id` VARCHAR(64) NOT NULL COMMENT '所属公司ID',
    `department_id` VARCHAR(64) DEFAULT NULL COMMENT '所属部门ID',
    `status` VARCHAR(20) NOT NULL COMMENT '状态（active/leave/banned）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `operator_emp_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_company_id` (`user_id`, `company_id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 任务表
CREATE TABLE IF NOT EXISTS `data_mission` (
    `id` VARCHAR(64) NOT NULL COMMENT '任务ID',
    `name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `description` TEXT COMMENT '任务描述',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` VARCHAR(20) NOT NULL COMMENT '任务状态',
    `company_id` VARCHAR(64) NOT NULL COMMENT '所属公司ID',
    `department_id` VARCHAR(64) NOT NULL COMMENT '所属部门ID',
    `employee_id` VARCHAR(64) DEFAULT NULL COMMENT '执行人ID',
    `operator_emp_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `publisher_emp_id` VARCHAR(64) NOT NULL COMMENT '发布人ID',
    `marked` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否标记',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 日程表
CREATE TABLE IF NOT EXISTS `data_schedule` (
    `id` VARCHAR(64) NOT NULL COMMENT '日程ID',
    `company_id` VARCHAR(64) NOT NULL COMMENT '公司ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '日程标题',
    `description` TEXT COMMENT '日程描述',
    `time` DATETIME NOT NULL COMMENT '日程时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程表';

-- 权限管理模块
-- =============================================

-- 权限表
CREATE TABLE IF NOT EXISTS `data_permission` (
    `id` VARCHAR(64) NOT NULL COMMENT '权限ID',
    `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    `parent_id` VARCHAR(64) DEFAULT NULL COMMENT '父权限ID',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色表
CREATE TABLE IF NOT EXISTS `data_role` (
    `id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    `company_id` VARCHAR(64) NOT NULL COMMENT '所属公司ID',
    `name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `operator_emp_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 员工-角色关联表
CREATE TABLE IF NOT EXISTS `link_employee_with_role` (
    `employee_id` VARCHAR(64) DEFAULT NULL COMMENT '员工ID',
    `company_id` VARCHAR(64) DEFAULT NULL COMMENT '公司ID',
    `role_id` VARCHAR(64) DEFAULT NULL COMMENT '角色ID',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `link_role_with_permission` (
    `company_id` VARCHAR(64) NOT NULL COMMENT '公司ID',
    `role_id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    `permission_id` VARCHAR(64) NOT NULL COMMENT '权限ID',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    KEY `idx_company_id` (`company_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 工具管理模块
-- =============================================

-- 工具信息表
CREATE TABLE IF NOT EXISTS `data_tool` (
    `id` VARCHAR(64) NOT NULL COMMENT '工具ID',
    `name` VARCHAR(100) NOT NULL COMMENT '工具名称',
    `description` TEXT NOT NULL COMMENT '工具描述',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '工具价格',
    `url` VARCHAR(255) NOT NULL COMMENT '工具链接',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `use_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `rating` INT NOT NULL DEFAULT 0 COMMENT '评分',
    `version` VARCHAR(50) NOT NULL COMMENT '版本号',
    `author_user_id` VARCHAR(64) NOT NULL COMMENT '作者ID',
    `type` VARCHAR(50) NOT NULL COMMENT '工具类型',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `status` VARCHAR(20) NOT NULL COMMENT '状态（private/public）',
    `audit_status` VARCHAR(20) NOT NULL COMMENT '审核状态',
    `tag_ids` VARCHAR(500) NOT NULL COMMENT '标签ID列表',
    `keywords` VARCHAR(500) NOT NULL COMMENT '关键词',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_author_user_id` (`author_user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具信息表';

-- 工具节点表（树形结构）
CREATE TABLE IF NOT EXISTS `node_tool` (
    `id` VARCHAR(64) NOT NULL COMMENT '节点ID',
    `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `type` VARCHAR(50) NOT NULL COMMENT '节点类型',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '用户ID',
    `company_id` VARCHAR(64) DEFAULT NULL COMMENT '公司ID',
    `operator_user_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `parent_id` VARCHAR(64) DEFAULT NULL COMMENT '父节点ID',
    `tool_id` VARCHAR(64) DEFAULT NULL COMMENT '工具ID',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    `url` VARCHAR(255) DEFAULT NULL COMMENT 'URL',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_tool_id` (`tool_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具节点表';

-- 工具使用记录表
CREATE TABLE IF NOT EXISTS `record_tool` (
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `tool_id` VARCHAR(64) NOT NULL COMMENT '工具ID',
    `company_id` VARCHAR(64) DEFAULT NULL COMMENT '公司ID',
    `detail` TEXT NOT NULL COMMENT '使用详情',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tool_id` (`tool_id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具使用记录表';

-- 工具推荐表
CREATE TABLE IF NOT EXISTS `data_tool_recommendation` (
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `company_id` VARCHAR(64) NOT NULL COMMENT '公司ID',
    `tool_ids` JSON NOT NULL COMMENT '推荐的工具ID列表',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`user_id`, `company_id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具推荐表';

-- 标签表
CREATE TABLE IF NOT EXISTS `data_tag` (
    `id` VARCHAR(64) NOT NULL COMMENT '标签ID',
    `name` VARCHAR(100) NOT NULL COMMENT '标签名称',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 数据库文档管理
-- =============================================

-- 数据库文档表
CREATE TABLE IF NOT EXISTS `data_database` (
    `id` VARCHAR(64) NOT NULL COMMENT '文档ID',
    `name` VARCHAR(100) NOT NULL COMMENT '文档名称',
    `description` TEXT COMMENT '文档描述',
    `type` VARCHAR(50) NOT NULL COMMENT '文档类型',
    `url` VARCHAR(255) DEFAULT NULL COMMENT '文档URL',
    `text` MEDIUMTEXT DEFAULT NULL COMMENT '文档文本内容',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库文档表';

-- 数据库文档节点表（树形结构）
CREATE TABLE IF NOT EXISTS `node_database` (
    `id` VARCHAR(64) NOT NULL COMMENT '节点ID',
    `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `type` VARCHAR(50) NOT NULL COMMENT '节点类型',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `company_id` VARCHAR(64) DEFAULT NULL COMMENT '公司ID',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    `operator_user_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `parent_id` VARCHAR(64) DEFAULT NULL COMMENT '父节点ID',
    `database_id` VARCHAR(64) DEFAULT NULL COMMENT '数据库文档ID',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    `data_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_database_id` (`database_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库文档节点表';

-- =============================================
-- 初始化数据
-- =============================================

-- 权限初始数据
INSERT INTO `data_permission` (`id`, `code`, `name`, `description`, `parent_id`, `data_deleted`) VALUES
('1', 'character', '角色管理', '角色管理', NULL, 0),
('11', 'character_add', '新增角色', '新增角色', '1', 0),
('12', 'character_edit', '编辑角色', '编辑角色', '1', 0),
('13', 'character_delete', '删除角色', '删除角色', '1', 0),
('2', 'department', '部门管理', '部门管理', NULL, 0),
('21', 'department_add', '新增部门', '新增部门', '2', 0),
('22', 'department_edit', '编辑部门', '编辑部门', '2', 0),
('23', 'department_delete', '删除部门', '删除部门', '2', 0),
('3', 'employee', '员工管理', '员工管理', NULL, 0),
('31', 'employee_add', '新增员工', '新增员工', '3', 0),
('32', 'employee_edit', '编辑员工', '编辑员工', '3', 0),
('33', 'employee_delete', '删除员工', '删除员工', '3', 0);

-- AI模型初始数据
INSERT INTO `data_model_option` (`code`, `name`, `create_time`, `data_deleted`) VALUES
('deepseek', 'DeepSeek', NOW(), 0),
('qwen3Max', 'Qwen3-Max', NOW(), 0);

-- =============================================
-- 说明文档
-- =============================================

/*
索引设计说明：

1. **主键索引**：所有表使用id作为主键
2. **唯一索引**：
   - data_user.phone: 手机号唯一
   - data_permission.code: 权限编码唯一
   - data_tag.name: 标签名称唯一
   - data_employee(user_id, company_id): 用户在公司内唯一

3. **高频查询索引**：
   - user_id: 几乎所有业务都围绕用户展开
   - company_id: 企业模块的核心查询条件
   - create_time/update_time: 时间范围查询
   - status/approve_status/audit_status: 状态过滤

4. **关联查询索引**：
   - parent_id: 树形结构查询
   - target_id: 关联目标查询

5. **复合索引**：
   - 避免重复创建冗余索引
   - 优先满足最左前缀原则

字段设计说明：

1. **ID字段**：所有id使用varchar(64)，存储无连字符的UUID
2. **逻辑删除**：全局使用data_deleted字段（1=已删除，0=未删除）
3. **时间字段**：统一使用DATETIME类型
4. **状态字段**：使用VARCHAR存储枚举值，便于扩展
5. **外键**：不使用物理外键约束，由应用层保证数据完整性
*/
