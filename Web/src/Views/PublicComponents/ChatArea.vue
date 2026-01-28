<script setup lang="ts">
import { ref, computed, nextTick, onMounted, defineProps, defineEmits, watch } from 'vue'
import { Promotion, Upload, UploadFilled, Document, Close, Star, ShoppingCart, Link, Platform } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { TimeFormatUtil } from "@/Util/TimeFormatUtil.ts"
import type { MessageData, ModelInfo } from "@/Entity/ChatEntity.ts"
import { ChatApi } from "@/Api/ChatApi.ts"

// 定义上传文件类型
export interface UploadedFile {
  name: string;
  size: number;
  file: File;
}

//==========================> 前置方法定义 <==========================
const aiMessageCallback = () => {
  loading.value = false
  scrollToBottom()
}

//==========================> 变量定义 <==========================
const props = defineProps({
  messageList: { type: Array<MessageData>, required: true },
  title: { type: String, required: true },
})
const emits = defineEmits(['sendMessage', 'uploadFile', 'updateTitle'])
defineExpose({ aiMessageCallback })

const userInput = ref('')
const loading = ref<boolean>(false)
const selectedFiles = ref<UploadedFile[]>([])
const showUploadDialog = ref(false)
const uploadedFiles = ref<UploadedFile[]>([])
const messagesContainer = ref<HTMLElement>()
const hasMessages = computed(() => props.messageList && props.messageList.length > 0)

// 知识库连接相关变量
const connectedDatabases = ref<Array<{id: string, name: string, description?: string}>>([])
const connectedDatapoints = ref<Array<{id: string, name: string, databaseId: string, databaseName: string}>>([])

// 模型选择相关变量
const modelList = ref<ModelInfo[]>([])
const selectedModel = ref<string>('')

// AI思考状态列表
// const thinkingMessages = [
//   'AI正在思考中...',
//   'AI正在变成可爱的猫娘...',
//   'AI正在练习游泳...',
//   'AI正在和van摔跤...',
//   'AI正在环游世界...',
//   'AI正在打CS:GO...',
//   'AI正在压力瓦学弟...',
//   'AI正在尝试画本子...'
// ]

// 新的AI思考状态列表
const thinkingMessages = [
  'AI正在思考中...',
  'AI正在检索知识库...',
  'AI正在分析数据...',
  'AI正在组织答案...',
  'AI正在努力思考...',
  'AI正在生成回复...',
  'AI正在认真思考...',
  'AI正在处理中...',
  'AI正在深入思考...',
  'AI正在查找相关信息...',
  'AI正在为您准备答案...',
  'AI正在分析您的问题...'
]
const currentThinkingMessage = ref(thinkingMessages[0])
let thinkingInterval: number | null = null

marked.setOptions({
  breaks: true,
  gfm: true,
})

const renderMarkdown = (content: string) => {
  if (!content) return ''
  try {
    return marked(content)
  }catch (e){
    console.error(e)
    return JSON.stringify(content)
  }

}

//==========================> 方法定义 <==========================
/** * 格式化文件大小 * @param size */
const formatFileSize = (size: number) => {
  if (size === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/** * 截断文件名 * @param name * @param maxLength */
const truncateFileName = (name: string, maxLength: number = 15) => {
  if (name.length <= maxLength) return name
  return name.substring(0, maxLength - 3) + '...'
}

/** * 拖拽上传文件改变 * @param file */
const handleDragUploadChange = (file: UploadFile) => {
  if (selectedFiles.value.length >= 10) {
    ElMessage.warning('最多只能上传10个文件')
    return false
  }
  selectedFiles.value.push({
    name: file.name || '',
    size: file.size || 0,
    file: file.raw as File
  })
}

/** * 文件数量超出限制 */
const handleExceed = () => {
  ElMessage.warning('最多只能上传10个文件')
}

/** * 删除上传文件 * @param index */
const removeFile = (index: number) => {
  uploadedFiles.value.splice(index, 1)
}

/** * 删除对话框文件 * @param index */
const removeDialogFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
}

/** * 关闭对话框 */
const handleDialogClose = () => {
  if (selectedFiles.value.length > 0) {
    ElMessageBox.confirm('确定取消上传？已选择的文件将丢失', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      selectedFiles.value = []
      showUploadDialog.value = false
    })
  } else {
    showUploadDialog.value = false
  }
}

