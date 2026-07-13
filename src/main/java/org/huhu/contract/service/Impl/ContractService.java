package org.huhu.contract.service.Impl;

import org.huhu.contract.Bo.ContractBo;
import org.huhu.contract.Vo.ContractVo;
import org.huhu.contract.entity.ContractEntity;
import org.huhu.contract.mapper.ContractMapper;
import org.huhu.contract.service.ContractServiceIntreface;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContractService implements ContractServiceIntreface {

    private final ContractMapper contractMapper;

    public ContractService(ContractMapper contractMapper) {
        this.contractMapper = contractMapper;
    }

    @Override
    public ContractVo addContract(ContractBo bo) {
        ContractEntity entity = new ContractEntity();
        BeanUtils.copyProperties(bo, entity);
        entity.setIsDeleted(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        contractMapper.insert(entity);

        // 返回插入后的实体
        ContractEntity saved = contractMapper.selectById(entity.getId());
        return toVo(saved);
    }

    @Override
    public ContractVo updateContract(ContractBo bo) {
        if (bo.getId() == null) {
            throw new RuntimeException("修改合同失败：ID不能为空");
        }

        ContractEntity existing = contractMapper.selectById(bo.getId());
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new RuntimeException("修改合同失败：合同不存在");
        }

        ContractEntity entity = new ContractEntity();
        BeanUtils.copyProperties(bo, entity);
        entity.setUpdateTime(LocalDateTime.now());

        contractMapper.updateById(entity);

        ContractEntity updated = contractMapper.selectById(entity.getId());
        return toVo(updated);
    }

    @Override
    public ContractVo getContractById(Long id) {
        ContractEntity entity = contractMapper.selectById(id);
        if (entity == null || entity.getIsDeleted() == 1) {
            return null;
        }
        return toVo(entity);
    }

    @Override
    public void deleteContract(Long id) {
        contractMapper.deleteById(id);
    }

    /**
     * Entity 转 Vo
     */
    private ContractVo toVo(ContractEntity entity) {
        if (entity == null) {
            return null;
        }
        ContractVo vo = new ContractVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
