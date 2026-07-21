<template>
  <BaseCard title="上传识别">
    <div class="upload-body">
      <div
        class="upload-area"
        :class="{ dragover }"
        @dragover.prevent="dragover = true"
        @dragleave.prevent="dragover = false"
        @drop.prevent="onDrop"
      >
      <input
        type="file"
        ref="fileInput"
        accept=".pdf,image/*"
        @change="onFileChange"
      />
      <div class="upload-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 16V4"></path>
          <path d="M7 9l5-5 5 5"></path>
          <path d="M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2"></path>
        </svg>
      </div>
      <div class="upload-tip">
        点击或拖拽 <span>PDF / 图片</span> 到此处上传识别
      </div>
      <div class="upload-sub-tip">支持格式：PDF、JPG、PNG、JPEG、BMP</div>
    </div>

    <!-- 文件预览（不使用图片预览，改用类型色块） -->
    <div v-if="file" class="file-preview">
      <div class="filechip" :class="fileKind === 'IMG' ? 'filechip--img' : 'filechip--pdf'">{{ fileKind }}</div>
      <div class="file-info">
        <div class="file-name">{{ file.name }}</div>
        <div class="file-size">{{ formatSize(file.size) }}</div>
      </div>
      <BaseButton variant="ghost" size="sm" @click="clearFile">清除</BaseButton>
      <BaseButton variant="primary" size="sm" :disabled="uploading" @click="uploadAndRecognize">
        {{ uploading ? '识别中...' : '开始识别' }}
      </BaseButton>
    </div>

    <!-- 识别状态 -->
    <div v-if="statusText" class="recognize-status" :class="statusType">
      {{ statusText }}
    </div>
    </div>
  </BaseCard>
</template>

<script>
/**
 * FileUpload —— 文件上传与识别入口。
 * 选择/拖拽 PDF 或图片后调用后端 /recognize，识别成功后通过 recognized 事件
 * 把结构化结果抛给父组件（App → ContractForm 自动回填）。
 */
import { ref, computed } from 'vue'
import { recognizeFile } from '../api/contract.js'
import BaseCard from './base/BaseCard.vue'
import BaseButton from './base/BaseButton.vue'

export default {
  name: 'FileUpload',
  emits: ['recognized'],
  setup(props, { emit }) {
    const fileInput = ref(null)
    const file = ref(null)
    const dragover = ref(false)
    const uploading = ref(false)
    const statusText = ref('')
    const statusType = ref('')

    // 文件类型色块标识（PDF / IMG），避免直接渲染图片预览
    const fileKind = computed(() => {
      const f = file.value
      if (!f) return ''
      return f.type.startsWith('image/') ? 'IMG' : 'PDF'
    })

    function formatSize(bytes) {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    }

    function onFileChange(e) {
      const f = e.target.files[0]
      if (!f) return
      handleFile(f)
    }

    function onDrop(e) {
      dragover.value = false
      const files = e.dataTransfer.files
      if (files.length > 0) handleFile(files[0])
    }

    function handleFile(f) {
      file.value = f
    }

    function clearFile() {
      file.value = null
      fileInput.value.value = ''
      statusText.value = ''
      statusType.value = ''
    }

    async function uploadAndRecognize() {
      if (!file.value) {
        alert('请先选择文件')
        return
      }
      uploading.value = true
      statusText.value = '正在上传并识别，请稍候...'
      statusType.value = 'loading'

      try {
        const result = await recognizeFile(file.value)
        statusText.value = '识别成功！请核对表单数据后保存。'
        statusType.value = 'success'
        if (result.data) emit('recognized', result.data)
      } catch (err) {
        statusText.value = '识别失败：' + err.message
        statusType.value = 'error'
      } finally {
        uploading.value = false
      }
    }

    return {
      fileInput, file, dragover, uploading, fileKind,
      statusText, statusType,
      onFileChange, onDrop, clearFile, uploadAndRecognize, formatSize
    }
  }
}
</script>

<style scoped>
.upload-body { padding: 16px; }
.upload-area {
  border: 2px dashed var(--border-strong);
  border-radius: var(--r);
  padding: 38px 20px;
  text-align: center;
  cursor: pointer;
  position: relative;
  transition: border-color .2s ease, background .2s ease, box-shadow .2s ease;
}
.upload-area:hover,
.upload-area.dragover {
  border-color: var(--brand);
  background: var(--gradient-soft);
  box-shadow: 0 0 0 4px rgba(17,24,39,.08);
}
.upload-area input[type="file"] {
  position: absolute;
  left: 0; top: 0; width: 100%; height: 100%;
  opacity: 0; cursor: pointer;
}
.upload-icon {
  width: 56px; height: 56px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: var(--gradient-soft);
  display: flex; align-items: center; justify-content: center;
  color: var(--brand);
}
.upload-icon svg { width: 28px; height: 28px; }
.upload-tip { color: var(--text-2); font-size: 14px; }
.upload-tip span { color: var(--brand); font-weight: 600; }
.upload-sub-tip { margin-top: 6px; color: var(--text-muted); font-size: 12px; }

.file-preview {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
}
.file-info { flex: 1; min-width: 0; }
.file-name {
  font-weight: 600;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.file-size { color: var(--text-muted); margin-top: 2px; font-size: 12px; }

.recognize-status {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: var(--r-sm);
  font-size: 13px;
}
.recognize-status.loading { background: var(--warning-bg); color: var(--warning); border: 1px solid #fde68a; }
.recognize-status.success { background: var(--success-bg); color: var(--success); border: 1px solid #a7f3d0; }
.recognize-status.error  { background: var(--danger-bg); color: var(--danger); border: 1px solid #fecaca; }
</style>