/** * 确认上传文件 */
const confirmDragUpload = () => {
  if (uploadedFiles.value.length + selectedFiles.value.length > 10) {
    ElMessage.warning('总文件数量不能超过10个')
    return
  }
  emits('uploadFile', selectedFiles.value)
  uploadedFiles.value.push(...selectedFiles.value)
  const addedCount = selectedFiles.value.length
  selectedFiles.value = []
  showUploadDialog.value = false
  ElMessage.success(`成功添加 ${addedCount} 个文件`)
}

/** * 发送消息 */
const sendMessage = async () => {
  if (!userInput.value.trim() && uploadedFiles.value.length === 0) {
    ElMessage.warning('请输入消息或上传文件')
    return
  }
  if (selectedModel.value === ''){
    selectedModel.value = modelList.value[0]!.code
  }

  var files = [] as File[]
  if (uploadedFiles.value.length > 0){
    files = uploadedFiles.value.map(item => item.file)
  }

  emits('sendMessage', userInput.value, selectedModel.value, files)
  userInput.value = ''
  uploadedFiles.value = [] // 发送后清空文件列表
  loading.value = true
}

/** * 滚动到底部 */
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const getMessageType = (type: string) => {
  if (type === 'USER' || type === 'user') {
    return 'user'
  } else {
    return 'assistant'
  }
}

onMounted(async () => {
  scrollToBottom()
  // 加载模型列表
  await loadModelList()
})

/**
 * 加载模型列表
 */
