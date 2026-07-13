package org.huhu.contract.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同实体 - 对应数据库表 t_contract
 */
@Data
public class ContractEntity {

    private Long id;
    private String contractNo;
    private String contractName;
    private String partyA;
    private String partyB;
    private BigDecimal contractAmount;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;             // 0-草稿 1-已签订 2-已终止
    private String content;
    private String remark;
    private Integer isDeleted;          // 0-正常 1-已删除
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
