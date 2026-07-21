package org.huhu.contract.service.Impl;

import org.huhu.contract.Bo.ContractBo;
import org.huhu.contract.Bo.ContractItemBo;
import org.huhu.contract.Bo.EquipmentPendingAuditBo;
import org.huhu.contract.Vo.ContractItemVo;
import org.huhu.contract.Vo.ContractVo;
import org.huhu.contract.Vo.EquipmentPendingAuditVo;
import org.huhu.contract.entity.ContractEntity;
import org.huhu.contract.entity.ContractItemEntity;
import org.huhu.contract.entity.EquipmentPendingAuditEntity;
import org.huhu.contract.mapper.ContractItemMapper;
import org.huhu.contract.mapper.ContractMapper;
import org.huhu.contract.mapper.EquipmentPendingAuditMapper;
import org.huhu.contract.service.ContractServiceIntreface;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractService implements ContractServiceIntreface {

    private final ContractMapper contractMapper;
    private final ContractItemMapper itemMapper;
    private final EquipmentPendingAuditMapper auditMapper;

    public ContractService(ContractMapper contractMapper,
                           ContractItemMapper itemMapper,
                           EquipmentPendingAuditMapper auditMapper) {
        this.contractMapper = contractMapper;
        this.itemMapper = itemMapper;
        this.auditMapper = auditMapper;
    }

    @Override
    @Transactional
    public ContractVo addContract(ContractBo bo) {
        // 1. 插入合同主表
        ContractEntity contract = new ContractEntity();
        BeanUtils.copyProperties(bo, contract);
        contract.setCreatedAt(LocalDateTime.now());
        contractMapper.insert(contract);

        // 2. 批量插入明细并级联保存设备入账记录
        if (!CollectionUtils.isEmpty(bo.getItems())) {
            List<ContractItemEntity> items = bo.getItems().stream().map(itemBo -> {
                ContractItemEntity item = new ContractItemEntity();
                BeanUtils.copyProperties(itemBo, item);
                item.setContractId(contract.getId());
                return item;
            }).collect(Collectors.toList());
            itemMapper.insertBatch(items);

            // 2.1 保存每条明细对应的设备入账信息（随合同一并自动录入）
            for (int i = 0; i < items.size(); i++) {
                ContractItemEntity item = items.get(i);
                ContractItemBo itemBo = bo.getItems().get(i);
                saveItemEquipments(item.getId(), itemBo.getEquipments());
            }
        }

        // 3. 返回完整数据
        return getContractById(contract.getId());
    }

    @Override
    @Transactional
    public ContractVo updateContract(ContractBo bo) {
        if (bo.getId() == null) {
            throw new RuntimeException("修改失败：ID不能为空");
        }

        // 1. 更新合同主表
        ContractEntity contract = new ContractEntity();
        BeanUtils.copyProperties(bo, contract);
        contractMapper.updateById(contract);

        // 2. 先删后插刷新明细（设备入账记录随明细级联删除）
        itemMapper.deleteByContractId(bo.getId());

        if (!CollectionUtils.isEmpty(bo.getItems())) {
            List<ContractItemEntity> items = bo.getItems().stream().map(itemBo -> {
                ContractItemEntity item = new ContractItemEntity();
                BeanUtils.copyProperties(itemBo, item);
                item.setContractId(bo.getId());
                return item;
            }).collect(Collectors.toList());
            itemMapper.insertBatch(items);

            // 2.1 重新保存每条明细对应的设备入账信息
            for (int i = 0; i < items.size(); i++) {
                ContractItemEntity item = items.get(i);
                ContractItemBo itemBo = bo.getItems().get(i);
                saveItemEquipments(item.getId(), itemBo.getEquipments());
            }
        }

        // 3. 返回完整数据
        return getContractById(bo.getId());
    }

    @Override
    public ContractVo getContractById(Long id) {
        ContractEntity contract = contractMapper.selectById(id);
        if (contract == null) {
            return null;
        }

        ContractVo vo = new ContractVo();
        BeanUtils.copyProperties(contract, vo);

        List<ContractItemEntity> items = itemMapper.selectByContractId(id);
        if (!CollectionUtils.isEmpty(items)) {
            vo.setItems(items.stream().map(entity -> {
                ContractItemVo itemVo = new ContractItemVo();
                BeanUtils.copyProperties(entity, itemVo);
                // 加载该明细下的设备入账记录
                List<EquipmentPendingAuditEntity> audits = auditMapper.selectByItemId(entity.getId());
                if (!CollectionUtils.isEmpty(audits)) {
                    itemVo.setEquipments(audits.stream().map(a -> {
                        EquipmentPendingAuditVo auditVo = new EquipmentPendingAuditVo();
                        BeanUtils.copyProperties(a, auditVo);
                        return auditVo;
                    }).collect(Collectors.toList()));
                }
                return itemVo;
            }).collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    public List<ContractVo> listAll() {
        List<ContractEntity> entities = contractMapper.selectList(null);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        List<ContractVo> vos = new ArrayList<>();
        for (ContractEntity entity : entities) {
            ContractVo vo = new ContractVo();
            BeanUtils.copyProperties(entity, vo);
            // 查询明细（列表页不加载设备入账信息以提升性能）
            List<ContractItemEntity> items = itemMapper.selectByContractId(entity.getId());
            if (!CollectionUtils.isEmpty(items)) {
                vo.setItems(items.stream().map(it -> {
                    ContractItemVo itemVo = new ContractItemVo();
                    BeanUtils.copyProperties(it, itemVo);
                    return itemVo;
                }).collect(Collectors.toList()));
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public IPage<ContractVo> pageContracts(Page<ContractEntity> page) {
        // 分页查主表（总数与当前页记录由 MyBatis-Plus 分页拦截器填充）
        contractMapper.selectPage(page, null);

        List<ContractVo> vos = new ArrayList<>();
        for (ContractEntity entity : page.getRecords()) {
            ContractVo vo = new ContractVo();
            BeanUtils.copyProperties(entity, vo);
            // 关联明细（与 listAll 保持一致，列表页不加载设备入账信息）
            List<ContractItemEntity> items = itemMapper.selectByContractId(entity.getId());
            if (!CollectionUtils.isEmpty(items)) {
                vo.setItems(items.stream().map(it -> {
                    ContractItemVo itemVo = new ContractItemVo();
                    BeanUtils.copyProperties(it, itemVo);
                    return itemVo;
                }).collect(Collectors.toList()));
            }
            vos.add(vo);
        }
        // 用转换后的 VO 列表构造新的分页对象，保留 total/current/size 等分页信息
        IPage<ContractVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(vos);
        return result;
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        // 外键 ON DELETE CASCADE 自动级联删除明细和设备入账记录
        contractMapper.deleteById(id);
    }

    // 保存单条明细的设备入账记录（新增/修改时复用）
    private void saveItemEquipments(Long itemId, List<EquipmentPendingAuditBo> audits) {
        if (CollectionUtils.isEmpty(audits)) {
            return;
        }
        List<EquipmentPendingAuditEntity> entities = audits.stream().map(auditBo -> {
            EquipmentPendingAuditEntity entity = new EquipmentPendingAuditEntity();
            BeanUtils.copyProperties(auditBo, entity);
            entity.setId(null); // 重新生成主键，避免与旧记录冲突
            entity.setItemId(itemId);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        }).collect(Collectors.toList());
        auditMapper.insertBatch(entities);
    }

    // ==================== 设备入账未审核 ====================

    @Override
    public List<EquipmentPendingAuditVo> getAuditsByItemId(Long itemId) {
        List<EquipmentPendingAuditEntity> entities = auditMapper.selectByItemId(itemId);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        return entities.stream().map(e -> {
            EquipmentPendingAuditVo vo = new EquipmentPendingAuditVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<EquipmentPendingAuditVo> saveAudits(Long itemId, List<EquipmentPendingAuditBo> audits) {
        // 先删后插
        auditMapper.deleteByItemId(itemId);

        if (!CollectionUtils.isEmpty(audits)) {
            List<EquipmentPendingAuditEntity> entities = audits.stream().map(bo -> {
                EquipmentPendingAuditEntity entity = new EquipmentPendingAuditEntity();
                BeanUtils.copyProperties(bo, entity);
                entity.setId(null);
                entity.setItemId(itemId);
                entity.setCreatedAt(LocalDateTime.now());
                return entity;
            }).collect(Collectors.toList());
            auditMapper.insertBatch(entities);
            // 返回刚插入的记录
            return entities.stream().map(e -> {
                EquipmentPendingAuditVo vo = new EquipmentPendingAuditVo();
                BeanUtils.copyProperties(e, vo);
                return vo;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<EquipmentPendingAuditVo> getAuditsByContractId(Long contractId) {
        List<EquipmentPendingAuditEntity> entities = auditMapper.selectByContractId(contractId);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        return entities.stream().map(e -> {
            EquipmentPendingAuditVo vo = new EquipmentPendingAuditVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
