-- 供应状态语义升级迁移（旧：0上架/1下架 -> 新：0发布中/1部分成交/2已下架/3全部成交）
-- 目标：把历史 status=1(下架) 迁移为新语义 status=2(已下架)
-- 说明：
-- - 可重复执行：多次执行不会再次改变已迁移的数据
-- - 可回滚：仅回滚“本次迁移前为 status=1 的记录”（通过备份表记录 id）

-- 1) 备份表：记录“迁移前 status=1”的供应 id
CREATE TABLE IF NOT EXISTS `bus_supply_status_migration_backup` (
  `supply_id` bigint NOT NULL,
  `from_status` tinyint NOT NULL,
  `to_status` tinyint NOT NULL,
  `migrated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`supply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='bus_supply.status 语义升级迁移备份（仅用于 1->2）';

-- 2) 写入备份（幂等）：只记录未记录过的 supply_id
INSERT IGNORE INTO `bus_supply_status_migration_backup` (`supply_id`, `from_status`, `to_status`)
SELECT `id`, 1, 2
FROM `bus_supply`
WHERE `status` = 1;

-- 3) 执行迁移：1 -> 2
UPDATE `bus_supply`
SET `status` = 2
WHERE `status` = 1;

-- =========================
-- 回滚（如需）：
-- 将“备份表中记录过”的供应，在当前仍为 2 的情况下回滚为 1
-- =========================
-- UPDATE `bus_supply` s
-- JOIN `bus_supply_status_migration_backup` b ON b.supply_id = s.id
-- SET s.status = 1
-- WHERE s.status = 2
--   AND b.from_status = 1
--   AND b.to_status = 2;







