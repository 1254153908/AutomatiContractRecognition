<template>
  <div class="form-section">
    <div class="form-header">
      {{ contractId ? '编辑合同' : '新增合同' }}（*为必填项）
    </div>

    <!-- 合同基本信息 -->
    <div class="form-body">
      <div class="form-row">
        <div class="form-group">
          <label class="form-label required">合同编号</label>
          <input type="text" v-model="form.contractNo" placeholder="请输入合同编号" />
        </div>
        <div class="form-group">
          <label class="form-label required">项目名称</label>
          <input type="text" v-model="form.projectName" placeholder="请输入项目名称" />
        </div>
        <div class="form-group">
          <label class="form-label required">甲方</label>
          <input type="text" v-model="form.partyA" placeholder="请输入甲方" />
        </div>
        <div class="form-group">
          <label class="form-label required">乙方</label>
          <input type="text" v-model="form.partyB" placeholder="请输入乙方" />
        </div>
        <div class="form-group">
          <label class="form-label required">签署日期</label>
          <input type="date" v-model="form.signDate" />
        </div>
        <div class="form-group">
          <label class="form-label required">总金额(元)</label>
          <input type="number" v-model.number="form.totalAmount" step="0.01" min="0" placeholder="请输入总金额" />
        </div>
      </div>
    </div>

    <!-- 合同明细 -->
    <div class="form-header" style="background:#fff;border-bottom:1px solid #eee;">
      <span>合同明细</span>
      <button class="btn btn-sm" @click="addItem">+ 添加明细</button>
    </div>
    <div class="table-wrap">
      <table class="item-table">
        <thead>
          <tr>
            <th style="width:50px;">序号</th>
            <th>产品名称</th>
            <th style="width:90px;">数量</th>
            <th style="width:120px;">单价(元)</th>
            <th style="width:120px;">总价(元)</th>
            <th style="width:80px;">单位</th>
            <th>型号</th>
            <th style="width:110px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="form.items.length === 0">
            <td colspan="9" class="empty-tip">暂无明细，请点击"添加明细"</td>
          </tr>
          <tr v-for="(item, index) in form.items" :key="index">
            <td class="text-center">{{ index + 1 }}</td>
            <td><input type="text" v-model="item.productName" placeholder="请输入" /></td>
            <td><input type="number" v-model.number="item.quantity" min="1" /></td>
            <td><input type="number" v-model.number="item.unitPrice" step="0.01" min="0" @input="calcTotal(item)" /></td>
            <td><input type="number" v-model.number="item.totalPrice" step="0.01" min="0" /></td>
            <td><input type="text" v-model="item.unit" placeholder="" /></td>
            <td><input type="text" v-model="item.specification" placeholder="" /></td>
            <td class="text-center">
              <button class="btn-audit" @click="openAudit(item)">设备入账</button>
              <button class="btn-del" @click="removeItem(index)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 底部按钮 -->
    <div class="form-footer">
      <button class="btn" @click="submitForm">{{ contractId ? '修改保存' : '保存数据' }}</button>
      <button class="btn btn-secondary" @click="resetForm">重置页面</button>
    </div>

    <!-- 设备入账编辑弹出卡片 -->
    <EquipmentAuditForm
      :visible="auditVisible"
      :item="auditItem"
      @close="auditVisible = false"
      @update="onAuditUpdate"
    />
  </div>
</template>

<script>
/**
 * ContractForm —— 合同新增/编辑表单。
 * - contractId 有值：编辑模式，自动加载该合同数据；
 * - contractId 为空：新增模式；
 * - recognizedData：识别接口返回的结构化合同字段，到达后自动回填（用户可继续修改再保存）。
 */
import { reactive, ref, watch, toRaw } from 'vue'
import { addContract, updateContract, getContract } from '../api/contract.js'
import EquipmentAuditForm from './EquipmentAuditForm.vue'

