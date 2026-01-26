-- ============================================================
-- 产品业态系统升级
-- V20260126_1: 新增业态表，扩展产品分类表
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 1. 创建业态表 nht_product_schema
-- ============================================================
CREATE TABLE IF NOT EXISTS `nht_product_schema` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '业态ID',
  `schema_code` varchar(32) NOT NULL COMMENT '业态代码：feed, poultry, meat, other',
  `schema_name` varchar(64) NOT NULL COMMENT '业态名称',
  `description` varchar(255) DEFAULT NULL COMMENT '业态描述',
  `form_config_json` text COMMENT '表单布局配置JSON',
  `icon` varchar(128) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` char(1) DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schema_code` (`schema_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品业态表';

-- 插入业态数据
INSERT INTO `nht_product_schema` (`schema_code`, `schema_name`, `description`, `form_config_json`, `icon`, `sort`) VALUES
('feed', '饲料原料', '谷物、油料粕类、油脂、蛋白原料、添加剂等饲料加工原料',
 '{"sections":["basic","params","logistics"],"basicFields":["origin","quantity","price","packaging"]}',
 'icon-feed', 1),
('poultry', '禽蛋种苗', '种蛋、鸡苗、鸭苗、鹅苗、商品蛋等禽类产品',
 '{"sections":["breed","biology","trade","logistics"],"basicFields":["breed","origin","quantity","price"]}',
 'icon-poultry', 2),
('meat', '畜禽肉类', '鸡肉、猪肉、牛肉、羊肉等屠宰加工产品',
 '{"sections":["product","quality","trace","trade"],"basicFields":["productForm","part","quantity","price"]}',
 'icon-meat', 3),
('other', '其他品类', '自定义发布，支持任意农产品类型',
 '{"sections":["description","custom","trade"],"basicFields":["categoryName","description","quantity","price"]}',
 'icon-other', 99);

-- ============================================================
-- 2. 扩展产品分类表 nht_product
-- ============================================================
ALTER TABLE `nht_product`
  ADD COLUMN `schema_code` varchar(32) DEFAULT 'feed' COMMENT '所属业态' AFTER `product_name`,
  ADD COLUMN `has_params` tinyint DEFAULT 1 COMMENT '是否有预设参数：0否 1是' AFTER `schema_code`,
  ADD COLUMN `allow_custom_name` tinyint DEFAULT 0 COMMENT '是否允许自定义名称：0否 1是' AFTER `has_params`;

-- 更新现有数据：所有现有分类归入 feed 业态
UPDATE `nht_product` SET `schema_code` = 'feed', `has_params` = 1, `allow_custom_name` = 0;

-- ============================================================
-- 3. 扩展参数表 nht_product_parameters
-- ============================================================
ALTER TABLE `nht_product_parameters`
  ADD COLUMN `param_group` varchar(32) DEFAULT 'quality' COMMENT '参数分组：quality/biology/logistics/trade' AFTER `param_type`,
  ADD COLUMN `unit` varchar(32) DEFAULT NULL COMMENT '单位' AFTER `param_group`,
  ADD COLUMN `placeholder` varchar(128) DEFAULT NULL COMMENT '输入提示' AFTER `unit`;

-- 更新现有参数的分组（全部归入 quality）
UPDATE `nht_product_parameters` SET `param_group` = 'quality' WHERE `param_group` IS NULL OR `param_group` = '';

-- ============================================================
-- 4. 新增禽蛋种苗品类 (poultry)
-- ============================================================

-- 4.1 添加禽蛋种苗大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '禽蛋种苗', 'poultry', 0, 0, '0', '0', NOW());

SET @poultry_parent_id = LAST_INSERT_ID();

-- 4.2 添加禽蛋种苗子品类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @poultry_parent_id, '种蛋', 'poultry', 1, 0, '0', '0', NOW()),
(1, @poultry_parent_id, '鸡苗', 'poultry', 1, 0, '0', '0', NOW()),
(1, @poultry_parent_id, '鸭苗', 'poultry', 1, 0, '0', '0', NOW()),
(1, @poultry_parent_id, '鹅苗', 'poultry', 1, 0, '0', '0', NOW()),
(1, @poultry_parent_id, '商品蛋', 'poultry', 1, 0, '0', '0', NOW()),
(1, @poultry_parent_id, '其他禽类', 'poultry', 0, 1, '0', '0', NOW());

-- 获取子品类ID用于添加参数
SET @zhongdan_id = (SELECT id FROM nht_product WHERE product_name = '种蛋' AND schema_code = 'poultry' LIMIT 1);
SET @jimiao_id = (SELECT id FROM nht_product WHERE product_name = '鸡苗' AND schema_code = 'poultry' LIMIT 1);
SET @yamiao_id = (SELECT id FROM nht_product WHERE product_name = '鸭苗' AND schema_code = 'poultry' LIMIT 1);
SET @emiao_id = (SELECT id FROM nht_product WHERE product_name = '鹅苗' AND schema_code = 'poultry' LIMIT 1);
SET @shangpindan_id = (SELECT id FROM nht_product WHERE product_name = '商品蛋' AND schema_code = 'poultry' LIMIT 1);

-- 4.3 种蛋参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@zhongdan_id, '品种', 1, 'breed', '海兰褐,京红1号,罗曼粉,罗曼灰,农大3号,海兰白,尼克粉,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@zhongdan_id, '种鸡周龄', 1, 'biology', '25-30周,30-40周,40-50周,50-60周,60周以上', '周', NULL, 'Y', 2, '0', NOW()),
(@zhongdan_id, '受精率承诺', 1, 'quality', '≥95%,≥92%,≥90%,≥85%,不承诺', NULL, NULL, 'N', 3, '0', NOW()),
(@zhongdan_id, '存储天数', 1, 'quality', '当日蛋,≤3天,≤5天,≤7天,≤10天', NULL, NULL, 'Y', 4, '0', NOW()),
(@zhongdan_id, '疫苗接种', 0, 'biology', NULL, NULL, '请填写疫苗接种情况，如：新城疫、禽流感等', 'N', 5, '0', NOW()),
(@zhongdan_id, '蛋重规格', 1, 'quality', '55-60g,60-65g,65-70g,混合规格', 'g', NULL, 'N', 6, '0', NOW());

-- 4.4 鸡苗参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@jimiao_id, '品种', 1, 'breed', '817肉杂,白羽肉鸡(AA),白羽肉鸡(罗斯308),黄羽肉鸡,三黄鸡,麻鸡,蛋鸡苗,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@jimiao_id, '日龄', 1, 'biology', '1日龄,7日龄,14日龄,21日龄,28日龄,其他', '日龄', NULL, 'Y', 2, '0', NOW()),
(@jimiao_id, '疫苗接种', 1, 'biology', '马立克+新城疫,仅马立克,马立克+法氏囊,全程免疫,未接种', NULL, NULL, 'Y', 3, '0', NOW()),
(@jimiao_id, '成活率承诺', 1, 'quality', '≥98%,≥96%,≥95%,≥92%,不承诺', NULL, NULL, 'N', 4, '0', NOW()),
(@jimiao_id, '母代来源', 0, 'breed', NULL, NULL, '请填写父母代种源信息', 'N', 5, '0', NOW()),
(@jimiao_id, '出雏日期', 0, 'biology', NULL, NULL, '请填写出雏日期', 'N', 6, '0', NOW());

-- 4.5 鸭苗参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@yamiao_id, '品种', 1, 'breed', '樱桃谷鸭,北京鸭,番鸭,麻鸭,蛋鸭苗,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@yamiao_id, '日龄', 1, 'biology', '1日龄,7日龄,14日龄,21日龄,其他', '日龄', NULL, 'Y', 2, '0', NOW()),
(@yamiao_id, '疫苗接种', 1, 'biology', '鸭病毒性肝炎,鸭瘟,全程免疫,未接种', NULL, NULL, 'Y', 3, '0', NOW()),
(@yamiao_id, '成活率承诺', 1, 'quality', '≥98%,≥96%,≥95%,不承诺', NULL, NULL, 'N', 4, '0', NOW());

-- 4.6 鹅苗参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@emiao_id, '品种', 1, 'breed', '三花鹅,狮头鹅,皖西白鹅,浙东白鹅,朗德鹅,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@emiao_id, '日龄', 1, 'biology', '1日龄,7日龄,14日龄,21日龄,其他', '日龄', NULL, 'Y', 2, '0', NOW()),
(@emiao_id, '疫苗接种', 1, 'biology', '小鹅瘟,鹅副粘病毒,全程免疫,未接种', NULL, NULL, 'Y', 3, '0', NOW()),
(@emiao_id, '成活率承诺', 1, 'quality', '≥98%,≥96%,≥95%,不承诺', NULL, NULL, 'N', 4, '0', NOW());

-- 4.7 商品蛋参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@shangpindan_id, '蛋品类型', 1, 'breed', '鸡蛋,鸭蛋,鹅蛋,鹌鹑蛋,其他', NULL, NULL, 'Y', 1, '0', NOW()),
(@shangpindan_id, '养殖方式', 1, 'breed', '笼养,散养,有机,其他', NULL, NULL, 'N', 2, '0', NOW()),
(@shangpindan_id, '蛋重规格', 1, 'quality', '小号(45-50g),中号(50-55g),大号(55-60g),特大(60g以上),混合', NULL, NULL, 'Y', 3, '0', NOW()),
(@shangpindan_id, '新鲜度', 1, 'quality', '当日蛋,≤3天,≤7天,≤15天', NULL, NULL, 'Y', 4, '0', NOW()),
(@shangpindan_id, '包装方式', 1, 'logistics', '散装,托盘,纸箱,蛋托+纸箱', NULL, NULL, 'N', 5, '0', NOW());

-- ============================================================
-- 5. 新增畜禽肉类品类 (meat)
-- ============================================================

-- 5.1 添加畜禽肉类大类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '畜禽肉类', 'meat', 0, 0, '0', '0', NOW());

SET @meat_parent_id = LAST_INSERT_ID();

-- 5.2 添加畜禽肉类子品类
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, @meat_parent_id, '白羽鸡肉', 'meat', 1, 0, '0', '0', NOW()),
(1, @meat_parent_id, '黄羽鸡肉', 'meat', 1, 0, '0', '0', NOW()),
(1, @meat_parent_id, '鸭肉', 'meat', 1, 0, '0', '0', NOW()),
(1, @meat_parent_id, '猪肉', 'meat', 1, 0, '0', '0', NOW()),
(1, @meat_parent_id, '牛肉', 'meat', 1, 0, '0', '0', NOW()),
(1, @meat_parent_id, '羊肉', 'meat', 1, 0, '0', '0', NOW()),
(1, @meat_parent_id, '其他肉类', 'meat', 0, 1, '0', '0', NOW());

-- 获取子品类ID
SET @baiyuji_id = (SELECT id FROM nht_product WHERE product_name = '白羽鸡肉' AND schema_code = 'meat' LIMIT 1);
SET @huangyuji_id = (SELECT id FROM nht_product WHERE product_name = '黄羽鸡肉' AND schema_code = 'meat' LIMIT 1);
SET @yarou_id = (SELECT id FROM nht_product WHERE product_name = '鸭肉' AND schema_code = 'meat' LIMIT 1);
SET @zhurou_id = (SELECT id FROM nht_product WHERE product_name = '猪肉' AND schema_code = 'meat' LIMIT 1);
SET @niurou_id = (SELECT id FROM nht_product WHERE product_name = '牛肉' AND schema_code = 'meat' LIMIT 1);
SET @yangrou_id = (SELECT id FROM nht_product WHERE product_name = '羊肉' AND schema_code = 'meat' LIMIT 1);

-- 5.3 白羽鸡肉参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@baiyuji_id, '产品形态', 1, 'quality', '白条鸡,分割品,深加工品', NULL, NULL, 'Y', 1, '0', NOW()),
(@baiyuji_id, '温度状态', 1, 'quality', '冷鲜(0-4℃),冷冻(-18℃),热鲜', NULL, NULL, 'Y', 2, '0', NOW()),
(@baiyuji_id, '部位', 1, 'quality', '整鸡,鸡胸,鸡腿,鸡翅,鸡爪,鸡架,鸡杂,副产品', NULL, NULL, 'N', 3, '0', NOW()),
(@baiyuji_id, '规格重量', 1, 'quality', '1.0-1.2kg,1.2-1.5kg,1.5-1.8kg,1.8-2.0kg,2.0kg以上', NULL, NULL, 'N', 4, '0', NOW()),
(@baiyuji_id, '屠宰日期', 0, 'trade', NULL, NULL, '请填写屠宰日期', 'Y', 5, '0', NOW()),
(@baiyuji_id, '保质期', 1, 'quality', '7天,15天,30天,3个月,6个月,12个月', NULL, NULL, 'Y', 6, '0', NOW()),
(@baiyuji_id, '品牌/厂家', 0, 'trade', NULL, NULL, '请填写品牌或屠宰厂名称', 'N', 7, '0', NOW());

-- 5.4 黄羽鸡肉参数模板（与白羽类似，部分差异）
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@huangyuji_id, '产品形态', 1, 'quality', '白条鸡,分割品,活禽', NULL, NULL, 'Y', 1, '0', NOW()),
(@huangyuji_id, '温度状态', 1, 'quality', '冷鲜(0-4℃),冷冻(-18℃),活禽', NULL, NULL, 'Y', 2, '0', NOW()),
(@huangyuji_id, '品种', 1, 'breed', '三黄鸡,清远鸡,文昌鸡,广西三黄,其他', NULL, NULL, 'N', 3, '0', NOW()),
(@huangyuji_id, '日龄/重量', 0, 'quality', NULL, NULL, '如：90日龄/1.5kg', 'N', 4, '0', NOW()),
(@huangyuji_id, '屠宰日期', 0, 'trade', NULL, NULL, '请填写屠宰日期', 'Y', 5, '0', NOW()),
(@huangyuji_id, '保质期', 1, 'quality', '7天,15天,30天,3个月,6个月', NULL, NULL, 'Y', 6, '0', NOW());

-- 5.5 鸭肉参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@yarou_id, '产品形态', 1, 'quality', '白条鸭,分割品,深加工品', NULL, NULL, 'Y', 1, '0', NOW()),
(@yarou_id, '温度状态', 1, 'quality', '冷鲜(0-4℃),冷冻(-18℃)', NULL, NULL, 'Y', 2, '0', NOW()),
(@yarou_id, '品种', 1, 'breed', '樱桃谷鸭,北京鸭,番鸭,麻鸭,其他', NULL, NULL, 'N', 3, '0', NOW()),
(@yarou_id, '部位', 1, 'quality', '整鸭,鸭胸,鸭腿,鸭翅,鸭掌,鸭脖,鸭杂,副产品', NULL, NULL, 'N', 4, '0', NOW()),
(@yarou_id, '屠宰日期', 0, 'trade', NULL, NULL, '请填写屠宰日期', 'Y', 5, '0', NOW()),
(@yarou_id, '保质期', 1, 'quality', '7天,15天,30天,6个月,12个月', NULL, NULL, 'Y', 6, '0', NOW());

-- 5.6 猪肉参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@zhurou_id, '产品形态', 1, 'quality', '白条猪,分割品,深加工品', NULL, NULL, 'Y', 1, '0', NOW()),
(@zhurou_id, '温度状态', 1, 'quality', '冷鲜(0-4℃),冷冻(-18℃),热鲜', NULL, NULL, 'Y', 2, '0', NOW()),
(@zhurou_id, '部位', 1, 'quality', '整猪,前腿,后腿,里脊,五花,排骨,猪蹄,猪头,副产品', NULL, NULL, 'N', 3, '0', NOW()),
(@zhurou_id, '屠宰日期', 0, 'trade', NULL, NULL, '请填写屠宰日期', 'Y', 4, '0', NOW()),
(@zhurou_id, '保质期', 1, 'quality', '7天,15天,30天,6个月,12个月', NULL, NULL, 'Y', 5, '0', NOW()),
(@zhurou_id, '品牌/厂家', 0, 'trade', NULL, NULL, '请填写品牌或屠宰厂名称', 'N', 6, '0', NOW());

-- 5.7 牛肉参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@niurou_id, '产品形态', 1, 'quality', '整牛,分割品,深加工品', NULL, NULL, 'Y', 1, '0', NOW()),
(@niurou_id, '温度状态', 1, 'quality', '冷鲜(0-4℃),冷冻(-18℃)', NULL, NULL, 'Y', 2, '0', NOW()),
(@niurou_id, '品种', 1, 'breed', '国产黄牛,进口牛肉,和牛,其他', NULL, NULL, 'N', 3, '0', NOW()),
(@niurou_id, '部位', 1, 'quality', '整牛,牛腩,牛腱,牛里脊,牛肋排,牛杂,副产品', NULL, NULL, 'N', 4, '0', NOW()),
(@niurou_id, '屠宰日期', 0, 'trade', NULL, NULL, '请填写屠宰日期', 'Y', 5, '0', NOW()),
(@niurou_id, '保质期', 1, 'quality', '15天,30天,6个月,12个月,24个月', NULL, NULL, 'Y', 6, '0', NOW());

-- 5.8 羊肉参数模板
INSERT INTO `nht_product_parameters` (`product_id`, `param_name`, `param_type`, `param_group`, `options`, `unit`, `placeholder`, `required`, `sort`, `del_flag`, `create_time`) VALUES
(@yangrou_id, '产品形态', 1, 'quality', '整羊,分割品,深加工品', NULL, NULL, 'Y', 1, '0', NOW()),
(@yangrou_id, '温度状态', 1, 'quality', '冷鲜(0-4℃),冷冻(-18℃)', NULL, NULL, 'Y', 2, '0', NOW()),
(@yangrou_id, '品种', 1, 'breed', '绵羊,山羊,滩羊,小尾寒羊,其他', NULL, NULL, 'N', 3, '0', NOW()),
(@yangrou_id, '部位', 1, 'quality', '整羊,羊腿,羊排,羊肉卷,羊杂,副产品', NULL, NULL, 'N', 4, '0', NOW()),
(@yangrou_id, '屠宰日期', 0, 'trade', NULL, NULL, '请填写屠宰日期', 'Y', 5, '0', NOW()),
(@yangrou_id, '保质期', 1, 'quality', '15天,30天,6个月,12个月', NULL, NULL, 'Y', 6, '0', NOW());

-- ============================================================
-- 6. 新增其他品类 (other)
-- ============================================================

INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`) VALUES
(1, 0, '其他品类', 'other', 0, 1, '0', '0', NOW());

