<!--
  文件预览组件
  功能：根据文件类型智能适配展示方式
  支持的文件类型：
    - 图片：jpg, jpeg, png, gif, bmp, webp, svg
    - 视频：mp4, webm, ogg, mov
    - 音频：mp3, wav, ogg, m4a
    - PDF：pdf
    - 代码：js, ts, vue, html, css, json, md, txt, java, kt, py, go, c, cpp, h, hpp, sql, xml, yaml, yml, php, rb, swift
    - 文档：doc, docx, xls, xlsx, ppt, pptx
-->
<template>
  <div class="file-preview">
    <!-- 图片预览 -->
    <div v-if="fileType === 'image'" class="preview-section image-preview">
      <el-image
        :src="fileUrl"
        :preview-src-list="[fileUrl]"
        fit="contain"
        class="preview-image"
      >
        <template #error>
          <div class="image-error">
            <el-icon><Picture /></el-icon>
            <span>图片加载失败</span>
          </div>
        </template>
      </el-image>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
    </div>

    <!-- 视频预览 -->
    <div v-else-if="fileType === 'video'" class="preview-section video-preview">
      <video :src="fileUrl" controls class="preview-video">
        您的浏览器不支持视频播放
      </video>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
    </div>

    <!-- 音频预览 -->
    <div v-else-if="fileType === 'audio'" class="preview-section audio-preview">
      <audio :src="fileUrl" controls class="preview-audio">
        您的浏览器不支持音频播放
      </audio>
      <div class="audio-info">
        <el-icon><Headset /></el-icon>
        <span>音频文件</span>
      </div>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
    </div>

    <!-- PDF 预览 -->
    <div v-else-if="fileType === 'pdf'" class="preview-section pdf-preview">
      <iframe :src="fileUrl" class="preview-pdf"></iframe>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="openInNewTab">
          <el-icon><View /></el-icon>
          在新窗口打开
        </el-button>
        <el-button type="primary" size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
    </div>

    <!-- 代码文件预览 -->
    <div v-else-if="fileType === 'code'" class="preview-section code-preview">
      <div class="code-header">
        <span class="file-name">{{ fileName }}</span>
        <el-button size="small" @click="copyContent">
          <el-icon><CopyDocument /></el-icon>
          复制代码
        </el-button>
      </div>
      <div class="code-content">
        <pre><code :class="`language-${codeLanguage}`">{{ codeContent }}</code></pre>
      </div>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
    </div>

    <!-- Office 文档预览 -->
    <div v-else-if="fileType === 'office'" class="preview-section office-preview">
      <div class="office-placeholder">
        <el-icon class="office-icon"><Document /></el-icon>
        <h3>{{ getOfficeTypeLabel() }}文档</h3>
        <p class="file-name">{{ fileName }}</p>
        <p class="file-hint">该文件类型不支持在线预览</p>
      </div>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
    </div>

    <!-- 其他文件类型 -->
    <div v-else class="preview-section unknown-preview">
      <div class="unknown-placeholder">
        <el-icon class="unknown-icon"><DocumentCopy /></el-icon>
        <h3>未知文件类型</h3>
        <p class="file-name">{{ fileName }}</p>
        <p class="file-hint">文件扩展名：{{ fileExtension }}</p>
      </div>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Picture,
  Download,
  Headset,
  View,
  CopyDocument,
  Document,
  DocumentCopy
} from '@element-plus/icons-vue'

// =========================> 属性定义 <=========================
interface Props {
  /** 文件 URL */
  fileUrl: string
  /** 文件名（包含扩展名） */
  fileName?: string
}

const props = withDefaults(defineProps<Props>(), {
  fileName: ''
})

// =========================> 文件类型定义 <=========================
/** 图片文件扩展名 */
const IMAGE_EXTENSIONS = [
  'jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'
]

/** 视频文件扩展名 */
const VIDEO_EXTENSIONS = [
  'mp4', 'webm', 'ogg', 'mov', 'avi', 'mkv', 'flv'
]

/** 音频文件扩展名 */
const AUDIO_EXTENSIONS = [
  'mp3', 'wav', 'ogg', 'm4a', 'aac', 'flac'
]

/** PDF 文件扩展名 */
const PDF_EXTENSIONS = ['pdf']

/** 代码文件扩展名及对应语言 */
const CODE_EXTENSIONS: Record<string, string> = {
  'js': 'javascript',
  'ts': 'typescript',
  'vue': 'vue',
  'html': 'html',
  'htm': 'html',
  'css': 'css',
  'scss': 'scss',
  'json': 'json',
  'xml': 'xml',
  'yaml': 'yaml',
  'yml': 'yaml',
  'txt': 'plaintext',
  'java': 'java',
  'kt': 'kotlin',
  'kts': 'kotlin',
  'py': 'python',
  'go': 'go',
  'c': 'c',
  'cpp': 'cpp',
  'h': 'c',
  'hpp': 'cpp',
  'sql': 'sql',
  'php': 'php',
  'rb': 'ruby',
  'swift': 'swift',
  'rs': 'rust',
  'sh': 'bash',
  'bat': 'batch',
  'md': 'markdown'
}

/** Office 文档扩展名 */
const OFFICE_EXTENSIONS = [
  'doc', 'docx',
  'xls', 'xlsx',
  'ppt', 'pptx'
]

