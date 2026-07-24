-- ============================================
-- 测试：设备入账表是否允许空字段插入
-- 用法：在 MySQL 客户端逐段执行观察结果
-- ============================================

USE contract_db;

-- 1. 确认表结构：lydwh/zcbhqj/zcflh/zcmc/je/ggrq 的 Null 列应为 YES
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'contract_db'
  AND TABLE_NAME = 'equipment_pending_audit'
ORDER BY ORDINAL_POSITION;

-- 2. 插入一条含空字段的测试记录（把 ppxh/gg/cj/je/ggrq 都设为 NULL）
-- 先查一个有效的 item_id（如果没有就先插入一条合同+明细）
SELECT id FROM contract_items ORDER BY id DESC LIMIT 1;

-- 把下面 @item_id 替换为上一步查到的真实 id
SET @item_id = 1;   -- ★ 请改成上面查到的真实值

INSERT INTO equipment_pending_audit (
    item_id, lydwh, lydwm, zcbhqj, zcflh, zcmc, ppxh, gg,
    sl, dj, je, jldw, cj, ggrq
) VALUES (
    @item_id,
    NULL, NULL, NULL, NULL,        -- lydwh, lydwm, zcbhqj, zcflh
    '测试设备名称',                -- zcmc
    NULL, NULL,                    -- ppxh, gg（之前是 NOT NULL，现在也要可空）
    1, 100.00,                     -- sl, dj
    NULL,                          -- je
    '台', NULL,                    -- jldw, cj
    NULL                           -- ggrq
);

-- 3. 验证是否插入成功
SELECT * FROM equipment_pending_audit ORDER BY id DESC LIMIT 1;

-- 4. 清理测试数据（把下面 @last_id 改为上一步查到的 id）
-- DELETE FROM equipment_pending_audit WHERE id = 最后查到的那个 id;