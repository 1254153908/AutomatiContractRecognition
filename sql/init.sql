-- 合同数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS contract_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE contract_db;

-- 合同表
CREATE TABLE IF NOT EXISTS t_contract (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    contract_no     VARCHAR(64)     DEFAULT NULL             COMMENT '合同编号',
    contract_name   VARCHAR(255)    NOT NULL                 COMMENT '合同名称',
    party_a         VARCHAR(255)    DEFAULT NULL             COMMENT '甲方',
    party_b         VARCHAR(255)    DEFAULT NULL             COMMENT '乙方',
    contract_amount DECIMAL(18,2)   DEFAULT NULL             COMMENT '合同金额',
    sign_date       DATE            DEFAULT NULL             COMMENT '签订日期',
    start_date      DATE            DEFAULT NULL             COMMENT '合同开始日期',
    end_date        DATE            DEFAULT NULL             COMMENT '合同结束日期',
    status          TINYINT         DEFAULT 0                COMMENT '状态: 0-草稿 1-已签订 2-已终止',
    content         TEXT            DEFAULT NULL             COMMENT '合同内容',
    remark          VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    is_deleted      TINYINT         DEFAULT 0                COMMENT '逻辑删除: 0-正常 1-已删除',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_contract_no (contract_no),
    KEY idx_status (status),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';
