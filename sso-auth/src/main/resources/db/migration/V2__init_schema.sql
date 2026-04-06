-- ========================================
-- SSO 缁熶竴璁よ瘉/閴存潈涓績骞冲彴 - 鏁版嵁搴撳垵濮嬪寲鑴氭湰 (V6.0 鏈€缁堢増)
-- 鐗规€э細
-- 1. 鍏ㄥ眬涓婚敭鍧囧懡鍚嶄负 id (瀹岀編鍏煎 MyBatis-Plus)
-- 2. 琛ラ綈鎵€鏈夎〃(闄ゆ棩蹇楀)鐨勫洓澶у璁″瓧娈碉紝鍚庣鐩存帴缁ф壙 BasePO
-- 3. 琛ラ綈搴旂敤缃戝叧闅旂銆佽嚜瀹氫箟鏁版嵁鏉冮檺銆佸瓧鍏歌〃浣撶郴
-- ========================================

-- ========================================
-- 鏍稿績瀹炰綋琛ㄥ尯鍩?(甯﹂€昏緫鍒犻櫎)
-- ========================================

-- ----------------------------
-- 1. 鎺ュ叆搴旂敤琛?(SSO澶氬簲鐢ㄩ殧绂诲熀搴?
-- ----------------------------
DROP TABLE IF EXISTS `t_sso_app`;
CREATE TABLE `t_sso_app`
(
    `id`          bigint(20)  NOT NULL COMMENT '涓婚敭ID',
    `app_code`    varchar(32) NOT NULL COMMENT '搴旂敤缂栫爜 (濡傦細mall, hr, sso)',
    `app_name`    varchar(64) NOT NULL COMMENT '搴旂敤鍚嶇О',
    `status`      tinyint(1)  NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-鍚敤锛?-鍋滅敤',
    `user_type`   tinyint(1)  NOT NULL DEFAULT 1 COMMENT '鐢ㄦ埛绫诲瀷锛?-鍐呴儴鍛樺伐(sys)锛?-澶栭儴浼氬憳(member)',
    `remark`      varchar(255)         DEFAULT NULL COMMENT '澶囨敞',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`   bigint(20)           DEFAULT NULL COMMENT '鍒涘缓浜?,
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `update_by`   bigint(20)           DEFAULT NULL COMMENT '鏇存柊浜?,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_code_del` (`app_code`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='鎺ュ叆搴旂敤閰嶇疆琛?;

-- ----------------------------
-- 2. 閮ㄩ棬缁勭粐鏋舵瀯琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_dept`;
CREATE TABLE `t_auth_dept`
(
    `id`          bigint(20)  NOT NULL COMMENT '閮ㄩ棬ID',
    `parent_id`   bigint(20)           DEFAULT 0 COMMENT '鐖堕儴闂↖D',
    `ancestors`   varchar(255)         DEFAULT '' COMMENT '绁栫骇鍒楄〃',
    `dept_name`   varchar(64) NOT NULL COMMENT '閮ㄩ棬鍚嶇О',
    `sort_num`    int(11)     NOT NULL DEFAULT 0 COMMENT '鏄剧ず椤哄簭',
    `status`      tinyint(1)  NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-鍋滅敤',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`   bigint(20)           DEFAULT NULL,
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)           DEFAULT NULL,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_sort` (`parent_id`, `sort_num`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='閮ㄩ棬缁勭粐琛?;

-- ----------------------------
-- 3. 宀椾綅閰嶇疆琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_post`;
CREATE TABLE `t_auth_post`
(
    `id`          bigint(20)  NOT NULL COMMENT '宀椾綅ID',
    `post_code`   varchar(64) NOT NULL COMMENT '宀椾綅缂栫爜',
    `post_name`   varchar(64) NOT NULL COMMENT '宀椾綅鍚嶇О',
    `sort_num`    int(11)     NOT NULL DEFAULT 0 COMMENT '鏄剧ず椤哄簭',
    `status`      tinyint(1)  NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-鍋滅敤',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`   bigint(20)           DEFAULT NULL,
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)           DEFAULT NULL,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_code_del` (`post_code`, `del_flag`),
    UNIQUE KEY `uk_post_name_del` (`post_name`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='宀椾綅琛?;

-- ----------------------------
-- 4. 缁熶竴鐢ㄦ埛琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user`;
CREATE TABLE `t_sys_user`
(
    `id`                     bigint(20)   NOT NULL COMMENT '涓婚敭ID',
    `username`               varchar(64)  NOT NULL COMMENT '鐧诲綍璐﹀彿',
    `password`               varchar(255) NOT NULL COMMENT '瀵嗙爜 (Argon2/BCrypt)',
    `user_type`              tinyint(4)   NOT NULL DEFAULT 1 COMMENT '1-绯荤粺鍛樺伐, 2-澶栭儴鎺ュ叆浜哄憳',
    `nickname`               varchar(64)           DEFAULT NULL COMMENT '鏄电О/濮撳悕',
    `avatar`                 varchar(255)          DEFAULT NULL COMMENT '澶村儚URL',
    `phone_number`           varchar(20)           DEFAULT NULL COMMENT '手机号',
    `email`                  varchar(128)          DEFAULT NULL COMMENT '閭',
    `sex`                    tinyint(1)            DEFAULT 0 COMMENT '0-鏈煡锛?-鐢凤紝2-濂?,
    `dept_id`                bigint(20)            DEFAULT NULL COMMENT '鎵€灞為儴闂↖D',
    `allow_concurrent_login` tinyint(1)   NOT NULL DEFAULT 1 COMMENT '鏄惁鍏佽骞跺彂鐧诲綍',
    `status`                 tinyint(1)   NOT NULL DEFAULT 1 COMMENT '璐﹀彿鐘舵€侊細1-姝ｅ父锛?-绂佺敤',
    `del_flag`               bigint(20)   NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`              bigint(20)            DEFAULT NULL,
    `create_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`              bigint(20)            DEFAULT NULL,
    `update_time`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username_del` (`username`, `del_flag`),
    UNIQUE KEY `uk_phone_del` (`phone_number`, `del_flag`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='缁熶竴鐢ㄦ埛琛?;

-- ----------------------------
-- 5. 绗笁鏂圭ぞ浼氬寲鐧诲綍鑱氬悎琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_user_social`;
CREATE TABLE `t_sys_user_social`
(
    `id`            bigint(20)   NOT NULL COMMENT '涓婚敭ID',
    `user_id`       bigint(20)   NOT NULL COMMENT '鍏宠仈鐨勫叏灞€鐢ㄦ埛ID',
    `identity_type` varchar(32)  NOT NULL COMMENT '绫诲瀷: WECHAT_OPEN, DINGTALK绛?,
    `identifier`    varchar(128) NOT NULL COMMENT '绗笁鏂瑰敮涓€鏍囪瘑',
    `credential`    varchar(255)          DEFAULT NULL COMMENT '绗笁鏂瑰嚟璇?,
    `del_flag`      bigint(20)   NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`     bigint(20)            DEFAULT NULL,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`     bigint(20)            DEFAULT NULL,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_ident_del` (`identity_type`, `identifier`, `del_flag`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='绗笁鏂规巿鏉冪櫥褰曠粦瀹氳〃';

-- ----------------------------
-- 6. C绔細鍛樿〃 (鏋佺畝銆佽交閲忋€佹姉楂樺苟鍙?
-- ----------------------------
DROP TABLE IF EXISTS `t_member_user`;
CREATE TABLE `t_member_user`
(
    `id`              bigint(20)  NOT NULL COMMENT '鍏ㄥ眬浼氬憳ID',
    `phone_number`    varchar(20)          DEFAULT NULL COMMENT '手机号，C 端核心主键',
    `password`        varchar(255)         DEFAULT NULL COMMENT '瀵嗙爜',
    `status`          tinyint(1)  NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-灏佺',
    `register_ip`     varchar(128)         DEFAULT NULL COMMENT '娉ㄥ唽IP',
    `last_login_time` datetime             DEFAULT NULL COMMENT '鏈€鍚庣櫥褰曟椂闂?,
    `del_flag`        bigint(20)  NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`       bigint(20)           DEFAULT 0 COMMENT '0浠ｈ〃鐢ㄦ埛鑷敞鍐?,
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`       bigint(20)           DEFAULT 0,
    `update_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone_del` (`phone_number`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='C绔粺涓€浼氬憳琛?;

-- ----------------------------
-- 7. C绔ぞ浼氬寲鐧诲綍缁戝畾琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_member_social`;
CREATE TABLE `t_member_social`
(
    `id`            bigint(20)   NOT NULL COMMENT '涓婚敭',
    `member_id`     bigint(20)   NOT NULL COMMENT '鍏宠仈 t_member_user 鐨?ID',
    `identity_type` varchar(32)  NOT NULL COMMENT '绫诲瀷: WX_MINI(灏忕▼搴?, WX_OPEN(PC鎵爜), APPLE...',
    `identifier`    varchar(128) NOT NULL COMMENT '绗笁鏂瑰敮涓€鏍囪瘑 (濡?OpenId)',
    `union_id`      varchar(255)          DEFAULT NULL COMMENT '鐢熸€佷簰閫氭爣璇?,
    `del_flag`      bigint(20)   NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`     bigint(20)            DEFAULT 0 COMMENT '0浠ｈ〃鐢ㄦ埛鑷敞鍐?,
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`     bigint(20)            DEFAULT 0,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_ident_del` (`identity_type`, `identifier`, `del_flag`),
    KEY `idx_union_id` (`union_id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='C绔涓夋柟璐﹀彿缁戝畾琛?;

-- ----------------------------
-- 8. 瑙掕壊琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_role`;
CREATE TABLE `t_auth_role`
(
    `id`          bigint(20)  NOT NULL COMMENT '涓婚敭ID',
    `app_code`    varchar(32) NOT NULL COMMENT '褰掑睘搴旂敤',
    `role_name`   varchar(64) NOT NULL COMMENT '瑙掕壊鍚嶇О',
    `role_code`   varchar(64) NOT NULL COMMENT '瑙掕壊缂栫爜',
    `data_scope`  tinyint(4)  NOT NULL DEFAULT 1 COMMENT '鑼冨洿锛?-鍏ㄩ儴, 2-鏈儴闂ㄥ強涓嬪睘, 3-鏈儴闂? 4-鏈汉, 5-鑷畾涔?,
    `status`      tinyint(1)  NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-鍋滅敤',
    `remark`      varchar(255)         DEFAULT NULL COMMENT '澶囨敞',
    `del_flag`    bigint(20)  NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`   bigint(20)           DEFAULT NULL,
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)           DEFAULT NULL,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_rcode_del` (`app_code`, `role_code`, `del_flag`),
    UNIQUE KEY `uk_app_rname_del` (`app_code`, `role_name`, `del_flag`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='绯荤粺瑙掕壊琛?;

-- ----------------------------
-- 9. 鏉冮檺琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_permission`;
CREATE TABLE `t_auth_permission`
(
    `id`             bigint(20)   NOT NULL COMMENT '涓婚敭ID',
    `app_code`       varchar(32)  NOT NULL COMMENT '褰掑睘搴旂敤',
    `name`           varchar(64)  NOT NULL COMMENT '鏉冮檺鍚嶇О',
    `identification` varchar(128) NOT NULL COMMENT '鏉冮檺鏍囪瘑',
    `parent_id`      bigint(20)            DEFAULT 0 COMMENT '鐖剁骇ID',
    `type`           tinyint(4)   NOT NULL DEFAULT 0 COMMENT '-1-骞冲彴 0-妯″潡 1-鑿滃崟 2-鎸夐挳 3-鎺ュ彛',
    `path`           varchar(255)          DEFAULT NULL COMMENT '鍓嶇璺敱',
    `com_path`       varchar(255)          DEFAULT NULL COMMENT '鍓嶇缁勪欢',
    `icon_str`       varchar(64)           DEFAULT NULL COMMENT '鍥炬爣',
    `display_no`     int(11)      NOT NULL DEFAULT 0 COMMENT '鎺掑簭',
    `is_frame`       tinyint(1)   NOT NULL DEFAULT 0 COMMENT '鏄惁澶栭摼',
    `status`         tinyint(1)   NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-鍋滅敤',
    `del_flag`       bigint(20)   NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`      bigint(20)            DEFAULT NULL,
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`      bigint(20)            DEFAULT NULL,
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_ident_del` (`app_code`, `identification`, `del_flag`),
    KEY `idx_app_parent_disp` (`app_code`, `parent_id`, `display_no`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='鏉冮檺鑿滃崟琛?;


-- ========================================
-- 瀛楀吀涓庡熀纭€鏁版嵁鍖哄煙 (甯﹂€昏緫鍒犻櫎)
-- ========================================

-- ----------------------------
-- 10. 瀛楀吀绫诲瀷琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_dict_type`;
CREATE TABLE `t_sys_dict_type`
(
    `id`          bigint(20)   NOT NULL COMMENT '涓婚敭ID',
    `dict_name`   varchar(100) NOT NULL COMMENT '瀛楀吀鍚嶇О (濡傦細鐢ㄦ埛鎬у埆)',
    `dict_type`   varchar(100) NOT NULL COMMENT '瀛楀吀绫诲瀷 (濡傦細sys_user_sex)',
    `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-鍋滅敤',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '澶囨敞',
    `del_flag`    bigint(20)   NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`   bigint(20)            DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)            DEFAULT NULL,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_del` (`dict_type`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='瀛楀吀绫诲瀷琛?;

-- ----------------------------
-- 11. 瀛楀吀鏁版嵁琛?
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_dict_data`;
CREATE TABLE `t_sys_dict_data`
(
    `id`          bigint(20)   NOT NULL COMMENT '涓婚敭ID',
    `dict_sort`   int(11)      NOT NULL DEFAULT 0 COMMENT '瀛楀吀鎺掑簭',
    `dict_label`  varchar(100) NOT NULL COMMENT '瀛楀吀鏍囩 (濡傦細鐢?',
    `dict_value`  varchar(100) NOT NULL COMMENT '瀛楀吀閿€?(濡傦細1)',
    `dict_type`   varchar(100) NOT NULL COMMENT '鍏宠仈绫诲瀷',
    `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-鍋滅敤',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '澶囨敞',
    `del_flag`    bigint(20)   NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`   bigint(20)            DEFAULT NULL,
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)            DEFAULT NULL,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='瀛楀吀鏁版嵁琛?;


-- ========================================
-- 鏄犲皠琛ㄥ尯鍩燂細鍏ㄩ儴銆愮墿鐞嗗垹闄ゃ€戯紝琛ュ叏瀹¤瀛楁
-- ========================================

-- ----------------------------
-- 12. 鐢ㄦ埛 - 鎺ュ叆搴旂敤鏄犲皠琛?(搴旂敤鐧诲綍鐧藉悕鍗?
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_user_app`;
CREATE TABLE `t_auth_user_app`
(
    `id`          bigint(20)  NOT NULL COMMENT '涓婚敭ID',
    `user_id`     bigint(20)  NOT NULL COMMENT '鐢ㄦ埛ID',
    `app_code`    varchar(32) NOT NULL COMMENT '鍏佽璁块棶鐨勫簲鐢ㄧ紪鐮?,
    `create_by`   bigint(20)           DEFAULT NULL,
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)           DEFAULT NULL,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_app` (`user_id`, `app_code`),
    KEY `idx_app_code` (`app_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='鐢ㄦ埛搴旂敤鐧藉悕鍗曟槧灏勮〃';

-- ----------------------------
-- 13. 瑙掕壊 - 閮ㄩ棬鏄犲皠琛?(鑷畾涔夋暟鎹潈闄?
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_role_dept`;
CREATE TABLE `t_auth_role_dept`
(
    `id`          bigint(20) NOT NULL COMMENT '涓婚敭ID',
    `role_id`     bigint(20) NOT NULL COMMENT '瑙掕壊ID',
    `dept_id`     bigint(20) NOT NULL COMMENT '鍙闂殑閮ㄩ棬ID',
    `create_by`   bigint(20)          DEFAULT NULL,
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)          DEFAULT NULL,
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_dept` (`role_id`, `dept_id`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='瑙掕壊鏁版嵁鏉冮檺鍏宠仈琛?;

-- ----------------------------
-- 14. 鏄犲皠琛? 鐢ㄦ埛 - 瑙掕壊
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_user_role`;
CREATE TABLE `t_auth_user_role`
(
    `id`          bigint(20) NOT NULL COMMENT '涓婚敭ID',
    `user_id`     bigint(20) NOT NULL COMMENT '鐢ㄦ埛ID',
    `role_id`     bigint(20) NOT NULL COMMENT '瑙掕壊ID',
    `del_flag`    bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_by`   bigint(20)          DEFAULT NULL,
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)          DEFAULT NULL,
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='鐢ㄦ埛瑙掕壊鍏宠仈琛?;

-- ----------------------------
-- 15. 鏄犲皠琛? 鐢ㄦ埛 - 宀椾綅
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_user_post`;
CREATE TABLE `t_auth_user_post`
(
    `id`          bigint(20) NOT NULL COMMENT '涓婚敭ID',
    `user_id`     bigint(20) NOT NULL COMMENT '鐢ㄦ埛ID',
    `post_id`     bigint(20) NOT NULL COMMENT '宀椾綅ID',
    `create_by`   bigint(20)          DEFAULT NULL,
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`   bigint(20)          DEFAULT NULL,
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='鐢ㄦ埛宀椾綅鍏宠仈琛?;

-- ----------------------------
-- 16. 鏄犲皠琛? 瑙掕壊 - 鏉冮檺
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_role_permission`;
CREATE TABLE `t_auth_role_permission`
(
    `id`            bigint(20) NOT NULL COMMENT '涓婚敭ID',
    `role_id`       bigint(20) NOT NULL COMMENT '瑙掕壊ID',
    `permission_id` bigint(20) NOT NULL COMMENT '鏉冮檺ID',
    `del_flag`      bigint(20) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_by`     bigint(20)          DEFAULT NULL,
    `create_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`     bigint(20)          DEFAULT NULL,
    `update_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='瑙掕壊鏉冮檺鍏宠仈琛?;


-- ========================================
-- 鏃ュ織琛ㄥ尯鍩燂細鏃犻渶 update 瀛楁锛岀函杩藉姞
-- ========================================

-- ----------------------------
-- 17. 鏃ュ織琛? 鐧诲綍璁块棶鏃ュ織
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_login_log`;
CREATE TABLE `t_auth_login_log`
(
    `id`             bigint(20) NOT NULL COMMENT '涓婚敭ID',
    `app_code`       varchar(32)         DEFAULT NULL COMMENT '搴旂敤缂栫爜',
    `username`       varchar(64)         DEFAULT NULL COMMENT '鐧诲綍璐﹀彿',
    `login_ip`       varchar(128)        DEFAULT NULL COMMENT '鐧诲綍IP',
    `login_location` varchar(255)        DEFAULT NULL COMMENT '鐧诲綍鍦扮偣',
    `browser`        varchar(64)         DEFAULT NULL COMMENT '娴忚鍣?,
    `os`             varchar(64)         DEFAULT NULL COMMENT '鎿嶄綔绯荤粺',
    `status`         tinyint(1) NOT NULL DEFAULT 1 COMMENT '1-鎴愬姛锛?-澶辫触',
    `msg`            varchar(255)        DEFAULT NULL COMMENT '鎻愮ず娑堟伅',
    `login_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鐧诲綍鏃堕棿',
    PRIMARY KEY (`id`),
    KEY `idx_app_time` (`app_code`, `login_time`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='绯荤粺鐧诲綍鏃ュ織琛?;

-- ----------------------------
-- 18. 鏃ュ織琛? 鏍稿績鎿嶄綔瀹¤鏃ュ織
-- ----------------------------
DROP TABLE IF EXISTS `t_auth_operate_log`;
CREATE TABLE `t_auth_operate_log`
(
    `id`              bigint(20) NOT NULL COMMENT '涓婚敭',
    `app_code`        varchar(32)         DEFAULT NULL COMMENT '鎿嶄綔搴旂敤',
    `module`          varchar(64)         DEFAULT NULL COMMENT '鍔熻兘妯″潡(濡?瑙掕壊绠＄悊)',
    `operate_type`    varchar(32)         DEFAULT NULL COMMENT '鎿嶄綔绫诲瀷(INSERT/UPDATE/DELETE)',
    `operate_user_id` bigint(20)          DEFAULT NULL COMMENT '鎿嶄綔浜篒D',
    `operate_ip`      varchar(128)        DEFAULT NULL COMMENT '鎿嶄綔IP',
    `request_url`     varchar(255)        DEFAULT NULL COMMENT '璇锋眰URL',
    `request_method`  varchar(10)         DEFAULT NULL COMMENT '璇锋眰鏂瑰紡(GET/POST)',
    `cost_time`       int(11)             DEFAULT 0 COMMENT '鑰楁椂(姣)',
    `request_param`   text COMMENT '璇锋眰鍙傛暟',
    `json_result`     text COMMENT '杩斿洖鍙傛暟',
    `status`          tinyint(1) NOT NULL DEFAULT 1 COMMENT '鐘舵€侊細1-姝ｅ父锛?-寮傚父',
    `error_msg`       text COMMENT '閿欒鏍?,
    `operate_time`    datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鎿嶄綔鏃堕棿',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`operate_user_id`, `operate_time`),
    KEY `idx_module` (`module`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='鎿嶄綔瀹¤鏃ュ織琛?;

-- ----------------------------
-- 19. 绯荤粺鍙傛暟閰嶇疆琛?(K-V 缁撴瀯)
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_config`;
CREATE TABLE `t_sys_config`
(
    `id`           bigint(20)   NOT NULL COMMENT '鍙傛暟涓婚敭',
    `config_name`  varchar(100) NOT NULL COMMENT '鍙傛暟鍚嶇О (濡傦細鐢ㄦ埛鍒濆瀵嗙爜)',
    `config_key`   varchar(100) NOT NULL COMMENT '鍙傛暟閿悕 (濡傦細sys.user.initPassword)',
    `config_value` varchar(500) NOT NULL COMMENT '鍙傛暟閿€?(濡傦細123456)',
    `config_type`  tinyint(1)   NOT NULL DEFAULT 0 COMMENT '绯荤粺鍐呯疆锛?-鏄?涓嶅彲鍒?锛?-鍚?,
    `remark`       varchar(255)          DEFAULT NULL COMMENT '澶囨敞',
    `del_flag`     bigint(20)   NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
    `create_by`    bigint(20)            DEFAULT NULL,
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`    bigint(20)            DEFAULT NULL,
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key_del` (`config_key`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='绯荤粺鍙傛暟閰嶇疆琛?;
