<script lang="ts" setup>
import { ref, watch, computed } from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import { Edit, User } from '@element-plus/icons-vue'
import type { ProfileData } from "./Sidebar.vue";

// Props定义
interface Props {
  visible: boolean
  profile: ProfileData
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  profile: () => ({ name: '', avatar: '' })
})

// Emits定义
const emit = defineEmits<{
  'update:visible': [value: boolean]
  save: [profile: ProfileData]
  close: []
  'avatar-change': [file: File]
}>()

// 响应式数据
const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})

const isEditMode = ref(false)
const displayData = ref<ProfileData>({ ...props.profile })
const editData = ref<ProfileData>({ name: '', avatar: '' })
const originalData = ref<ProfileData>({ name: '', avatar: '' })
const fileInput = ref<HTMLInputElement>()
const saveLoading = ref(false)

// 监听props.profile的变化
watch(() => props.profile, (newProfile) => {
  displayData.value = { ...newProfile }
}, { deep: true, immediate: true })

// 计算属性
const getGenderText = (gender?: number) => {
  switch (gender) {
    case 1: return '男'
    case 2: return '女'
    default: return '未知'
  }
}

// 简单的Markdown渲染函数
const renderMarkdown = (text: string) => {
  if (!text || text === '未填写') return text

  return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/# (.*?)(\n|$)/g, '<h3>$1</h3>')
      .replace(/## (.*?)(\n|$)/g, '<h4>$1</h4>')
      .replace(/- (.*?)(\n|$)/g, '<li>$1</li>')
      .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
      .replace(/\n/g, '<br>')
}

//==========================> 方法定义 <==========================
/**
 * 关闭对话框
 */
const handleClose = () => {
  if (isEditMode.value) {
    handleCancel()
  } else {
    dialogVisible.value = false
    emit('close')
  }
}

/**
 * 进入编辑模式
 */
const enterEditMode = () => {
  isEditMode.value = true
  originalData.value = { ...displayData.value }
  editData.value = { ...displayData.value }
}

/**
 * 取消编辑
 */
const handleCancel = async () => {
  if (isEditMode.value) {
    // 检查是否有未保存的修改
    const hasChanges = JSON.stringify(editData.value) !== JSON.stringify(originalData.value)

    if (hasChanges) {
      try {
        await ElMessageBox.confirm('当前有未保存的修改，确定要取消吗？', '提示', {
          type: 'warning',
          confirmButtonText: '确定取消',
          cancelButtonText: '继续编辑'
        })

        // 用户确认取消
        isEditMode.value = false
        editData.value = { ...originalData.value }
        emit('close')
      } catch {
        // 用户选择继续编辑
        return
      }
    } else {
      isEditMode.value = false
      emit('close')
    }
  } else {
    dialogVisible.value = false
    emit('close')
  }
}

/**
 * 保存个人资料
 */
const handleSave = async () => {
  try {
    saveLoading.value = true

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    emit('save', editData.value)
    displayData.value = { ...editData.value }
    ElMessage.success('资料保存成功')
    isEditMode.value = false
  } catch (error) {
    ElMessage.error('保存失败')
    console.error('保存个人资料失败:', error)
  } finally {
    saveLoading.value = false
  }
}

/**
 * 触发文件选择
 */
const triggerFileInput = () => {
  fileInput.value?.click()
}

/**
 * 处理头像变更
 */
const handleAvatarChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (file) {
    // 检查文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }

    // 检查文件大小（限制为5MB）
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过5MB')
      return
    }

    // 创建临时URL预览
    const reader = new FileReader()
    reader.onload = (e) => {
      if (e.target?.result) {
        editData.value.avatar = e.target.result as string
      }
    }
    reader.readAsDataURL(file)

    // 发送文件给父组件
    emit('avatar-change', file)

    // 清空input，以便可以选择同一文件再次触发change事件
    target.value = ''
  }
}
</script>

