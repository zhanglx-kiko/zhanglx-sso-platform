-- 运行时配置数据库迁移脚本
-- 用途：将默认密码、短信渠道凭证、Argon2 Pepper 迁移到 t_sys_config
-- 说明：适用于已经存在旧版 t_sys_config 的环境，脚本可重复执行

SET @schema_name = DATABASE();

SET @column_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_sys_config'
      AND column_name = 'config_group'
);
SET @ddl = IF(@column_exists = 0,
              'ALTER TABLE `t_sys_config` ADD COLUMN `config_group` varchar(64) NOT NULL DEFAULT ''default'' COMMENT ''参数分组'' AFTER `config_value`',
              'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_sys_config'
      AND column_name = 'sensitive_flag'
);
SET @ddl = IF(@column_exists = 0,
              'ALTER TABLE `t_sys_config` ADD COLUMN `sensitive_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否敏感参数：1-是，0-否'' AFTER `config_group`',
              'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 't_sys_config'
      AND column_name = 'status'
);
SET @ddl = IF(@column_exists = 0,
              'ALTER TABLE `t_sys_config` ADD COLUMN `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''状态：1-启用，0-停用'' AFTER `sensitive_flag`',
              'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 't_sys_config'
      AND index_name = 'idx_config_group_status_del'
);
SET @ddl = IF(@index_exists = 0,
              'ALTER TABLE `t_sys_config` ADD INDEX `idx_config_group_status_del` (`config_group`, `status`, `del_flag`)',
              'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 先逻辑清理旧键，避免 default.password 与 sys.user.initPassword 双来源并存
UPDATE `t_sys_config`
SET `del_flag`    = `id`,
    `update_by`   = 1300000000000000001,
    `update_time` = CURRENT_TIMESTAMP
WHERE `config_key` = 'sys.user.initPassword'
  AND `del_flag` = 0;

INSERT INTO `t_sys_config`
(`id`, `config_name`, `config_key`, `config_value`, `config_group`, `sensitive_flag`, `status`, `config_type`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1700000000000000001, '默认重置密码', 'default.password', '123456', 'auth.security', 1, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000002, '短信通用户标识', 'sms-chinese.uid', 'zhanglx', 'sms.chinese', 0, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000003, '短信通接口密钥', 'sms-chinese.key', 'd41d8cd98f00b204e980', 'sms.chinese', 1, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000004, '短信通发送地址', 'sms-chinese.send-url', 'https://utf8api.smschinese.cn/', 'sms.chinese', 0, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000005, '阿里云短信 AccessKeyId', 'sms.aliyun.access-key-id', 'LTAI5tRZ4ykafWhYjdrM13aE', 'sms.aliyun', 0, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000006, '阿里云短信 AccessKeySecret', 'sms.aliyun.access-key-secret', 'rSKxXB5NkLEE9Vggf7idw0Wdapdsmf', 'sms.aliyun', 1, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000007, '阿里云短信 Endpoint', 'sms.aliyun.endpoint', 'dypnsapi.aliyuncs.com', 'sms.aliyun', 0, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000008, '阿里云短信 Region', 'sms.aliyun.region', 'cn-qingdao', 'sms.aliyun', 0, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000009, '阿里云短信签名', 'sms.aliyun.sign-name', '速通互联验证平台', 'sms.aliyun', 0, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001),
       (1700000000000000010, 'Argon2 Pepper', 'security.argon2.pepper', 'pR7$S2kF9!aG5dJ3hZ8', 'security.argon2', 1, 1, 1, '系统内置运行时配置', 0, 1300000000000000001, 1300000000000000001)
ON DUPLICATE KEY UPDATE
    `config_name`    = VALUES(`config_name`),
    `config_value`   = VALUES(`config_value`),
    `config_group`   = VALUES(`config_group`),
    `sensitive_flag` = VALUES(`sensitive_flag`),
    `status`         = VALUES(`status`),
    `config_type`    = VALUES(`config_type`),
    `remark`         = VALUES(`remark`),
    `update_by`      = VALUES(`update_by`),
    `update_time`    = CURRENT_TIMESTAMP;
