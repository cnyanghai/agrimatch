-- 添加拒绝原因字段，将拒绝原因与提交备注分离
ALTER TABLE `bus_contract_milestone`
  ADD COLUMN `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因' AFTER `remark`;
