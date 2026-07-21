<template>
  <div class="toolbar">
    <BaseButton variant="primary" @click="$emit('new')">+ 新增合同</BaseButton>
    <BaseButton variant="ghost" @click="$emit('save')">保存数据</BaseButton>
    <BaseButton variant="ghost" @click="$emit('copy')">复制数据</BaseButton>
    <BaseButton variant="ghost" @click="$emit('reset')">重置</BaseButton>
  </div>
</template>

<script>
/**
 * Toolbar —— 顶部操作栏。
 * 仅负责触发 new / save / copy / reset 四个动作，业务逻辑由父组件 App 处理。
 */
import BaseButton from './base/BaseButton.vue'

export default {
  name: 'Toolbar',
  components: { BaseButton },
  emits: ['new', 'save', 'copy', 'reset']
}
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}
</style>
