DROP TABLE IF EXISTS `t_horticultural_plant_category`;
CREATE TABLE `t_horticultural_plant_category`
(
    `id`            bigint(20)  NOT NULL COMMENT '主键ID',
    `category_name` varchar(64) NOT NULL COMMENT '分类名称',
    `category_code` varchar(64) NOT NULL COMMENT '分类编码',
    `sort_num`      int(11)     NOT NULL DEFAULT 0 COMMENT '排序号',
    `status`        tinyint(1)  NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `description`   varchar(255)         DEFAULT NULL COMMENT '分类描述',
    `del_flag`      bigint(20)  NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`     bigint(20)           DEFAULT 0 COMMENT '创建人',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     bigint(20)           DEFAULT 0 COMMENT '更新人',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_horticultural_category_code_del` (`category_code`, `del_flag`),
    UNIQUE KEY `uk_horticultural_category_name_del` (`category_name`, `del_flag`),
    KEY `idx_horticultural_category_status_sort` (`status`, `sort_num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='花草苗木分类表';

DROP TABLE IF EXISTS `t_horticultural_plant_item`;
CREATE TABLE `t_horticultural_plant_item`
(
    `id`                     bigint(20)    NOT NULL COMMENT '主键ID',
    `publisher_user_id`      bigint(20)    NOT NULL COMMENT '发布会员ID',
    `category_id`            bigint(20)    NOT NULL COMMENT '分类ID',
    `category_name`          varchar(64)   NOT NULL COMMENT '分类名称快照',
    `title`                  varchar(64)   NOT NULL COMMENT '花草苗木名称',
    `cover_image_url`        varchar(255)  NOT NULL COMMENT '封面图地址',
    `suggested_retail_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '建议零售价',
    `unit`                   varchar(16)   NOT NULL COMMENT '价格单位',
    `short_description`      varchar(200)  NOT NULL COMMENT '简介',
    `detail_description`     text          NOT NULL COMMENT '详情描述',
    `province`               varchar(32)            DEFAULT NULL COMMENT '省份',
    `city`                   varchar(32)            DEFAULT NULL COMMENT '城市',
    `area`                   varchar(32)            DEFAULT NULL COMMENT '区县',
    `publish_status`         tinyint(1)    NOT NULL DEFAULT 1 COMMENT '发布状态：0-草稿，1-上架，2-下架',
    `view_count`             bigint(20)    NOT NULL DEFAULT 0 COMMENT '浏览量',
    `del_flag`               bigint(20)    NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`              bigint(20)             DEFAULT 0 COMMENT '创建人',
    `create_time`            datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`              bigint(20)             DEFAULT 0 COMMENT '更新人',
    `update_time`            datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_horticultural_item_publish_time` (`publish_status`, `create_time`),
    KEY `idx_horticultural_item_publisher_status_time` (`publisher_user_id`, `publish_status`, `create_time`),
    KEY `idx_horticultural_item_category_status_time` (`category_id`, `publish_status`, `create_time`),
    KEY `idx_horticultural_item_city_status_time` (`city`, `publish_status`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='花草苗木内容表';

DROP TABLE IF EXISTS `t_horticultural_plant_item_image`;
CREATE TABLE `t_horticultural_plant_item_image`
(
    `id`            bigint(20)   NOT NULL COMMENT '主键ID',
    `plant_item_id` bigint(20)   NOT NULL COMMENT '花草苗木内容ID',
    `image_url`     varchar(255) NOT NULL COMMENT '图片地址',
    `sort_num`      int(11)      NOT NULL DEFAULT 0 COMMENT '排序号',
    `cover_flag`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否封面：1-是，0-否',
    `del_flag`      bigint(20)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `create_by`     bigint(20)            DEFAULT 0 COMMENT '创建人',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     bigint(20)            DEFAULT 0 COMMENT '更新人',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_horticultural_item_image_item_sort` (`plant_item_id`, `sort_num`),
    KEY `idx_horticultural_item_image_cover` (`plant_item_id`, `cover_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='花草苗木内容图片表';
