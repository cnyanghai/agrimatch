-- 合同表新增：发票类型、包装要求、备注
ALTER TABLE bus_contract
  ADD COLUMN invoice_type varchar(50)  DEFAULT NULL COMMENT '发票类型'  AFTER delivery_mode,
  ADD COLUMN packaging    varchar(100) DEFAULT NULL COMMENT '包装要求'  AFTER invoice_type,
  ADD COLUMN remark       varchar(500) DEFAULT NULL COMMENT '备注'      AFTER packaging;
