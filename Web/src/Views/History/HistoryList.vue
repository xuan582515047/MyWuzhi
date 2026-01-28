<script setup lang="ts">
import {computed, defineEmits, defineProps, ref} from 'vue'
import {Delete, EditPen, More, Star} from "@element-plus/icons-vue";
import type {ConversationData} from "@/Entity/ChatEntity.ts";
//==========================> 类型定义 <==========================
interface TimeGroup {
  label: string
  timeRange: [Date, Date]
  conversations: ConversationData[]
}

//==========================> 变量定义 <==========================
// 通信变量
const emits = defineEmits(['click', 'rename', 'delete'])
const props = defineProps({
  conversationList: {type: Array<ConversationData>, required: true},
  currentId: {type: String, default: ''},
  splitTime: {type: Array<string>, default: () => ['0', '7', '30']}
})

// 响应式变量
const showRenameDialog = ref(false);
const showDeleteDialog = ref(false);
const hoveredItem = ref('') // 鼠标悬停的项目ID
const currentConversation = ref({} as ConversationData)

//==========================> 方法定义 <=========================

/**
 * 为对话重命名
 * @param conversation
 */
const renameConversation = (conversation: ConversationData) => {
  showRenameDialog.value = true
  currentConversation.value = conversation
  currentConversation.value = conversation
}

/**
 * 收藏对话
 * @param conversation
 */
const markConversation = (conversation: ConversationData) => {
  currentConversation.value = conversation
  // 这里可以添加收藏逻辑
}

/**
 * 删除对话
 * @param conversation
 */
const deleteConversation = (conversation: ConversationData) => {
  showDeleteDialog.value = true
  currentConversation.value = conversation
}

/**
 * 确认重命名
 */
const confirmRename = () => {
  showRenameDialog.value = false
  emits('rename', currentConversation.value)
}

/**
 * 确认删除
 */
const confirmDelete = () => {
  showDeleteDialog.value = false
  emits('delete', currentConversation.value)
  currentConversation.value = {} as ConversationData
}

/**
 * 获取分组标签
 * @param start
 * @param end
 */
const getGroupLabel = (start: number, end: number): string => {
  if (start === 0) {
    return `${end}天内`
  }
  return `${start}-${end}天`
}

/**
 * 选中对话
 * @param conversation
 */
const selectConversation = (conversation: ConversationData) => {
  emits('click', conversation)
}

/**
 * 鼠标进入项目
 * @param conversationId
 */
const handleMouseEnter = (conversationId: string) => {
  hoveredItem.value = conversationId
}

/**
 * 鼠标离开项目
 */
const handleMouseLeave = () => {
  hoveredItem.value = ''
}

/**
 * 计算分组后的对话列表
 */
const groupedConversations = computed(() => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const groups: TimeGroup[] = []

  // 转换时间点为数字
  const timePoints = props.splitTime.map(time => parseInt(time))

  // 添加第一个分组 - 今天
  groups.push({
    label: '今天',
    timeRange: [today, new Date(today.getTime() + 24 * 60 * 60 * 1000)],
    conversations: []
  })

  // 添加中间分组
  for (let i = 0; i < timePoints.length - 1; i++) {
    const start = new Date(today.getTime() - timePoints[i+1]! * 24 * 60 * 60 * 1000)
    const end = new Date(today.getTime() - timePoints[i]! * 24 * 60 * 60 * 1000)

    groups.push({
      label: getGroupLabel(timePoints[i]!, timePoints[i+1]!),
      timeRange: [start, end],
      conversations: []
    })
  }

  // 添加最后一个分组 - 超过最后一个时间点
  groups.push({
    label: `${timePoints[timePoints.length - 1]}天外`,
    timeRange: [new Date(0), new Date(today.getTime() - timePoints[timePoints.length - 1]! * 24 * 60 * 60 * 1000)],
    conversations: []
  })

  // 将对话分配到对应分组
  props.conversationList.forEach(conversation => {
    const conversationTime = new Date(conversation.updateTime)

    for (const group of groups) {
      if (conversationTime >= group.timeRange[0] && conversationTime < group.timeRange[1]) {
        group.conversations.push(conversation)
        break
      }
    }
  })

  // 过滤掉没有对话的分组
  return groups.filter(group => group.conversations.length > 0)
})
</script>

