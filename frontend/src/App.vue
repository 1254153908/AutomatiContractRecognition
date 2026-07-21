<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="app-header__inner">
        <div class="app-brand">
          <div class="app-logo">资</div>
          <div>
            <div class="app-title">资产识别录入</div>
            <div class="app-sub">合同识别 · 结构化录入 · 设备入账管理</div>
          </div>
        </div>
        <Toolbar
          @new="startNew"
          @save="saveData"
          @copy="copyData"
          @reset="resetPage"
        />
      </div>
    </header>

    <main class="app-main">
      <!-- 合同列表 -->
      <ContractList ref="listRef" @edit="onEdit" />
      <!-- 文件上传区域（仅在新增/编辑时显示） -->
      <FileUpload v-if="showForm" @recognized="onRecognized" />
      <!-- 表单区域 -->
      <ContractForm
        v-if="showForm"
        ref="formRef"
        :contract-id="editingId"
        :recognized-data="recognizedData"
      />
    </main>
  </div>
</template>

<script>
/**
 * App —— 页面根组件，负责组装各模块并协调状态：
 * Toolbar（操作） / ContractList（列表） / FileUpload（上传识别） / ContractForm（表单）。
 * 关键数据流：FileUpload 识别成功 → onRecognized → recognizedData → ContractForm 自动回填。
 */
import { ref } from 'vue'
import Toolbar from './components/Toolbar.vue'
import FileUpload from './components/FileUpload.vue'
import ContractForm from './components/ContractForm.vue'
import ContractList from './components/ContractList.vue'

export default {
  name: 'App',
  components: { Toolbar, FileUpload, ContractForm, ContractList },
  setup() {
    const formRef = ref(null)
    const listRef = ref(null)
    const recognizedData = ref({})
    const showForm = ref(false)
    const editingId = ref(null)

    function startNew() {
      editingId.value = null
      recognizedData.value = {}
      showForm.value = true
      // 表单 reset 由 ContractForm watch editingId 处理
    }

    function onEdit(id) {
      if (!id) {
        showForm.value = false
        editingId.value = null
        return
      }
      editingId.value = id
      recognizedData.value = {}
      showForm.value = true
    }

    function onRecognized(data) {
      recognizedData.value = { ...data }
    }

    async function saveData() {
      if (formRef.value) {
        await formRef.value.submitForm()
        // 保存成功后刷新列表
        if (listRef.value) listRef.value.refreshList()
      }
    }

    function copyData() {
      if (formRef.value) {
        const data = formRef.value.getFormData()
        navigator.clipboard.writeText(JSON.stringify(data, null, 2))
          .then(() => alert('表单数据已复制到剪贴板'))
          .catch(() => alert('复制失败'))
      }
    }

    function resetPage() {
      if (confirm('确定要重置页面吗？所有未保存数据将丢失。')) {
        editingId.value = null
        recognizedData.value = {}
        showForm.value = false
      }
    }

    return { formRef, listRef, recognizedData, showForm, editingId, startNew, onEdit, onRecognized, saveData, copyData, resetPage }
  }
}
</script>

