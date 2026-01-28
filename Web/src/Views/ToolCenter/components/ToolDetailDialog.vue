<template>
  <el-dialog
      v-model="dialogVisible"
      :title="tool?.name || '工具详情'"
      width="900px"
      destroy-on-close
      class="tool-detail-dialog"
  >
    <div v-if="tool" class="tool-detail-content">
      <!-- 工具头部信息 -->
      <div class="detail-header">
        <div class="tool-icon-large">
          <el-icon size="48"><Tools /></el-icon>
        </div>
        <div class="tool-basic-info">
          <h2>{{ tool.name }}</h2>
          <div class="tool-meta">
            <el-tag type="primary">{{ getToolTypeName(tool.type) }}</el-tag>
            <span v-if="tool.price === '0'" class="free-badge">免费工具</span>
            <span v-else class="price">¥{{ tool.price }}</span>
            <span class="version">版本 {{ tool.version || '1.0.0' }}</span>
          </div>
          <div class="tool-stats">
            <div class="stat">
              <el-icon><Star /></el-icon>
              <span>{{ tool.rating || 0 }} 分</span>
            </div>
            <div class="stat">
              <el-icon><View /></el-icon>
              <span>{{ tool.viewCount || 0 }} 次浏览</span>
            </div>
            <div class="stat">
              <el-icon><User /></el-icon>
              <span>{{ tool.useCount || 0 }} 次使用</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 工具描述 -->
      <div class="detail-section">
        <h3>工具描述</h3>
        <div class="description-content">
          <p>{{ tool.description }}</p>
        </div>
      </div>

      <!-- 工具标签 -->
      <div class="detail-section" v-if="tool.tagIds">
        <h3>相关标签</h3>
        <div class="tags-content">
          <el-tag
              v-for="(tagId, index) in parseTagIds(tool.tagIds)"
              :key="tagId"
              type="info"
              class="tag-item"
          >
            {{ getTagName(tagId) }}
          </el-tag>
        </div>
      </div>

      <!-- 工具关键词 -->
      <div class="detail-section" v-if="tool.keywords">
        <h3>关键词</h3>
        <div class="keywords-content">
          <el-tag
              v-for="(keyword, index) in parseKeywords(tool.keywords)"
              :key="index"
              type="info"
              size="small"
              class="keyword-item"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </div>

      <!-- 工具信息 -->
      <div class="detail-section">
        <h3>工具信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>工具ID:</label>
            <span>{{ tool.id }}</span>
          </div>
          <div class="info-item">
            <label>访问地址:</label>
            <el-link :href="tool.url" target="_blank" type="primary">
              {{ tool.url }}
              <el-icon><Link /></el-icon>
            </el-link>
          </div>
          <div class="info-item">
            <label>创建时间:</label>
            <span>{{ formatDateTime(tool.createTime) }}</span>
          </div>
          <div class="info-item">
            <label>更新时间:</label>
            <span>{{ formatDateTime(tool.updateTime) }}</span>
          </div>
          <div class="info-item">
            <label>价格:</label>
            <span :class="tool.price === '0' ? 'free-text' : 'price-text'">
              {{ tool.price === '0' ? '免费' : `¥${tool.price}` }}
            </span>
          </div>
          <div class="info-item">
            <label>工具类型:</label>
            <span>{{ getToolTypeName(tool.type) }}</span>
          </div>
        </div>
      </div>

      <!-- 操作区域 -->
      <div class="action-section">
        <div class="action-buttons">
          <el-button @click="handleClose">关闭</el-button>
          <el-button @click="handleShare">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
          <el-button
              v-if="tool.price !== '0' && tool.price !== '0.00' && parseFloat(tool.price) > 0"
              type="primary"
              size="large"
              @click="handlePurchase"
          >
            立即购买
          </el-button>
          <el-button
              v-else
              type="success"
              size="large"
              @click="handleUse"
          >
            立即使用
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Tools,
  User,
  Star,
  Share,
  Link,
  View
} from '@element-plus/icons-vue'

// 导入类型
import type { ToolInfo, TagInfo, ConstantItem } from '@/Entity/ToolEntity'

