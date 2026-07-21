<template>
  <button
    :type="type"
    class="btn"
    :class="[`btn--${variant}`, size === 'sm' ? 'btn--sm' : '']"
    :disabled="disabled"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>
</template>

<script>
/**
 * BaseButton —— 设计系统基础按钮。
 * variant: primary(渐变主操作) | ghost(描边次操作) | danger(删除/危险)
 * size: '' | 'sm'；disabled 透传原生禁用。
 * 所有按钮统一走此组件，换肤只改 global.css 的 .btn*。
 */
export default {
  name: 'BaseButton',
  props: {
    variant: { type: String, default: 'ghost' }, // primary | ghost | danger
    size: { type: String, default: '' },          // '' | 'sm'
    disabled: { type: Boolean, default: false },
    type: { type: String, default: 'button' }
  },
  emits: ['click']
}
</script>
