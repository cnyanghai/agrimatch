-- 标签词典表
CREATE TABLE `nht_tags` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `tag_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签键（识别码）',
  `domain` VARCHAR(20) NOT NULL DEFAULT 'general' COMMENT '所属领域：biological, processing, material, equipment, general',
  `tag_type` TINYINT NOT NULL DEFAULT 0 COMMENT '标签类型：0-文本, 1-数值, 2-选项, 3-范围',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '数值单位',
  `options` TEXT DEFAULT NULL COMMENT '选项列表(JSON数组)',
  `recommend_categories` TEXT DEFAULT NULL COMMENT '推荐品类ID列表(JSON数组)',
  `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门：0-否, 1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
  `sort` INT DEFAULT 0 COMMENT '排序权重',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全产业链标签词典表';

-- 初始数据导入
-- 1. 原料饲料 (material)
INSERT INTO `nht_tags` (`tag_name`, `tag_key`, `domain`, `tag_type`, `unit`, `is_hot`, `sort`) VALUES
('水分', 'moisture', 'material', 1, '%', 1, 10),
('粗蛋白', 'protein', 'material', 1, '%', 1, 11),
('霉变粒', 'mold', 'material', 1, '%', 1, 12),
('产地', 'origin', 'material', 0, NULL, 1, 1),
('年份', 'year', 'material', 0, NULL, 1, 2),
('杂质', 'impurity', 'material', 1, '%', 0, 13);

-- 2. 生物种苗 (biological)
INSERT INTO `nht_tags` (`tag_name`, `tag_key`, `domain`, `tag_type`, `unit`, `is_hot`, `sort`) VALUES
('日龄', 'age_days', 'biological', 1, '天', 1, 20),
('免疫情况', 'vaccination', 'biological', 0, NULL, 1, 21),
('成活率', 'survival_rate', 'biological', 1, '%', 1, 22),
('亲本品种', 'parent_breed', 'biological', 0, NULL, 0, 23),
('规格', 'spec_weight', 'biological', 1, 'g/只', 0, 24);

-- 3. 农牧加工 (processing)
INSERT INTO `nht_tags` (`tag_name`, `tag_key`, `domain`, `tag_type`, `is_hot`, `sort`, `options`) VALUES
('部位', 'meat_part', 'processing', 2, 1, 30, '["胸肉","腿肉","翅中","爪子","内脏","整鸡"]'),
('温控要求', 'temp_control', 'processing', 2, 1, 31, '["常温","冷藏(0-4℃)","冷冻(-18℃)","速冻"]'),
('保质期', 'shelf_life', 'processing', 0, 1, 32, NULL),
('检疫合格证', 'quarantine_cert', 'processing', 2, 0, 33, '["已提供","待补办","无需提供"]');

-- 4. 装备物流 (equipment)
INSERT INTO `nht_tags` (`tag_name`, `tag_key`, `domain`, `tag_type`, `unit`, `is_hot`, `sort`) VALUES
('载重', 'load_capacity', 'equipment', 1, '吨', 1, 40),
('动力方式', 'power_type', 'equipment', 0, NULL, 1, 41),
('品牌', 'brand', 'equipment', 0, NULL, 1, 42),
('作业时长', 'working_hours', 'equipment', 1, '小时', 0, 43),
('保修期', 'warranty_period', 'equipment', 0, NULL, 0, 44),
('新旧程度', 'condition_level', 'equipment', 0, NULL, 0, 45);

-- 为 Supply 和 Requirement 表增加 tags_json 字段
ALTER TABLE `bus_supply` ADD COLUMN `tags_json` TEXT COMMENT '标签数据快照(JSON)' AFTER `params_json`;
ALTER TABLE `bus_requirement` ADD COLUMN `tags_json` TEXT COMMENT '标签数据快照(JSON)' AFTER `params_json`;