const loadModelList = async () => {
  try {
    const models = await ChatApi.getModelList()
    modelList.value = models
    // 默认选择第一个模型
    if (models && models.length > 0) {
      selectedModel.value = models[0]!.code
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

// 监听loading状态变化，自动滚动到底部
watch(loading, (newValue) => {
  if (newValue) {
    nextTick(() => {
      scrollToBottom()
    })
    // 开始切换思考状态
    startThinkingAnimation()
  } else {
    // 停止切换思考状态
    stopThinkingAnimation()
  }
})

/** * 开始思考动画 */
const startThinkingAnimation = () => {
  // 重置为第一个消息
  currentThinkingMessage.value = thinkingMessages[0]
  // 每秒随机切换一次消息
  thinkingInterval = setInterval(() => {
    const randomIndex = Math.floor(Math.random() * thinkingMessages.length)
    currentThinkingMessage.value = thinkingMessages[randomIndex]
  }, 5000)
}

/** * 停止思考动画 */
const stopThinkingAnimation = () => {
  if (thinkingInterval) {
    clearInterval(thinkingInterval)
    thinkingInterval = null
  }
}



</script>

<template>
  <div class="chat-area">
    <div class="chat-title-box" v-if="hasMessages">
      <div class="title-content">
        {{ props.title }}
      </div>
    </div>
    <div class="messages-container" ref="messagesContainer" v-if="hasMessages">
      <div class="messages-list">
        <div v-for="(message, index) in props.messageList" :key="index" :class="['message', getMessageType(message.type)]">
          <div class="message-content">
            <div v-if="getMessageType(message.type) === 'assistant'" class="message-text markdown-render" v-html="renderMarkdown(message.content)"></div>
            <div v-else class="message-text" v-if="message.content && message.content.length > 0">{{ message.content }}</div>
            <div class="message-time">{{ TimeFormatUtil.getTimeView(message.createTime) }}</div>
          </div>
        </div>
        <!-- AI回复加载动画 -->
        <div v-if="loading" class="message assistant">
          <div class="message-content">
            <div class="message-text loading-message">
              <div class="loading-dots">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
              <div class="loading-text">{{ currentThinkingMessage }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="welcome-container" v-if="!hasMessages">
      <div class="welcome-content">
        <div style="display: flex; justify-content: center; align-items: center; margin-bottom: 10px; position: relative">
          <h1 style="margin-left: 10px">
            <span class="title-text"> 无 知 </span>
            <span style="margin: 0 20px" class="title-text">・</span>
            <span class="title-text"> 求 索 </span>
          </h1>
        </div>
        <div class="centered-input-wrapper">
          <div class="centered-input-container">
            <div class="input-box">
              <div class="input-header">
                <div class="file-grid" v-if="uploadedFiles.length > 0">
                  <div v-for="(file, index) in uploadedFiles" :key="index" class="file-grid-item" >
                    <div class="file-icon">
                      <el-icon><Document /></el-icon>
                    </div>
                    <div class="file-info">
                      <span class="file-name">{{ truncateFileName(file.name) }}</span>
                      <span class="file-size">{{ formatFileSize(file.size) }}</span>
                    </div>
                    <el-button class="file-remove" type="danger" text @click="removeFile(index)" >
                      <el-icon><Close /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
              <el-input style="margin-top: 10px" v-model="userInput" resize="none" type="textarea" :rows="2" placeholder="请输入您的问题..." :autosize="{ minRows: 2, maxRows: 10 }" @keydown.enter.exact.prevent="sendMessage" />
              <div class="input-footer">
                <div class="action-buttons">
                  <el-select v-model="selectedModel" placeholder="选择模型" class="model-select-embed">
                    <el-option
                      v-for="model in modelList"
                      :key="model.code"
                      :label="model.name"
                      :value="model.code"
                    >
                      <div class="model-option">
                        <el-icon><Platform /></el-icon>
                        <span>{{ model.name }}</span>
                      </div>
                    </el-option>
                  </el-select>
                  <el-button class="file-upload-btn" @click="showUploadDialog = true" >
                    <el-icon><Upload /></el-icon> 文件上传
                  </el-button>
                </div>
                <div class="status-info">
                  <div class="file-count" v-if="uploadedFiles.length > 0"> 已选择 {{ uploadedFiles.length }} 个文件 </div>
                  <div class="database-count" v-if="connectedDatabases.length > 0"> 已连接 {{ connectedDatabases.length }} 个知识库 </div>
                  <div class="datapoint-count" v-if="connectedDatapoints.length > 0"> 已链接 {{ connectedDatapoints.length }} 条数据 </div>
                </div>
                <el-button type="primary" @click="sendMessage" :disabled="!userInput.trim() && uploadedFiles.length === 0" class="send-btn" >
                  <el-icon><Promotion /></el-icon> 发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-input-wrapper" v-if="hasMessages">
      <div class="bottom-input-container">
        <div class="input-box">
          <div class="file-grid" v-if="uploadedFiles.length > 0">
            <div v-for="(file, index) in uploadedFiles" :key="index" class="file-grid-item" >
              <div class="file-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="file-info">
                <span class="file-name">{{ truncateFileName(file.name) }}</span>
                <span class="file-size">{{ formatFileSize(file.size) }}</span>
              </div>
              <el-button class="file-remove" type="danger" text @click="removeFile(index)" >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
          <el-input style="margin-top: 10px" v-model="userInput" type="textarea" resize="none" :rows="2" placeholder="请输入您的问题..." :autosize="{ minRows: 2, maxRows: 8 }" @keydown.enter.exact.prevent="sendMessage" />
          <div style="display: flex; justify-content: space-between;width: 100%; margin-top: 10px">
            <div class="action-buttons">
              <el-select v-model="selectedModel" placeholder="选择模型" class="model-select-embed">
                <el-option
                  v-for="model in modelList"
                  :key="model.code"
                  :label="model.name"
                  :value="model.code"
                >
                  <div class="model-option">
                    <el-icon><Platform /></el-icon>
                    <span>{{ model.name }}</span>
                  </div>
                </el-option>
              </el-select>
              <el-button class="file-upload-btn" @click="showUploadDialog = true" >
                <el-icon><Upload /></el-icon>
              </el-button>
              <div class="connection-count" v-if="connectedDatabases.length > 0 || connectedDatapoints.length > 0">
                <div v-if="connectedDatabases.length > 0">已连接 {{ connectedDatabases.length }} 个知识库</div>
                <div v-if="connectedDatapoints.length > 0">已链接 {{ connectedDatapoints.length }} 条数据</div>
              </div>
            </div>
            <el-button type="primary" :loading="loading" @click="sendMessage" :disabled="!userInput.trim() && uploadedFiles.length === 0" class="send-btn" >
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 文件上传对话框 -->
    <el-dialog v-model="showUploadDialog" style="user-select: none;" title="上传文件" width="500px" :before-close="handleDialogClose" >
      <div class="drag-upload-dialog">
        <el-upload ref="dragUploadRef" class="drag-upload-area" drag action="#" :auto-upload="false" :multiple="true" :limit="10" :on-exceed="handleExceed" :on-change="handleDragUploadChange" :show-file-list="false" accept="*/*" >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text"> 将文件拖到此处，或<em>点击上传</em> </div>
          <template #tip>
            <div class="el-upload__tip"> 支持各种类型文件，最多可上传10个文件 </div>
          </template>
        </el-upload>
        <div class="selected-files" v-if="selectedFiles.length > 0">
          <h4>已选择文件：</h4>
          <div v-for="(file, index) in selectedFiles" :key="index" class="selected-file-item" >
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
            <el-button size="small" type="danger" text @click="removeDialogFile(index)" >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="confirmDragUpload" :disabled="selectedFiles.length === 0" > 确认上传 ({{ selectedFiles.length }}) </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chat-area {
  flex: 1;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, var(--gradient-start) 0%, var(--gradient-end) 100%);
  position: relative;
  animation: fadeIn 0.6s ease-out;
  overflow: hidden;
}

.chat-area ::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.chat-area ::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 10px;
}

.chat-area ::-webkit-scrollbar-thumb {
  background: var(--border-primary);
  border-radius: 10px;
}

.chat-area ::-webkit-scrollbar-thumb:hover {
  background: var(--border-secondary);
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: scroll;
  scroll-behavior: smooth;
  background: transparent;
}

.messages-list {
  max-width: 85%;
  margin: 0 auto;
}

.file-grid {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  gap: 8px;
}

.chat-area::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse at center, var(--accent-primary) 0%, transparent 70%);
  opacity: 0.08;
  pointer-events: none;
  transition: all 0.5s ease;
  z-index: 0;
}

