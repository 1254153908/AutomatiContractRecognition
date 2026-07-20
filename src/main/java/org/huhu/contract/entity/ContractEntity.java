package org.huhu.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("contracts")
public class ContractEntity {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractNo;
    private String projectName;
    private String partyA;
    private String partyB;
    private LocalDate signDate;
    private BigDecimal totalAmount;
    private String filePath;
    private Integer status;             // 0-待处理 1-已识别 2-已完成
    private LocalDateTime createdAt;
}
