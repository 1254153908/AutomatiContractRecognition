-- 合同数据库初始化脚本
DROP DATABASE IF EXISTS contract_db;
CREATE DATABASE IF NOT EXISTS contract_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE contract_db;

-- 1. 合同主表
CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_no VARCHAR(50) NOT NULL COMMENT '合同编号',
    project_name VARCHAR(200) COMMENT '项目名称',
    party_a VARCHAR(100) COMMENT '甲方',
    party_b VARCHAR(100) COMMENT '乙方',
    sign_date DATE COMMENT '签约日期',
    total_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '合同总价',
    file_path VARCHAR(255) COMMENT 'PDF文件存储路径',
    status TINYINT DEFAULT 0 COMMENT '0:待处理 1:已识别 2:已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_no (contract_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS contract_items;
-- 2. 合同明细表
CREATE TABLE IF NOT EXISTS contract_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL COMMENT '关联合同ID',
    product_name VARCHAR(200) COMMENT '产品名称',
    quantity INT DEFAULT 0 COMMENT '数量',
    unit_price DECIMAL(15,2) DEFAULT 0.00 COMMENT '单价',
    total_price DECIMAL(15,2) DEFAULT 0.00 COMMENT '合价',
    unit VARCHAR(255) COMMENT '单位',
    specification VARCHAR(255) COMMENT '规格/型号',
    FOREIGN KEY (contract_id) REFERENCES contracts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS equipment_pending_audit;
-- 3. 设备入账未审核表（每条合同明细对应一个或多个设备入账记录）
CREATE TABLE IF NOT EXISTS equipment_pending_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    item_id BIGINT NOT NULL COMMENT '关联合同明细ID（contract_items.id）',
    lydwh VARCHAR(10) DEFAULT NULL COMMENT '使用单位号（辅关键字，可空，后续可补录）',
    lydwm VARCHAR(60) DEFAULT '' COMMENT '使用单位名',
    zcbhqj VARCHAR(30) DEFAULT NULL COMMENT '设备编号区间（可空，后续可补录）',
    zcflh VARCHAR(8) DEFAULT NULL COMMENT '分类号（关键字，可空，后续可补录）',
    zcmc VARCHAR(40) DEFAULT NULL COMMENT '设备名称（可空，后续可补录）',
    ppxh VARCHAR(30) DEFAULT NULL COMMENT '品牌型号（可空，后续可补录）',
    gg VARCHAR(50) DEFAULT NULL COMMENT '规格（可空，后续可补录）',
    sl INT DEFAULT 0 COMMENT '数量',
    dj DECIMAL(12,2) DEFAULT 0.00 COMMENT '单价',
    je DECIMAL(12,2) DEFAULT NULL COMMENT '金额（辅关键字，可空，后续可补录）',
    jldw VARCHAR(20) DEFAULT '台' COMMENT '计量单位（台/套/张/个）',
    cj VARCHAR(40) DEFAULT NULL COMMENT '厂家（可空，后续可补录）',
    ggrq DATE DEFAULT NULL COMMENT '购置日期（辅关键字，可空，后续可补录）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (item_id) REFERENCES contract_items (id) ON DELETE CASCADE,
    INDEX idx_item_id (item_id),
    INDEX idx_lydwh (lydwh),
    INDEX idx_zcflh (zcflh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备入账未审核表';
