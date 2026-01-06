-- ============================================================
-- 修复 bus_contract_signature 表缺失的列
-- 执行时间：2026-01-06 已执行
-- ============================================================

-- 添加 signer_title 列（签署人职位）
ALTER TABLE `bus_contract_signature` 
  ADD COLUMN `signer_title` varchar(64) DEFAULT NULL COMMENT '签署人职位' AFTER `signer_name`;

-- 注意：sign_ip 列已存在，无需添加