// —— 表单工厂：保证每次重置都是干净对象 ——
const emptyItem = () => ({
  productName: '',
  quantity: 1,
  unitPrice: null,
  totalPrice: null,
  unit: '',
  specification: '',
  equipments: []
})

const newForm = () => ({
  contractNo: '',
  projectName: '',
  partyA: '',
  partyB: '',
  signDate: '',
  totalAmount: null,
  filePath: '',
  status: 0,
  items: []
})

// 必填项校验配置（key 与表单字段对应）
const requiredFields = [
  { key: 'contractNo', label: '合同编号' },
  { key: 'projectName', label: '项目名称' },
  { key: 'partyA', label: '甲方' },
  { key: 'partyB', label: '乙方' },
  { key: 'signDate', label: '签署日期' },
  { key: 'totalAmount', label: '总金额' }
]

export default {
  name: 'ContractForm',
  components: { EquipmentAuditForm },
  props: {
    contractId: { type: [Number, String], default: null },
    recognizedData: { type: Object, default: () => ({}) }
  },
  setup(props) {
    const form = reactive(newForm())
    const auditVisible = ref(false)
    const auditItem = ref({})

    // 明细字段映射：统一做空值兜底，避免多处重复
    function mapItem(it) {
      return {
        id: it.id || undefined,        // 保留 id 用于更新
        productName: it.productName || '',
        quantity: it.quantity || 1,
        unitPrice: it.unitPrice || null,
        totalPrice: it.totalPrice || null,
        unit: it.unit || '',
        specification: it.specification || '',
        equipments: it.equipments || []
      }
    }

    // 把后端返回的合同数据填充到表单（主表 + 明细）
    function fillForm(data) {
      if (!data) return
      form.contractNo = data.contractNo || ''
      form.projectName = data.projectName || ''
      form.partyA = data.partyA || ''
      form.partyB = data.partyB || ''
      form.signDate = data.signDate || ''
      form.totalAmount = data.totalAmount ?? null
      form.filePath = data.filePath || ''
      form.status = data.status ?? 0
      form.items = (data.items || []).map(mapItem)
    }

    // 加载已有合同（编辑模式 / 重置时复用，避免重复 watch）
    async function loadContract(id) {
      try {
        fillForm(await getContract(id))
      } catch (e) {
        alert('加载合同失败：' + e.message)
      }
    }

    // contractId 变化 → 加载已有合同 or 清空为新增
    watch(
      () => props.contractId,
      (id) => (id ? loadContract(id) : Object.assign(form, newForm())),
      { immediate: true }
    )

    // 识别结果到达 → 自动回填（只覆盖识别到的字段，未识别项保留为空供用户填写）
    watch(
      () => props.recognizedData,
      (data) => {
        if (!data || Object.keys(data).length === 0) return
        if (data.contractNo) form.contractNo = data.contractNo
        if (data.projectName) form.projectName = data.projectName
        if (data.partyA) form.partyA = data.partyA
        if (data.partyB) form.partyB = data.partyB
        if (data.signDate) form.signDate = data.signDate
        if (data.totalAmount) form.totalAmount = data.totalAmount
        if (data.filePath) form.filePath = data.filePath
        if (data.items && Array.isArray(data.items)) {
          form.items = data.items.map(mapItem)
        }
      },
      { deep: true }
    )

    function addItem() {
      form.items.push(emptyItem())
    }

    function removeItem(index) {
      form.items.splice(index, 1)
    }

    // 单价/数量变动时自动算总价
    function calcTotal(item) {
      if (item.quantity != null && item.unitPrice != null) {
        item.totalPrice = parseFloat((item.quantity * item.unitPrice).toFixed(2))
      }
    }

    function getFormData() {
      return { ...toRaw(form) }
    }

    async function submitForm() {
      for (const field of requiredFields) {
        const val = form[field.key]
        if (val === undefined || val === null || val === '') {
          alert('请填写必填项：' + field.label)
          return
        }
      }
      try {
        const payload = { ...toRaw(form), id: props.contractId || null }
        if (props.contractId) {
          await updateContract(payload)
          alert('修改成功！')
        } else {
          await addContract(payload)
          alert('新增成功！')
        }
      } catch (err) {
        alert('保存失败：' + err.message)
      }
    }

    function resetForm() {
      if (!confirm('确定要重置表单吗？未保存数据将丢失。')) return
      // 复用 loadContract，不再像旧代码那样在 reset 里新建 watch（会泄漏且逻辑错误）
      if (props.contractId) loadContract(props.contractId)
      else Object.assign(form, newForm())
    }

    /** 打开设备入账编辑卡片（明细未保存时也能编辑本地数据） */
    function openAudit(item) {
      auditItem.value = item
      auditVisible.value = true
    }

    /** 卡片保存后更新当前明细的本地 equipments */
    function onAuditUpdate(equipments) {
      auditItem.value.equipments = equipments
    }

    return { form, auditVisible, auditItem, addItem, removeItem, calcTotal, getFormData, submitForm, resetForm, openAudit, onAuditUpdate }
  }
}
</script>

