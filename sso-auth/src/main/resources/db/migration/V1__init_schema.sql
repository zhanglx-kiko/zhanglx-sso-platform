-- ========================================
-- SSO 统一认证平台 - 数据库初始化脚本 (V2.0 最终优化版)
-- 数据库版本：MySQL 9.6.0
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_0900_ai_ci
-- 存储引擎：InnoDB
-- ========================================

-- 1. 用户表
DROP TABLE IF EXISTS `t_auth_user`;
CREATE TABLE `t_auth_user`
(
    `id`                     bigint(20)   NOT NULL COMMENT '主键 ID (雪花算法)',
    `username`               varchar(64)  NOT NULL COMMENT '账号',
    `password`               varchar(255) NOT NULL COMMENT '密码 (Argon2 加密)',
    `nickname`               varchar(64)           DEFAULT NULL COMMENT '昵称',
    `avatar`                 varchar(255)          DEFAULT NULL COMMENT '头像 URL',
    `open_id`                varchar(128)          DEFAULT NULL COMMENT '微信 OpenID',
    `allow_concurrent_login` tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否允许并发登录：0-禁止，1-允许',
    `dept_id`                bigint(20)            DEFAULT NULL COMMENT '部门 ID',
    `status`                 tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `del_flag`               bigint(20)   NOT NULL DEFAULT 0 COMMENT '【优化】逻辑删除：0-未删，非0(等于id)-已删',
    `create_by`              bigint(20)            DEFAULT NULL COMMENT '创建人 ID',
    `create_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`              bigint(20)            DEFAULT NULL COMMENT '修改人 ID',
    `update_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    -- 【优化】改为联合唯一索引，完美兼容逻辑删除，允许重复注册已被软删除的账号
    UNIQUE KEY `uk_username_del` (`username`, `del_flag`) COMMENT '账号与逻辑删除联合唯一索引',
    KEY `idx_open_id` (`open_id`) COMMENT '微信 OpenID 索引',
    KEY `idx_dept_id` (`dept_id`) COMMENT '部门 ID 索引',
    KEY `idx_status_del` (`status`, `del_flag`) COMMENT '联合索引：状态查询 + 逻辑删除',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表';

-- 2. 角色表
DROP TABLE IF EXISTS `t_auth_role`;
CREATE TABLE `t_auth_role`
(
    `id`          bigint(20)  NOT NULL COMMENT '主键 ID',
    `role_name`   varchar(64) NOT NULL COMMENT '角色名称',
    `role_code`   varchar(64) NOT NULL COMMENT '角色编码',
    `role_type`   varchar(32)          DEFAULT NULL COMMENT '角色类型',
    `build_in`    tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否内置：1-内置，0-非内置',
    `remark`      varchar(255)         DEFAULT NULL COMMENT '备注',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '【优化】逻辑删除：0-未删，非0(等于id)-已删',
    `create_by`   bigint(20)           DEFAULT NULL COMMENT '创建人 ID',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)           DEFAULT NULL COMMENT '修改人 ID',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    -- 【优化】升级为联合唯一索引
    UNIQUE KEY `uk_role_code_del` (`role_code`, `del_flag`) COMMENT '角色编码联合唯一索引',
    UNIQUE KEY `uk_role_name_del` (`role_name`, `del_flag`) COMMENT '角色名称联合唯一索引',
    KEY `idx_build_in_del` (`build_in`, `del_flag`) COMMENT '联合索引：内置角色过滤',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色表';

-- 3. 权限表
DROP TABLE IF EXISTS `t_auth_permission`;
CREATE TABLE `t_auth_permission`
(
    `id`               bigint(20)   NOT NULL COMMENT '主键 ID',
    `name`             varchar(64)  NOT NULL COMMENT '权限名称',
    `identification`   varchar(128) NOT NULL COMMENT '权限标识 (唯一)',
    `parent_id`        bigint(20)            DEFAULT NULL COMMENT '父权限 ID',
    `identity_lineage` varchar(255)          DEFAULT NULL COMMENT '标识血缘',
    `com_path`         varchar(255)          DEFAULT NULL COMMENT '组件地址',
    `path`             varchar(255)          DEFAULT NULL COMMENT '路由地址',
    `icon_str`         varchar(64)           DEFAULT NULL COMMENT '图标',
    `display_no`       int(11)      NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `is_frame`         tinyint(1)            DEFAULT NULL COMMENT '是否外链',
    `type`             tinyint(4)   NOT NULL DEFAULT 0 COMMENT '类型：-1 平台 0 模块 1 菜单 2 按钮 3 接口',
    `remark`           varchar(255)          DEFAULT NULL COMMENT '备注',
    `del_flag`         bigint(20)   NOT NULL DEFAULT 0 COMMENT '【优化】逻辑删除：0-未删，非0(等于id)-已删',
    `create_by`        bigint(20)            DEFAULT NULL COMMENT '创建人 ID',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        bigint(20)            DEFAULT NULL COMMENT '修改人 ID',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    -- 【优化】升级为联合唯一索引
    UNIQUE KEY `uk_identification_del` (`identification`, `del_flag`) COMMENT '权限标识联合唯一索引',
    -- 【优化】删除了冗余的 idx_parent_id 索引，因为它被下方的 idx_parent_display 完美覆盖
    KEY `idx_parent_display` (`parent_id`, `display_no`) COMMENT '联合索引：树形构建 + 排序',
    KEY `idx_identity_lineage` (`identity_lineage`(191)) COMMENT '前缀索引',
    KEY `idx_type_del` (`type`, `del_flag`) COMMENT '联合索引',
    KEY `idx_display_no` (`display_no`) COMMENT '显示顺序索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='权限表';

-- 4. 用户角色关联表
DROP TABLE IF EXISTS `t_auth_user_role_mapping`;
CREATE TABLE `t_auth_user_role_mapping`
(
    `id`          bigint(20) NOT NULL COMMENT '主键 ID',
    `user_id`     bigint(20) NOT NULL COMMENT '用户 ID',
    `role_id`     bigint(20) NOT NULL COMMENT '角色 ID',
    `del_flag`    bigint(20) NOT NULL DEFAULT 0 COMMENT '【优化】逻辑删除：0-未删，非0(等于id)-已删',
    `create_by`   bigint(20)          DEFAULT NULL COMMENT '创建人 ID',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)          DEFAULT NULL COMMENT '修改人 ID',
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    -- 【优化】升级为联合唯一索引，防止软删除后重新绑定报错
    UNIQUE KEY `uk_user_role_del` (`user_id`, `role_id`, `del_flag`) COMMENT '用户-角色联合唯一索引',
    -- 【优化】删除了冗余的 idx_user_id (被 uk_user_role_del 的第一列覆盖)
    KEY `idx_role_id` (`role_id`) COMMENT '角色 ID 索引 (为通过角色反查用户提供支撑)',
    KEY `idx_del_flag` (`del_flag`) COMMENT '逻辑删除索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户角色关联表';

-- 5. 角色权限关联表
DROP TABLE IF EXISTS `t_auth_role_permission_mapping`;
CREATE TABLE `t_auth_role_permission_mapping`
(
    `id`            bigint(20) NOT NULL COMMENT '主键 ID',
    `role_id`       bigint(20) NOT NULL COMMENT '角色 ID',
    `permission_id` bigint(20) NOT NULL COMMENT '权限 ID',
    `expire_time`   datetime            DEFAULT NULL COMMENT '过期时间',
    `del_flag`      bigint(20) NOT NULL DEFAULT 0 COMMENT '【优化】逻辑删除：0-未删，非0(等于id)-已删',
    `create_by`     bigint(20)          DEFAULT NULL COMMENT '创建人 ID',
    `create_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     bigint(20)          DEFAULT NULL COMMENT '修改人 ID',
    `update_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    -- 【优化】升级为联合唯一索引
    UNIQUE KEY `uk_role_permission_del` (`role_id`, `permission_id`, `del_flag`) COMMENT '角色-权限联合唯一索引',
    -- 【优化】删除了冗余的 idx_role_id (被 uk_role_permission_del 的第一列覆盖)
    KEY `idx_permission_id` (`permission_id`) COMMENT '权限 ID 索引 (为通过权限反查角色提供支撑)',
    KEY `idx_expire_time` (`expire_time`) COMMENT '过期时间索引',
    KEY `idx_del_flag` (`del_flag`) COMMENT '逻辑删除索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色权限关联表';


-- ========================================
-- 初始化数据 (保持原样即可，因 del_flag 默认值为 0，数据结构完全兼容)
-- ========================================

-- 1. 插入默认超级管理员用户
INSERT INTO `t_auth_user`
(id, username, password, nickname, avatar, open_id, allow_concurrent_login, dept_id, status, del_flag, create_by, update_by, create_time, update_time)
VALUES
    (2031984412698099713, 'zhanglx', '$argon2id$v=19$m=16,t=682,p=4$77+9XlDvv71c77+977+977+9FSzvv71+Ae+/vdmbGTrvv73vv73vv70dXu+/ve+/ve+/vUnvv70Q77+9UXs$Iheyqmb2s5imzzstmHU4Urq2V6pXnONWfye82Wbdkug',
     '超级管理员-￥$误删', NULL, NULL, 1, NULL, 1, 0, 1, 1, '2026-03-12 14:43:31', '2026-03-12 14:43:31');

-- 3. 插入超级管理员角色
INSERT INTO `t_auth_role`
(id, role_name, role_code, role_type, build_in, remark, del_flag, create_by, update_by, create_time, update_time)
VALUES
    (2031984412698099800, '超级管理员', 'ROLE_SUPER_ADMIN', 'SYSTEM', 1, '拥有电商系统全部权限', 0, 1, 1, NOW(), NOW());

-- 4. 绑定超级管理员账号与角色
INSERT INTO `t_auth_user_role_mapping`
(id, user_id, role_id, del_flag, create_by, update_by, create_time, update_time)
VALUES
    (2031984412698099801, 2031984412698099713, 2031984412698099800, 0, 1, 1, NOW(), NOW());

-- 5. 插入标准电商权限树数据
-- 注意：这里的 identification (权限标识) 统一使用冒号(:)分隔，identity_lineage (血缘) 统一使用点(.)分隔，严格对齐。
INSERT INTO `t_auth_permission`
(id, name, identification, parent_id, identity_lineage, com_path, path, icon_str, display_no, is_frame, type, remark, del_flag)
VALUES
-- =================== 平台级 (-1) ===================
(3000000000000000001, '电商业务平台', 'mall', NULL, 'mall', NULL, '/mall', 'shop', 1, 0, -1, '电商核心业务线', 0),
(3000000000000000002, '系统基础设施', 'system', NULL, 'system', NULL, '/system', 'setting', 2, 0, -1, '系统基础设置', 0),

-- =================== 模块级 (0) ===================
(3000000000000000010, '商品中心', 'mall:goods', 3000000000000000001, 'mall.goods', 'mall/goods/index', '/mall/goods', 'shopping-cart', 1, 0, 0, '商品与库存管理', 0),
(3000000000000000011, '订单中心', 'mall:order', 3000000000000000001, 'mall.order', 'mall/order/index', '/mall/order', 'document', 2, 0, 0, '交易与售后管理', 0),
(3000000000000000012, '营销中心', 'mall:market', 3000000000000000001, 'mall.market', 'mall/market/index', '/mall/market', 'present', 3, 0, 0, '优惠券与活动', 0),
(3000000000000000013, '权限管控', 'system:auth', 3000000000000000002, 'system.auth', 'system/auth/index', '/system/auth', 'lock', 1, 0, 0, '用户角色权限', 0),

-- =================== 菜单级 (1) ===================
-- 商品菜单
(3000000000000000100, '商品列表', 'mall:goods:list', 3000000000000000010, 'mall.goods.list', 'mall/goods/list/index', '/mall/goods/list', NULL, 1, 0, 1, '', 0),
(3000000000000000101, '商品分类', 'mall:goods:category', 3000000000000000010, 'mall.goods.category', 'mall/goods/category/index', '/mall/goods/category', NULL, 2, 0, 1, '', 0),
-- 订单菜单
(3000000000000000110, '所有订单', 'mall:order:list', 3000000000000000011, 'mall.order.list', 'mall/order/list/index', '/mall/order/list', NULL, 1, 0, 1, '', 0),
(3000000000000000111, '退款维权', 'mall:order:refund', 3000000000000000011, 'mall.order.refund', 'mall/order/refund/index', '/mall/order/refund', NULL, 2, 0, 1, '', 0),
-- 权限菜单
(3000000000000000130, '管理员列表', 'system:auth:user', 3000000000000000013, 'system.auth.user', 'system/auth/user/index', '/system/auth/user', NULL, 1, 0, 1, '', 0),
(3000000000000000131, '角色列表', 'system:auth:role', 3000000000000000013, 'system.auth.role', 'system/auth/role/index', '/system/auth/role', NULL, 2, 0, 1, '', 0),

-- =================== 按钮级 (2) ===================
-- 商品列表按钮
(3000000000000001000, '发布商品', 'mall:goods:list:add', 3000000000000000100, 'mall.goods.list.add', NULL, NULL, NULL, 1, 0, 2, '', 0),
(3000000000000001001, '商品上下架', 'mall:goods:list:status', 3000000000000000100, 'mall.goods.list.status', NULL, NULL, NULL, 2, 0, 2, '', 0),
-- 订单列表按钮
(3000000000000001100, '订单发货', 'mall:order:list:ship', 3000000000000000110, 'mall.order.list.ship', NULL, NULL, NULL, 1, 0, 2, '', 0),
(3000000000000001101, '导出订单', 'mall:order:list:export', 3000000000000000110, 'mall.order.list.export', NULL, NULL, NULL, 2, 0, 2, '', 0),

-- =================== 接口级 (3) ===================
(3000000000000010000, '保存商品接口', 'mall:goods:api:save', 3000000000000001000, 'mall.goods.list.add.api', NULL, '/apis/v1/goods/save', NULL, 1, 0, 3, '发布商品的物理接口', 0),
(3000000000000011000, '执行发货接口', 'mall:order:api:ship', 3000000000000001100, 'mall.order.list.ship.api', NULL, '/apis/v1/order/ship', NULL, 1, 0, 3, '回传物流单号的接口', 0);


-- 6. 一键为超级管理员角色授予上述【所有】权限（完美动态绑定）
INSERT INTO `t_auth_role_permission_mapping`
(id, role_id, permission_id, expire_time, del_flag, create_by, update_by, create_time, update_time)
SELECT
    3000000000000020000 + ROW_NUMBER() OVER () AS id,
    2031984412698099800 AS role_id,  -- 刚才创建的超级管理员角色ID
    id AS permission_id,             -- 遍历所有权限表的ID
    NULL AS expire_time,
    0 AS del_flag,
    1 AS create_by,
    1 AS update_by,
    NOW() AS create_time,
    NOW() AS update_time
FROM `t_auth_permission`;