-- ============================================================
-- 7. 为现有饲料原料大类添加"其他"选项
-- ============================================================

-- 谷物及其加工产品 - 添加"其他谷物"
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, 1, '其他谷物', 'feed', 0, 1, '0', '0', NOW()
WHERE NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = 1 AND product_name = '其他谷物');

-- 油料籽实及其加工产品 - 添加"其他粕类"
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, 18, '其他粕类', 'feed', 0, 1, '0', '0', NOW()
WHERE NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = 18 AND product_name = '其他粕类');

-- 油脂类 - 添加"其他油脂"
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, 27, '其他油脂', 'feed', 0, 1, '0', '0', NOW()
WHERE NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = 27 AND product_name = '其他油脂');

-- 微生物发酵产品 - 添加"其他发酵产品"
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, 35, '其他发酵产品', 'feed', 0, 1, '0', '0', NOW()
WHERE NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = 35 AND product_name = '其他发酵产品');

-- 动物产品及其副产品 - 添加"其他动物产品"
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, 41, '其他动物产品', 'feed', 0, 1, '0', '0', NOW()
WHERE NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = 41 AND product_name = '其他动物产品');

-- 矿物质 - 添加"其他矿物质"
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, 49, '其他矿物质', 'feed', 0, 1, '0', '0', NOW()
WHERE NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = 49 AND product_name = '其他矿物质');

-- 添加剂 - 添加"其他添加剂"
INSERT INTO `nht_product` (`user_id`, `parent_id`, `product_name`, `schema_code`, `has_params`, `allow_custom_name`, `status`, `del_flag`, `create_time`)
SELECT 1, 54, '其他添加剂', 'feed', 0, 1, '0', '0', NOW()
WHERE NOT EXISTS (SELECT 1 FROM nht_product WHERE parent_id = 54 AND product_name = '其他添加剂');
