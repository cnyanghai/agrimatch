-- ============================================================
-- 合同管理系统扩展 - 签署、履约、公章
-- ============================================================

-- 1. 扩展 bus_contract 表，增加关联字段
ALTER TABLE `bus_contract` 
  ADD COLUMN `quote_message_id` bigint DEFAULT NULL COMMENT '关联报价消息ID（bus_chat_message.id）' AFTER `user_id`,
  ADD COLUMN `conversation_id` bigint DEFAULT NULL COMMENT '关联会话ID（bus_chat_conversation.id）' AFTER `quote_message_id`,
  ADD COLUMN `party_a_user_id` bigint DEFAULT NULL COMMENT '甲方用户ID（sys_user.user_id）' AFTER `party_a`,
  ADD COLUMN `party_a_company_id` bigint DEFAULT NULL COMMENT '甲方公司ID（bus_company.id）' AFTER `party_a_user_id`,
  ADD COLUMN `party_b_user_id` bigint DEFAULT NULL COMMENT '乙方用户ID（sys_user.user_id）' AFTER `party_b`,
  ADD COLUMN `party_b_company_id` bigint DEFAULT NULL COMMENT '乙方公司ID（bus_company.id）' AFTER `party_b_user_id`,
  ADD COLUMN `params_json` longtext COMMENT '产品参数JSON（从报价单获取）' AFTER `unit_price`,
  ADD COLUMN `pdf_url` varchar(255) DEFAULT NULL COMMENT 'PDF文件URL' AFTER `pdf_hash`;

ALTER TABLE `bus_contract`
  ADD KEY `idx_contract_quote_msg` (`quote_message_id`),
  ADD KEY `idx_contract_conv` (`conversation_id`);

-- 2. 公司电子章表
CREATE TABLE IF NOT EXISTS `bus_company_seal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '印章ID',
  `company_id` bigint NOT NULL COMMENT '所属公司ID（bus_company.id）',
  `user_id` bigint NOT NULL COMMENT '上传用户ID（sys_user.user_id）',
  `seal_type` varchar(20) NOT NULL DEFAULT 'official' COMMENT '印章类型（official=公章, contract=合同章, finance=财务章）',
  `seal_name` varchar(64) NOT NULL COMMENT '印章名称',
  `seal_url` varchar(255) DEFAULT NULL COMMENT '上传的印章图片URL',
  `is_generated` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否系统生成（0=上传, 1=生成）',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认章',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_seal_company` (`company_id`),
  KEY `idx_seal_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公司电子章表';

-- 3. 合同签署记录表
CREATE TABLE IF NOT EXISTS `bus_contract_signature` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签署记录ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID（bus_contract.id）',
  `user_id` bigint NOT NULL COMMENT '签署人用户ID（sys_user.user_id）',
  `company_id` bigint DEFAULT NULL COMMENT '签署人公司ID（bus_company.id）',
  `party_type` varchar(10) NOT NULL COMMENT '签署方类型（party_a/party_b）',
  `sign_type` varchar(20) NOT NULL COMMENT '签署方式（seal=盖章, handwrite=手写签名, typed=打字签名, seal_handwrite=盖章+签名）',
  `seal_id` bigint DEFAULT NULL COMMENT '使用的印章ID（bus_company_seal.id）',
  `seal_url` varchar(255) DEFAULT NULL COMMENT '盖章图片URL（冗余存储）',
  `signature_url` varchar(255) DEFAULT NULL COMMENT '签名图片URL',
  `signer_name` varchar(64) DEFAULT NULL COMMENT '签署人姓名',
  `signer_title` varchar(64) DEFAULT NULL COMMENT '签署人职位',
  `sign_ip` varchar(45) DEFAULT NULL COMMENT '签署IP地址',
  `sign_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '签署时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sig_contract` (`contract_id`),
  KEY `idx_sig_user` (`user_id`),
  UNIQUE KEY `uk_contract_party` (`contract_id`, `party_type`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同签署记录表';

-- 4. 合同履约节点表
CREATE TABLE IF NOT EXISTS `bus_contract_milestone` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID（bus_contract.id）',
  `milestone_type` varchar(30) NOT NULL COMMENT '节点类型（SHIP=发货, RECEIVE=收货, PAY=付款, INSPECT=质检, CUSTOM=自定义）',
  `milestone_name` varchar(64) NOT NULL COMMENT '节点名称',
  `description` varchar(500) DEFAULT NULL COMMENT '节点描述',
  `expected_date` date DEFAULT NULL COMMENT '预期完成日期',
  `actual_date` date DEFAULT NULL COMMENT '实际完成日期',
  `operator_user_id` bigint DEFAULT NULL COMMENT '操作人用户ID',
  `evidence_url` varchar(255) DEFAULT NULL COMMENT '凭证附件URL',
  `evidence_json` longtext COMMENT '多个凭证JSON数组',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态（pending=待完成, submitted=已提交, confirmed=已确认, rejected=已拒绝）',
  `confirm_user_id` bigint DEFAULT NULL COMMENT '确认人用户ID',
  `confirm_time` datetime(3) DEFAULT NULL COMMENT '确认时间',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_milestone_contract` (`contract_id`),
  KEY `idx_milestone_type` (`milestone_type`),
  KEY `idx_milestone_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同履约节点表';

-- 5. 合同变更审计日志表
CREATE TABLE IF NOT EXISTS `bus_contract_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID（bus_contract.id）',
  `change_type` varchar(30) NOT NULL COMMENT '变更类型（CREATE=创建, UPDATE=修改, STATUS=状态变更, SIGN=签署, MILESTONE=履约节点）',
  `change_desc` varchar(255) DEFAULT NULL COMMENT '变更描述',
  `before_json` longtext COMMENT '变更前数据JSON',
  `after_json` longtext COMMENT '变更后数据JSON',
  `operator_user_id` bigint NOT NULL COMMENT '操作人用户ID',
  `operator_ip` varchar(45) DEFAULT NULL COMMENT '操作IP',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_changelog_contract` (`contract_id`),
  KEY `idx_changelog_type` (`change_type`),
  KEY `idx_changelog_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同变更审计日志表';

