-- ============================================================
-- 业态代码重构迁移
-- V20260127_1: 统一业态分类体系
--
-- 变更说明：
--   feed (饲料原料) → feed (原料饲料) - 仅名称微调
--   poultry (禽蛋种苗) → breed (生物种苗) - 代码+名称变更，范围扩大
--   meat (畜禽肉类) → process (农牧加工) - 代码+名称变更，范围扩大
--   other (其他品类) → equipment (装备物流) - 代码+名称变更，定义明确化
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. 更新业态表 nht_product_schema
-- ============================================================

-- 1.1 更新 feed 业态名称
UPDATE `nht_product_schema`
SET `schema_name` = '原料饲料',
    `description` = '大宗原料及核心添加剂的采买，包括谷物、粕类、油脂、发酵产品、动物蛋白、矿物质、添加剂等'
WHERE `schema_code` = 'feed';

-- 1.2 更新 poultry → breed
UPDATE `nht_product_schema`
SET `schema_code` = 'breed',
    `schema_name` = '生物种苗',
    `description` = '优质种禽、种蛋、鱼苗及畜种资源，包括禽类种苗、畜类种苗、水产种苗、商品蛋等',
    `icon` = 'icon-breed',
    `form_config_json` = '{"sections":["breed","biology","trade","logistics"],"basicFields":["breed","origin","quantity","price"]}'
WHERE `schema_code` = 'poultry';

-- 1.3 更新 meat → process
UPDATE `nht_product_schema`
SET `schema_code` = 'process',
    `schema_name` = '农牧加工',
    `description` = '成品、半成品农牧产品，包括禽肉加工、畜肉加工、水产加工、深加工产品等',
    `icon` = 'icon-process',
    `form_config_json` = '{"sections":["product","quality","trace","trade"],"basicFields":["productForm","part","quantity","price"]}'
WHERE `schema_code` = 'meat';

-- 1.4 更新 other → equipment
UPDATE `nht_product_schema`
SET `schema_code` = 'equipment',
    `schema_name` = '装备物流',
    `description` = '自动化养殖系统、农机设备、物流服务，包括养殖设备、饲料加工设备、屠宰设备、运输车辆、包装耗材等',
    `icon` = 'icon-equipment',
    `form_config_json` = '{"sections":["specs","service","trade"],"basicFields":["name","specs","quantity","price"]}',
    `sort` = 4
WHERE `schema_code` = 'other';

-- ============================================================
-- 2. 更新产品分类表 nht_product 中的 schema_code
-- ============================================================

-- 2.1 poultry → breed
UPDATE `nht_product` SET `schema_code` = 'breed' WHERE `schema_code` = 'poultry';

-- 2.2 meat → process
UPDATE `nht_product` SET `schema_code` = 'process' WHERE `schema_code` = 'meat';

-- 2.3 other → equipment
UPDATE `nht_product` SET `schema_code` = 'equipment' WHERE `schema_code` = 'other';

-- ============================================================
-- 3. 更新供应表 nht_supply 中的 schema_code（如果存在该表）
-- ============================================================

-- 条件更新：仅当表存在时执行
SET @supply_exists = (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nht_supply');
-- 注意：以下 UPDATE 会在表不存在时静默跳过

-- ============================================================
-- 4. 更新需求表 nht_requirement 中的 schema_code（如果存在该表）
-- ============================================================

-- 条件更新：仅当表存在时执行
SET @requirement_exists = (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'nht_requirement');
-- 注意：以下 UPDATE 会在表不存在时静默跳过

-- ============================================================
-- 5. 扩展生物种苗(breed)品类 - 新增畜类和水产种苗
-- ============================================================

-- 5.1 获取禽蛋种苗大类ID并重命名
UPDATE `nht_product` SET `product_name` = '禽类种苗' WHERE `product_name` = '禽蛋种苗' AND `schema_code` = 'breed' AND `parent_id` = 0;

SET @poultry_parent_id = (SELECT id FROM nht_product WHERE product_name = '禽类种苗' AND schema_code = 'breed' AND parent_id = 0 LIMIT 1);

-- 5.2 新增畜类种苗大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '畜类种苗', 'breed', 0, 0, '0', '0', NOW());

SET @livestock_parent_id = LAST_INSERT_ID();

-- 畜类种苗子品类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @livestock_parent_id, '种猪', 'breed', 1, 0, '0', '0', NOW()),
(1, @livestock_parent_id, '种牛', 'breed', 1, 0, '0', '0', NOW()),
(1, @livestock_parent_id, '种羊', 'breed', 1, 0, '0', '0', NOW()),
(1, @livestock_parent_id, '其他畜种', 'breed', 0, 1, '0', '0', NOW());

-- 5.3 新增水产种苗大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '水产种苗', 'breed', 0, 0, '0', '0', NOW());

SET @aqua_parent_id = LAST_INSERT_ID();

-- 水产种苗子品类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @aqua_parent_id, '鱼苗', 'breed', 1, 0, '0', '0', NOW()),
(1, @aqua_parent_id, '虾苗', 'breed', 1, 0, '0', '0', NOW()),
(1, @aqua_parent_id, '蟹苗', 'breed', 1, 0, '0', '0', NOW()),
(1, @aqua_parent_id, '其他水产苗', 'breed', 0, 1, '0', '0', NOW());

