package org.huhu.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备入账未审核表实体（equipment_pending_audit）
 */
@Data
@TableName("equipment_pending_audit")
public class EquipmentPendingAuditEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long itemId;               // 关联 contract_items.id

    private String lydwh;              // 使用单位号（NOT NULL）
    private String lydwm;              // 使用单位名
    private String zcbhqj;             // 设备编号区间（NOT NULL）
    private String zcflh;              // 分类号（NOT NULL，关键字）
    private String zcmc;               // 设备名称（NOT NULL）
    private String ppxh;               // 品牌型号（NOT NULL，默认*）
    private String gg;                 // 规格（NOT NULL，默认*）
    private Integer sl;                // 数量
    private BigDecimal dj;             // 单价
    private BigDecimal je;             // 金额（NOT NULL，辅关键字）
    private String jldw;               // 计量单位（默认"台"）
    private String cj;                 // 厂家（NOT NULL，默认"无"）
    private LocalDate ggrq;            // 购置日期（NOT NULL，辅关键字）
    private LocalDateTime createdAt;
}