<template>
  <el-dialog
      draggable
      v-model="dialogVisible"
      :title="isEditMode ? '编辑个人资料' : '个人资料'"
      width="600px"
      :before-close="handleClose"
      custom-class="profile-dialog"
      align-center
      destroy-on-close
  >
    <div class="profile-container">
      <!-- 基础信息区域 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><User /></el-icon>
            基础信息
          </h3>
          <el-button
              v-if="!isEditMode"
              type="primary"
              text
              @click="enterEditMode"
              class="edit-btn"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </div>

        <div class="form-row">
          <div class="avatar-section">
            <el-avatar
                :size="100"
                :src="displayData.avatar"
                class="profile-avatar"
            >
              {{ displayData.username?.charAt(0) || '用户' }}
            </el-avatar>
            <el-button
                v-if="isEditMode"
                type="primary"
                text
                @click="triggerFileInput"
                class="avatar-upload-btn"
            >
              更换头像
            </el-button>
            <input
                ref="fileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleAvatarChange"
            />
          </div>

          <div class="basic-info">
            <el-form :model="displayData" label-width="80px">
              <el-form-item label="用户名">
                <el-input
                    v-if="isEditMode"
                    v-model="editData.username"
                    placeholder="请输入用户名"
                    class="input-field"
                />
                <span v-else class="readonly-text">{{ displayData.username || '未填写' }}</span>
              </el-form-item>

              <el-form-item label="邮箱">
                <el-input
                    v-if="isEditMode"
                    v-model="editData.email"
                    placeholder="请输入邮箱"
                    class="input-field"
                />
                <span v-else class="readonly-text">{{ displayData.email || '未填写' }}</span>
              </el-form-item>

              <el-form-item label="手机号">
                <el-input
                    v-if="isEditMode"
                    v-model="editData.phone"
                    placeholder="请输入手机号"
                    class="input-field"
                />
                <span v-else class="readonly-text">{{ displayData.phone || '未填写' }}</span>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 社交信息区域 -->
      <div class="section">
        <h3 class="section-title">
          <el-icon><User /></el-icon>
          社交信息
        </h3>
        <el-form :model="displayData" label-width="80px">
          <el-form-item label="微信昵称">
            <el-input
                v-if="isEditMode"
                v-model="editData.wechatName"
                placeholder="请输入微信昵称"
                class="input-field"
            />
            <span v-else class="readonly-text">{{ displayData.wechatName || '未填写' }}</span>
          </el-form-item>

          <el-form-item label="QQ昵称">
            <el-input
                v-if="isEditMode"
                v-model="editData.qqName"
                placeholder="请输入QQ昵称"
                class="input-field"
            />
            <span v-else class="readonly-text">{{ displayData.qqName || '未填写' }}</span>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <template v-if="!isEditMode">
          <el-button @click="handleClose" class="cancel-btn">关闭</el-button>
          <el-button type="primary" @click="enterEditMode" class="confirm-btn">编辑资料</el-button>
        </template>
        <template v-else>
          <el-button @click="handleCancel" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saveLoading" class="confirm-btn">
            保存资料
          </el-button>
        </template>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
:deep(.profile-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--dialog-shadow);
  backdrop-filter: blur(20px);
}

:deep(.profile-dialog:hover) {
  box-shadow: var(--dialog-shadow-hover);
  border-color: var(--border-primary);
}

:deep(.profile-dialog .el-dialog__header) {
  background: var(--dialog-header-bg);
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid var(--dialog-header-border);
  position: relative;
}

:deep(.profile-dialog .el-dialog__header::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, var(--border-primary) 50%, transparent 100%);
  opacity: 0.5;
}

:deep(.profile-dialog .el-dialog__title) {
  color: var(--dialog-title-color);
  font-weight: 600;
  font-size: 16px;
  line-height: 1.4;
}

:deep(.profile-dialog .el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--card-hover);
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
}

:deep(.profile-dialog .el-dialog__headerbtn:hover) {
  background: var(--card-bg);
  border-color: var(--border-primary);
  transform: scale(1.05);
}

:deep(.profile-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 500;
}

:deep(.profile-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--text-primary);
}

:deep(.profile-dialog .el-dialog__body) {
  padding: 0;
  background: var(--dialog-body-bg);
  max-height: 70vh;
  overflow: hidden;
}

.profile-container {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
  background: var(--dialog-body-bg);
}