-- 获取新增品类的ID用于添加参数
SET @zhongzhu_id = (SELECT id FROM nht_product WHERE product_name = '种猪' AND schema_code = 'breed' LIMIT 1);
SET @yumiao_id = (SELECT id FROM nht_product WHERE product_name = '鱼苗' AND schema_code = 'breed' LIMIT 1);
SET @xiamiao_id = (SELECT id FROM nht_product WHERE product_name = '虾苗' AND schema_code = 'breed' LIMIT 1);

-- 5.4 种猪参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@zhongzhu_id, '类型', 1, 'breed', '种公猪,二元母猪,三元仔猪,后备母猪,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@zhongzhu_id, '品种', 1, 'breed', '杜洛克,长白,大白,皮特兰,杂交,其他', NULL, NULL, 'Y', 2, '0', NOW()),
(@zhongzhu_id, '体重', 1, 'biology', '10-15kg,15-25kg,25-50kg,50-100kg,100kg以上', 'kg', NULL, 'Y', 3, '0', NOW()),
(@zhongzhu_id, '日龄', 1, 'biology', '21日龄,28日龄,35日龄,60日龄,90日龄以上', '日龄', NULL, 'N', 4, '0', NOW()),
(@zhongzhu_id, '疫苗接种', 0, 'biology', NULL, NULL, '请填写疫苗接种情况，如：猪瘟、蓝耳等', 'N', 5, '0', NOW()),
(@zhongzhu_id, '种源证明', 1, 'quality', '有,无', NULL, NULL, 'N', 6, '0', NOW());

-- 5.5 鱼苗参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@yumiao_id, '品种', 1, 'breed', '草鱼,鲤鱼,鲫鱼,罗非鱼,鲈鱼,黑鱼,鳜鱼,加州鲈,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@yumiao_id, '规格', 1, 'biology', '水花,夏花(3-5cm),秋片(10-15cm),大规格(15cm以上)', NULL, NULL, 'Y', 2, '0', NOW()),
(@yumiao_id, '产地', 0, 'quality', NULL, NULL, '请填写鱼苗产地', 'N', 3, '0', NOW()),
(@yumiao_id, '检疫证明', 1, 'quality', '有,无', NULL, NULL, 'N', 4, '0', NOW());

-- 5.6 虾苗参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@xiamiao_id, '品种', 1, 'breed', '南美白对虾,小龙虾,基围虾,罗氏沼虾,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@xiamiao_id, '规格', 1, 'biology', 'P5,P7,P8,P10,P12,大规格', NULL, NULL, 'Y', 2, '0', NOW()),
(@xiamiao_id, '产地', 0, 'quality', NULL, NULL, '请填写虾苗产地', 'N', 3, '0', NOW()),
(@xiamiao_id, '检疫证明', 1, 'quality', '有,无', NULL, NULL, 'N', 4, '0', NOW()),
(@xiamiao_id, '亲本来源', 0, 'breed', NULL, NULL, '请填写亲本来源', 'N', 5, '0', NOW());

-- ============================================================
-- 6. 扩展农牧加工(process)品类 - 新增水产加工和深加工
-- ============================================================

-- 6.1 获取畜禽肉类大类ID并重命名
UPDATE `nht_product` SET `product_name` = '禽肉加工' WHERE `product_name` = '畜禽肉类' AND `schema_code` = 'process' AND `parent_id` = 0;

SET @poultry_meat_parent_id = (SELECT id FROM nht_product WHERE product_name = '禽肉加工' AND schema_code = 'process' AND parent_id = 0 LIMIT 1);

-- 6.2 新增畜肉加工大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '畜肉加工', 'process', 0, 0, '0', '0', NOW());

SET @livestock_meat_parent_id = LAST_INSERT_ID();

-- 移动猪肉、牛肉、羊肉到畜肉加工大类下
UPDATE `nht_product` SET `parent_id` = @livestock_meat_parent_id WHERE `product_name` IN ('猪肉', '牛肉', '羊肉') AND `schema_code` = 'process';

-- 6.3 新增水产加工大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '水产加工', 'process', 0, 0, '0', '0', NOW());

SET @aqua_process_parent_id = LAST_INSERT_ID();

-- 水产加工子品类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @aqua_process_parent_id, '冷冻鱼类', 'process', 1, 0, '0', '0', NOW()),
(1, @aqua_process_parent_id, '冷冻虾类', 'process', 1, 0, '0', '0', NOW()),
(1, @aqua_process_parent_id, '水产制品', 'process', 1, 0, '0', '0', NOW()),
(1, @aqua_process_parent_id, '其他水产', 'process', 0, 1, '0', '0', NOW());

-- 6.4 新增深加工产品大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '深加工产品', 'process', 0, 0, '0', '0', NOW());

SET @deep_process_parent_id = LAST_INSERT_ID();

