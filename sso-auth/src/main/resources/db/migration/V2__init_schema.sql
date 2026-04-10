-- ========================================
-- SSO 统一认证中心数据库初始化脚本（V2）
-- 说明：
-- 1. 所有业务主键统一使用 id，便于与 MyBatis-Plus 的 BasePO 对齐。
-- 2. 除日志表外，业务表统一补齐审计字段与逻辑删除字段。
-- 3. 在保留现有代码模型的前提下，补齐应用、组织、岗位、字典、参数与关系表。
-- ========================================

-- ========================================
-- 核心业务表
-- ========================================

DROP TABLE IF EXISTS `t_sso_app`;
CREATE TABLE `t_sso_app`
(
    `id`          bigint(20)  NOT NULL COMMENT '主键 ID',
    `app_code`    varchar(32) NOT NULL COMMENT '应用编码，例如 sso、mall',
    `app_name`    varchar(64) NOT NULL COMMENT '应用名称',
    `status`      tinyint(1)  NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `user_type`   tinyint(1)  NOT NULL DEFAULT 1 COMMENT '用户类型：1-系统用户，2-会员用户',
    `remark`      varchar(255)         DEFAULT NULL COMMENT '备注',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)           DEFAULT NULL COMMENT '创建人',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)           DEFAULT NULL COMMENT '更新人',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_code_del` (`app_code`, `del_flag`),
    UNIQUE KEY `uk_app_name_del` (`app_name`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='接入应用表';

DROP TABLE IF EXISTS `t_auth_dept`;
CREATE TABLE `t_auth_dept`
(
    `id`          bigint(20)   NOT NULL COMMENT '主键 ID',
    `parent_id`   bigint(20)   NOT NULL DEFAULT 0 COMMENT '父部门 ID，0 表示根节点',
    `ancestors`   varchar(255) NOT NULL DEFAULT '0' COMMENT '祖级列表，使用逗号分隔',
    `dept_name`   varchar(64)  NOT NULL COMMENT '部门名称',
    `sort_num`    int(11)      NOT NULL DEFAULT 0 COMMENT '排序号',
    `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `del_flag`    bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_parent_name_del` (`parent_id`, `dept_name`, `del_flag`),
    KEY `idx_parent_sort` (`parent_id`, `sort_num`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='部门表';

DROP TABLE IF EXISTS `t_auth_post`;
CREATE TABLE `t_auth_post`
(
    `id`          bigint(20)  NOT NULL COMMENT '主键 ID',
    `post_code`   varchar(64) NOT NULL COMMENT '岗位编码',
    `post_name`   varchar(64) NOT NULL COMMENT '岗位名称',
    `sort_num`    int(11)     NOT NULL DEFAULT 0 COMMENT '排序号',
    `status`      tinyint(1)  NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)           DEFAULT NULL COMMENT '创建人',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)           DEFAULT NULL COMMENT '更新人',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_code_del` (`post_code`, `del_flag`),
    UNIQUE KEY `uk_post_name_del` (`post_name`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='岗位表';

DROP TABLE IF EXISTS `t_sys_user`;
CREATE TABLE `t_sys_user`
(
    `id`                     bigint(20)   NOT NULL COMMENT '主键 ID',
    `username`               varchar(64)  NOT NULL COMMENT '登录账号',
    `password`               varchar(255) NOT NULL COMMENT '密码哈希',
    `user_type`              tinyint(1)   NOT NULL DEFAULT 1 COMMENT '用户类型：1-内部用户，2-外部接入用户',
    `nickname`               varchar(64)           DEFAULT NULL COMMENT '昵称',
    `avatar`                 varchar(255)          DEFAULT NULL COMMENT '头像地址',
    `phone_number`           varchar(20)           DEFAULT NULL COMMENT '手机号',
    `email`                  varchar(128)          DEFAULT NULL COMMENT '邮箱',
    `sex`                    tinyint(1)            DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday`               date                  DEFAULT NULL COMMENT '生日',
    `dept_id`                bigint(20)            DEFAULT NULL COMMENT '所属部门 ID',
    `allow_concurrent_login` tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否允许并发登录：1-允许，0-禁止',
    `status`                 tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `del_flag`               bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`              bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`              bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username_del` (`username`, `del_flag`),
    UNIQUE KEY `uk_phone_del` (`phone_number`, `del_flag`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统用户表';

DROP TABLE IF EXISTS `t_sys_user_social`;
CREATE TABLE `t_sys_user_social`
(
    `id`            bigint(20)   NOT NULL COMMENT '主键 ID',
    `user_id`       bigint(20)   NOT NULL COMMENT '用户 ID',
    `identity_type` varchar(32)  NOT NULL COMMENT '第三方身份类型，例如 WECHAT_OPEN',
    `identifier`    varchar(128) NOT NULL COMMENT '第三方唯一标识',
    `credential`    varchar(255)          DEFAULT NULL COMMENT '第三方凭证',
    `del_flag`      bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`     bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_identifier_del` (`identity_type`, `identifier`, `del_flag`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统用户第三方账号绑定表';

DROP TABLE IF EXISTS `t_member_user`;
CREATE TABLE `t_member_user`
(
    `id`               bigint(20)   NOT NULL COMMENT '主键 ID',
    `phone_number`     varchar(20)           DEFAULT NULL COMMENT '手机号',
    `password`         varchar(255)          DEFAULT NULL COMMENT '密码哈希',
    `nickname`         varchar(64)           DEFAULT NULL COMMENT '昵称',
    `avatar`           varchar(255)          DEFAULT NULL COMMENT '头像地址',
    `sex`              tinyint(1)            DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday`         date                  DEFAULT NULL COMMENT '生日',
    `email`            varchar(128)          DEFAULT NULL COMMENT '邮箱',
    `user_level`       int(11)      NOT NULL DEFAULT 1 COMMENT '用户等级，默认 1 级',
    `points`           bigint(20)   NOT NULL DEFAULT 0 COMMENT '会员积分',
    `member_type`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '会员类型：0-普通会员，1-VIP会员',
    `real_name_status` tinyint(1)   NOT NULL DEFAULT 0 COMMENT '实名状态：0-未认证，1-认证中，2-已认证，3-认证失败',
    `profile_extra`    text                  DEFAULT NULL COMMENT '扩展资料，建议存储 JSON 字符串',
    `status`           tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `register_ip`      varchar(128)          DEFAULT NULL COMMENT '注册 IP',
    `last_login_time`  datetime              DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`    varchar(128)          DEFAULT NULL COMMENT '最后登录 IP',
    `del_flag`         bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`        bigint(20)            DEFAULT 0 COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        bigint(20)            DEFAULT 0 COMMENT '更新人',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_phone_del` (`phone_number`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='会员表';

DROP TABLE IF EXISTS `t_member_social`;
CREATE TABLE `t_member_social`
(
    `id`            bigint(20)   NOT NULL COMMENT '主键 ID',
    `member_id`     bigint(20)   NOT NULL COMMENT '会员 ID',
    `identity_type` varchar(32)  NOT NULL COMMENT '第三方身份类型',
    `identifier`    varchar(128) NOT NULL COMMENT '第三方唯一标识',
    `union_id`      varchar(255)          DEFAULT NULL COMMENT '第三方生态统一标识',
    `del_flag`      bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`     bigint(20)            DEFAULT 0 COMMENT '创建人',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     bigint(20)            DEFAULT 0 COMMENT '更新人',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_type_identifier_del` (`identity_type`, `identifier`, `del_flag`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_union_id` (`union_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='会员第三方账号绑定表';

DROP TABLE IF EXISTS `t_auth_role`;
CREATE TABLE `t_auth_role`
(
    `id`          bigint(20)  NOT NULL COMMENT '主键 ID',
    `app_code`    varchar(32) NOT NULL COMMENT '所属应用编码',
    `role_name`   varchar(64) NOT NULL COMMENT '角色名称',
    `role_code`   varchar(64) NOT NULL COMMENT '角色编码',
    `data_scope`  tinyint(1)  NOT NULL DEFAULT 1 COMMENT '数据范围：1-全部，2-本部门及以下，3-本部门，4-本人，5-自定义',
    `status`      tinyint(1)  NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `remark`      varchar(255)         DEFAULT NULL COMMENT '备注',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)           DEFAULT NULL COMMENT '创建人',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)           DEFAULT NULL COMMENT '更新人',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_role_code_del` (`app_code`, `role_code`, `del_flag`),
    UNIQUE KEY `uk_app_role_name_del` (`app_code`, `role_name`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色表';

DROP TABLE IF EXISTS `t_auth_permission`;
CREATE TABLE `t_auth_permission`
(
    `id`               bigint(20)   NOT NULL COMMENT '主键 ID',
    `name`             varchar(64)  NOT NULL COMMENT '权限名称',
    `identification`   varchar(128) NOT NULL COMMENT '权限标识',
    `parent_id`        bigint(20)            DEFAULT 0 COMMENT '父权限 ID',
    `identity_lineage` varchar(255)          DEFAULT NULL COMMENT '权限血缘路径',
    `com_path`         varchar(255)          DEFAULT NULL COMMENT '前端组件路径',
    `path`             varchar(255)          DEFAULT NULL COMMENT '前端路由路径',
    `icon_str`         varchar(64)           DEFAULT NULL COMMENT '图标',
    `display_no`       int(11)      NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `is_frame`         tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否外链：1-是，0-否',
    `type`             tinyint(4)   NOT NULL DEFAULT 0 COMMENT '类型：-1-平台，0-模块，1-菜单，2-按钮，3-接口',
    `remark`           varchar(255)          DEFAULT NULL COMMENT '备注',
    `status`           tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `del_flag`         bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`        bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_identifier_del` (`identification`, `del_flag`),
    KEY `idx_parent_display` (`parent_id`, `display_no`),
    KEY `idx_type_status_del` (`type`, `status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='权限表';

DROP TABLE IF EXISTS `t_sys_dict_type`;
CREATE TABLE `t_sys_dict_type`
(
    `id`          bigint(20)   NOT NULL COMMENT '主键 ID',
    `dict_name`   varchar(100) NOT NULL COMMENT '字典名称',
    `dict_type`   varchar(100) NOT NULL COMMENT '字典类型编码',
    `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '备注',
    `del_flag`    bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_del` (`dict_type`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='字典类型表';

DROP TABLE IF EXISTS `t_sys_dict_data`;
CREATE TABLE `t_sys_dict_data`
(
    `id`          bigint(20)   NOT NULL COMMENT '主键 ID',
    `dict_sort`   int(11)      NOT NULL DEFAULT 0 COMMENT '字典排序',
    `dict_label`  varchar(100) NOT NULL COMMENT '字典标签',
    `dict_value`  varchar(100) NOT NULL COMMENT '字典值',
    `dict_type`   varchar(100) NOT NULL COMMENT '字典类型编码',
    `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '备注',
    `del_flag`    bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_value_del` (`dict_type`, `dict_value`, `del_flag`),
    UNIQUE KEY `uk_dict_type_label_del` (`dict_type`, `dict_label`, `del_flag`),
    KEY `idx_dict_type_status_del` (`dict_type`, `status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='字典数据表';

DROP TABLE IF EXISTS `t_sys_config`;
CREATE TABLE `t_sys_config`
(
    `id`             bigint(20)   NOT NULL COMMENT '主键 ID',
    `config_name`    varchar(100) NOT NULL COMMENT '参数名称',
    `config_key`     varchar(100) NOT NULL COMMENT '参数键',
    `config_value`   varchar(500) NOT NULL COMMENT '参数值',
    `config_group`   varchar(64)  NOT NULL DEFAULT 'default' COMMENT '参数分组',
    `sensitive_flag` tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否敏感参数：1-是，0-否',
    `status`         tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `config_type`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否系统内置：1-是，0-否',
    `remark`         varchar(255)          DEFAULT NULL COMMENT '备注',
    `del_flag`       bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`      bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key_del` (`config_key`, `del_flag`),
    KEY `idx_config_group_status_del` (`config_group`, `status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统参数表';
-- ========================================
-- 关系表
-- 为了与当前项目统一的 BasePO / 逻辑删除风格保持一致，关系表同样补充 del_flag。
-- ========================================

DROP TABLE IF EXISTS `t_auth_user_app`;
CREATE TABLE `t_auth_user_app`
(
    `id`          bigint(20)  NOT NULL COMMENT '主键 ID',
    `user_id`     bigint(20)  NOT NULL COMMENT '用户 ID',
    `app_code`    varchar(32) NOT NULL COMMENT '应用编码',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)           DEFAULT NULL COMMENT '创建人',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)           DEFAULT NULL COMMENT '更新人',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_app_del` (`user_id`, `app_code`, `del_flag`),
    KEY `idx_app_code_del` (`app_code`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户应用授权表';

DROP TABLE IF EXISTS `t_auth_role_dept`;
CREATE TABLE `t_auth_role_dept`
(
    `id`          bigint(20) NOT NULL COMMENT '主键 ID',
    `role_id`     bigint(20) NOT NULL COMMENT '角色 ID',
    `dept_id`     bigint(20) NOT NULL COMMENT '部门 ID',
    `del_flag`    bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)          DEFAULT NULL COMMENT '创建人',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)          DEFAULT NULL COMMENT '更新人',
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_dept_del` (`role_id`, `dept_id`, `del_flag`),
    KEY `idx_dept_del` (`dept_id`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色数据范围部门表';

DROP TABLE IF EXISTS `t_auth_user_role`;
CREATE TABLE `t_auth_user_role`
(
    `id`          bigint(20) NOT NULL COMMENT '主键 ID',
    `user_id`     bigint(20) NOT NULL COMMENT '用户 ID',
    `role_id`     bigint(20) NOT NULL COMMENT '角色 ID',
    `del_flag`    bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)          DEFAULT NULL COMMENT '创建人',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)          DEFAULT NULL COMMENT '更新人',
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role_del` (`user_id`, `role_id`, `del_flag`),
    KEY `idx_role_del` (`role_id`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户角色关联表';

DROP TABLE IF EXISTS `t_auth_user_post`;
CREATE TABLE `t_auth_user_post`
(
    `id`          bigint(20) NOT NULL COMMENT '主键 ID',
    `user_id`     bigint(20) NOT NULL COMMENT '用户 ID',
    `post_id`     bigint(20) NOT NULL COMMENT '岗位 ID',
    `del_flag`    bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`   bigint(20)          DEFAULT NULL COMMENT '创建人',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20)          DEFAULT NULL COMMENT '更新人',
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post_del` (`user_id`, `post_id`, `del_flag`),
    KEY `idx_post_del` (`post_id`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户岗位关联表';

DROP TABLE IF EXISTS `t_auth_role_permission`;
CREATE TABLE `t_auth_role_permission`
(
    `id`            bigint(20) NOT NULL COMMENT '主键 ID',
    `role_id`       bigint(20) NOT NULL COMMENT '角色 ID',
    `permission_id` bigint(20) NOT NULL COMMENT '权限 ID',
    `expire_time`   datetime            DEFAULT NULL COMMENT '授权过期时间，预留未来按时效授权能力',
    `del_flag`      bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`     bigint(20)          DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     bigint(20)          DEFAULT NULL COMMENT '更新人',
    `update_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission_del` (`role_id`, `permission_id`, `del_flag`),
    KEY `idx_permission_del` (`permission_id`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色权限关联表';

-- ========================================
-- 日志表
-- ========================================

DROP TABLE IF EXISTS `t_auth_login_log`;
CREATE TABLE `t_auth_login_log`
(
    `id`             bigint(20)  NOT NULL COMMENT '主键 ID',
    `user_id`        bigint(20)           DEFAULT NULL COMMENT '用户ID',
    `app_code`       varchar(32)          DEFAULT NULL COMMENT '应用编码',
    `username`       varchar(64)          DEFAULT NULL COMMENT '登录账号',
    `display_name`   varchar(128)         DEFAULT NULL COMMENT '展示名称',
    `event_type`     varchar(16) NOT NULL DEFAULT 'LOGIN' COMMENT '事件类型：LOGIN/LOGOUT',
    `login_result`   varchar(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '登录结果：SUCCESS/FAILURE',
    `fail_reason`    varchar(512)         DEFAULT NULL COMMENT '失败原因摘要',
    `login_ip`       varchar(128)         DEFAULT NULL COMMENT '登录 IP',
    `user_agent`     varchar(512)         DEFAULT NULL COMMENT '用户代理摘要',
    `device_type`    varchar(64)          DEFAULT NULL COMMENT '设备类型',
    `trace_id`       varchar(64)          DEFAULT NULL COMMENT '链路追踪ID',
    `request_id`     varchar(64)          DEFAULT NULL COMMENT '请求ID',
    `client_type`    varchar(64)          DEFAULT NULL COMMENT '客户端类型',
    `login_location` varchar(255)         DEFAULT NULL COMMENT '登录地点',
    `browser`        varchar(64)          DEFAULT NULL COMMENT '浏览器',
    `os`             varchar(64)          DEFAULT NULL COMMENT '操作系统',
    `status`         tinyint(1)  NOT NULL DEFAULT 1 COMMENT '状态：1-成功，0-失败',
    `msg`            varchar(255)         DEFAULT NULL COMMENT '提示信息',
    `login_time`     datetime             DEFAULT NULL COMMENT '登录时间',
    `logout_time`    datetime             DEFAULT NULL COMMENT '登出时间',
    `create_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `ext_json`       text COMMENT '受控扩展字段JSON',
    PRIMARY KEY (`id`),
    KEY `idx_login_app_time` (`app_code`, `create_time`),
    KEY `idx_login_status_time` (`status`, `create_time`),
    KEY `idx_login_user_time` (`user_id`, `create_time`),
    KEY `idx_login_username_time` (`username`, `create_time`),
    KEY `idx_login_event_time` (`event_type`, `create_time`),
    KEY `idx_login_result_time` (`login_result`, `create_time`),
    KEY `idx_login_ip_time` (`login_ip`, `create_time`),
    KEY `idx_login_trace_id` (`trace_id`),
    KEY `idx_login_request_id` (`request_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='登录审计日志表';
