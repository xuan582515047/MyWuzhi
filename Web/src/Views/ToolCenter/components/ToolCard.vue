<template>
  <!-- 网格视图 -->
  <div v-if="!listView" class="tool-card" @click="handleViewDetail">
    <div class="card-header">
      <div class="tool-icon">
        <el-icon size="32"><Tools /></el-icon>
      </div>
      <div class="tool-price">
        <span v-if="tool.price === '0'" class="free-badge">免费</span>
        <span v-else class="price">¥{{ tool.price }}</span>
      </div>
    </div>

    <div class="card-content">
      <h3 class="tool-name" :title="tool.name">{{ tool.name }}</h3>
      <p class="tool-description" :title="tool.description">{{ tool.description }}</p>

      <!-- 标签显示 -->
      <div class="tool-tags" v-if="tool.tagIds">
        <el-tag
            v-for="(tagId, index) in parseTagIds(tool.tagIds).slice(0, 3)"
            :key="tagId"
            size="small"
            type="info"
        >
          {{ getTagName(tagId) }}
        </el-tag>
        <el-tag v-if="parseTagIds(tool.tagIds).length > 3" size="small" type="info">
          +{{ parseTagIds(tool.tagIds).length - 3 }}
        </el-tag>
      </div>

      <!-- 统计信息 -->
      <div class="tool-stats">
        <div class="stat-item">
          <el-icon><Star /></el-icon>
          <span>{{ tool.rating || 0 }}</span>
        </div>
        <div class="stat-item">
          <el-icon><View /></el-icon>
          <span>{{ tool.viewCount || 0 }}</span>
        </div>
      </div>

      <!-- 版本信息 -->
      <div class="tool-version">
        <span class="version-text">{{ getToolTypeName(tool.type) }}</span>
        <span class="tool-type">{{ tool.version || '1.0.0' }}</span>
      </div>
    </div>

    <div class="card-actions">
      <el-button size="small" @click.stop="handleViewDetail">
        查看详情
      </el-button>
      <el-button
          v-if="tool.price !== '0' && tool.price !== '0.00' && parseFloat(tool.price) > 0"
          type="primary"
          size="small"
          @click.stop="handlePurchase"
      >
        立即购买
      </el-button>
      <el-button
          v-else
          type="success"
          size="small"
          @click.stop="handleUse"
      >
        立即使用
      </el-button>
    </div>
  </div>

  <!-- 列表视图 -->
  <div v-else class="tool-list-item" @click="handleViewDetail">
    <div class="item-left">
      <div class="tool-icon">
        <el-icon size="24"><Tools /></el-icon>
      </div>
      <div class="tool-info">
        <h3 class="tool-name">{{ tool.name }}</h3>
        <p class="tool-description">{{ tool.description }}</p>
        <div class="tool-meta">
          <span class="type">{{ getToolTypeName(tool.type) }}</span>
          <span class="price-info">
            {{ tool.price === '0' ? '免费' : `¥${tool.price}` }}
          </span>
        </div>
      </div>
    </div>

    <div class="item-right">
      <!-- 标签 -->
      <div class="tool-tags" v-if="tool.tagIds">
        <el-tag
            v-for="(tagId, index) in parseTagIds(tool.tagIds).slice(0, 4)"
            :key="tagId"
            size="small"
            type="info"
        >
          {{ getTagName(tagId) }}
        </el-tag>
        <el-tag v-if="parseTagIds(tool.tagIds).length > 4" size="small" type="info">
          +{{ parseTagIds(tool.tagIds).length - 4 }}
        </el-tag>
      </div>

      <!-- 统计信息 -->
      <div class="tool-stats">
        <div class="stat-item">
          <el-icon><Star /></el-icon>
          <span>{{ tool.rating || 0 }}</span>
        </div>
        <div class="stat-item">
          <el-icon><View /></el-icon>
          <span>{{ tool.viewCount || 0 }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="item-actions">
        <el-button size="small" @click.stop="handleViewDetail">
          查看详情
        </el-button>
        <el-button size="small" @click.stop="handleShare">
          <el-icon><Share /></el-icon>
        </el-button>
        <el-button
            v-if="tool.price !== '0' && tool.price !== '0.00' && parseFloat(tool.price) > 0"
            type="primary"
            size="small"
            @click.stop="handlePurchase"
        >
          立即购买
        </el-button>
        <el-button
            v-else
            type="success"
            size="small"
            @click.stop="handleUse"
        >
          立即使用
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Tools,
  View,
  User,
  Star,
  Share
} from '@element-plus/icons-vue'

