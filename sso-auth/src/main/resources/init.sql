DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint(20) NOT NULL COMMENT '主键ID',
                            `username` varchar(64) NOT NULL COMMENT '账号',
                            `password` varchar(128) NOT NULL COMMENT '密码',
                            `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
                            `avatar` varchar(255) DEFAULT NULL COMMENT '头像',

                            `allow_concurrent_login` tinyint(1) DEFAULT 1 COMMENT '是否允许并发: 0-禁止, 1-允许',
                            `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
                            `status` tinyint(1) DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',

    -- 【新增】逻辑删除字段 (MyBatis Plus 默认: 0-未删, 1-已删)
                            `del_flag` tinyint(1) DEFAULT 0 COMMENT '删除标志: 0-存在, 1-删除',

                            `create_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
                            `update_by` bigint(20) DEFAULT NULL COMMENT '修改人ID',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',

                            PRIMARY KEY (`id`),
    -- 唯一索引依然保留，保证正常用户的唯一性
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表(PO)';

-- 初始化数据
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `allow_concurrent_login`, `status`, `del_flag`, `create_time`)
VALUES (1888888888888888888, 'admin', '123456', '管理员', 1, 1, 0, NOW());