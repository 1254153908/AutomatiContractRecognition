package org.huhu.contract.Bo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合同业务对象 - 接收前端请求参数
 */
@Data
public class ContractBo {

    private Long id;                    // 修改时必填
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
}
