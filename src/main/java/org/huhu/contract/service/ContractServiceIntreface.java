package org.huhu.contract.service;

import org.huhu.contract.Bo.ContractBo;
import org.huhu.contract.Vo.ContractVo;

/**
 * 合同服务接口
 */
public interface ContractServiceIntreface {

    /**
     * 新增合同
     */
    ContractVo addContract(ContractBo bo);

    /**
     * 修改合同
     */
    ContractVo updateContract(ContractBo bo);

    /**
     * 根据ID查询合同
     */
    ContractVo getContractById(Long id);

    /**
     * 删除合同（逻辑删除）
     */
    void deleteContract(Long id);
}
