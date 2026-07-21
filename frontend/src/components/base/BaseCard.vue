<template>
  <div class="card">
    <div v-if="title || $slots.header || $slots.actions" class="section-title">
      <slot name="header">
        <span>{{ title }}</span>
      </slot>
      <span v-if="$slots.actions" class="section-actions">
        <slot name="actions" />
      </span>
    </div>
    <slot />
  </div>
</template>

<script>
/**
 * BaseCard —— 设计系统卡片容器。
 * - title 或 #header 插槽：渲染 .section-title（带渐变强调条）；
 * - #actions 插槽：标题栏右侧操作区（自动右对齐）。
 * 默认插槽：卡片主体内容。
 */
export default {
  name: 'BaseCard',
  props: {
    title: { type: String, default: '' }
  }
}
</script>
