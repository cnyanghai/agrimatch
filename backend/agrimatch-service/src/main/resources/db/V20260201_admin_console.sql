-- 管理后台：企业认证状态字段
ALTER TABLE bus_company ADD COLUMN verified_status tinyint DEFAULT 0
  COMMENT '审核状态: 0-未审核 1-已认证 2-已拒绝';
