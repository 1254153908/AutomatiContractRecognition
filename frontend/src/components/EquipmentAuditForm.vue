<template>
  <div class="modal-overlay" v-if="visible" @click.self="$emit('close')">
    <div class="audit-card">
      <div class="card-header">
        <span>设备入账信息 - {{ item.productName || '未命名明细' }}</span>
        <button class="btn-close" @click="$emit('close')">×</button>
      </div>

      <div class="card-body">
        <div class="toolbar-row">
          <button class="btn btn-sm" @click="addEquipment">+ 添加设备</button>
          <span class="hint">每行代表一台设备</span>
        </div>

        <div v-if="equipments.length === 0" class="empty-tip">
          暂未添加设备，请点击“添加设备”开始录入
        </div>

        <div class="equipment-list">
          <div v-for="(eq, index) in equipments" :key="eq._key" class="equipment-card">
            <div class="equipment-header">
              <span class="equipment-title">设备 {{ index + 1 }}</span>
              <button class="btn-del" @click="removeEquipment(index)">删除</button>
            </div>

            <div class="equipment-grid">
              <div class="form-row">
                <label class="required">使用单位号</label>
                <input type="text" v-model="eq.lydwh" placeholder="请输入" />
              </div>
              <div class="form-row">
                <label>使用单位名</label>
                <input type="text" v-model="eq.lydwm" placeholder="请输入" />
              </div>
              <div class="form-row">
                <label class="required">设备编号区间</label>
                <input type="text" v-model="eq.zcbhqj" placeholder="请输入" />
              </div>
              <div class="form-row">
                <label class="required">分类号</label>
                <input type="text" v-model="eq.zcflh" placeholder="请输入" />
              </div>
              <div class="form-row">
                <label class="required">设备名称</label>
                <input type="text" v-model="eq.zcmc" placeholder="请输入" />
              </div>
              <div class="form-row">
                <label>品牌型号</label>
                <input type="text" v-model="eq.ppxh" placeholder="无则填 *" />
              </div>
              <div class="form-row">
                <label>规格</label>
                <input type="text" v-model="eq.gg" placeholder="无则填 *" />
              </div>
              <div class="form-row">
                <label>数量</label>
                <input type="number" v-model.number="eq.sl" min="0" />
              </div>
              <div class="form-row">
                <label>单价(元)</label>
                <input type="number" v-model.number="eq.dj" step="0.01" min="0" @input="calcJe(eq)" />
              </div>
              <div class="form-row">
                <label class="required">金额(元)</label>
                <input type="number" v-model.number="eq.je" step="0.01" min="0" />
              </div>
              <div class="form-row">
                <label>计量单位</label>
                <select v-model="eq.jldw">
                  <option value="台">台</option>
                  <option value="套">套</option>
                  <option value="张">张</option>
                  <option value="个">个</option>
                </select>
              </div>
              <div class="form-row">
                <label>厂家</label>
                <input type="text" v-model="eq.cj" placeholder="无则填 无" />
              </div>
              <div class="form-row">
                <label class="required">购置日期</label>
                <input type="date" v-model="eq.ggrq" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card-footer">
        <button class="btn" @click="confirmSave">保存</button>
        <button class="btn btn-secondary" @click="$emit('close')">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * EquipmentAuditForm —— 设备入账未审核编辑卡片（宽屏卡片式）。
 * 该卡片只编辑本地数据，不直接调用后端接口；确认后通过 update 事件把
 *  equipments 数组回传给父组件，随合同保存时一并自动提交。
 */
import { ref, watch } from 'vue'

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
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.audit-card {
  background: #fff;
  width: 95%;
  max-width: 1400px;
  max-height: 90vh;
  border-radius: 4px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.22);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.card-header {
  background: #1e88e5;
  color: #fff;
  padding: 10px 20px;
  font-size: 15px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.btn-close {
  background: none;
  border: none;
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
}
.btn-close:hover { opacity: 0.7; }
.card-body {
  flex: 1;
  overflow: auto;
  padding: 16px 20px;
  background: #f5f7fa;
}
.toolbar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.hint {
  color: #888;
  font-size: 12px;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 30px;
  font-size: 13px;
  background: #fff;
  border: 1px dashed #ddd;
}

.equipment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.equipment-card {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}
.equipment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  background: #eef4fa;
  border-bottom: 1px solid #ddd;
}
.equipment-title {
  font-weight: bold;
  font-size: 13px;
  color: #333;
}
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  padding: 8px 14px 14px;
}
.form-row {
  display: flex;
  align-items: stretch;
  border: 1px solid #ddd;
  margin: -1px 0 0 -1px;
  min-height: 34px;
}
.form-row label {
  width: 100px;
  background: #f9f9f9;
  padding: 7px 8px;
  text-align: right;
  color: #333;
  font-size: 12px;
  border-right: 1px solid #ddd;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.form-row label.required::before {
  content: "*";
  color: #d32f2f;
  margin-right: 2px;
}
.form-row input,
.form-row select {
  flex: 1;
  border: none;
  padding: 6px 8px;
  font-size: 12px;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
  min-width: 0;
}
.form-row input:focus,
.form-row select:focus { background: #f0f8ff; }
.card-footer {
  display: flex;
  justify-content: flex-end;
  padding: 12px 20px;
  gap: 10px;
  background: #fff;
  border-top: 1px solid #ddd;
}
.btn {
  padding: 5px 18px;
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
.btn-sm { padding: 4px 12px; font-size: 12px; }
.btn-del {
  padding: 2px 10px;
  border: 1px solid #d32f2f;
  background: #fff;
  color: #d32f2f;
  cursor: pointer;
  font-size: 12px;
  border-radius: 2px;
}
.btn-del:hover { background: #ffebee; }

@media (max-width: 900px) {
  .equipment-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .equipment-grid { grid-template-columns: 1fr; }
}
</style>
