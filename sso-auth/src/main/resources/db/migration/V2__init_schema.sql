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
    `id`              bigint(20) NOT NULL COMMENT '主键 ID',
    `phone_number`    varchar(20)         DEFAULT NULL COMMENT '手机号',
    `password`        varchar(255)        DEFAULT NULL COMMENT '密码哈希',
    `status`          tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `register_ip`     varchar(128)        DEFAULT NULL COMMENT '注册 IP',
    `last_login_time` datetime            DEFAULT NULL COMMENT '最后登录时间',
    `del_flag`        bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`       bigint(20)          DEFAULT 0 COMMENT '创建人',
    `create_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       bigint(20)          DEFAULT 0 COMMENT '更新人',
    `update_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
    `id`           bigint(20)   NOT NULL COMMENT '主键 ID',
    `config_name`  varchar(100) NOT NULL COMMENT '参数名称',
    `config_key`   varchar(100) NOT NULL COMMENT '参数键',
    `config_value` varchar(500) NOT NULL COMMENT '参数值',
    `config_type`  tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否系统内置：1-是，0-否',
    `remark`       varchar(255)          DEFAULT NULL COMMENT '备注',
    `del_flag`     bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`    bigint(20)            DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    bigint(20)            DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key_del` (`config_key`, `del_flag`)
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
-- ========================================
-- 初始化基础数据
-- ========================================

INSERT INTO `t_sso_app`
(`id`, `app_code`, `app_name`, `status`, `user_type`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1000000000000000001, 'sso', 'SSO 认证中心', 1, 1, '系统默认应用', 0, 0, 0);

INSERT INTO `t_auth_dept`
(`id`, `parent_id`, `ancestors`, `dept_name`, `sort_num`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1100000000000000001, 0, '0', '平台管理部', 1, 1, 0, 0, 0);

INSERT INTO `t_auth_post`
(`id`, `post_code`, `post_name`, `sort_num`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1200000000000000001, 'ADMIN', '系统管理员', 1, 1, 0, 0, 0);

INSERT INTO `t_sys_user`
(`id`, `username`, `password`, `user_type`, `nickname`, `avatar`, `phone_number`, `email`, `sex`, `dept_id`,
 `allow_concurrent_login`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1300000000000000001,
        'zhanglx',
        '$argon2id$v=19$m=16,t=3,p=4$77+9dSF5CDUFae+/vWJhKe+/ve+/vUnvv71G77+9CQ3vv71t77+977+977+9MzkU77+9eRw$OHuy8PypTUMvizp9luVk5Gbw1N/MBfoIwVcW0EkRjYo',
        1,
        '超级管理员',
        NULL,
        '13800138000',
        'admin@zhanglx.com',
        1,
        1100000000000000001,
        1,
        1,
        0,
        1300000000000000001,
        1300000000000000001);

INSERT INTO `t_auth_role`
(`id`, `app_code`, `role_name`, `role_code`, `data_scope`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1400000000000000001,
        'sso',
        '超级管理员',
        'ROLE_SUPER_ADMIN',
        1,
        1,
        '系统默认超级管理员角色',
        0,
        1300000000000000001,
        1300000000000000001);

INSERT INTO `t_auth_user_role`
    (`id`, `user_id`, `role_id`, `del_flag`, `create_by`, `update_by`)
VALUES (1500000000000000001, 1300000000000000001, 1400000000000000001, 0, 1300000000000000001, 1300000000000000001);

INSERT INTO `t_auth_user_app`
    (`id`, `user_id`, `app_code`, `del_flag`, `create_by`, `update_by`)
VALUES (1500000000000000002, 1300000000000000001, 'sso', 0, 1300000000000000001, 1300000000000000001);

INSERT INTO `t_auth_user_post`
    (`id`, `user_id`, `post_id`, `del_flag`, `create_by`, `update_by`)
VALUES (1500000000000000003, 1300000000000000001, 1200000000000000001, 0, 1300000000000000001, 1300000000000000001);

-- 字典
-- ====================== 1. 扩展字典类型 ======================
INSERT INTO `t_sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1600000000000000003, '证件类型', 'sys_id_card_type', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000004, '学历', 'sys_education', 1, '系统内置字典', 0, 1300000000000000001, 1300000000000000001),
       (1600000000000000005, '婚姻状况', 'sys_marital_status', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000006, '政治面貌', 'sys_political_status', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000007, '请假类型', 'sys_leave_type', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000008, '审批状态', 'sys_approval_status', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000009, '支付方式', 'sys_payment_method', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000010, '物流状态', 'sys_logistics_status', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000011, '客户等级', 'sys_customer_level', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001),
       (1600000000000000012, '产品状态', 'sys_product_status', 1, '系统内置字典', 0, 1300000000000000001,
        1300000000000000001);