<style scoped>
.form-section {
  background: #fff;
  border: 1px solid #ddd;
}
.form-header {
  background: #eef4fa;
  padding: 6px 10px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.form-body { padding: 10px; }
.form-row {
  display: flex;
  flex-wrap: wrap;
}
.form-group {
  width: 50%;
  display: flex;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  min-height: 32px;
  align-items: center;
}
.form-group:nth-child(2n) { border-right: none; }
.form-label {
  width: 90px;
  background: #f9f9f9;
  padding: 6px 8px;
  text-align: right;
  color: #333;
  border-right: 1px solid #eee;
  flex-shrink: 0;
  font-size: 12px;
}
.form-label.required::before {
  content: "*";
  color: #d32f2f;
  margin-right: 2px;
}
.form-group input {
  flex: 1;
  border: 1px solid #ccc;
  padding: 3px 5px;
  font-size: 12px;
  outline: none;
  font-family: inherit;
  margin: 3px 5px;
}
.form-group input:focus { border-color: #1e88e5; }

/* 明细表格 */
.table-wrap { padding: 0 10px 10px; }
.item-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.item-table th {
  background: #f5f5f5;
  border: 1px solid #ddd;
  padding: 5px 4px;
  font-weight: bold;
  text-align: center;
}
.item-table td {
  border: 1px solid #ddd;
  padding: 2px 4px;
}
.item-table td input {
  width: 100%;
  border: 1px solid #ccc;
  padding: 3px 4px;
  font-size: 12px;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
}
.item-table td input:focus { border-color: #1e88e5; }
.empty-tip {
  text-align: center;
  color: #999;
  padding: 16px;
}
.text-center { text-align: center; }
.btn-audit {
  padding: 2px 8px;
  border: 1px solid #2e7d32;
  background: #fff;
  color: #2e7d32;
  cursor: pointer;
  font-size: 12px;
  border-radius: 2px;
  margin: 0 1px;
}
.btn-audit:hover { background: #e8f5e9; }
.btn-del {
  padding: 2px 8px;
  border: 1px solid #d32f2f;
  background: #fff;
  color: #d32f2f;
  cursor: pointer;
  font-size: 12px;
  border-radius: 2px;
  margin: 0 1px;
}
.btn-del:hover { background: #ffebee; }

/* 底部 */
.form-footer {
  display: flex;
  justify-content: flex-end;
  padding: 10px;
  gap: 8px;
  background: #f9f9f9;
  border-top: 1px solid #ddd;
}
.btn {
  padding: 5px 14px;
  border: 1px solid #1e88e5;
  background: #1e88e5;
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  border-radius: 3px;
}
.btn:hover { background: #1565c0; }
.btn-secondary {
  background: #fff;
  color: #333;
  border: 1px solid #ccc;
}
.btn-secondary:hover { background: #f0f0f0; }
.btn-sm { padding: 2px 10px; font-size: 11px; }
</style>