.chat-title-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 20px;
  padding: 20px;
  border-bottom: 1px solid var(--border-primary);
  background: var(--sidebar-bg);
  color: var(--text-primary);
  position: relative;
  z-index: 1;
  box-shadow: var(--shadow-light);
  backdrop-filter: blur(10px);
}

.title-content {
  flex: 1;
  text-align: center;
}

.model-selector-wrapper {
  margin-left: 20px;
}

.title-text {
  background: linear-gradient(135deg, var(--primary-blue) 0%, var(--primary-purple) 100%);
  -webkit-background-clip: text;
  color: transparent;
  user-select: none;
  animation: titleGlow 3s ease-in-out infinite alternate;
}

@keyframes titleGlow {
  from { text-shadow: 0 0 5px var(--accent-primary); }
  to { text-shadow: 0 0 15px var(--accent-primary); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}

.file-grid-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  min-width: 0;
  max-width: 200px;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
  animation: itemPop 0.3s ease-out;
}

@keyframes itemPop {
  0% { transform: scale(0.8); opacity: 0; }
  80% { transform: scale(1.05); }
  100% { transform: scale(1); opacity: 1; }
}

.file-grid-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.file-icon {
  margin-right: 8px;
  color: var(--text-primary);
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-remove {
  padding: 4px;
  margin-left: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.file-remove:hover {
  background: var(--hover-bg);
  transform: scale(1.1);
}

.welcome-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
}

.welcome-content {
  text-align: center;
  color: var(--text-secondary);
  width: 100%;
  padding: 0 20px;
}

.welcome-content h1 {
  font-size: 32px;
  margin-bottom: 12px;
  font-weight: 600;
}

.centered-input-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.centered-input-container {
  width: 100%;
  max-width: 800px;
}

.input-box {
  border: 1px solid var(--border-primary);
  background: var(--bg-secondary);
  border-radius: 20px;
  box-shadow: var(--shadow-light);
  padding: 20px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.input-box:focus-within {
  box-shadow: var(--shadow-medium);
  transform: translateY(-2px);
  border-color: var(--accent-primary);
}

.input-header {
  width: 100%;
}

.file-upload-btn {
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.3s ease;
}

.file-upload-btn:hover {
  background: var(--hover-bg);
  color: var(--text-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 12px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  min-width: 120px;
}

.file-count, .database-count, .datapoint-count {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 8px;
  background-color: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  white-space: nowrap;
  margin-bottom: 4px;
}

.send-btn {
  border-radius: 12px;
  padding: 8px 20px;
  background: var(--accent-primary);
  border: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

.send-btn:disabled {
  background: var(--border-primary);
  cursor: not-allowed;
}

.bottom-input-wrapper {
  width: 100%;
  padding: 0 20px 30px 20px;
  background: transparent;
  position: relative;
}

.upload-enterprise-btn {
  width: 180px;
  height: 45px;
  border-radius: 12px !important;
  line-height: 16px;
  font-size: 12px;
  background: var(--bg-tertiary);
  border: 2px solid var(--border-primary);
  color: var(--text-primary);
  font-weight: 600;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-right: 40px;
  padding: 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.upload-enterprise-btn:hover {
  background: var(--hover-bg);
  border-color: var(--accent-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

.bottom-input-container {
  min-width: 85%;
  max-width: 85%;
  margin: 0 auto;
}

.bottom-input-wrapper .input-box {
  display: flex;
  width: 100%;
  align-items: flex-end;
  flex-direction: column;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 31px;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.bottom-input-wrapper .input-box:focus-within {
  box-shadow: var(--shadow-medium);
  transform: translateY(-2px);
}

.drag-upload-dialog {
  padding: 10px 0;
  background: var(--bg-secondary);
}

.drag-upload-area :deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
  border-radius: 16px;
  border: 2px dashed var(--border-primary) !important;
  background: var(--bg-tertiary);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: none !important;
}

.drag-upload-area :deep(.el-upload-dragger:hover) {
  background: var(--hover-bg);
  transform: translateY(-2px);
}

.drag-upload-area :deep(.el-icon--upload) {
  font-size: 48px;
  color: var(--accent-primary);
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.drag-upload-area :deep(.el-upload-dragger:hover .el-icon--upload) {
  color: var(--accent-primary);
  transform: scale(1.1);
}

.drag-upload-area :deep(.el-upload__text) {
  font-size: 14px;
  color: var(--text-primary);
  text-align: center;
  font-weight: 500;
}

.drag-upload-area :deep(.el-upload__tip) {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  margin-top: 8px;
}

.selected-files {
  margin-top: 20px;
  max-height: 200px;
  overflow-y: auto;
  background: var(--bg-secondary);
}

.selected-files h4 {
  margin-bottom: 12px;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}

.selected-file-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  margin-bottom: 8px;
  gap: 8px;
  border: 1px solid var(--border-primary);
  transition: all 0.3s ease;
}

.selected-file-item:hover {
  background: var(--hover-bg);
  transform: translateY(-1px);
}

.selected-file-item .file-name {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.selected-file-item .file-size {
  font-size: 12px;
  color: var(--text-secondary);
  margin-right: 8px;
}

.message {
  display: flex;
  margin-bottom: 24px;
  gap: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 100%;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  padding: 16px 20px;
  border-radius: 20px;
  line-height: 1.6;
  text-align: left;
  word-wrap: break-word;
  font-size: 14px;
  max-width: 100%;
  box-shadow: var(--shadow-light);
}

.message.assistant .message-text {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
}

.message.user .message-text {
  background: var(--accent-primary);
  color: white;
}

.message-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
  font-weight: 500;
  padding: 0 8px;
}

.message.user .message-time {
  text-align: right;
}

/*==========================> AI加载动画样式 <==========================*/
.loading-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  min-height: 52px;
}

.loading-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.loading-dots .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--accent-primary);
  animation: loadingDot 1.4s ease-in-out infinite both;
}

.loading-dots .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots .dot:nth-child(2) {
  animation-delay: -0.16s;
}

.loading-dots .dot:nth-child(3) {
  animation-delay: 0s;
}

.loading-text {
  font-size: 14px;
  color: var(--text-secondary);
  font-style: italic;
}

@keyframes loadingDot {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/*==========================> 知识库连接相关样式 <==========================*/
.knowledge-base-btn {
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.knowledge-base-btn:hover {
  background: var(--hover-bg);
  color: var(--text-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

.connection-count {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.connection-count div {
  background-color: var(--bg-tertiary);
  padding: 2px 8px;
  border-radius: 8px;
  border: 1px solid var(--border-primary);
  white-space: nowrap;
  margin-bottom: 4px;
}

.model-tabs-container {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  box-shadow: var(--shadow-light);
  min-height: 44px;
  min-width: fit-content;
}

.model-tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  position: relative;
  overflow: hidden;
  white-space: nowrap;
  min-width: fit-content;
}

.model-tab-item .el-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.model-tab-item .model-tab-label {
  white-space: nowrap;
}

.model-tab-item:hover {
  background: var(--hover-bg);
  color: var(--text-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
}

.model-tab-item:hover .el-icon {
  color: var(--accent-primary);
  transform: scale(1.1);
}

.model-tab-item.active {
  background: var(--primary-gradient);
  color: white;
  border: 1px solid var(--accent-primary);
  box-shadow: var(--shadow-medium), var(--card-glow);
}

.model-tab-item.active .el-icon {
  color: white;
  transform: scale(1.1);
}

.model-tab-item.active::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.model-tab-item.active:hover::before {
  opacity: 1;
}

.model-select-top {
  width: 180px;
}

.model-select-top :deep(.el-input__wrapper) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 4px 8px;
}

.model-select-top :deep(.el-input__wrapper:hover) {
  background: var(--hover-bg);
  border-radius: 8px;
}

.model-select-top :deep(.el-input__wrapper.is-focus) {
  background: var(--hover-bg);
  border-radius: 8px;
  box-shadow: 0 0 0 2px var(--primary-color-light);
}

.model-select-top :deep(.el-input__inner) {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
}

.model-select-top :deep(.el-select__caret) {
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.model-select-top:hover :deep(.el-select__caret) {
  color: var(--accent-primary);
}

.model-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.model-option .el-icon {
  color: var(--accent-primary);
  font-size: 18px;
}

.model-select-top :deep(.el-select-dropdown) {
  background: var(--card-bg);
  border: 1px solid var(--border-primary);
  border-radius: 16px;
  box-shadow: var(--shadow-heavy);
  padding: 8px;
}

.model-select-top :deep(.el-select-dropdown__item) {
  padding: 12px 16px;
  border-radius: 10px;
  color: var(--text-primary);
  font-weight: 500;
  transition: all 0.2s ease;
  margin: 2px 0;
}

.model-select-top :deep(.el-select-dropdown__item:hover) {
  background: var(--hover-bg);
  transform: translateX(2px);
  box-shadow: var(--shadow-light);
}

.model-select-top :deep(.el-select-dropdown__item.is-selected) {
  background: linear-gradient(135deg, var(--primary-color-light), var(--bg-tertiary));
  color: var(--accent-primary);
  font-weight: 600;
  border: 1px solid var(--accent-primary);
  box-shadow: var(--card-glow);
}

.model-select-top :deep(.el-select-dropdown__item.is-selected):hover {
  transform: translateX(2px);
  box-shadow: var(--shadow-medium);
}

.model-select-top :deep(.el-select-dropdown__item .el-icon) {
  transition: all 0.3s ease;
}

.model-select-top :deep(.el-select-dropdown__item:hover .el-icon) {
  transform: scale(1.1);
  color: var(--accent-primary);
}

.model-select-embed {
  width: 140px;
}

.model-select-embed :deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  box-shadow: none;
  padding: 4px 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.model-select-embed :deep(.el-input__wrapper:hover) {
  background: var(--hover-bg);
  border-color: var(--accent-primary);
}

.model-select-embed :deep(.el-input__wrapper.is-focus) {
  background: var(--hover-bg);
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px var(--primary-color-light);
}

.model-select-embed :deep(.el-input__inner) {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
}

.model-select-embed :deep(.el-select__caret) {
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.model-select-embed:hover :deep(.el-select__caret) {
  color: var(--accent-primary);
}

.model-select-embed :deep(.el-select-dropdown) {
  background: var(--card-bg);
  border: 1px solid var(--border-primary);
  border-radius: 16px;
  box-shadow: var(--shadow-heavy);
  padding: 8px;
}

.model-select-embed :deep(.el-select-dropdown__item) {
  padding: 12px 16px;
  border-radius: 10px;
  color: var(--text-primary);
  font-weight: 500;
  transition: all 0.2s ease;
  margin: 2px 0;
}

.model-select-embed :deep(.el-select-dropdown__item:hover) {
  background: var(--hover-bg);
  transform: translateX(2px);
  box-shadow: var(--shadow-light);
}

.model-select-embed :deep(.el-select-dropdown__item.is-selected) {
  background: linear-gradient(135deg, var(--primary-color-light), var(--bg-tertiary));
  color: var(--accent-primary);
  font-weight: 600;
  border: 1px solid var(--accent-primary);
  box-shadow: var(--card-glow);
}

.model-select-embed :deep(.el-select-dropdown__item.is-selected):hover {
  transform: translateX(2px);
  box-shadow: var(--shadow-medium);
}

.model-select-embed :deep(.el-select-dropdown__item .el-icon) {
  transition: all 0.3s ease;
}

.model-select-embed :deep(.el-select-dropdown__item:hover .el-icon) {
  transform: scale(1.1);
  color: var(--accent-primary);
}

.knowledge-tabs {
  margin-top: 10px;
}

.knowledge-tabs :deep(.el-tabs__header) {
  margin-bottom: 15px;
}

.knowledge-tabs :deep(.el-tabs__nav) {
  border-bottom: 1px solid var(--border-primary);
}

.knowledge-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
  font-weight: 500;
}

.knowledge-tabs :deep(.el-tabs__item.is-active) {
  color: var(--accent-primary);
}

.knowledge-base-dialog {
  padding: 0;
  background: var(--bg-secondary);
}

.database-list {
  max-height: 500px;
  width: 100%;
  overflow-y: auto;
  padding-right: 5px;
}

.database-item {
  margin-bottom: 10px;
  padding: 12px;
  border-radius: 8px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  transition: all 0.2s ease;
  width: 100%;
}

.database-item.selected {
  background: var(--bg-primary);
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.2);
}

.database-header {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
}

.database-header:hover {
  background: var(--hover-bg);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
  border-radius: 8px;
  padding: 6px;
  margin: -6px;
}

.database-checkbox {
  margin-right: 12px;
  margin-top: 2px;
  flex-shrink: 0;
}

.database-info {
  width: 100%;
  overflow: hidden;
  flex: 1;
}

.database-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.database-description {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.database-status {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--border-secondary);
}

.status-dot.connected {
  background-color: var(--accent-primary);
}

.datapoints-list {
  margin-top: 10px;
  padding-left: 24px;
  border-left: 2px dashed var(--border-secondary);
}

.datapoint-item {
  margin-bottom: 8px;
  padding: 10px;
  border-radius: 6px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  transition: all 0.2s ease;
  display: flex;
  align-items: flex-start;
  cursor: pointer;
}

.datapoint-item:hover {
  background: var(--hover-bg);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
}

.datapoint-item.selected {
  background: var(--bg-primary);
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.1);
}

.datapoint-checkbox {
  margin-right: 10px;
  margin-top: 2px;
  flex-shrink: 0;
}

.datapoint-info {
  width: 100%;
  overflow: hidden;
  flex: 1;
}

.datapoint-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 3px;
}

.datapoint-status {
  font-size: 11px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.expand-btn {
  font-size: 13px;
  font-weight: 500;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.expand-btn:hover {
  background-color: var(--hover-bg);
  color: var(--accent-primary);
}

.connected-list {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 5px;
}

.connected-section {
  margin-bottom: 20px;
}

.connected-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  border-bottom: 1px solid var(--border-primary);
  padding-bottom: 8px;
}

.connected-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  margin-bottom: 8px;
  transition: all 0.2s ease;
}

.connected-item:hover {
  background: var(--hover-bg);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
}

.connected-info {
  flex: 1;
  min-width: 0;
  margin-right: 10px;
}

.connected-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.connected-description {
  font-size: 12px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.connected-database {
  font-size: 11px;
  color: var(--accent-primary);
  font-weight: 500;
  margin-top: 4px;
  display: inline-block;
  padding: 2px 6px;
  background: rgba(124, 58, 237, 0.1);
  border-radius: 4px;
}

/*==========================> markdown渲染样式优化 ==========================*/
.markdown-render {
  text-align: left;
  line-height: 1.6;
  color: var(--text-primary);
}

.markdown-render :deep(h1) {
  color: var(--text-primary);
  margin: 20px 0 12px 0;
  font-size: 1.5em;
  font-weight: 600;
  border-bottom: 2px solid var(--border-primary);
  padding-bottom: 8px;
}

.markdown-render :deep(h2) {
  color: var(--accent-primary);
  margin: 18px 0 10px 0;
  font-size: 1.3em;
  font-weight: 600;
}

.markdown-render :deep(h3) {
  color: var(--text-primary);
  margin: 16px 0 8px 0;
  font-size: 1.1em;
  font-weight: 600;
}

.markdown-render :deep(p) {
  margin: 12px 0;
  color: var(--text-primary);
}

.markdown-render :deep(ul),
.markdown-render :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.markdown-render :deep(li) {
  margin: 6px 0;
  color: var(--text-primary);
}

.markdown-render :deep(blockquote) {
  margin: 12px 0;
  padding: 12px 20px;
  border-left: 4px solid var(--accent-primary);
  border-radius: 8px;
  color: var(--text-primary);
}

.markdown-render :deep(code) {
  background: var(--bg-tertiary);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
}

.markdown-render :deep(pre) {
  background: var(--bg-tertiary);
  padding: 16px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 16px 0;
  border: 1px solid var(--border-primary);
}

.markdown-render :deep(pre code) {
  background: transparent;
  padding: 0;
  color: var(--text-primary);
  border: none;
}

.markdown-render :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--shadow-light);
}

.markdown-render :deep(th),
.markdown-render :deep(td) {
  padding: 12px 16px;
  border: 1px solid var(--border-primary);
}

.markdown-render :deep(th) {
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 600;
}

.markdown-render :deep(td) {
  background: var(--bg-secondary);
}

.markdown-render :deep(a) {
  color: var(--accent-primary);
  text-decoration: none;
  font-weight: 500;
  border-bottom: 1px dashed var(--accent-primary);
}

.markdown-render :deep(a:hover) {
  color: var(--accent-primary);
  opacity: 0.8;
  border-bottom: 1px solid var(--accent-primary);
}

.markdown-render :deep(strong) {
  font-weight: 600;
  color: var(--text-primary);
}

.markdown-render :deep(em) {
  font-style: italic;
  color: var(--accent-primary);
}

@media (max-width: 768px) {
  .messages-list {
    max-width: 95%;
  }
  .bottom-input-container {
    min-width: 95%;
    max-width: 95%;
  }
  .upload-enterprise-btn {
    width: 160px;
    height: 40px;
    font-size: 11px;
    line-height: 14px;
    margin-right: 20px;
    padding: 0 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    background: var(--bg-tertiary);
    border: 2px solid var(--border-primary);
    color: var(--text-primary);
    font-weight: 600;
  }

  .upload-enterprise-btn:hover {
    background: var(--hover-bg);
    border-color: var(--accent-primary);
    transform: translateY(-1px);
    box-shadow: var(--shadow-medium);
  }

  .chat-title-box {
    padding: 20px 0;
  }
}
</style>