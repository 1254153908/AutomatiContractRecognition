package org.huhu.contract.service;

import org.huhu.contract.Bo.ContractBo;
import org.huhu.contract.Bo.EquipmentPendingAuditBo;
import org.huhu.contract.Vo.ContractVo;
import org.huhu.contract.Vo.EquipmentPendingAuditVo;
import org.huhu.contract.entity.ContractEntity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface ContractServiceIntreface {

    /** 登记合同 */
    ContractVo addContract(ContractBo bo);

    /** 修改合同 */
    ContractVo updateContract(ContractBo bo);

    /** 查询合同（含明细） */
    ContractVo getContractById(Long id);

    /** 查询所有合同列表 */
    List<ContractVo> listAll();

    /** 分页查询合同列表 */
    IPage<ContractVo> pageContracts(Page<ContractEntity> page);

    /** 删除合同（级联删除明细） */
    void deleteContract(Long id);

    /** 根据明细ID查询设备入账未审核记录 */
    List<EquipmentPendingAuditVo> getAuditsByItemId(Long itemId);

    /** 保存/更新某条明细的设备入账未审核记录（先删后插） */
    List<EquipmentPendingAuditVo> saveAudits(Long itemId, List<EquipmentPendingAuditBo> audits);

    /** 根据合同ID查询所有设备入账未审核记录 */
    List<EquipmentPendingAuditVo> getAuditsByContractId(Long contractId);
}
