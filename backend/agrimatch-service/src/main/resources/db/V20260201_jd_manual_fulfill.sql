-- 京东 E 卡手动发卡系统
-- 1. sys_user 加 is_admin 字段
ALTER TABLE sys_user ADD COLUMN is_admin tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否管理员';
UPDATE sys_user SET is_admin = 1 WHERE user_id = 1;

-- 2. bus_jd_redeem 加管理字段
ALTER TABLE bus_jd_redeem
  ADD COLUMN admin_user_id bigint DEFAULT NULL COMMENT '处理管理员ID',
  ADD COLUMN admin_remark varchar(500) DEFAULT NULL COMMENT '管理员备注',
  ADD COLUMN fulfill_time datetime(3) DEFAULT NULL COMMENT '发卡时间';