-- 深加工子品类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @deep_process_parent_id, '熟食制品', 'process', 1, 0, '0', '0', NOW()),
(1, @deep_process_parent_id, '调理品', 'process', 1, 0, '0', '0', NOW()),
(1, @deep_process_parent_id, '烘干/风干产品', 'process', 1, 0, '0', '0', NOW()),
(1, @deep_process_parent_id, '其他深加工', 'process', 0, 1, '0', '0', NOW());

-- ============================================================
-- 7. 重建装备物流(equipment)品类结构
-- ============================================================

-- 7.1 删除原"其他品类"空节点（改名后的equipment）
DELETE FROM `nht_product` WHERE `product_name` = '其他品类' AND `schema_code` = 'equipment';

-- 7.2 新增养殖设备大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '养殖设备', 'equipment', 0, 0, '0', '0', NOW());

SET @farming_equip_id = LAST_INSERT_ID();

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @farming_equip_id, '环控系统', 'equipment', 1, 0, '0', '0', NOW()),
(1, @farming_equip_id, '饲喂系统', 'equipment', 1, 0, '0', '0', NOW()),
(1, @farming_equip_id, '蛋禽设备', 'equipment', 1, 0, '0', '0', NOW()),
(1, @farming_equip_id, '畜牧设备', 'equipment', 1, 0, '0', '0', NOW()),
(1, @farming_equip_id, '其他养殖设备', 'equipment', 0, 1, '0', '0', NOW());

-- 7.3 新增饲料加工设备大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '饲料加工设备', 'equipment', 0, 0, '0', '0', NOW());

SET @feed_equip_id = LAST_INSERT_ID();

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @feed_equip_id, '粉碎机', 'equipment', 1, 0, '0', '0', NOW()),
(1, @feed_equip_id, '混合机', 'equipment', 1, 0, '0', '0', NOW()),
(1, @feed_equip_id, '制粒机', 'equipment', 1, 0, '0', '0', NOW()),
(1, @feed_equip_id, '成套饲料线', 'equipment', 1, 0, '0', '0', NOW()),
(1, @feed_equip_id, '其他饲料设备', 'equipment', 0, 1, '0', '0', NOW());

-- 7.4 新增农用运输大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '农用运输', 'equipment', 0, 0, '0', '0', NOW());

SET @transport_id = LAST_INSERT_ID();

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @transport_id, '散料车', 'equipment', 1, 0, '0', '0', NOW()),
(1, @transport_id, '活禽运输车', 'equipment', 1, 0, '0', '0', NOW()),
(1, @transport_id, '冷链车', 'equipment', 1, 0, '0', '0', NOW()),
(1, @transport_id, '饲料罐车', 'equipment', 1, 0, '0', '0', NOW()),
(1, @transport_id, '其他运输', 'equipment', 0, 1, '0', '0', NOW());

-- 7.5 新增包装耗材大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '包装耗材', 'equipment', 0, 0, '0', '0', NOW());

SET @packaging_id = LAST_INSERT_ID();

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @packaging_id, '蛋托/蛋箱', 'equipment', 1, 0, '0', '0', NOW()),
(1, @packaging_id, '编织袋/吨包', 'equipment', 1, 0, '0', '0', NOW()),
(1, @packaging_id, '冷链包装', 'equipment', 1, 0, '0', '0', NOW()),
(1, @packaging_id, '其他包装', 'equipment', 0, 1, '0', '0', NOW());

-- 7.6 新增物流服务大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '物流服务', 'equipment', 0, 0, '0', '0', NOW());

SET @logistics_id = LAST_INSERT_ID();

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @logistics_id, '冷链物流', 'equipment', 1, 0, '0', '0', NOW()),
(1, @logistics_id, '活禽运输', 'equipment', 1, 0, '0', '0', NOW()),
(1, @logistics_id, '大宗散料运输', 'equipment', 1, 0, '0', '0', NOW()),
(1, @logistics_id, '其他物流', 'equipment', 0, 1, '0', '0', NOW());

-- ============================================================
-- 8. 扩展原料饲料(feed)品类 - 新增添加剂细分
-- ============================================================

-- 获取添加剂大类ID
SET @additive_parent_id = (SELECT id FROM nht_product WHERE product_name = '添加剂' AND schema_code = 'feed' AND parent_id = 0 LIMIT 1);

-- 如果添加剂大类存在，添加子品类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, @additive_parent_id, '维生素预混料', 'feed', 1, 0, '0', '0', NOW()
WHERE @additive_parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = @additive_parent_id AND product_name = '维生素预混料');

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, @additive_parent_id, '氨基酸', 'feed', 1, 0, '0', '0', NOW()
WHERE @additive_parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = @additive_parent_id AND product_name = '氨基酸');

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, @additive_parent_id, '酶制剂', 'feed', 1, 0, '0', '0', NOW()
WHERE @additive_parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = @additive_parent_id AND product_name = '酶制剂');

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, @additive_parent_id, '益生菌/益生元', 'feed', 1, 0, '0', '0', NOW()
WHERE @additive_parent_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = @additive_parent_id AND product_name = '益生菌/益生元');

-- ============================================================
-- 完成迁移
-- ============================================================