// Props
interface Props {
  visible: boolean
  tool?: ToolInfo | null
  tagsList?: TagInfo[]
  toolTypes?: ConstantItem[]
}

const props = withDefaults(defineProps<Props>(), {
  tool: null,
  tagsList: () => [],
  toolTypes: () => []
})

// Emits
const emit = defineEmits<{
  'update:visible': [visible: boolean]
  purchase: [tool: ToolInfo]
  share: [tool: ToolInfo]
  use: [tool: ToolInfo]
}>()

// =========================> 计算属性 <=========================
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

/**
 * 获取工具类型名称
 */
const getToolTypeName = (typeValue: string): string => {
  const type = props.toolTypes.find(t => t.value === typeValue)
  return type ? type.name : typeValue
}

/**
 * 根据标签ID获取标签名称
 */
const getTagName = (tagId: string): string => {
  const tag = props.tagsList.find(t => t.id === tagId)
  return tag ? tag.name : tagId
}

/**
 * 将tagIds转换为数组（后端已返回数组，此方法备用）
 */
const parseTagIds = (tagIds: string | string[]): string[] => {
  if (!tagIds) return []
  if (Array.isArray(tagIds)) return tagIds
  return tagIds.split(',').filter(id => id.trim().length > 0)
}

/**
 * 将keywords转换为数组（后端已返回数组，此方法备用）
 */
const parseKeywords = (keywords: string | string[]): string[] => {
  if (!keywords) return []
  if (Array.isArray(keywords)) return keywords
  return keywords.split(',').filter(keyword => keyword.trim().length > 0)
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return '未知'

  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateTime
  }
}

// =========================> 方法定义 <=========================
/**
 * 关闭弹窗
 */
const handleClose = () => {
  dialogVisible.value = false
}

/**
 * 分享工具
 */
const handleShare = () => {
  if (props.tool) {
    emit('share', props.tool)
  }
}

/**
 * 购买工具
 */
const handlePurchase = () => {
  if (props.tool) {
    emit('purchase', props.tool)
  }
}

/**
 * 使用工具
 */
const handleUse = () => {
  if (props.tool) {
    emit('use', props.tool)
  }
}
</script>

<style scoped>
.tool-detail-dialog :deep(.el-dialog__body) {
  padding: 0 24px 24px;
}

.tool-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.tool-icon-large {
  width: 80px;
  height: 80px;
  background-color: var(--el-color-primary-light-9);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.tool-basic-info {
  flex: 1;
}

.tool-basic-info h2 {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.tool-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.version {
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.free-badge {
  background-color: var(--el-color-success);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.price {
  color: var(--el-color-primary);
  font-size: 18px;
  font-weight: 600;
}

.tool-stats {
  display: flex;
  gap: 24px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.stat .el-icon {
  color: var(--el-color-warning);
}

.detail-section {
  padding: 20px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.description-content {
  line-height: 1.6;
  color: var(--el-text-color-regular);
}

.tags-content,
.keywords-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0;
}

.keyword-item {
  margin: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item label {
  font-weight: 500;
  color: var(--el-text-color-secondary);
  min-width: 80px;
}

.info-item span {
  color: var(--el-text-color-regular);
  flex: 1;
}

.free-text {
  color: var(--el-color-success);
  font-weight: 600;
}

.price-text {
  color: var(--el-color-primary);
  font-weight: 600;
}

.action-section {
  padding: 24px 0 0 0;
  border-top: 1px solid var(--el-border-color-lighter);
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.action-buttons .el-button {
  min-width: 100px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .tool-meta {
    justify-content: center;
    flex-wrap: wrap;
  }

  .tool-stats {
    justify-content: center;
    flex-wrap: wrap;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-buttons {
    justify-content: center;
    flex-wrap: wrap;
  }
}

/* 深色主题适配 */
.dark .tool-detail-dialog :deep(.el-dialog__body) {
  background-color: var(--el-bg-color);
}

.dark .detail-header,
.dark .detail-section {
  border-color: var(--el-border-color);
}

.dark .tool-icon-large {
  background-color: var(--el-color-primary-dark-2);
}

.dark .free-badge {
  background-color: var(--el-color-success-dark-2);
}
</style>