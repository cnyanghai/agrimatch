-- 履约节点增加负责方字段
-- responsible_party: buyer=买方负责, seller=卖方负责, NULL=旧数据不限制（向后兼容）
ALTER TABLE bus_contract_milestone
  ADD COLUMN responsible_party varchar(10) DEFAULT NULL COMMENT '负责方（buyer/seller）' AFTER milestone_type;
