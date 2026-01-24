-- AgriMatch 全产业链数据底座加固 - 数据库变更脚本
-- 目标：标准化板块(Domain)分类，建立扁平化标签索引以支持高性能搜索

-- 1. 创建标签值扁平化索引表
CREATE TABLE IF NOT EXISTS `bus_item_tag_values` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `entity_type` VARCHAR(20) NOT NULL COMMENT '实体类型: supply, requirement, post, company',
  `entity_id` BIGINT NOT NULL COMMENT '实体ID',
  `tag_id` INT NOT NULL COMMENT '标签ID',
  `tag_key` VARCHAR(50) NOT NULL COMMENT '标签Key',
  `tag_value_text` VARCHAR(255) DEFAULT NULL COMMENT '文本值',
  `tag_value_num` DECIMAL(18, 4) DEFAULT NULL COMMENT '数值值',
  `domain` VARCHAR(20) NOT NULL DEFAULT 'general' COMMENT '所属领域',
  `create_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
  INDEX `idx_entity` (`entity_type`, `entity_id`),
  INDEX `idx_tag_lookup_num` (`tag_key`, `tag_value_num`),
  INDEX `idx_tag_lookup_text` (`tag_key`, `tag_value_text`),
  INDEX `idx_domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签值扁平化索引表';

-- 2. 为供应表增加 domain 字段
ALTER TABLE `bus_supply` ADD COLUMN `domain` VARCHAR(20) DEFAULT 'general' COMMENT '所属板块' AFTER `category_name`;

-- 3. 为需求表增加 domain 字段
ALTER TABLE `bus_requirement` ADD COLUMN `domain` VARCHAR(20) DEFAULT 'general' COMMENT '所属板块' AFTER `category_name`;

-- 4. 为话题表增加 domain 和 tags_json 字段
ALTER TABLE `bus_post` ADD COLUMN `domain` VARCHAR(20) DEFAULT 'general' COMMENT '所属板块' AFTER `content`;
ALTER TABLE `bus_post` ADD COLUMN `tags_json` JSON DEFAULT NULL COMMENT '标签数据' AFTER `domain`;

-- 5. 为公司表增加 tags_json 字段
ALTER TABLE `bus_company` ADD COLUMN `tags_json` JSON DEFAULT NULL COMMENT '行业标签数据' AFTER `company_intro`;

