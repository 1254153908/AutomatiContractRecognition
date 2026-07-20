package org.huhu.contract.Vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContractVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String contractNo;
    private String projectName;
    private String partyA;
    private String partyB;
    private LocalDate signDate;
    private BigDecimal totalAmount;
    private String filePath;
    private Integer status;
    private LocalDateTime createdAt;
    private List<ContractItemVo> items;
}
