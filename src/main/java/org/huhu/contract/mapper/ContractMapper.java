package org.huhu.contract.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.huhu.contract.entity.ContractEntity;

/**
 * 合同 MyBatis Mapper 接口
 */
@Mapper
public interface ContractMapper {

    /**
     * 新增合同
     */
    int insert(ContractEntity contract);

    /**
     * 修改合同
     */
    int updateById(ContractEntity contract);

    /**
     * 根据ID查询合同
     */
    ContractEntity selectById(Long id);

    /**
     * 逻辑删除合同
     */
    int deleteById(Long id);
}
