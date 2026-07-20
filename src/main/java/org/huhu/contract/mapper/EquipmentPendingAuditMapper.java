package org.huhu.contract.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.huhu.contract.entity.EquipmentPendingAuditEntity;
import java.util.List;

@Mapper
public interface EquipmentPendingAuditMapper extends BaseMapper<EquipmentPendingAuditEntity> {

    /** 批量插入设备入账记录 → XML */
    int insertBatch(@Param("list") List<EquipmentPendingAuditEntity> list);

    /** 根据明细ID查询设备入账记录 */
    List<EquipmentPendingAuditEntity> selectByItemId(@Param("itemId") Long itemId);

    /** 根据明细ID删除设备入账记录 */
    int deleteByItemId(@Param("itemId") Long itemId);

    /** 根据合同ID查询所有明细下的设备入账记录 */
    List<EquipmentPendingAuditEntity> selectByContractId(@Param("contractId") Long contractId);
}