<template>
  <div class="conversation-history">
    <div v-for="group in groupedConversations" :key="group.label" class="time-group">
      <div class="group-label">{{ group.label }}</div>
      <div class="conversation-list">
        <!-- 每一个历史对话框 -->
        <div
            v-for="conversation in group.conversations"
            :key="conversation.id"
            class="conversation-item"
            :class="{
              active: props.currentId === conversation.id,
              hovered: hoveredItem === conversation.id
            }"
            @click="selectConversation(conversation)"
            @mouseenter="handleMouseEnter(conversation.id)"
            @mouseleave="handleMouseLeave"
        >
          <!-- 对话图标 -->
          <div class="conversation-icon">
            <div class="icon-wrapper">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M20 2H4C2.9 2 2 2.9 2 4V22L6 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2Z"
                      :fill="props.currentId === conversation.id ? 'white' : 'var(--text-secondary)'"/>
              </svg>
            </div>
          </div>

          <!-- 对话内容 -->
          <div class="conversation-content">
            <div class="conversation-title">
              {{ conversation.title }}
            </div>
          </div>

          <!-- 操作菜单 -->
          <div class="conversation-actions" :class="{ visible: hoveredItem === conversation.id }">
            <el-dropdown placement="bottom-start" trigger="click" :hide-on-click="false">
              <div class="action-trigger" @click.stop>
                <el-icon><More /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="conversation-menu">
                  <el-dropdown-item @click="renameConversation(conversation)" class="menu-item">
                    <el-icon class="menu-icon"><EditPen /></el-icon>
                    <span>重命名</span>
                  </el-dropdown-item>
                  <el-dropdown-item @click="markConversation(conversation)" class="menu-item">
                    <el-icon class="menu-icon"><Star /></el-icon>
                    <span>收藏</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                      @click="deleteConversation(conversation)"
                      class="menu-item delete-item"
                      divided
                  >
                    <el-icon class="menu-icon"><Delete /></el-icon>
                    <span>删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 重命名对话框 -->
  <el-dialog
      v-model="showRenameDialog"
      title="重命名对话"
      width="400px"
      class="modern-dialog"
      :before-close="() => showRenameDialog = false"
  >
    <div class="dialog-content">
      <el-input
          v-model="currentConversation.title"
          maxlength="26"
          show-word-limit
          placeholder="请输入新的对话名称"
          class="rename-input"
      />
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="showRenameDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="confirmRename" class="confirm-btn">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 删除确认对话框 -->
  <el-dialog
      v-model="showDeleteDialog"
      title="删除对话"
      width="400px"
      class="modern-dialog danger-dialog"
      :before-close="() => showDeleteDialog = false"
  >
    <div class="dialog-content">
      <div class="delete-warning">
        <el-icon color="#ff4d4f" class="warning-icon"><Delete /></el-icon>
        <span>删除后，该对话将不可恢复。确认删除吗？</span>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="showDeleteDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="danger" @click="confirmDelete" class="confirm-btn">
          确认删除
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.conversation-history {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  user-select: none;
  padding: 16px 12px;
  background: transparent;
  position: relative;
}

.time-group {
  margin-bottom: 20px;
  animation: slideInMenu 0.3s ease-out;
}

