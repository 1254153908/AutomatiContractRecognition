<template>
  <div class="field">
    <label class="field__label" :class="{ 'is-required': required }">{{ label }}</label>
    <slot />
  </div>
</template>

<script>
/**
 * FormField —— 设计系统表单字段外壳。
 * 渲染 .field（圆角整行）+ 左侧 .field__label（required 显红 *），
 * 默认插槽放入 <input>/<select>/<textarea>。换肤只改 global.css 的 .field*。
 */
export default {
  name: 'FormField',
  props: {
    label: { type: String, required: true },
    required: { type: Boolean, default: false }
  }
}
</script>
