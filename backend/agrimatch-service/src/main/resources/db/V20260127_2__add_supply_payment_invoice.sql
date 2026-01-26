-- 为 bus_supply 表添加付款方式和发票类型字段
-- 与 bus_requirement 表保持一致

ALTER TABLE bus_supply
ADD COLUMN payment_method varchar(20) DEFAULT NULL COMMENT '付款方式（现款/账期等）' AFTER delivery_mode,
ADD COLUMN invoice_type varchar(50) DEFAULT NULL COMMENT '发票类型' AFTER payment_method;
