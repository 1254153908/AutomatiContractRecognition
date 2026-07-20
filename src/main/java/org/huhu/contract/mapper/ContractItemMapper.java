package org.huhu.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.huhu.contract.entity.ContractItemEntity;
import java.util.List;

@Mapper
public interface ContractItemMapper extends BaseMapper<ContractItemEntity> {

    /** 批量插入明细 → XML */
    int insertBatch(@Param("list") List<ContractItemEntity> items);

    /** 根据合同ID查询明细 → XML */
    List<ContractItemEntity> selectByContractId(@Param("contractId") Long contractId);

    /** 根据合同ID删除明细 → XML */
    int deleteByContractId(@Param("contractId") Long contractId);
}
