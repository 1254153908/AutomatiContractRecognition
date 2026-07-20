<template>
  <div class="list-section">
    <div class="list-header">
      <span>合同列表</span>
      <div class="list-header-right">
        <input type="text" v-model="keyword" placeholder="搜索合同编号/项目名称" class="search-input" />
        <button class="btn btn-sm" @click="refreshList">刷新</button>
      </div>
    </div>
    <table class="list-table">
      <thead>
        <tr>
          <th style="width:120px;">合同编号</th>
          <th>项目名称</th>
          <th style="width:120px;">甲方</th>
          <th style="width:120px;">乙方</th>
          <th style="width:100px;">签署日期</th>
          <th style="width:100px;">总金额(元)</th>
          <th style="width:80px;">原件</th>
          <th style="width:130px;">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="filteredList.length === 0">
          <td colspan="8" class="empty-tip">暂无合同数据</td>
        </tr>
        <tr v-for="row in filteredList" :key="row.id" :class="{ selected: selectedId === row.id }">
          <td>{{ row.contractNo }}</td>
          <td>{{ row.projectName }}</td>
          <td>{{ row.partyA }}</td>
          <td>{{ row.partyB }}</td>
          <td>{{ row.signDate }}</td>
          <td class="text-right">{{ row.totalAmount }}</td>
          <td>
            <a v-if="row.filePath" :href="row.filePath" target="_blank" rel="noopener" class="file-link">查看原件</a>
            <span v-else class="no-file">—</span>
          </td>
          <td class="text-center">
            <button class="btn-action" @click="onEdit(row)">编辑</button>
            <button class="btn-action btn-del" @click="onDelete(row)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
/**
 * ContractList —— 合同列表。
 * 负责加载/搜索/删除合同；点击“编辑”通过 edit 事件把合同 id 抛给父组件，
 * 由 ContractForm 进入编辑模式。
 */
import { ref, computed, onMounted } from 'vue'
import { listContracts, deleteContract } from '../api/contract.js'

export default {
  name: 'ContractList',
  emits: ['edit'],
  setup(props, { emit }) {
    const list = ref([])
    const selectedId = ref(null)
    const keyword = ref('')

    const filteredList = computed(() => {
      if (!keyword.value) return list.value
      const kw = keyword.value.toLowerCase()
      return list.value.filter(row =>
        (row.contractNo && row.contractNo.toLowerCase().includes(kw)) ||
        (row.projectName && row.projectName.toLowerCase().includes(kw))
      )
    })


    async function refreshList() {
      try {
        list.value = await listContracts()
      } catch (e) {
        alert('加载列表失败：' + e.message)
      }
    }

    function onEdit(row) {
      selectedId.value = row.id
      emit('edit', row.id)
    }

    async function onDelete(row) {
      if (!confirm('确定删除合同【' + row.contractNo + '】吗？')) return
      try {
        await deleteContract(row.id)
        alert('删除成功')
        await refreshList()
        emit('edit', null) // 通知父组件清除编辑
      } catch (e) {
        alert('删除失败：' + e.message)
      }
    }

    onMounted(() => {
      refreshList()
    })

    return { list, filteredList, selectedId, keyword, refreshList, onEdit, onDelete }
  }
}
</script>

<style scoped>
.list-section {
  background: #fff;
  border: 1px solid #ddd;
  margin-bottom: 10px;
}
.list-header {
  background: #eef4fa;
  padding: 6px 10px;
  font-weight: bold;
  font-size: 13px;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.list-header-right {
  display: flex;
  gap: 8px;
  align-items: center;
}
.search-input {
  width: 200px;
  border: 1px solid #ccc;
  padding: 3px 6px;
  font-size: 12px;
  outline: none;
}
.search-input:focus { border-color: #1e88e5; }
.list-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.list-table th {
  background: #f5f5f5;
  border: 1px solid #ddd;
  padding: 5px 4px;
  text-align: center;
  white-space: nowrap;
}
.list-table td {
  border: 1px solid #ddd;
  padding: 4px 4px;
}
.list-table tr:hover { background: #f9f9f9; }
.list-table tr.selected { background: #e3f2fd; }
.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}
.text-center { text-align: center; }
.text-right { text-align: right; }
.status-pending { color: #f57c00; font-weight: bold; }
.status-done { color: #2e7d32; font-weight: bold; }
.status-finish { color: #1565c0; font-weight: bold; }
.file-link {
  color: #1e88e5;
  text-decoration: underline;
  cursor: pointer;
}
.file-link:hover { color: #1565c0; }
.no-file { color: #999; }
.btn-action {
  padding: 2px 8px;
  border: 1px solid #1e88e5;
  background: #fff;
  color: #1e88e5;
  cursor: pointer;
  font-size: 12px;
  border-radius: 2px;
  margin: 0 2px;
}
.btn-action:hover { background: #e3f2fd; }
.btn-action.btn-del {
  border-color: #d32f2f;
  color: #d32f2f;
}
.btn-action.btn-del:hover { background: #ffebee; }
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
.btn-sm { padding: 3px 10px; font-size: 11px; }
</style>