.section {
  margin-bottom: 24px;
  padding: 24px;
  background: var(--card-bg);
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.section:hover {
  box-shadow: var(--shadow-medium);
  border-color: var(--border-primary);
  transform: translateY(-1px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.section-title {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  border-left: 3px solid var(--primary-color);
  padding-left: 12px;
  line-height: 1.2;
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--card-hover);
  padding: 8px 12px;
  border-radius: 6px;
}

.edit-btn {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  background: var(--primary-color);
  border: 1px solid var(--primary-color);
  color: white;
  transition: all 0.3s ease;
}

.edit-btn:hover {
  background: var(--accent-secondary);
  border-color: var(--accent-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

.form-row {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 120px;
}

.profile-avatar {
  border: 4px solid var(--text-on-primary);
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
  font-size: 24px;
  font-weight: 600;
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
}

.profile-avatar:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-hover);
}

.avatar-upload-btn {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 12px;
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
  background: var(--bg-button);
  transition: all 0.3s ease;
}

.avatar-upload-btn:hover {
  background: var(--bg-button-hover);
  border-color: var(--primary-color);
  color: var(--text-primary);
  transform: translateY(-1px);
}

.basic-info {
  flex: 1;
  min-width: 0;
}

:deep(.basic-info .el-form-item__label) {
  color: var(--primary-color) !important;
  font-weight: 600;
  font-size: 14px;
}

.readonly-text {
  color: var(--text-primary);
  line-height: 32px;
  min-height: 32px;
  display: block;
  font-weight: 500;
  padding: 4px 0;
  background: var(--bg-tag);
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px dashed var(--border-secondary);
}

.readonly-textarea {
  color: var(--text-primary);
  line-height: 1.6;
  padding: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  background: var(--bg-tag);
  border-radius: 6px;
  border: 1px dashed var(--border-secondary);
  min-height: 60px;
}

.full-width-textarea {
  width: 100%;
  border-radius: 8px;
}

:deep(.input-field .el-input__inner),
:deep(.input-field .el-textarea__inner) {
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  transition: all 0.3s ease;
  background: var(--gradient-end);
  color: var(--text-primary);
}

:deep(.input-field .el-input__inner:focus),
:deep(.input-field .el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: var(--focus-ring);
  transform: translateY(-1px);
}

:deep(.input-field .el-input__inner::placeholder),
:deep(.input-field .el-textarea__inner::placeholder) {
  color: var(--text-secondary);
  opacity: 0.7;
}

.view-mode-toggle {
  display: flex;
  align-items: center;
}

:deep(.view-mode-toggle .el-button-group) {
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  overflow: hidden;
}

:deep(.view-mode-toggle .el-button) {
  border: none;
  color: var(--text-primary);
  background: var(--gradient-end);
  transition: all 0.3s ease;
}

:deep(.view-mode-toggle .el-button:hover) {
  background: var(--bg-tag);
  color: var(--text-primary);
}

:deep(.view-mode-toggle .el-button.is-active) {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
}

.markdown-editor {
  position: relative;
  width: 100%;
}

.markdown-textarea {
  width: 100% !important;
  border-radius: 8px;
  border: 1px solid var(--border-primary);
  background: var(--gradient-end);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.markdown-textarea:focus {
  border-color: var(--primary-color);
  box-shadow: var(--focus-ring);
}

.markdown-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
  padding-left: 4px;
}

.markdown-preview {
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  padding: 16px;
  background: var(--gradient-end);
  min-height: 200px;
  width: 100%;
}

.markdown-render {
  line-height: 1.6;
  min-height: 150px;
  color: var(--text-primary);
}

.markdown-render :deep(strong) {
  font-weight: bold;
  color: var(--text-primary);
}

.markdown-render :deep(em) {
  font-style: italic;
  color: var(--text-secondary);
}

.markdown-render :deep(code) {
  background: var(--bg-tag);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
}

.markdown-render :deep(h3) {
  color: var(--text-primary);
  margin: 20px 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid var(--border-secondary);
  padding-bottom: 8px;
}

.markdown-render :deep(h4) {
  color: var(--text-secondary);
  margin: 16px 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.markdown-render :deep(ul) {
  margin: 12px 0;
  padding-left: 24px;
}

.markdown-render :deep(li) {
  margin: 6px 0;
  color: var(--text-primary);
}

.skills-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skills-input {
  margin: 10px 0;
  display: flex;
  gap: 12px;
  align-items: center;
}

.skill-input {
  width: 300px;
  border-radius: 6px;
}

:deep(.skill-input .el-input__inner) {
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  background: var(--gradient-end);
  color: var(--text-primary);
}

:deep(.skill-input .el-input__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: var(--focus-ring);
}

.skills-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  min-height: 32px;
  align-items: center;
}

.skill-tag {
  background: var(--gradient-overlay);
  border: 1px solid var(--border-primary);
  color: var(--text-primary);
  border-radius: 16px;
  padding: 6px 12px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.skill-tag:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-hover);
  border-color: var(--primary-color);
  background: var(--bg-button-hover);
}

.no-skills {
  color: var(--text-secondary);
  font-style: italic;
  padding: 8px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0 0 0;
  border-top: 1px solid var(--border-light);
  margin-top: 8px;
}

.cancel-btn {
  border-radius: 8px;
  padding: 10px 20px;
  border: 1px solid var(--border-primary);
  color: var(--text-primary);
  background: var(--card-bg);
  transition: all 0.3s ease;
  font-weight: 500;
}

.cancel-btn:hover {
  background: var(--card-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
  border-color: var(--border-primary);
  color: var(--text-primary);
}

.confirm-btn {
  border-radius: 8px;
  padding: 10px 20px;
  background: var(--primary-color);
  border: 1px solid var(--primary-color);
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  background: var(--accent-secondary);
  border-color: var(--accent-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

/* 滚动条优化 */
.profile-container::-webkit-scrollbar {
  width: 6px;
}

.profile-container::-webkit-scrollbar-track {
  background: var(--scrollbar-track);
  border-radius: 3px;
}

.profile-container::-webkit-scrollbar-thumb {
  background: var(--scrollbar-thumb);
  border-radius: 3px;
}

.profile-container::-webkit-scrollbar-thumb:hover {
  background: var(--scrollbar-thumb-hover);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .section {
    padding: 16px;
    margin-bottom: 16px;
  }

  .form-row {
    flex-direction: column;
    gap: 20px;
  }

  .avatar-section {
    align-self: center;
  }

  .skills-input {
    flex-direction: column;
    align-items: stretch;
  }

  .skill-input {
    width: 100%;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .view-mode-toggle {
    align-self: flex-end;
  }
}

/* 动画效果 */
.section {
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section:nth-child(1) { animation-delay: 0.1s; }
.section:nth-child(2) { animation-delay: 0.2s; }
.section:nth-child(3) { animation-delay: 0.3s; }
.section:nth-child(4) { animation-delay: 0.4s; }

/* 暗色模式样式 */
.dark :deep(.profile-dialog .el-dialog__header) {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
  border-bottom: 1px solid var(--dark-border);
}

.dark :deep(.profile-dialog .el-dialog__body) {
  background: linear-gradient(135deg, var(--dark-bg-start) 0%, var(--dark-bg-mid) 100%);
}

.dark .profile-container {
  background: linear-gradient(135deg, var(--dark-bg-start) 0%, var(--dark-bg-mid) 100%);
}

.dark .section {
  background: linear-gradient(135deg, var(--dark-accent-start) 0%, var(--dark-accent-mid) 100%);
  border: 1px solid var(--dark-border);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.dark .section:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.4);
  border-color: var(--dark-accent-blue);
}

.dark .section-header {
  border-bottom: 1px solid var(--dark-border);
}

.dark .section-title {
  color: var(--dark-text);
  border-left: 4px solid var(--dark-accent-blue);
  background: linear-gradient(135deg, rgba(52, 152, 219, 0.1) 0%, rgba(155, 89, 182, 0.05) 100%);
}

.dark .edit-btn {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
}

.dark .edit-btn:hover {
  background: linear-gradient(135deg, var(--dark-accent-purple) 0%, var(--dark-accent-blue) 100%);
}

.dark .profile-avatar {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
}

.dark .avatar-upload-btn {
  color: var(--dark-text);
  border: 1px solid var(--dark-border);
  background: var(--dark-accent-start);
}

.dark .avatar-upload-btn:hover {
  background: var(--dark-accent-mid);
  border-color: var(--dark-accent-blue);
  color: var(--dark-text);
}

.dark :deep(.basic-info .el-form-item__label) {
  color: var(--dark-text) !important;
}

.dark .readonly-text {
  color: var(--dark-text);
  background: rgba(52, 152, 219, 0.1);
  border: 1px dashed var(--dark-border);
}

.dark .readonly-textarea {
  color: var(--dark-text);
  background: rgba(52, 152, 219, 0.1);
  border: 1px dashed var(--dark-border);
}

.dark :deep(.input-field .el-input__inner),
.dark :deep(.input-field .el-textarea__inner) {
  border: 1px solid var(--dark-border);
  background: var(--dark-accent-start);
  color: var(--dark-text);
}

.dark :deep(.input-field .el-input__inner:focus),
.dark :deep(.input-field .el-textarea__inner:focus) {
  border-color: var(--dark-accent-blue);
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.dark :deep(.input-field .el-input__inner::placeholder),
.dark :deep(.input-field .el-textarea__inner::placeholder) {
  color: var(--dark-text);
  opacity: 0.7;
}

.dark :deep(.view-mode-toggle .el-button-group) {
  border: 1px solid var(--dark-border);
}

.dark :deep(.view-mode-toggle .el-button) {
  border: none;
  color: var(--dark-text);
  background: var(--dark-accent-start);
}

.dark :deep(.view-mode-toggle .el-button:hover) {
  background: var(--dark-accent-mid);
  color: var(--dark-text);
}

.dark :deep(.view-mode-toggle .el-button.is-active) {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
  color: white;
}

.dark .markdown-textarea {
  border: 1px solid var(--dark-border);
  background: var(--dark-accent-start);
  color: var(--dark-text);
}

.dark .markdown-textarea:focus {
  border-color: var(--dark-accent-blue);
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.dark .markdown-hint {
  color: var(--dark-text);
  opacity: 0.7;
}

.dark .markdown-preview {
  border: 1px solid var(--dark-border);
  background: var(--dark-accent-start);
}

.dark .markdown-render {
  color: var(--dark-text);
}

.dark .markdown-render :deep(strong) {
  color: var(--dark-text);
}

.dark .markdown-render :deep(em) {
  color: var(--dark-text);
  opacity: 0.8;
}

.dark .markdown-render :deep(code) {
  background: rgba(52, 152, 219, 0.1);
  color: var(--dark-text);
  border: 1px solid var(--dark-border);
}

.dark .markdown-render :deep(h3) {
  color: var(--dark-text);
  border-bottom: 1px solid var(--dark-border);
}

.dark .markdown-render :deep(h4) {
  color: var(--dark-text);
}

.dark .markdown-render :deep(li) {
  color: var(--dark-text);
}

.dark :deep(.skill-input .el-input__inner) {
  border: 1px solid var(--dark-border);
  background: var(--dark-accent-start);
  color: var(--dark-text);
}

.dark :deep(.skill-input .el-input__inner:focus) {
  border-color: var(--dark-accent-blue);
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.dark .skill-tag {
  background: linear-gradient(135deg, rgba(52, 152, 219, 0.1) 0%, rgba(155, 89, 182, 0.05) 100%);
  border: 1px solid var(--dark-border);
  color: var(--dark-text);
}

.dark .skill-tag:hover {
  border-color: var(--dark-accent-blue);
  background: var(--dark-accent-mid);
}

.dark .no-skills {
  color: var(--dark-text);
  opacity: 0.7;
}

.dark .dialog-footer {
  border-top: 1px solid var(--dark-border);
}

.dark .cancel-btn {
  color: var(--dark-text);
  border: 1px solid var(--dark-border);
  background: var(--dark-accent-start);
}

.dark .cancel-btn:hover {
  background: var(--dark-accent-mid);
  border-color: var(--dark-accent-blue);
  color: var(--dark-text);
}

.dark .confirm-btn {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
}

.dark .confirm-btn:hover {
  background: linear-gradient(135deg, var(--dark-accent-purple) 0%, var(--dark-accent-blue) 100%);
}

.dark .profile-container::-webkit-scrollbar-track {
  background: rgba(44, 62, 80, 0.5);
}

.dark .profile-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
}
</style>