// =========================> 响应式数据 <=========================
/** 代码文件内容 */
const codeContent = ref('')

// =========================> 计算属性 <=========================
/** 文件扩展名（小写） */
const fileExtension = computed(() => {
  if (!props.fileName) {
    // 如果没有文件名，尝试从 URL 中提取
    const urlParts = props.fileUrl?.split('/') || []
    const lastPart = urlParts[urlParts.length - 1] || ''
    const nameParts = lastPart?.split('.') || []
    if (nameParts.length > 1) {
      return nameParts[nameParts.length - 1]!.toLowerCase()
    }
    return ''
  }
  const parts = props.fileName?.split('.') || []
  return parts.length > 1 ? parts[parts.length - 1]!.toLowerCase() : ''
})

/** 文件类型 */
const fileType = computed(() => {
  const ext = fileExtension.value
  
  if (IMAGE_EXTENSIONS.includes(ext)) return 'image'
  if (VIDEO_EXTENSIONS.includes(ext)) return 'video'
  if (AUDIO_EXTENSIONS.includes(ext)) return 'audio'
  if (PDF_EXTENSIONS.includes(ext)) return 'pdf'
  if (ext in CODE_EXTENSIONS) return 'code'
  if (OFFICE_EXTENSIONS.includes(ext)) return 'office'
  
  return 'unknown'
})

/** 代码语言类型（用于语法高亮） */
const codeLanguage = computed(() => {
  return CODE_EXTENSIONS[fileExtension.value] || 'plaintext'
})

// =========================> 方法定义 <=========================
/**
 * 获取 Office 文档类型标签
 */
const getOfficeTypeLabel = () => {
  const ext = fileExtension.value
  if (ext === 'doc' || ext === 'docx') return 'Word'
  if (ext === 'xls' || ext === 'xlsx') return 'Excel'
  if (ext === 'ppt' || ext === 'pptx') return 'PowerPoint'
  return 'Office'
}

/**
 * 复制代码内容到剪贴板
 */
const copyContent = () => {
  if (!codeContent.value) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  
  navigator.clipboard.writeText(codeContent.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

/**
 * 在新窗口打开 PDF
 */
const openInNewTab = () => {
  window.open(props.fileUrl, '_blank')
}

/**
 * 下载文件
 */
const downloadFile = () => {
  const link = document.createElement('a')
  link.href = props.fileUrl
  link.download = props.fileName || `download.${fileExtension.value}`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('开始下载')
}

/**
 * 加载代码文件内容
 */
const loadCodeContent = async () => {
  if (fileType.value !== 'code') return
  
  try {
    const response = await fetch(props.fileUrl)
    if (response.ok) {
      codeContent.value = await response.text()
    } else {
      ElMessage.error('加载文件内容失败')
    }
  } catch (error) {
    console.error('加载代码文件失败:', error)
    ElMessage.error('加载文件内容失败')
  }
}

// =========================> 监听器 <=========================
watch(
  () => props.fileUrl,
  () => {
    if (fileType.value === 'code') {
      loadCodeContent()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.file-preview {
  width: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.preview-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  min-height: 400px;
}

/* ========== 图片预览 ========== */
.image-preview {
  background-color: #f5f5f5;
}

.preview-image {
  max-width: 100%;
  max-height: 500px;
  border-radius: 4px;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.image-error .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

/* ========== 视频预览 ========== */
.video-preview {
  background-color: #000;
}

.preview-video {
  max-width: 100%;
  max-height: 500px;
}

/* ========== 音频预览 ========== */
.audio-preview {
  background-color: #f5f5f5;
}

.preview-audio {
  width: 100%;
  max-width: 600px;
  margin: 20px 0;
}

.audio-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  color: #606266;
}

.audio-info .el-icon {
  font-size: 24px;
}

/* ========== PDF 预览 ========== */
.pdf-preview {
  padding: 0;
}

.preview-pdf {
  width: 100%;
  height: 600px;
  border: none;
}

/* ========== 代码预览 ========== */
.code-preview {
  background-color: #f5f5f5;
  padding: 0;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
}

.code-header .file-name {
  font-weight: 500;
  color: #303133;
}

.code-content {
  flex: 1;
  overflow: auto;
  padding: 16px;
  background-color: #282c34;
  color: #abb2bf;
}

.code-content pre {
  margin: 0;
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.code-content code {
  display: block;
}

/* ========== Office 文档预览 ========== */
.office-preview,
.unknown-preview {
  background-color: #f5f5f5;
}

.office-placeholder,
.unknown-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.office-icon,
.unknown-icon {
  font-size: 80px;
  color: #409eff;
  margin-bottom: 20px;
}

.office-placeholder h3,
.unknown-placeholder h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #303133;
}

.file-name {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

.file-hint {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #909399;
}

/* ========== 预览操作按钮 ========== */
.preview-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding: 0 20px 20px;
  justify-content: center;
}

/* ========== 滚动条样式 ========== */
.code-content::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.code-content::-webkit-scrollbar-track {
  background: #1e2227;
}

.code-content::-webkit-scrollbar-thumb {
  background: #4b5263;
  border-radius: 4px;
}

.code-content::-webkit-scrollbar-thumb:hover {
  background: #5c6370;
}
</style>
