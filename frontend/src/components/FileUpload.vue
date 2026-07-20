<template>
  <div class="upload-section">
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
      <div class="upload-icon">📄</div>
      <div class="upload-tip">
        点击或拖拽 <span>PDF / 图片</span> 到此处上传识别
      </div>
      <div class="upload-sub-tip">支持格式：PDF、JPG、PNG、JPEG、BMP</div>
    </div>

    <!-- 文件预览 -->
    <div v-if="file" class="file-preview">
      <img v-if="previewUrl" :src="previewUrl" alt="预览" />
      <div v-else class="file-icon">📄 PDF</div>
      <div class="file-info">
        <div class="file-name">{{ file.name }}</div>
        <div class="file-size">{{ formatSize(file.size) }}</div>
      </div>
      <button class="btn btn-secondary" @click="clearFile">清除</button>
      <button class="btn" @click="uploadAndRecognize" :disabled="uploading">
        {{ uploading ? '识别中...' : '开始识别' }}
      </button>
    </div>

    <!-- 识别状态 -->
    <div v-if="statusText" class="recognize-status" :class="statusType">
      {{ statusText }}
    </div>
  </div>
</template>

<script>
/**
 * FileUpload —— 文件上传与识别入口。
 * 选择/拖拽 PDF 或图片后调用后端 /recognize，识别成功后通过 recognized 事件
 * 把结构化结果抛给父组件（App → ContractForm 自动回填）。
 */
import { ref } from 'vue'
import { recognizeFile } from '../api/contract.js'

export default {
  name: 'FileUpload',
  emits: ['recognized'],
  setup(props, { emit }) {
    const fileInput = ref(null)
    const file = ref(null)
    const previewUrl = ref('')
    const dragover = ref(false)
    const uploading = ref(false)
    const statusText = ref('')
    const statusType = ref('')

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
      previewUrl.value = f.type.startsWith('image/') ? URL.createObjectURL(f) : ''
    }

    function clearFile() {
      file.value = null
      previewUrl.value = ''
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
      fileInput, file, previewUrl, dragover, uploading,
      statusText, statusType,
      onFileChange, onDrop, clearFile, uploadAndRecognize, formatSize
    }
  }
}
</script>

<style scoped>
.upload-section {
  background: #fff;
  border: 1px solid #ddd;
  padding: 15px;
  margin-bottom: 10px;
}
.upload-area {
  border: 2px dashed #1e88e5;
  border-radius: 4px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
}
.upload-area:hover, .upload-area.dragover { background: #e3f2fd; }
.upload-area input[type="file"] {
  position: absolute;
  left: 0; top: 0; width: 100%; height: 100%;
  opacity: 0; cursor: pointer;
}
.upload-icon { font-size: 36px; color: #1e88e5; margin-bottom: 8px; }
.upload-tip { color: #666; font-size: 13px; }
.upload-tip span { color: #1e88e5; font-weight: bold; }
.upload-sub-tip { margin-top: 5px; color: #999; font-size: 11px; }
.file-preview {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.file-preview img {
  max-width: 120px;
  max-height: 80px;
  border: 1px solid #ddd;
  border-radius: 3px;
}
.file-icon {
  font-size: 24px; color: #d32f2f;
  width: 60px; text-align: center;
}
.file-info { flex: 1; }
.file-name { font-weight: bold; color: #333; }
.file-size { color: #999; margin-top: 2px; }
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
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary {
  background: #fff;
  color: #333;
  border: 1px solid #ccc;
}
.btn-secondary:hover { background: #f0f0f0; }
.recognize-status {
  margin-top: 8px;
  padding: 6px 10px;
  border-radius: 3px;
}
.recognize-status.loading {
  background: #fff8e1; color: #f57c00;
  border: 1px solid #ffcc80;
}
.recognize-status.success {
  background: #e8f5e9; color: #2e7d32;
  border: 1px solid #a5d6a7;
}
.recognize-status.error {
  background: #ffebee; color: #c62828;
  border: 1px solid #ef9a9a;
}
</style>