@keyframes slideInMenu {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.group-label {
  font-size: 12px;
  color: var(--text-tertiary);
  padding: 8px 16px 6px;
  font-weight: 500;
  margin: 0 8px 10px 8px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-label::before {
  content: '';
  width: 3px;
  height: 12px;
  background: var(--primary-color);
  border-radius: 2px;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin: 0 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
  width: calc(100% - 16px) !important;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  border: 1px solid var(--border-light);
  justify-content: flex-start;
}



.conversation-item:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  border-color: var(--border-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.conversation-item.active {
  background: var(--primary-color);
  color: white;
  font-weight: 600;
  border: 1px solid var(--primary-color);
  position: relative;
  box-shadow: 0 2px 12px rgba(124, 58, 237, 0.25);
}



.conversation-icon {
  flex-shrink: 0;
  margin-right: 12px;
  position: relative;
  z-index: 1;
}

.icon-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  transition: all 0.2s ease;
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
}

.conversation-item:hover .icon-wrapper {
  background: var(--hover-bg);
  border-color: var(--primary-color);
}

.conversation-item.active .icon-wrapper {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.25);
}

.conversation-content {
  flex: 1;
  min-width: 0;
  margin-right: 10px;
  position: relative;
  z-index: 1;
}

.conversation-title {
  min-width: 180px;
  max-width: 180px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.25s ease;
}

.conversation-item:hover .conversation-title {
  color: var(--text-primary);
}

.conversation-item.active .conversation-title {
  color: white;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.conversation-preview {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  opacity: 0.8;
  transition: all 0.3s ease;
}

.conversation-item:hover .conversation-preview {
  opacity: 1;
  color: var(--text-secondary);
}

.conversation-item.active .conversation-preview {
  color: rgba(255, 255, 255, 0.9);
  opacity: 0.9;
}

.conversation-actions {
  opacity: 0;
  transform: translateX(8px);
  transition: all 0.25s ease;
  position: relative;
  z-index: 2;
}

.conversation-actions.visible {
  opacity: 1;
  transform: translateX(0);
}

.action-trigger {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-secondary);
  background: transparent;
  border: none;
  position: relative;
  overflow: hidden;
}

.action-trigger:hover {
  color: var(--text-primary);
}

.dark .action-trigger:hover {
  color: var(--text-primary);
}

.action-trigger:hover .el-icon {
  color: white;
  transform: scale(1.1);
}

.conversation-item:hover .conversation-actions {
  opacity: 1;
  transform: translateX(0) scale(1);
}



/* 下拉菜单样式优化 */
:deep(.conversation-menu) {
  border-radius: 20px !important;
  border: 1px solid var(--border-light) !important;
  background: var(--card-bg) !important;
  padding: 12px !important;
  box-shadow: var(--shadow-large) !important;
  backdrop-filter: blur(20px) !important;
  overflow: hidden;
}

:deep(.conversation-menu .el-dropdown-menu__item) {
  border-radius: 16px !important;
  margin: 4px !important;
  padding: 14px 18px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  color: var(--text-primary) !important;
  font-weight: 600;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.conversation-menu .el-dropdown-menu__item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-purple) 0%, var(--primary-blue) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 16px;
}

:deep(.conversation-menu .el-dropdown-menu__item:hover) {
  transform: translateX(6px) scale(1.02) !important;
  color: white !important;
}

:deep(.conversation-menu .el-dropdown-menu__item:hover::before) {
  opacity: 1;
}

