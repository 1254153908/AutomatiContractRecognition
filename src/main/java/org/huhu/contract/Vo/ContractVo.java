package org.huhu.contract.Vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同视图对象 - 返回给前端
 */
@Data
public class ContractVo {

    private Long id;
    private String contractNo;
    private String contractName;
    private String partyA;
    private String partyB;
    private BigDecimal contractAmount;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String content;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
