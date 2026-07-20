<template>
  <div class="toolbar">
    <button class="btn btn-secondary" @click="$emit('new')">+ 新 增</button>
    <button class="btn" @click="$emit('save')">保存数据</button>
    <button class="btn btn-secondary" @click="$emit('copy')">复制数据</button>
    <button class="btn btn-secondary" @click="$emit('reset')">重置页面</button>
  </div>
</template>

<script>
/**
 * Toolbar —— 顶部操作栏。
 * 仅负责触发 new / save / copy / reset 四个动作，业务逻辑由父组件 App 处理。
 */
export default {
  name: 'Toolbar',
  emits: ['new', 'save', 'copy', 'reset']
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  padding: 8px;
  background: #fff;
  border: 1px solid #ddd;
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
</style>