:deep(.conversation-menu .menu-icon) {
  width: 20px;
  height: 20px;
  color: inherit;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

:deep(.conversation-menu .delete-item) {
  color: var(--danger-color);
}

:deep(.conversation-menu .delete-item::before) {
  background: linear-gradient(135deg, #f56c6c, #e53e3e) !important;
}

:deep(.conversation-menu .delete-item:hover) {
  transform: translateX(6px) scale(1.02) !important;
  color: white !important;
}

/* 对话框样式优化 */
:deep(.modern-dialog) {
  border-radius: 24px !important;
  background: var(--card-bg) !important;
  border: 1px solid var(--border-light);
  backdrop-filter: blur(20px) !important;
  overflow: hidden;
}

:deep(.modern-dialog .el-dialog__header) {
  padding: 28px 32px 0;
  margin: 0;
  background: linear-gradient(135deg, var(--primary-purple) 0%, var(--primary-blue) 100%);
  border-radius: 24px 24px 0 0;
  color: white;
  position: relative;
}

:deep(.modern-dialog .el-dialog__header::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
}

:deep(.modern-dialog .el-dialog__title) {
  color: white !important;
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.5px;
}

:deep(.modern-dialog .el-dialog__body) {
  padding: 28px 32px;
  background: transparent;
}

:deep(.modern-dialog .el-dialog__footer) {
  padding: 0 32px 28px;
  margin: 0;
  background: transparent;
}

:deep(.danger-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #f56c6c, #e53e3e) !important;
}

.dialog-content {
  padding: 16px 0;
}

.rename-input {
  width: 100%;
  border-radius: 16px;
}

:deep(.rename-input .el-input__wrapper) {
  border-radius: 16px;
  border: 2px solid var(--border-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--card-bg);
  box-shadow: var(--shadow-light);
}

:deep(.rename-input .el-input__wrapper:hover) {
  border-color: var(--border-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

:deep(.rename-input.is-focus .el-input__wrapper) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-color-light);
  transform: translateY(-3px);
}

:deep(.rename-input .el-input__inner) {
  color: var(--text-primary);
  font-weight: 600;
  background: transparent;
  font-size: 15px;
}

.delete-warning {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: var(--danger-bg);
  border-radius: 16px;
  border: 2px solid var(--danger-border);
  color: var(--danger-color);
  font-weight: 600;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

.warning-icon {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6px); }
  75% { transform: translateX(6px); }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.cancel-btn {
  padding: 12px 28px;
  border-radius: 16px;
  border: 2px solid var(--border-light);
  color: var(--text-primary);
  background: var(--card-bg);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  box-shadow: var(--shadow-light);
  font-size: 15px;
}

.cancel-btn:hover {
  background: var(--card-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
  border-color: var(--border-primary);
}

.confirm-btn {
  padding: 12px 28px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--primary-purple) 0%, var(--primary-blue) 100%);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: white;
  font-weight: 600;
  box-shadow: var(--shadow-medium);
  font-size: 15px;
}

.confirm-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: var(--shadow-heavy);
}

:deep(.danger-dialog .confirm-btn) {
  background: linear-gradient(135deg, #f56c6c, #e53e3e);
}

:deep(.danger-dialog .confirm-btn:hover) {
  box-shadow: 0 6px 24px rgba(245, 108, 108, 0.4);
}

/* 滚动条样式优化 */
.conversation-history::-webkit-scrollbar {
  width: 8px;
}

.conversation-history::-webkit-scrollbar-track {
  background: var(--scrollbar-track);
  border-radius: 4px;
  margin: 12px 0;
}

.conversation-history::-webkit-scrollbar-thumb {
  background: var(--scrollbar-thumb);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.conversation-history::-webkit-scrollbar-thumb:hover {
  background: var(--scrollbar-thumb-hover);
  transform: scaleY(1.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .conversation-history {
    padding: 8px 4px;
  }

  .conversation-item {
    margin: 0 4px;
    padding: 8px 12px;
  }

  .conversation-content {
    margin-right: 6px;
  }

  .conversation-actions {
    opacity: 0.6; /* 移动端半透明显示操作按钮 */
    transform: translateX(0);
  }

  .conversation-item:hover .conversation-actions {
    opacity: 1;
  }

  .group-label {
    margin: 0 4px 6px 4px;
    padding: 4px 12px;
    font-size: 11px;
  }

  .icon-wrapper {
    width: 24px;
    height: 24px;
  }

  .action-trigger {
    width: 24px;
    height: 24px;
  }

  .action-trigger:hover {
    /* 移动端也无背景效果 */
  }

  .conversation-title {
    font-size: 13px;
  }
}

/* 暗色模式特殊处理 */
.dark .conversation-item {
  backdrop-filter: blur(20px);
}

.dark .icon-wrapper,
.dark .action-trigger {
  backdrop-filter: blur(10px);
}

/* 加载动画 */
.conversation-item {
  animation: slideInMenu 0.6s ease-out;
}

/* 空状态样式 */
.conversation-history:empty::before {
  content: '暂无对话记录';
  display: flex;
  align-items: center;
  justify-content: center;
  width: 300px;
  height: 200px;
  color: var(--text-tertiary);
  font-size: 15px;
  font-weight: 600;
}
</style>