-- ====================== 2. 扩展字典数据 ======================
INSERT INTO `t_sys_dict_data` (`id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `remark`,
                               `del_flag`, `create_by`, `update_by`)
VALUES
-- 证件类型 (sys_id_card_type)
(1610000000000000006, 1, '身份证', '1', 'sys_id_card_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000007, 2, '护照', '2', 'sys_id_card_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000008, 3, '军官证', '3', 'sys_id_card_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000009, 4, '驾驶证', '4', 'sys_id_card_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000010, 5, '港澳通行证', '5', 'sys_id_card_type', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),

-- 学历 (sys_education)
(1610000000000000011, 1, '小学', '1', 'sys_education', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000012, 2, '初中', '2', 'sys_education', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000013, 3, '高中', '3', 'sys_education', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000014, 4, '大专', '4', 'sys_education', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000015, 5, '本科', '5', 'sys_education', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000016, 6, '硕士研究生', '6', 'sys_education', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000017, 7, '博士研究生', '7', 'sys_education', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),

-- 婚姻状况 (sys_marital_status)
(1610000000000000018, 1, '未婚', '1', 'sys_marital_status', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000019, 2, '已婚', '2', 'sys_marital_status', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000020, 3, '离异', '3', 'sys_marital_status', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000021, 4, '丧偶', '4', 'sys_marital_status', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),

-- 政治面貌 (sys_political_status)
(1610000000000000022, 1, '中共党员', '1', 'sys_political_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000023, 2, '中共预备党员', '2', 'sys_political_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000024, 3, '共青团员', '3', 'sys_political_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000025, 4, '群众', '4', 'sys_political_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000026, 5, '民革党员', '5', 'sys_political_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000027, 6, '民盟盟员', '6', 'sys_political_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),

-- 请假类型 (sys_leave_type)
(1610000000000000028, 1, '事假', '1', 'sys_leave_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000029, 2, '病假', '2', 'sys_leave_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000030, 3, '年假', '3', 'sys_leave_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000031, 4, '婚假', '4', 'sys_leave_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000032, 5, '产假', '5', 'sys_leave_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000033, 6, '陪产假', '6', 'sys_leave_type', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),

-- 审批状态 (sys_approval_status)
(1610000000000000034, 1, '待提交', '0', 'sys_approval_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000035, 2, '待审批', '1', 'sys_approval_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000036, 3, '已通过', '2', 'sys_approval_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000037, 4, '已拒绝', '3', 'sys_approval_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000038, 5, '已撤回', '4', 'sys_approval_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),

-- 支付方式 (sys_payment_method)
(1610000000000000039, 1, '现金', '1', 'sys_payment_method', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000040, 2, '支付宝', '2', 'sys_payment_method', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000041, 3, '微信支付', '3', 'sys_payment_method', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000042, 4, '银行卡', '4', 'sys_payment_method', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000043, 5, '信用卡', '5', 'sys_payment_method', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),

-- 物流状态 (sys_logistics_status)
(1610000000000000044, 1, '待发货', '1', 'sys_logistics_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000045, 2, '已发货', '2', 'sys_logistics_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000046, 3, '运输中', '3', 'sys_logistics_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000047, 4, '已签收', '4', 'sys_logistics_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000048, 5, '已拒收', '5', 'sys_logistics_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),

-- 客户等级 (sys_customer_level)
(1610000000000000049, 1, '普通客户', '1', 'sys_customer_level', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000050, 2, 'VIP客户', '2', 'sys_customer_level', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000051, 3, 'SVIP客户', '3', 'sys_customer_level', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000052, 4, '钻石客户', '4', 'sys_customer_level', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),

-- 产品状态 (sys_product_status)
(1610000000000000053, 1, '草稿', '0', 'sys_product_status', 1, '系统内置', 0, 1300000000000000001, 1300000000000000001),
(1610000000000000054, 2, '待审核', '1', 'sys_product_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000055, 3, '已上架', '2', 'sys_product_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000056, 4, '已下架', '3', 'sys_product_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001),
(1610000000000000057, 5, '已停产', '4', 'sys_product_status', 1, '系统内置', 0, 1300000000000000001,
 1300000000000000001);

INSERT INTO `t_sys_config`
(`id`, `config_name`, `config_key`, `config_value`, `config_type`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1700000000000000001,
        '系统用户默认密码',
        'sys.user.initPassword',
        '123456',
        1,
        '系统内置参数',
        0,
        1300000000000000001,
        1300000000000000001);

-- 权限菜单 (t_auth_permission)
-- 当前前端真实组件路径：
-- DashboardView
-- system/auth/user/index
-- system/auth/role/index
-- system/auth/permission/index
-- system/auth/dept/index
-- system/auth/post/index
-- system/auth/app/index
-- system/auth/config/index
-- system/auth/dict/index
INSERT INTO `t_auth_permission`
(`id`, `name`, `identification`, `parent_id`, `identity_lineage`, `com_path`, `path`, `icon_str`, `display_no`,
 `is_frame`, `type`, `remark`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3000000000000000001, '系统平台', 'system', 0, 'system', NULL, '/system', 'Setting', 2, 0, -1, '系统级平台入口',
        1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000000002, '仪表盘', 'dashboard', 0, 'dashboard', 'DashboardView', '/dashboard', 'PieChart', 1, 0, 1,
        '首页仪表盘菜单', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000000010, '认证与授权', 'system:auth', 3000000000000000001, 'system.auth', NULL, '/system/auth',
        'Lock', 1, 0, 0, '认证与授权管理模块', 1, 0, 1300000000000000001, 1300000000000000001),

       (3000000000000000100, '用户管理', 'system:auth:user', 3000000000000000010, 'system.auth.user',
        'system/auth/user/index', '/system/auth/user', 'User', 1, 0, 1, '系统用户管理菜单', 1, 0, 1300000000000000001,
        1300000000000000001),
       (3000000000000000101, '角色管理', 'system:auth:role', 3000000000000000010, 'system.auth.role',
        'system/auth/role/index', '/system/auth/role', 'UserFilled', 2, 0, 1, '角色管理菜单', 1, 0, 1300000000000000001,
        1300000000000000001),
       (3000000000000000102, '权限管理', 'system:auth:permission', 3000000000000000010, 'system.auth.permission',
        'system/auth/permission/index', '/system/auth/permission', 'List', 3, 0, 1, '权限管理菜单', 1, 0,
        1300000000000000001, 1300000000000000001),
       (3000000000000000104, '部门管理', 'system:auth:dept', 3000000000000000010, 'system.auth.dept',
        'system/auth/dept/index', '/system/auth/dept', 'Folder', 4, 0, 1, '部门管理菜单', 1, 0, 1300000000000000001,
        1300000000000000001),
       (3000000000000000105, '岗位管理', 'system:auth:post', 3000000000000000010, 'system.auth.post',
        'system/auth/post/index', '/system/auth/post', 'Tickets', 5, 0, 1, '岗位管理菜单', 1, 0, 1300000000000000001,
        1300000000000000001),
       (3000000000000000103, '应用管理', 'system:auth:app', 3000000000000000010, 'system.auth.app',
        'system/auth/app/index', '/system/auth/app', 'Grid', 6, 0, 1, '应用管理菜单', 1, 0, 1300000000000000001,
        1300000000000000001),
       (3000000000000000107, '参数管理', 'system:auth:config', 3000000000000000010, 'system.auth.config',
        'system/auth/config/index', '/system/auth/config', 'Setting', 7, 0, 1, '系统参数管理菜单', 1, 0,
        1300000000000000001, 1300000000000000001),
       (3000000000000000106, '字典管理', 'system:auth:dict', 3000000000000000010, 'system.auth.dict',
        'system/auth/dict/index', '/system/auth/dict', 'Document', 8, 0, 1, '字典管理菜单', 1, 0, 1300000000000000001,
        1300000000000000001),

       (3000000000000001000, '新增用户', 'user:add', 3000000000000000100, 'system.auth.user.add', NULL, NULL, NULL, 1,
        0, 2, '新增系统用户', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001001, '修改用户', 'user:edit', 3000000000000000100, 'system.auth.user.edit', NULL, NULL, NULL, 2,
        0, 2, '修改系统用户', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001002, '删除用户', 'user:remove', 3000000000000000100, 'system.auth.user.remove', NULL, NULL,
        NULL, 3, 0, 2, '删除单个用户', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001003, '查询用户', 'user:list', 3000000000000000100, 'system.auth.user.list', NULL, NULL, NULL, 4,
        0, 2, '分页查询用户', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001004, '查看用户', 'user:view', 3000000000000000100, 'system.auth.user.view', NULL, NULL, NULL, 5,
        0, 2, '查看用户详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001005, '重置密码', 'user:reset', 3000000000000000100, 'system.auth.user.reset', NULL, NULL, NULL,
        6, 0, 2, '重置用户密码', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001006, '状态变更', 'user:status', 3000000000000000100, 'system.auth.user.status', NULL, NULL,
        NULL, 7, 0, 2, '启停用用户', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001007, '批量删除用户', 'user:batch-remove', 3000000000000000100, 'system.auth.user.batch.remove',
        NULL, NULL, NULL, 8, 0, 2, '批量删除用户', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001008, '绑定应用', 'user:assign-app', 3000000000000000100, 'system.auth.user.assign.app', NULL,
        NULL, NULL, 9, 0, 2, '维护用户应用关系', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001009, '绑定岗位', 'user:assign-post', 3000000000000000100, 'system.auth.user.assign.post', NULL,
        NULL, NULL, 10, 0, 2, '维护用户岗位关系', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001010, '绑定角色', 'user:assign-role', 3000000000000000100, 'system.auth.user.assign.role', NULL,
        NULL, NULL, 11, 0, 2, '维护用户角色关系', 1, 0, 1300000000000000001, 1300000000000000001),

       (3000000000000001100, '新增角色', 'role:add', 3000000000000000101, 'system.auth.role.add', NULL, NULL, NULL, 1,
        0, 2, '新增角色', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001101, '修改角色', 'role:edit', 3000000000000000101, 'system.auth.role.edit', NULL, NULL, NULL, 2,
        0, 2, '修改角色', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001102, '删除角色', 'role:remove', 3000000000000000101, 'system.auth.role.remove', NULL, NULL,
        NULL, 3, 0, 2, '删除角色', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001103, '查询角色', 'role:list', 3000000000000000101, 'system.auth.role.list', NULL, NULL, NULL, 4,
        0, 2, '分页查询角色', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001104, '查看角色', 'role:view', 3000000000000000101, 'system.auth.role.view', NULL, NULL, NULL, 5,
        0, 2, '查看角色详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001105, '绑定用户', 'role:bind-user', 3000000000000000101, 'system.auth.role.bind.user', NULL,
        NULL, NULL, 6, 0, 2, '绑定角色用户', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001106, '分配权限', 'role:assign-permission', 3000000000000000101,
        'system.auth.role.assign.permission', NULL, NULL, NULL, 7, 0, 2, '分配角色权限', 1, 0, 1300000000000000001,
        1300000000000000001),
       (3000000000000001107, '分配部门', 'role:assign-dept', 3000000000000000101, 'system.auth.role.assign.dept', NULL,
        NULL, NULL, 8, 0, 2, '分配数据范围部门', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001108, '状态变更', 'role:status', 3000000000000000101, 'system.auth.role.status', NULL, NULL,
        NULL, 9, 0, 2, '启停用角色', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001109, '批量删除角色', 'role:batch-remove', 3000000000000000101, 'system.auth.role.batch.remove',
        NULL, NULL, NULL, 10, 0, 2, '批量删除角色', 1, 0, 1300000000000000001, 1300000000000000001),

       (3000000000000001200, '新增权限', 'permission:add', 3000000000000000102, 'system.auth.permission.add', NULL,
        NULL, NULL, 1, 0, 2, '新增权限', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001201, '修改权限', 'permission:edit', 3000000000000000102, 'system.auth.permission.edit', NULL,
        NULL, NULL, 2, 0, 2, '修改权限', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001202, '删除权限', 'permission:remove', 3000000000000000102, 'system.auth.permission.remove',
        NULL, NULL, NULL, 3, 0, 2, '删除权限', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001203, '查询权限', 'permission:list', 3000000000000000102, 'system.auth.permission.list', NULL,
        NULL, NULL, 4, 0, 2, '查询权限树', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001204, '查看权限', 'permission:view', 3000000000000000102, 'system.auth.permission.view', NULL,
        NULL, NULL, 5, 0, 2, '查看权限详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001205, '状态变更', 'permission:status', 3000000000000000102, 'system.auth.permission.status',
        NULL, NULL, NULL, 6, 0, 2, '启停用权限', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001206, '导入权限', 'permission:import', 3000000000000000102, 'system.auth.permission.import',
        NULL, NULL, NULL, 7, 0, 2, '导入权限数据', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001207, '导出权限', 'permission:export', 3000000000000000102, 'system.auth.permission.export',
        NULL, NULL, NULL, 8, 0, 2, '导出权限数据', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001208, '批量删除权限', 'permission:batch-remove', 3000000000000000102,
        'system.auth.permission.batch.remove', NULL, NULL, NULL, 9, 0, 2, '批量删除权限', 1, 0, 1300000000000000001,
        1300000000000000001),

       (3000000000000001300, '新增应用', 'app:add', 3000000000000000103, 'system.auth.app.add', NULL, NULL, NULL, 1, 0,
        2, '新增应用', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001301, '修改应用', 'app:edit', 3000000000000000103, 'system.auth.app.edit', NULL, NULL, NULL, 2,
        0, 2, '修改应用', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001302, '删除应用', 'app:remove', 3000000000000000103, 'system.auth.app.remove', NULL, NULL, NULL,
        3, 0, 2, '删除应用', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001303, '查询应用', 'app:list', 3000000000000000103, 'system.auth.app.list', NULL, NULL, NULL, 4,
        0, 2, '查询应用', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001304, '查看应用', 'app:view', 3000000000000000103, 'system.auth.app.view', NULL, NULL, NULL, 5,
        0, 2, '查看应用详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001305, '状态变更', 'app:status', 3000000000000000103, 'system.auth.app.status', NULL, NULL, NULL,
        6, 0, 2, '启停用应用', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001306, '批量删除应用', 'app:batch-remove', 3000000000000000103, 'system.auth.app.batch.remove',
        NULL, NULL, NULL, 7, 0, 2, '批量删除应用', 1, 0, 1300000000000000001, 1300000000000000001),

       (3000000000000001400, '新增部门', 'dept:add', 3000000000000000104, 'system.auth.dept.add', NULL, NULL, NULL, 1,
        0, 2, '新增部门', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001401, '修改部门', 'dept:edit', 3000000000000000104, 'system.auth.dept.edit', NULL, NULL, NULL, 2,
        0, 2, '修改部门', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001402, '删除部门', 'dept:remove', 3000000000000000104, 'system.auth.dept.remove', NULL, NULL,
        NULL, 3, 0, 2, '删除部门', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001403, '查询部门', 'dept:list', 3000000000000000104, 'system.auth.dept.list', NULL, NULL, NULL, 4,
        0, 2, '查询部门', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001404, '查看部门', 'dept:view', 3000000000000000104, 'system.auth.dept.view', NULL, NULL, NULL, 5,
        0, 2, '查看部门详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001405, '状态变更', 'dept:status', 3000000000000000104, 'system.auth.dept.status', NULL, NULL,
        NULL, 6, 0, 2, '启停用部门', 1, 0, 1300000000000000001, 1300000000000000001),

       (3000000000000001500, '新增岗位', 'post:add', 3000000000000000105, 'system.auth.post.add', NULL, NULL, NULL, 1,
        0, 2, '新增岗位', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001501, '修改岗位', 'post:edit', 3000000000000000105, 'system.auth.post.edit', NULL, NULL, NULL, 2,
        0, 2, '修改岗位', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001502, '删除岗位', 'post:remove', 3000000000000000105, 'system.auth.post.remove', NULL, NULL,
        NULL, 3, 0, 2, '删除岗位', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001503, '查询岗位', 'post:list', 3000000000000000105, 'system.auth.post.list', NULL, NULL, NULL, 4,
        0, 2, '查询岗位', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001504, '查看岗位', 'post:view', 3000000000000000105, 'system.auth.post.view', NULL, NULL, NULL, 5,
        0, 2, '查看岗位详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001505, '状态变更', 'post:status', 3000000000000000105, 'system.auth.post.status', NULL, NULL,
        NULL, 6, 0, 2, '启停用岗位', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001506, '批量删除岗位', 'post:batch-remove', 3000000000000000105, 'system.auth.post.batch.remove',
        NULL, NULL, NULL, 7, 0, 2, '批量删除岗位', 1, 0, 1300000000000000001, 1300000000000000001),

       (3000000000000001600, '新增字典类型', 'dict:type:add', 3000000000000000106, 'system.auth.dict.type.add', NULL,
        NULL, NULL, 1, 0, 2, '新增字典类型', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001601, '修改字典类型', 'dict:type:edit', 3000000000000000106, 'system.auth.dict.type.edit', NULL,
        NULL, NULL, 2, 0, 2, '修改字典类型', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001602, '删除字典类型', 'dict:type:remove', 3000000000000000106, 'system.auth.dict.type.remove',
        NULL, NULL, NULL, 3, 0, 2, '删除字典类型', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001603, '查询字典类型', 'dict:type:list', 3000000000000000106, 'system.auth.dict.type.list', NULL,
        NULL, NULL, 4, 0, 2, '查询字典类型', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001604, '查看字典类型', 'dict:type:view', 3000000000000000106, 'system.auth.dict.type.view', NULL,
        NULL, NULL, 5, 0, 2, '查看字典类型详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001605, '状态变更', 'dict:type:status', 3000000000000000106, 'system.auth.dict.type.status', NULL,
        NULL, NULL, 6, 0, 2, '启停用字典类型', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001606, '新增字典数据', 'dict:data:add', 3000000000000000106, 'system.auth.dict.data.add', NULL,
        NULL, NULL, 7, 0, 2, '新增字典数据', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001607, '修改字典数据', 'dict:data:edit', 3000000000000000106, 'system.auth.dict.data.edit', NULL,
        NULL, NULL, 8, 0, 2, '修改字典数据', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001608, '删除字典数据', 'dict:data:remove', 3000000000000000106, 'system.auth.dict.data.remove',
        NULL, NULL, NULL, 9, 0, 2, '删除字典数据', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001609, '查询字典数据', 'dict:data:list', 3000000000000000106, 'system.auth.dict.data.list', NULL,
        NULL, NULL, 10, 0, 2, '查询字典数据', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001610, '查看字典数据', 'dict:data:view', 3000000000000000106, 'system.auth.dict.data.view', NULL,
        NULL, NULL, 11, 0, 2, '查看字典数据详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001611, '状态变更', 'dict:data:status', 3000000000000000106, 'system.auth.dict.data.status', NULL,
        NULL, NULL, 12, 0, 2, '启停用字典数据', 1, 0, 1300000000000000001, 1300000000000000001),

       (3000000000000001700, '新增参数', 'config:add', 3000000000000000107, 'system.auth.config.add', NULL, NULL, NULL,
        1, 0, 2, '新增系统参数', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001701, '修改参数', 'config:edit', 3000000000000000107, 'system.auth.config.edit', NULL, NULL,
        NULL, 2, 0, 2, '修改系统参数', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001702, '删除参数', 'config:remove', 3000000000000000107, 'system.auth.config.remove', NULL, NULL,
        NULL, 3, 0, 2, '删除系统参数', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001703, '查询参数', 'config:list', 3000000000000000107, 'system.auth.config.list', NULL, NULL,
        NULL, 4, 0, 2, '分页查询系统参数', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001704, '查看参数', 'config:view', 3000000000000000107, 'system.auth.config.view', NULL, NULL,
        NULL, 5, 0, 2, '查看系统参数详情', 1, 0, 1300000000000000001, 1300000000000000001);

INSERT INTO `t_auth_role_permission`
(`id`, `role_id`, `permission_id`, `del_flag`, `create_by`, `update_by`)
SELECT 1900000000000000000 + ROW_NUMBER() OVER (ORDER BY `id`) AS `id`,
       1400000000000000001                                     AS `role_id`,
       `id`                                                    AS `permission_id`,
       0                                                       AS `del_flag`,
       1300000000000000001                                     AS `create_by`,
       1300000000000000001                                     AS `update_by`
FROM `t_auth_permission`
WHERE `del_flag` = 0;


-- 日志审计菜单与权限
INSERT INTO `t_auth_permission`
(`id`, `name`, `identification`, `parent_id`, `identity_lineage`, `com_path`, `path`, `icon_str`, `display_no`,
 `is_frame`, `type`, `remark`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3000000000000000108, '日志审计', 'system:auth:log', 3000000000000000010, 'system.auth.log',
        'system/auth/log/index', '/system/auth/log', 'DocumentChecked', 9, 0, 1, '登录日志与操作日志审计菜单', 1, 0,
        1300000000000000001, 1300000000000000001),
       (3000000000000001800, '查询登录日志', 'login-log:list', 3000000000000000108, 'system.auth.log.login.list', NULL,
        NULL, NULL, 1, 0, 2, '分页查询登录日志', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001801, '查看登录日志', 'login-log:view', 3000000000000000108, 'system.auth.log.login.view', NULL,
        NULL, NULL, 2, 0, 2, '查看登录日志详情', 1, 0, 1300000000000000001, 1300000000000000001),
       (3000000000000001802, '查询操作日志', 'operation-log:list', 3000000000000000108,
        'system.auth.log.operation.list', NULL, NULL, NULL, 3, 0, 2, '分页查询操作日志', 1, 0, 1300000000000000001,
        1300000000000000001),
       (3000000000000001803, '查看操作日志', 'operation-log:view', 3000000000000000108,
        'system.auth.log.operation.view', NULL, NULL, NULL, 4, 0, 2, '查看操作日志详情', 1, 0, 1300000000000000001,
        1300000000000000001);

INSERT INTO `t_auth_role_permission`
(`id`, `role_id`, `permission_id`, `del_flag`, `create_by`, `update_by`)
VALUES (1900000000000001800, 1400000000000000001, 3000000000000000108, 0, 1300000000000000001, 1300000000000000001),
       (1900000000000001801, 1400000000000000001, 3000000000000001800, 0, 1300000000000000001, 1300000000000000001),
       (1900000000000001802, 1400000000000000001, 3000000000000001801, 0, 1300000000000000001, 1300000000000000001),
       (1900000000000001803, 1400000000000000001, 3000000000000001802, 0, 1300000000000000001, 1300000000000000001),
       (1900000000000001804, 1400000000000000001, 3000000000000001803, 0, 1300000000000000001, 1300000000000000001);
