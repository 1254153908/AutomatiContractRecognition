-- 对已存在的数据库：将设备入账未审核表的部分字段从 NOT NULL 改为可空，
-- 配合前端放开必填校验使用。
-- 执行前请确认当前数据库名，默认使用 contract_db。

USE contract_db;

ALTER TABLE equipment_pending_audit
    MODIFY COLUMN lydwh VARCHAR(10) DEFAULT NULL COMMENT '使用单位号（辅关键字，可空，后续可补录）',
    MODIFY COLUMN zcbhqj VARCHAR(30) DEFAULT NULL COMMENT '设备编号区间（可空，后续可补录）',
    MODIFY COLUMN zcflh VARCHAR(8) DEFAULT NULL COMMENT '分类号（关键字，可空，后续可补录）',
    MODIFY COLUMN zcmc VARCHAR(40) DEFAULT NULL COMMENT '设备名称（可空，后续可补录）',
    MODIFY COLUMN ppxh VARCHAR(30) DEFAULT NULL COMMENT '品牌型号（可空，后续可补录）',
    MODIFY COLUMN gg VARCHAR(50) DEFAULT NULL COMMENT '规格（可空，后续可补录）',
    MODIFY COLUMN je DECIMAL(12,2) DEFAULT NULL COMMENT '金额（辅关键字，可空，后续可补录）',
    MODIFY COLUMN cj VARCHAR(40) DEFAULT NULL COMMENT '厂家（可空，后续可补录）',
    MODIFY COLUMN ggrq DATE DEFAULT NULL COMMENT '购置日期（辅关键字，可空，后续可补录）';