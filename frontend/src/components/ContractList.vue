<template>
  <BaseCard title="合同列表">
    <template #actions>
      <div class="search">
        <svg class="search__icon" viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="7"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input class="search__input" type="text" v-model="keyword" placeholder="搜索合同编号 / 项目名称" />
      </div>
      <BaseButton variant="ghost" size="sm" @click="refreshList">刷新</BaseButton>
    </template>

    <div class="table-scroll">
      <table class="list-table">
        <thead>
          <tr>
            <th style="width:120px;">合同编号</th>
            <th>项目名称</th>
            <th style="width:120px;">甲方</th>
            <th style="width:120px;">乙方</th>
            <th style="width:100px;">签署日期</th>
            <th style="width:110px;">总金额(元)</th>
            <th style="width:80px;">原件</th>
            <th style="width:130px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredList.length === 0">
            <td colspan="8" class="u-empty">暂无合同数据</td>
          </tr>
          <tr v-for="row in filteredList" :key="row.id" :class="{ selected: selectedId === row.id }">
            <td class="cell-strong">{{ row.contractNo }}</td>
            <td>{{ row.projectName }}</td>
            <td>{{ row.partyA }}</td>
            <td>{{ row.partyB }}</td>
            <td>{{ row.signDate }}</td>
            <td class="u-text-right num">{{ row.totalAmount }}</td>
            <td>
              <a v-if="row.filePath" :href="row.filePath" target="_blank" rel="noopener" class="file-link">查看</a>
              <span v-else class="u-muted">—</span>
            </td>
            <td class="u-text-center">
              <BaseButton variant="ghost" size="sm" @click="onEdit(row)">编辑</BaseButton>
              <BaseButton variant="danger" size="sm" @click="onDelete(row)">删除</BaseButton>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页栏 -->
    <div class="pager" v-if="total > 0">
      <BaseButton variant="ghost" size="sm" :disabled="current <= 1" @click="goPage(1)">首页</BaseButton>
      <BaseButton variant="ghost" size="sm" :disabled="current <= 1" @click="goPrev">上一页</BaseButton>
      <span class="pager-info">第 {{ current }} / {{ totalPages }} 页</span>
      <BaseButton variant="ghost" size="sm" :disabled="current >= totalPages" @click="goNext">下一页</BaseButton>
      <BaseButton variant="ghost" size="sm" :disabled="current >= totalPages" @click="goPage(totalPages)">末页</BaseButton>

      <span class="pager-sep">|</span>
      <span class="pager-label">每页</span>
      <!-- ★ 每一页的数据条数在这里设置：修改 size 或下拉选项即可 -->
      <select class="pager-size" :value="size" @change="changeSize($event.target.value)">
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
        <option :value="100">100</option>
      </select>
      <span class="pager-label">条</span>
      <span class="pager-total">共 {{ total }} 条</span>
    </div>
  </BaseCard>
</template>

<script>
/**
 * ContractList —— 合同列表（服务端分页）。
 * 负责加载/搜索/删除合同；点击“编辑”通过 edit 事件把合同 id 抛给父组件，
 * 由 ContractForm 进入编辑模式。
 */
import { ref, computed, onMounted, watch } from 'vue'
import { pageContracts, deleteContract } from '../api/contract.js'
import BaseCard from './base/BaseCard.vue'
import BaseButton from './base/BaseButton.vue'

export default {
  name: 'ContractList',
  components: { BaseCard, BaseButton },
  emits: ['edit'],
  setup(props, { emit }) {
    // ===== 分页状态 =====
    const list = ref([])          // 当前页的合同记录（来自 /page 的 records）
    const total = ref(0)          // 全表合同总数（来自 /page 的 total）
    const current = ref(1)        // 当前页码（从 1 开始）
    const size = ref(10)          // 每页数据条数（改这里即可调整每页显示多少条）
    const loading = ref(false)
    const selectedId = ref(null)
    const keyword = ref('')

    const totalPages = computed(() =>
      size.value > 0 ? Math.max(1, Math.ceil(total.value / size.value)) : 1
    )

    // 关键字仅对“当前页”做前端过滤（后端 /page 暂未支持关键字，如需全局搜索可扩展后端）
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
        loading.value = true
        const page = await pageContracts(current.value, size.value)
        list.value = page.records || []
        total.value = page.total || 0
      } catch (e) {
        alert('加载列表失败：' + e.message)
      } finally {
        loading.value = false
      }
    }

    // 切页
    async function goPage(p) {
      const target = Math.min(Math.max(1, p), totalPages.value)
      if (target === current.value) return
      current.value = target
      await refreshList()
    }
    function goPrev() { goPage(current.value - 1) }
    function goNext() { goPage(current.value + 1) }

    // 修改每页条数：回到第 1 页重新查询
    async function changeSize(s) {
      size.value = Number(s)
      current.value = 1
      await refreshList()
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
        // 删除后若当前页已空且不是第一页，回退一页
        if (list.value.length === 1 && current.value > 1) {
          current.value -= 1
        }
        await refreshList()
        emit('edit', null) // 通知父组件清除编辑
      } catch (e) {
        alert('删除失败：' + e.message)
      }
    }

    // 搜索时回到第一页
    watch(keyword, () => { current.value = 1 })

    onMounted(() => {
      refreshList()
    })

    return {
      list, filteredList, selectedId, keyword,
      total, current, size, totalPages, loading,
      refreshList, onEdit, onDelete, goPage, goPrev, goNext, changeSize
    }
  }
}
</script>

<style scoped>
.search {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--surface);
  border: 1px solid var(--border-strong);
  border-radius: var(--r-pill);
  padding: 4px 12px;
  transition: border-color .15s ease, box-shadow .15s ease;
}
.search:focus-within {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(17,24,39,.12);
}
.search__icon { color: var(--text-muted); flex-shrink: 0; }
.search__input {
  border: none;
  outline: none;
  font-size: 12px;
  font-family: inherit;
  width: 180px;
  background: transparent;
  color: var(--text);
}

.table-scroll { overflow-x: auto; }
.list-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.list-table th {
  background: var(--surface-2);
  color: var(--text-2);
  font-weight: 600;
  border-bottom: 1px solid var(--border);
  padding: 11px 12px;
  text-align: left;
  white-space: nowrap;
}
.list-table td {
  border-bottom: 1px solid var(--border);
  padding: 11px 12px;
  color: var(--text);
}
.list-table tbody tr {
  transition: background .12s ease;
}
.list-table tbody tr:hover { background: var(--surface-2); }
.list-table tbody tr.selected { background: var(--gradient-soft); }
.cell-strong { font-weight: 600; color: var(--text); }
.num { font-variant-numeric: tabular-nums; }
.file-link {
  color: var(--brand);
  text-decoration: none;
  font-weight: 500;
}
.file-link:hover { text-decoration: underline; }

/* 分页 */
.pager {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 14px;
  border-top: 1px solid var(--border);
  background: var(--surface-2);
  border-radius: 0 0 var(--r) var(--r);
}
.pager-info { font-weight: 600; color: var(--text-2); min-width: 92px; text-align: center; }
.pager-sep { color: var(--border-strong); margin: 0 2px; }
.pager-label { color: var(--text-2); }
.pager-size {
  border: 1px solid var(--border-strong);
  border-radius: var(--r-pill);
  padding: 4px 8px;
  font-size: 12px;
  font-family: inherit;
  color: var(--text);
  background: var(--surface);
  outline: none;
}
.pager-size:focus { border-color: var(--brand); }
.pager-total { color: var(--text-muted); margin-left: 2px; }
</style>
