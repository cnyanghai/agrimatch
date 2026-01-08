-- ============================================================
-- 修复 bus_contract 表的 company_id 字段约束
-- 原因：新设计使用 buyer_company_id + seller_company_id 替代单一 company_id
-- 
-- 适用于：远程服务器上旧版数据库结构
-- 本地开发环境可能已经是新结构（没有 company_id 字段），可忽略执行错误
-- ============================================================

-- 将 company_id 改为允许 NULL（如果字段存在）
-- 如果字段不存在会报错，可以忽略
ALTER TABLE bus_contract 
  MODIFY company_id bigint DEFAULT NULL 
  COMMENT '已废弃，请使用 buyer_company_id/seller_company_id';

-- 将 user_id 也改为允许 NULL（如果字段存在）
ALTER TABLE bus_contract 
  MODIFY user_id bigint DEFAULT NULL 
  COMMENT '已废弃，买卖双方分别记录在签署记录中';

-- 注意：如果远程服务器报错 "Unknown column 'company_id'"，
-- 说明数据库已经是新结构，无需执行此脚本

