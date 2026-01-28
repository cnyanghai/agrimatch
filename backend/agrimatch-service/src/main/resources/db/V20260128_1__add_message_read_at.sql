-- 添加消息阅读时间字段
ALTER TABLE bus_chat_message
ADD COLUMN read_at DATETIME(3) DEFAULT NULL COMMENT '阅读时间' AFTER is_read;

-- 为已读消息补充阅读时间（使用更新时间）
UPDATE bus_chat_message
SET read_at = update_time
WHERE is_read = 1 AND read_at IS NULL;
