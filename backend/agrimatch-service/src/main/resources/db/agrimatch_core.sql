-- ============================================================
-- AgriMatch (农汇通) - Core Schema Init
-- 说明：
-- - 仅包含农汇通核心业务表 + sys_user 增量字段
-- - 可重复执行：业务表使用 DROP TABLE IF EXISTS；sys_user 使用 ADD COLUMN IF NOT EXISTS
-- ============================================================

-- sys_user：兼容“全新库”场景（若已存在则不创建）
CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户账号',
  `password` varchar(100) DEFAULT '' COMMENT '密码(BCrypt)',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID（关联 bus_company.id）',
  `is_buyer` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否采购商（0否 1是）',
  `is_seller` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否供应商（0否 1是）',
  `user_type` varchar(20) DEFAULT 'SYS_USER' COMMENT '用户类型（SYS_USER/CG_USER/GY_USER/CG_GY_USER）',
  `pay_info_json` longtext COMMENT '收付款信息（银行卡/三方账户）JSON',
  `status` char(1) DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  KEY `idx_sys_user_user_name` (`user_name`),
  KEY `idx_sys_user_phone` (`phonenumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户基础信息表（农汇通）';

-- Extend Table: sys_user (增补字段)
ALTER TABLE `sys_user` ADD COLUMN `wechat` varchar(50) DEFAULT NULL COMMENT '微信号';
ALTER TABLE `sys_user` ADD COLUMN `company_id` bigint DEFAULT NULL COMMENT '公司ID（关联 bus_company.id）';
ALTER TABLE `sys_user` ADD COLUMN `is_buyer` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否采购商（0否 1是）';
ALTER TABLE `sys_user` ADD COLUMN `is_seller` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否供应商（0否 1是）';
ALTER TABLE `sys_user` ADD COLUMN `pay_info_json` longtext COMMENT '收付款信息（银行卡/三方账户）JSON';
ALTER TABLE `sys_user` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）';
ALTER TABLE `sys_user` ADD COLUMN `password` varchar(100) DEFAULT '' COMMENT '密码(BCrypt)';

-- Table structure for bus_company
CREATE TABLE IF NOT EXISTS `bus_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公司ID',
  `owner_user_id` bigint DEFAULT NULL COMMENT '创建人用户ID（sys_user.user_id）',
  `company_name` varchar(128) NOT NULL COMMENT '公司名称',
  `license_no` varchar(64) DEFAULT NULL COMMENT '营业执照号/统一社会信用代码',
  `license_img_url` varchar(255) DEFAULT NULL COMMENT '营业执照/资质图片URL',
  `contacts` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `address` varchar(255) DEFAULT NULL COMMENT '公司地址',
  `lat` decimal(10,7) DEFAULT NULL COMMENT '公司默认纬度',
  `lng` decimal(10,7) DEFAULT NULL COMMENT '公司默认经度',
  `locations_json` longtext COMMENT '多发货点/多收货点坐标JSON',
  `bank_info_json` longtext COMMENT '公司收付款信息JSON（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_bus_company_owner_user` (`owner_user_id`),
  KEY `idx_bus_company_name` (`company_name`),
  KEY `idx_bus_company_phone` (`phone`),
  KEY `idx_bus_company_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公司资质及位置坐标表';

-- 企业类型（饲料厂/贸易商/粮库/加工厂/物流仓储/其他）
ALTER TABLE `bus_company` ADD COLUMN `company_type` varchar(32) DEFAULT NULL COMMENT '企业类型';

-- Table structure for bus_requirement
CREATE TABLE IF NOT EXISTS `bus_requirement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '需求ID',
  `company_id` bigint NOT NULL COMMENT '采购方公司ID（bus_company.id）',
  `user_id` bigint NOT NULL COMMENT '发布用户ID（sys_user.user_id）',
  `category_name` varchar(64) NOT NULL COMMENT '品类名称（支持自定义）',
  `contract_no` varchar(64) DEFAULT NULL COMMENT '合同编号/单号（后端生成或前端传入）',
  `quantity` decimal(18,3) DEFAULT NULL COMMENT '数量（吨/件等，单位由 params_json 说明）',
  `expected_price` decimal(18,2) DEFAULT NULL COMMENT '期望价格（元/吨等）',
  `packaging` varchar(20) DEFAULT NULL COMMENT '包装（散装/袋装等）',
  `invoice_type` varchar(50) DEFAULT NULL COMMENT '发票类型',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '付款方式（现款/账期等）',
  `delivery_method` varchar(20) DEFAULT NULL COMMENT '交货方式（自提/送货等）',
  `params_json` longtext COMMENT '动态属性JSON（指标/模板字段等）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `expire_minutes` int DEFAULT NULL COMMENT '发布时效（分钟）',
  `expire_time` datetime(3) DEFAULT NULL COMMENT '过期时间',
  `purchase_lat` decimal(10,7) DEFAULT NULL COMMENT '采购点纬度',
  `purchase_lng` decimal(10,7) DEFAULT NULL COMMENT '采购点经度',
  `purchase_address` varchar(255) DEFAULT NULL COMMENT '采购点地址',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0发布 1部分成交 2下架 3全部成交）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_req_company` (`company_id`),
  KEY `idx_req_user` (`user_id`),
  KEY `idx_req_category` (`category_name`),
  KEY `idx_req_contract_no` (`contract_no`),
  KEY `idx_req_create_time` (`create_time`),
  KEY `idx_req_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购需求表';

ALTER TABLE `bus_requirement` ADD COLUMN `contract_no` varchar(64) DEFAULT NULL COMMENT '合同编号/单号（后端生成或前端传入）';
ALTER TABLE `bus_requirement` ADD COLUMN `expected_price` decimal(18,2) DEFAULT NULL COMMENT '期望价格（元/吨等）';
ALTER TABLE `bus_requirement` ADD COLUMN `invoice_type` varchar(50) DEFAULT NULL COMMENT '发票类型';
ALTER TABLE `bus_requirement` ADD COLUMN `delivery_method` varchar(20) DEFAULT NULL COMMENT '交货方式（自提/送货等）';
ALTER TABLE `bus_requirement` ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE `bus_requirement` ADD COLUMN `expire_minutes` int DEFAULT NULL COMMENT '发布时效（分钟）';
ALTER TABLE `bus_requirement` ADD COLUMN `expire_time` datetime(3) DEFAULT NULL COMMENT '过期时间';

-- Table structure for bus_requirement_template
CREATE TABLE IF NOT EXISTS `bus_requirement_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `owner_user_id` bigint NOT NULL COMMENT '所属用户ID（sys_user.user_id）',
  `template_name` varchar(128) NOT NULL COMMENT '模板名称',
  `template_json` longtext COMMENT '模板内容JSON（发布字段快照）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_req_tpl_owner_user` (`owner_user_id`),
  KEY `idx_req_tpl_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购需求发布模板表';

-- Table structure for bus_supply
CREATE TABLE IF NOT EXISTS `bus_supply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应ID',
  `company_id` bigint NOT NULL COMMENT '供应方公司ID（bus_company.id）',
  `user_id` bigint NOT NULL COMMENT '发布用户ID（sys_user.user_id）',
  `category_name` varchar(64) NOT NULL COMMENT '品类名称',
  `supply_no` varchar(64) DEFAULT NULL COMMENT '供应编号/单号（后端生成或前端传入）',
  `origin` varchar(128) DEFAULT NULL COMMENT '产地/来源地',
  `quantity` decimal(18,3) DEFAULT NULL COMMENT '供应数量（吨/件等）',
  `ex_factory_price` decimal(18,2) NOT NULL COMMENT '出厂价',
  `ship_address` varchar(255) DEFAULT NULL COMMENT '发货点地址',
  `delivery_mode` varchar(20) DEFAULT NULL COMMENT '交付方式（自提/到厂等）',
  `packaging` varchar(20) DEFAULT NULL COMMENT '包装方式（袋装/散装等）',
  `storage_method` varchar(20) DEFAULT NULL COMMENT '储存方式（常温/冷藏等）',
  `price_rules_json` longtext COMMENT '多组价格/交付规则JSON',
  `params_json` longtext COMMENT '产品参数/指标JSON（下拉选择+自定义）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `expire_minutes` int DEFAULT NULL COMMENT '发布时效（分钟）',
  `expire_time` datetime(3) DEFAULT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0上架 1下架）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_supply_company` (`company_id`),
  KEY `idx_supply_user` (`user_id`),
  KEY `idx_supply_category` (`category_name`),
  KEY `idx_supply_no` (`supply_no`),
  KEY `idx_supply_status` (`status`),
  KEY `idx_supply_expire_time` (`expire_time`),
  KEY `idx_supply_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应发布表';

ALTER TABLE `bus_supply` ADD COLUMN `params_json` longtext COMMENT '产品参数/指标JSON（下拉选择+自定义）';
ALTER TABLE `bus_supply` ADD COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0上架 1下架）';
ALTER TABLE `bus_supply` ADD COLUMN `supply_no` varchar(64) DEFAULT NULL COMMENT '供应编号/单号（后端生成或前端传入）';
ALTER TABLE `bus_supply` ADD COLUMN `origin` varchar(128) DEFAULT NULL COMMENT '产地/来源地';
ALTER TABLE `bus_supply` ADD COLUMN `quantity` decimal(18,3) DEFAULT NULL COMMENT '供应数量（吨/件等）';
ALTER TABLE `bus_supply` ADD COLUMN `packaging` varchar(20) DEFAULT NULL COMMENT '包装方式（袋装/散装等）';
ALTER TABLE `bus_supply` ADD COLUMN `storage_method` varchar(20) DEFAULT NULL COMMENT '储存方式（常温/冷藏等）';
ALTER TABLE `bus_supply` ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注';
ALTER TABLE `bus_supply` ADD COLUMN `expire_minutes` int DEFAULT NULL COMMENT '发布时效（分钟）';
ALTER TABLE `bus_supply` ADD COLUMN `expire_time` datetime(3) DEFAULT NULL COMMENT '过期时间';

-- Table structure for bus_contract_template
CREATE TABLE IF NOT EXISTS `bus_contract_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `owner_user_id` bigint NOT NULL COMMENT '所属用户ID（sys_user.user_id）',
  `owner_company_id` bigint DEFAULT NULL COMMENT '所属公司ID（bus_company.id）',
  `product_type` varchar(64) NOT NULL COMMENT '产品类型（豆粕/钢材等）',
  `template_name` varchar(128) NOT NULL COMMENT '模板名称',
  `template_content` longtext COMMENT '模板内容（可存HTML/占位符文本）',
  `file_url` varchar(255) DEFAULT NULL COMMENT '模板文件URL（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tpl_owner_user` (`owner_user_id`),
  KEY `idx_tpl_product_type` (`product_type`),
  KEY `idx_tpl_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同模板表';

-- Table structure for bus_order_eval
CREATE TABLE IF NOT EXISTS `bus_order_eval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `deal_id` bigint DEFAULT NULL COMMENT '成交ID（bus_deal.id）',
  `requirement_id` bigint DEFAULT NULL COMMENT '需求ID（bus_requirement.id）',
  `supply_id` bigint DEFAULT NULL COMMENT '供应ID（bus_supply.id）',
  `from_user_id` bigint NOT NULL COMMENT '评价人用户ID（sys_user.user_id）',
  `to_company_id` bigint NOT NULL COMMENT '被评价公司ID（bus_company.id）',
  `stars` tinyint NOT NULL COMMENT '星级（1-5）',
  `comment` varchar(500) DEFAULT NULL COMMENT '文字评价',
  `images_json` longtext COMMENT '图片/附件JSON（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_eval_deal` (`deal_id`),
  KEY `idx_eval_to_company` (`to_company_id`),
  KEY `idx_eval_from_user` (`from_user_id`),
  KEY `idx_eval_create_time` (`create_time`),
  UNIQUE KEY `uk_eval_deal_from_user` (`deal_id`, `from_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价记录表';

ALTER TABLE `bus_order_eval` ADD COLUMN `deal_id` bigint DEFAULT NULL COMMENT '成交ID（bus_deal.id）';

-- ============================================================
-- Deal (成交记录) - MVP闭环：需求可多次成交（部分成交/全部成交）
-- ============================================================
CREATE TABLE IF NOT EXISTS `bus_deal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成交ID',
  `requirement_id` bigint NOT NULL COMMENT '需求ID（bus_requirement.id）',
  `supply_id` bigint NOT NULL COMMENT '供应ID（bus_supply.id）',
  `buyer_company_id` bigint NOT NULL COMMENT '采购方公司ID（bus_company.id）',
  `seller_company_id` bigint NOT NULL COMMENT '供应方公司ID（bus_company.id）',
  `buyer_user_id` bigint NOT NULL COMMENT '采购方用户ID（sys_user.user_id）',
  `seller_user_id` bigint NOT NULL COMMENT '供应方用户ID（sys_user.user_id）',
  `quantity` decimal(18,3) NOT NULL COMMENT '成交数量（吨/件等）',
  `final_ex_factory_price` decimal(18,2) NOT NULL COMMENT '成交出厂价（单价）',
  `delivery_mode` varchar(20) DEFAULT NULL COMMENT '交付方式（到厂/自提等）',
  `distance_km` decimal(18,3) DEFAULT NULL COMMENT '估算距离（公里）',
  `freight_rate_per_ton_km` decimal(18,4) DEFAULT NULL COMMENT '运费单价（元/吨/公里）',
  `delivered_price` decimal(18,2) DEFAULT NULL COMMENT '估算到厂价（单价）= 出厂价 + distanceKm * rate',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1已确认 2已取消）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_deal_req` (`requirement_id`),
  KEY `idx_deal_supply` (`supply_id`),
  KEY `idx_deal_buyer_company` (`buyer_company_id`),
  KEY `idx_deal_seller_company` (`seller_company_id`),
  KEY `idx_deal_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='成交记录表（MVP）';

-- Table structure for bus_post (观点帖子)
CREATE TABLE IF NOT EXISTS `bus_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `company_id` bigint DEFAULT NULL COMMENT '发布公司ID（bus_company.id，可空）',
  `user_id` bigint NOT NULL COMMENT '发布用户ID（sys_user.user_id）',
  `title` varchar(120) NOT NULL COMMENT '标题',
  `content` longtext COMMENT '正文内容',
  `images_json` longtext COMMENT '图片JSON（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_company` (`company_id`),
  KEY `idx_post_user` (`user_id`),
  KEY `idx_post_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='观点帖子表';

-- ============================================================
-- Points (积分)
-- 说明：演示版约定：1 积分 = 1 元（可后续做成配置项/汇率表）
-- ============================================================

-- 积分账户（每个用户一行）
CREATE TABLE IF NOT EXISTS `bus_points_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（sys_user.user_id）',
  `points_balance` bigint NOT NULL DEFAULT 0 COMMENT '积分余额',
  `cny_balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '可兑换人民币余额（演示）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_points_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分账户表';

-- 积分流水
CREATE TABLE IF NOT EXISTS `bus_points_tx` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tx_type` varchar(20) NOT NULL COMMENT '类型（RECHARGE/REDEEM）',
  `points_delta` bigint NOT NULL DEFAULT 0 COMMENT '积分变动（正为增加，负为扣减）',
  `cny_delta` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '人民币变动（正为增加，负为扣减）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_points_tx_user` (`user_id`),
  KEY `idx_points_tx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分流水表';

-- ============================================================
-- Post Social (评论/点赞)
-- ============================================================

CREATE TABLE IF NOT EXISTS `bus_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID（bus_post.id）',
  `user_id` bigint NOT NULL COMMENT '用户ID（sys_user.user_id）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_like` (`post_id`, `user_id`),
  KEY `idx_post_like_post` (`post_id`),
  KEY `idx_post_like_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帖子点赞表';

CREATE TABLE IF NOT EXISTS `bus_post_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID（bus_post.id）',
  `company_id` bigint DEFAULT NULL COMMENT '评论公司ID（bus_company.id，可空）',
  `user_id` bigint NOT NULL COMMENT '评论用户ID（sys_user.user_id）',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_comment_post` (`post_id`),
  KEY `idx_post_comment_user` (`user_id`),
  KEY `idx_post_comment_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帖子评论表';

-- ============================================================
-- Notifications (消息通知)
-- ============================================================
CREATE TABLE IF NOT EXISTS `bus_notify` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `to_user_id` bigint NOT NULL COMMENT '接收用户ID（sys_user.user_id）',
  `type` varchar(30) NOT NULL COMMENT '类型（POST_LIKE/POST_COMMENT 等）',
  `title` varchar(120) NOT NULL COMMENT '标题',
  `content` varchar(500) DEFAULT NULL COMMENT '内容',
  `link` varchar(255) DEFAULT NULL COMMENT '跳转链接（前端路由）',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0否 1是）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_notify_to_user` (`to_user_id`),
  KEY `idx_notify_is_read` (`is_read`),
  KEY `idx_notify_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息通知表';

-- ============================================================
-- Chat (即时聊天)
-- ============================================================
CREATE TABLE IF NOT EXISTS `bus_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者用户ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者用户ID',
  `content` varchar(2000) NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0否 1是）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_from` (`from_user_id`),
  KEY `idx_chat_to` (`to_user_id`),
  KEY `idx_chat_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='即时聊天消息表';


