package org.huhu.contract.controller;

import org.huhu.contract.Bo.ContractBo;
import org.huhu.contract.Bo.EquipmentPendingAuditBo;
import org.huhu.contract.Vo.ContractVo;
import org.huhu.contract.Vo.EquipmentPendingAuditVo;
import org.huhu.contract.common.R;
import org.huhu.contract.service.ContractServiceIntreface;
import org.huhu.contract.service.RecognitionServiceIntreface;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contract")
public class ContractController {

    private final ContractServiceIntreface contractService;
    private final RecognitionServiceIntreface recognitionService;

    public ContractController(ContractServiceIntreface contractService,
                              RecognitionServiceIntreface recognitionService) {
        this.contractService = contractService;
        this.recognitionService = recognitionService;
    }

    @PostMapping
    public R<ContractVo> add(@RequestBody ContractBo bo) {
        try {
            return R.ok("合同登记成功", contractService.addContract(bo));
        } catch (Exception e) {
            return R.fail("合同登记失败：" + e.getMessage());
        }
    }

    @PutMapping
    public R<ContractVo> update(@RequestBody ContractBo bo) {
        try {
            return R.ok("合同修改成功", contractService.updateContract(bo));
        } catch (Exception e) {
            return R.fail("合同修改失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public R<ContractVo> getById(@PathVariable Long id) {
        ContractVo vo = contractService.getContractById(id);
        if (vo == null) {
            return R.fail(404, "合同不存在");
        }
        return R.ok(vo);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        try {
            contractService.deleteContract(id);
            return R.ok("合同删除成功", null);
        } catch (Exception e) {
            return R.fail("合同删除失败：" + e.getMessage());
        }
    }

    /** 查询所有合同列表 */

    @GetMapping("/list")
    public R<List<ContractVo>> list() {
        try {
            return R.ok(contractService.listAll());
        } catch (Exception e) {
            return R.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 文件上传识别接口
     * 仅做参数校验，真正的"本地暂存 → MinIO → Python 识别 → 清理"逻辑在 RecognitionService 中。
     */
    @PostMapping("/recognize")
    public R<Map<String, Object>> recognize(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }
        try {
            Map<String, Object> data = recognitionService.recognize(file);
            return R.ok("文件识别成功", data);
        } catch (Exception e) {
            return R.fail("文件识别失败：" + e.getMessage());
        }
    }

    // ==================== 设备入账未审核 ====================

    /** 根据明细ID查询设备入账未审核记录 */
    @GetMapping("/items/{itemId}/audits")
    public R<List<EquipmentPendingAuditVo>> getAuditsByItemId(@PathVariable Long itemId) {
        try {
            return R.ok(contractService.getAuditsByItemId(itemId));
        } catch (Exception e) {
            return R.fail("查询入账信息失败：" + e.getMessage());
        }
    }

    /** 保存某条明细的设备入账未审核记录（先删后插） */
    @PostMapping("/items/{itemId}/audits")
    public R<List<EquipmentPendingAuditVo>> saveAudits(
            @PathVariable Long itemId,
            @RequestBody List<EquipmentPendingAuditBo> audits) {
        try {
            return R.ok("入账信息保存成功", contractService.saveAudits(itemId, audits));
        } catch (Exception e) {
            return R.fail("保存入账信息失败：" + e.getMessage());
        }
    }

    /** 根据合同ID查询所有设备入账未审核记录 */
    @GetMapping("/equipment-audit/by-contract/{contractId}")
    public R<List<EquipmentPendingAuditVo>> getAuditsByContractId(@PathVariable Long contractId) {
        try {
            return R.ok(contractService.getAuditsByContractId(contractId));
        } catch (Exception e) {
            return R.fail("查询入账信息失败：" + e.getMessage());
        }
    }
}
