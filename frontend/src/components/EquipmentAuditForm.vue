<template>
  <div class="modal-overlay" v-if="visible" @click.self="$emit('close')">
    <BaseCard class="audit-card">
      <template #header>
        <span>设备入账信息 - {{ item.productName || '未命名明细' }}</span>
        <button class="modal-close" @click="$emit('close')">×</button>
      </template>

      <div class="card-body">
        <div class="toolbar-row">
          <BaseButton variant="primary" size="sm" @click="addEquipment">+ 添加设备</BaseButton>
          <span class="hint">每行代表一台设备</span>
        </div>

        <div v-if="equipments.length === 0" class="u-empty">
          暂未添加设备，请点击“添加设备”开始录入
        </div>

        <div class="equipment-list">
          <div v-for="(eq, index) in equipments" :key="eq._key" class="equipment-card">
            <div class="equipment-header">
              <span class="equipment-title">设备 {{ index + 1 }}</span>
              <BaseButton variant="danger" size="sm" @click="removeEquipment(index)">删除</BaseButton>
            </div>

            <div class="equipment-grid">
              <FormField label="使用单位号" required><input type="text" v-model="eq.lydwh" placeholder="请输入" /></FormField>
              <FormField label="使用单位名"><input type="text" v-model="eq.lydwm" placeholder="请输入" /></FormField>
              <FormField label="设备编号区间" required><input type="text" v-model="eq.zcbhqj" placeholder="请输入" /></FormField>
              <FormField label="分类号" required><input type="text" v-model="eq.zcflh" placeholder="请输入" /></FormField>
              <FormField label="设备名称" required><input type="text" v-model="eq.zcmc" placeholder="请输入" /></FormField>
              <FormField label="品牌型号"><input type="text" v-model="eq.ppxh" placeholder="无则填 *" /></FormField>
              <FormField label="规格"><input type="text" v-model="eq.gg" placeholder="无则填 *" /></FormField>
              <FormField label="数量"><input type="number" v-model.number="eq.sl" min="0" /></FormField>
              <FormField label="单价(元)"><input type="number" v-model.number="eq.dj" step="0.01" min="0" @input="calcJe(eq)" /></FormField>
              <FormField label="金额(元)" required><input type="number" v-model.number="eq.je" step="0.01" min="0" /></FormField>
              <FormField label="计量单位">
                <select v-model="eq.jldw">
                  <option value="台">台</option>
                  <option value="套">套</option>
                  <option value="张">张</option>
                  <option value="个">个</option>
                </select>
              </FormField>
              <FormField label="厂家"><input type="text" v-model="eq.cj" placeholder="无则填 无" /></FormField>
              <FormField label="购置日期" required><input type="date" v-model="eq.ggrq" /></FormField>
            </div>
          </div>
        </div>
      </div>

      <div class="card-footer">
        <BaseButton variant="primary" @click="confirmSave">保存</BaseButton>
        <BaseButton variant="ghost" @click="$emit('close')">取消</BaseButton>
      </div>
    </BaseCard>
  </div>
</template>

<script>
/**
 * EquipmentAuditForm —— 设备入账未审核编辑卡片（宽屏卡片式）。
 * 该卡片只编辑本地数据，不直接调用后端接口；确认后通过 update 事件把
 *  equipments 数组回传给父组件，随合同保存时一并自动提交。
 */
import { ref, watch } from 'vue'
import BaseCard from './base/BaseCard.vue'
import BaseButton from './base/BaseButton.vue'
import FormField from './base/FormField.vue'

let keyCounter = 0
function newKey() {
  return '_eq_' + (++keyCounter)
}

const emptyEquipment = () => ({
  _key: newKey(),
  lydwh: '',
  lydwm: '',
  zcbhqj: '',
  zcflh: '',
  zcmc: '',
  ppxh: '*',
  gg: '*',
  sl: 0,
  dj: 0,
  je: 0,
  jldw: '台',
  cj: '无',
  ggrq: ''
})

export default {
  name: 'EquipmentAuditForm',
  components: { BaseCard, BaseButton, FormField },
  props: {
    visible: { type: Boolean, default: false },
    item: { type: Object, default: () => ({}) }
  },
  emits: ['close', 'update'],
  setup(props, { emit }) {
    const equipments = ref([])

    // 打开时从 item.equipments 初始化本地编辑副本
    watch(
      () => [props.visible, props.item],
      ([vis]) => {
        if (!vis) {
          equipments.value = []
          return
        }
        const list = props.item?.equipments || []
        equipments.value = list.map(e => ({ ...e, _key: newKey() }))
      },
      { immediate: true, deep: true }
    )

    function addEquipment() {
      equipments.value.push(emptyEquipment())
    }

    function removeEquipment(index) {
      equipments.value.splice(index, 1)
    }

    function calcJe(eq) {
      if (eq.sl != null && eq.dj != null) {
        eq.je = parseFloat((eq.sl * eq.dj).toFixed(2))
      }
    }

    function confirmSave() {
      for (let i = 0; i < equipments.value.length; i++) {
        const eq = equipments.value[i]
        if (!eq.lydwh) return alert(`设备 ${i + 1}：使用单位号不能为空`)
        if (!eq.zcbhqj) return alert(`设备 ${i + 1}：设备编号区间不能为空`)
        if (!eq.zcflh) return alert(`设备 ${i + 1}：分类号不能为空`)
        if (!eq.zcmc) return alert(`设备 ${i + 1}：设备名称不能为空`)
        if (!eq.je && eq.je !== 0) return alert(`设备 ${i + 1}：金额不能为空`)
        if (!eq.ggrq) return alert(`设备 ${i + 1}：购置日期不能为空`)
      }

      // 去掉 _key 后回传
      const payload = equipments.value.map(e => {
        const { _key, ...rest } = e
        return rest
      })
      emit('update', payload)
      emit('close')
    }

    return { equipments, addEquipment, removeEquipment, calcJe, confirmSave }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,23,42,.45);
  backdrop-filter: blur(2px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 24px;
}
.audit-card {
  width: 100%;
  max-width: 1100px;
  max-height: 90vh;
  border-radius: var(--r-lg);
  box-shadow: var(--sh-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.modal-close {
  background: none;
  border: none;
  color: var(--text-2);
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  padding: 0 4px;
  transition: color .15s ease;
}
.modal-close:hover { color: var(--brand); }
.card-body {
  flex: 1;
  overflow: auto;
  padding: 18px 22px;
  background: var(--bg);
}
.toolbar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.hint { color: var(--text-muted); font-size: 12px; }

.equipment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.equipment-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r);
  box-shadow: var(--sh-sm);
  overflow: hidden;
}
.equipment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: var(--surface-2);
  border-bottom: 1px solid var(--border);
}
.equipment-title { font-weight: 700; font-size: 13px; color: var(--text); }
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 16px;
}
.card-footer {
  display: flex;
  justify-content: flex-end;
  padding: 14px 22px;
  gap: 10px;
  background: var(--surface-2);
  border-top: 1px solid var(--border);
}

@media (max-width: 900px) {
  .equipment-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .equipment-grid { grid-template-columns: 1fr; }
}
</style>
