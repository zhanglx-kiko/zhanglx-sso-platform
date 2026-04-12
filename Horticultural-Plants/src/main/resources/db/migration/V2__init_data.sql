INSERT INTO `t_horticultural_plant_category`
(`id`, `category_name`, `category_code`, `sort_num`, `status`, `description`, `del_flag`, `create_by`, `update_by`)
VALUES (1810000000000000001, '观花绿植', 'flowering-greens', 1, 1, '适合门店与家庭日常陈列的观花盆栽', 0, 0, 0),
       (1810000000000000002, '庭院树苗', 'courtyard-trees', 2, 1, '适合庭院和园区景观种植的树苗', 0, 0, 0),
       (1810000000000000003, '耐阴观叶', 'shade-foliage', 3, 1, '适合室内和半阴环境的观叶植物', 0, 0, 0),
       (1810000000000000004, '香草盆栽', 'fragrant-herbs', 4, 1, '适合阳台与零售礼盒的香草盆栽', 0, 0, 0),
       (1810000000000000005, '阳台果苗', 'balcony-fruit', 5, 1, '适合社区与家庭阳台的小型果苗', 0, 0, 0);

INSERT INTO `t_horticultural_plant_item`
(`id`, `publisher_user_id`, `category_id`, `category_name`, `title`, `cover_image_url`, `suggested_retail_price`, `unit`,
 `short_description`, `detail_description`, `province`, `city`, `area`, `publish_status`, `view_count`, `del_flag`,
 `create_by`, `update_by`)
VALUES (1820000000000000001, 1800000000000000001, 1810000000000000001, '观花绿植', '茶花精品盆栽',
        '/apis/v1/horticultural-plants/public/assets/seeds/camellia-a.svg', 188.00, '盆',
        '花苞饱满、株型紧凑，适合门店零售与节令礼赠。',
        '精选茶花中苗，冠幅均衡，开品稳定。建议陈列在散射光区域，保持盆土微润，适合花店、园艺集合店和节礼场景作为高颜值零售款。',
        '浙江省', '杭州市', '余杭区', 1, 126, 0, 1800000000000000001, 1800000000000000001),
       (1820000000000000002, 1800000000000000002, 1810000000000000002, '庭院树苗', '金桂庭院苗',
        '/apis/v1/horticultural-plants/public/assets/seeds/osmanthus-a.svg', 268.00, '株',
        '株型挺拔，根系完整，适合庭院入口与社区景观零售。',
        '选用两年生金桂苗，分枝均匀、香味明显，适合作为庭院香化与社区绿化零售商品。建议移栽后做好缓苗管理，保持土壤透气。',
        '江苏省', '苏州市', '吴中区', 1, 89, 0, 1800000000000000002, 1800000000000000002),
       (1820000000000000003, 1800000000000000001, 1810000000000000003, '耐阴观叶', '波士顿蕨悬挂盆',
        '/apis/v1/horticultural-plants/public/assets/seeds/fern-a.svg', 59.00, '盆',
        '叶片丰盈、成型快，适合阳台与咖啡店角落陈列。',
        '采用中号吊盆规格，叶量饱满，适合零售端做轻养护绿植款。推荐摆放在通风且半阴的位置，定期喷水提升叶面质感。',
        '四川省', '成都市', '温江区', 1, 214, 0, 1800000000000000001, 1800000000000000001);

INSERT INTO `t_horticultural_plant_item_image`
(`id`, `plant_item_id`, `image_url`, `sort_num`, `cover_flag`, `del_flag`, `create_by`, `update_by`)
VALUES (1830000000000000001, 1820000000000000001, '/apis/v1/horticultural-plants/public/assets/seeds/camellia-a.svg', 1, 1,
        0, 1800000000000000001, 1800000000000000001),
       (1830000000000000002, 1820000000000000001, '/apis/v1/horticultural-plants/public/assets/seeds/camellia-b.svg', 2, 0,
        0, 1800000000000000001, 1800000000000000001),
       (1830000000000000003, 1820000000000000002, '/apis/v1/horticultural-plants/public/assets/seeds/osmanthus-a.svg', 1, 1,
        0, 1800000000000000002, 1800000000000000002),
       (1830000000000000004, 1820000000000000002, '/apis/v1/horticultural-plants/public/assets/seeds/osmanthus-b.svg', 2, 0,
        0, 1800000000000000002, 1800000000000000002),
       (1830000000000000005, 1820000000000000003, '/apis/v1/horticultural-plants/public/assets/seeds/fern-a.svg', 1, 1, 0,
        1800000000000000001, 1800000000000000001),
       (1830000000000000006, 1820000000000000003, '/apis/v1/horticultural-plants/public/assets/seeds/fern-b.svg', 2, 0, 0,
        1800000000000000001, 1800000000000000001);