// 导入类型
import type { ToolInfo, TagInfo, ConstantItem } from '@/Entity/ToolEntity'

// Props
interface Props {
  tool: ToolInfo
  listView?: boolean
  tagsList?: TagInfo[]
  toolTypes?: ConstantItem[]
}

const props = withDefaults(defineProps<Props>(), {
  listView: false,
  tagsList: () => [],
  toolTypes: () => []
})

// Emits
const emit = defineEmits<{
  viewDetail: [tool: ToolInfo]
  purchase: [tool: ToolInfo]
  share: [tool: ToolInfo]
  use: [tool: ToolInfo]
}>()

// =========================> 工具函数 <=========================
/**
 * 将tagIds转换为数组（后端已返回数组，此方法备用）
 */
const parseTagIds = (tagIds: string | string[]): string[] => {
  if (!tagIds) return []
  if (Array.isArray(tagIds)) return tagIds
  return tagIds.split(',').filter(id => id.trim().length > 0)
}

// =========================> 计算属性 <=========================
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

// =========================> 方法定义 <=========================
/**
 * 查看详情
 */
const handleViewDetail = () => {
  emit('viewDetail', props.tool)
}

/**
 * 购买工具
 */
const handlePurchase = () => {
  emit('purchase', props.tool)
}

/**
 * 分享工具
 */
const handleShare = () => {
  emit('share', props.tool)
}

/**
 * 使用工具
 */
const handleUse = () => {
  emit('use', props.tool)
}
</script>

<style scoped>
/* 网格卡片样式 */
.tool-card {
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: var(--el-color-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.tool-icon {
  width: 48px;
  height: 48px;
  background-color: var(--el-color-primary-light-9);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
}

.tool-price {
  text-align: right;
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

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tool-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-description {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.tool-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tool-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stat-item .el-icon {
  color: var(--el-color-warning);
}

.tool-version {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.version-text {
  color: var(--el-text-color-secondary);
}

.tool-type {
  color: var(--el-color-primary);
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.card-actions .el-button {
  flex: 1;
}

/* 列表项样式 */
.tool-list-item {
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 16px;
}

.tool-list-item:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.item-left .tool-icon {
  width: 40px;
  height: 40px;
  background-color: var(--el-color-primary-light-9);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.tool-info {
  flex: 1;
}

.tool-info .tool-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.tool-info .tool-description {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tool-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.type {
  color: var(--el-color-primary);
}

.price-info {
  font-weight: 600;
  color: var(--el-color-success);
}

.item-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-right .tool-tags {
  max-width: 200px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.item-right .tool-stats {
  padding: 0;
  border: none;
  flex-direction: column;
  gap: 4px;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.item-actions .el-button {
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tool-list-item {
    flex-direction: column;
    align-items: stretch;
  }

  .item-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .item-right {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .item-actions {
    width: 100%;
    justify-content: stretch;
  }

  .item-actions .el-button {
    flex: 1;
  }
}

/* 深色主题适配 */
.dark .tool-card,
.dark .tool-list-item {
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color);
}

.dark .tool-card:hover,
.dark .tool-list-item:hover {
  border-color: var(--el-color-primary);
}

.dark .tool-icon {
  background-color: var(--el-color-primary-dark-2);
}
</style>