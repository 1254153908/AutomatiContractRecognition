package org.huhu.contract.Vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备入账未审核出参
 */
@Data
public class EquipmentPendingAuditVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long itemId;

    private String lydwh;
    private String lydwm;
    private String zcbhqj;
    private String zcflh;
    private String zcmc;
    private String ppxh;
    private String gg;
    private Integer sl;
    private BigDecimal dj;
    private BigDecimal je;
    private String jldw;
    private String cj;
    private LocalDate ggrq;
}
