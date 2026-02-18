-- =====================================================================
-- 会话合并迁移：将同一对用户的多个会话合并为一个
-- 执行前请先备份 bus_chat_conversation 和 bus_chat_message 表
-- =====================================================================

-- 1. 找出每对用户保留的会话（最新活跃的那个）
CREATE TEMPORARY TABLE _keep_conv AS
SELECT MIN(id) AS keep_id, a_user_id, b_user_id
FROM (
    SELECT id, a_user_id, b_user_id,
           ROW_NUMBER() OVER (
               PARTITION BY a_user_id, b_user_id
               ORDER BY last_time DESC NULLS LAST, id DESC
           ) AS rn
    FROM bus_chat_conversation
    WHERE is_deleted = 0
) ranked
WHERE rn = 1
GROUP BY a_user_id, b_user_id;

-- 2. 找出需要合并（即将被废弃）的会话
CREATE TEMPORARY TABLE _merge_conv AS
SELECT c.id AS old_id, k.keep_id
FROM bus_chat_conversation c
JOIN _keep_conv k ON k.a_user_id = c.a_user_id AND k.b_user_id = c.b_user_id
WHERE c.is_deleted = 0
  AND c.id != k.keep_id;

-- 3. 将被废弃会话的消息迁移到保留会话
UPDATE bus_chat_message m
JOIN _merge_conv mc ON m.conversation_id = mc.old_id
SET m.conversation_id = mc.keep_id,
    m.update_time = NOW(3);

-- 4. 软删除被废弃的会话
UPDATE bus_chat_conversation c
JOIN _merge_conv mc ON c.id = mc.old_id
SET c.is_deleted = 1,
    c.update_time = NOW(3);

-- 5. 更新保留会话的 last_content / last_time（取最新消息）
UPDATE bus_chat_conversation c
JOIN (
    SELECT conversation_id,
           MAX(id) AS last_msg_id
    FROM bus_chat_message
    WHERE is_deleted = 0
      AND conversation_id IS NOT NULL
    GROUP BY conversation_id
) latest ON latest.conversation_id = c.id
JOIN bus_chat_message lm ON lm.id = latest.last_msg_id
SET c.last_msg_id = lm.id,
    c.last_content = lm.content,
    c.last_time = lm.create_time,
    c.update_time = NOW(3)
WHERE c.is_deleted = 0;

-- 6. 清理临时表
DROP TEMPORARY TABLE IF EXISTS _keep_conv;
DROP TEMPORARY TABLE IF EXISTS _merge_conv;

-- 验证：每对用户应该只有一个活跃会话
SELECT a_user_id, b_user_id, COUNT(*) AS cnt
FROM bus_chat_conversation
WHERE is_deleted = 0
GROUP BY a_user_id, b_user_id
HAVING cnt > 1;
-- 如果上面查询返回空结果，说明迁移成功
