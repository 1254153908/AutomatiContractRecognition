package org.huhu.contract.Vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractItemVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long contractId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String unit;
    private String specification;

    /** 该明细对应的设备入账未审核记录列表 */
    private List<EquipmentPendingAuditVo> equipments;
}
