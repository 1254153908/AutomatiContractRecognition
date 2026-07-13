package org.huhu.contract.controller;

import org.huhu.contract.Bo.ContractBo;
import org.huhu.contract.Vo.ContractVo;
import org.huhu.contract.common.R;
import org.huhu.contract.service.ContractServiceIntreface;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contract")
public class ContractController {

    private final ContractServiceIntreface contractService;

    public ContractController(ContractServiceIntreface contractService) {
        this.contractService = contractService;
    }

    /**
     * 合同登记（新增）
     */
    @PostMapping
    public R<ContractVo> add(@RequestBody ContractBo bo) {
        try {
            ContractVo vo = contractService.addContract(bo);
            return R.ok("合同登记成功", vo);
        } catch (Exception e) {
            return R.fail("合同登记失败：" + e.getMessage());
        }
    }

    /**
     * 合同修改
     */
    @PutMapping
    public R<ContractVo> update(@RequestBody ContractBo bo) {
        try {
            ContractVo vo = contractService.updateContract(bo);
            return R.ok("合同修改成功", vo);
        } catch (Exception e) {
            return R.fail("合同修改失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询合同
     */
    @GetMapping("/{id}")
    public R<ContractVo> getById(@PathVariable Long id) {
        ContractVo vo = contractService.getContractById(id);
        if (vo == null) {
            return R.fail(404, "合同不存在");
        }
        return R.ok(vo);
    }

    /**
     * 删除合同
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            contractService.deleteContract(id);
            return R.ok("合同删除成功", null);
        } catch (Exception e) {
            return R.fail("合同删除失败：" + e.getMessage());
        }
    }
}
