-- 添加会话发起人字段，用于判断买方/卖方角色
ALTER TABLE bus_chat_conversation
    ADD COLUMN initiator_user_id BIGINT NULL COMMENT '会话发起人userId' AFTER subject_snapshot_json;
