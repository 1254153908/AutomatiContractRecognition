package org.huhu.contract.Bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ContractBo {

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
    private List<ContractItemBo> items;
}
