<template>
  <div class="tool-market-sidebar">
    <div class="sidebar-section">
      <div class="section-header">
        <el-icon><Tools /></el-icon>
        <span>工具类型</span>
      </div>
      <div class="section-content">
        <el-radio-group v-model="localToolType" @change="handleToolTypeChange" class="type-radio-group compact">
          <el-radio label="">全部类型</el-radio>
          <el-radio
              v-for="type in toolTypes"
              :key="type.value"
              :label="type.value"
          >
            {{ type.name }}
          </el-radio>
        </el-radio-group>
      </div>
    </div>

    <div class="sidebar-section">
      <div class="section-header">
        <el-icon><Money /></el-icon>
        <span>价格筛选</span>
      </div>
      <div class="section-content">
        <el-radio-group v-model="localPriceType" @change="handlePriceTypeChange" class="price-radio-group compact">
          <el-radio label="">全部价格</el-radio>
          <el-radio label="free">免费</el-radio>
          <el-radio label="paid">付费</el-radio>
        </el-radio-group>

        <!-- 价格范围选择器 -->
        <div class="price-range" v-if="localPriceType === 'paid'">
          <el-slider
              v-model="localPriceRange"
              :min="0"
              :max="maxPriceValue"
              :step="10"
              range
              show-stops
              :format-tooltip="formatPrice"
              @change="handlePriceRangeChange"
              class="compact-slider"
          />
          <div class="price-display">
            <span>¥{{ localPriceRange[0] }}</span>
            <span>¥{{ localPriceRange[1] }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="sidebar-section">
      <div class="section-header">
        <el-icon><CollectionTag /></el-icon>
        <span>热门标签</span>
        <el-button type="text" size="small" @click="showAllTags = !showAllTags" class="toggle-tags-btn">
          {{ showAllTags ? '收起' : '展开' }}
        </el-button>
      </div>
      <div class="section-content">
        <div class="tags-cloud compact">
          <el-tag
              v-for="tag in displayTags"
              :key="tag.id"
              :type="isTagSelected(tag.id) ? 'primary' : 'info'"
              :effect="isTagSelected(tag.id) ? 'dark' : 'plain'"
              @click="toggleTag(tag.id)"
              class="tag-item"
              size="small"
          >
            {{ tag.name }}
          </el-tag>
        </div>
      </div>
    </div>

    <div class="sidebar-section">
      <div class="section-header">
        <el-icon><Sort /></el-icon>
        <span>排序方式</span>
      </div>
      <div class="section-content compact">
        <div class="sort-container">
          <el-select
              v-model="localSortBy"
              @change="handleSortByChange"
              placeholder="排序方式"
              size="small"
              class="sort-select"
          >
            <el-option label="创建时间（最新）" value="createTime" />
            <el-option label="评分（最高）" value="rating" />
            <el-option label="使用量（最多）" value="useCount" />
            <el-option label="浏览量（最多）" value="viewCount" />
            <el-option label="价格（最低）" value="price" />
          </el-select>

          <el-radio-group v-model="localSortOrder" @change="handleSortOrderChange" class="sort-order" size="small">
            <el-radio-button label="desc">降序</el-radio-button>
            <el-radio-button label="asc">升序</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <div class="sidebar-section">
      <div class="section-header">
        <el-icon><TrendCharts /></el-icon>
        <span>统计信息</span>
      </div>
      <div class="section-content">
        <div class="stats-info compact">
          <div class="stat-item">
            <span class="stat-value">{{ stats.totalTools || 0 }}</span>
            <span class="stat-label">工具总数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.freeTools || 0 }}</span>
            <span class="stat-label">免费工具</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.paidTools || 0 }}</span>
            <span class="stat-label">付费工具</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  Tools,
  Money,
  CollectionTag,
  Sort,
  TrendCharts
} from '@element-plus/icons-vue'

// 导入类型
import type { TagInfo, ConstantItem } from '@/Entity/ToolEntity'

// Props
interface Props {
  toolType?: string
  tags?: string[]
  priceRange?: [number, number]
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
  toolTypes?: ConstantItem[]
  popularTags?: TagInfo[]
  stats?: {
    totalTools: number
    freeTools: number
    paidTools: number
  }
}

const props = withDefaults(defineProps<Props>(), {
  toolType: '',
  tags: () => [],
  priceRange: () => [0, 9999],
  sortBy: 'createTime',
  sortOrder: 'desc',
  toolTypes: () => [],
  popularTags: () => [],
  stats: () => ({
    totalTools: 0,
    freeTools: 0,
    paidTools: 0
  })
})

// Emits
const emit = defineEmits<{
  'update:toolType': [toolType: string]
  'update:tags': [tags: string[]]
  'update:priceRange': [priceRange: [number, number]]
  'update:sortBy': [sortBy: string]
  'update:sortOrder': [sortOrder: 'asc' | 'desc']
  filterChange: []
}>()

// =========================> 响应式数据 <=========================
/** 本地状态 */
const localToolType = ref(props.toolType)
const localPriceType = ref('')
const localPriceRange = ref<[number, number]>([...props.priceRange])
const localSortBy = ref(props.sortBy)
const localSortOrder = ref<'asc' | 'desc'>(props.sortOrder)
const showAllTags = ref(false)
const maxPriceValue = ref(9999)

// =========================> 计算属性 <=========================
/** 显示的标签列表 */
const displayTags = computed(() => {
  return showAllTags.value ? props.popularTags : props.popularTags.slice(0, 10)
})

// =========================> 监听器 <=========================
watch(
    () => props.toolType,
    (newType) => {
      localToolType.value = newType
    }
)

watch(
    () => props.tags,
    () => {
      // 这里不需要直接更新，因为tags是从外部管理的
    },
    { deep: true }
)

watch(
    () => props.priceRange,
    (newRange) => {
      localPriceRange.value = [...newRange]
      // 更新价格类型
      if (newRange[0] === 0 && newRange[1] === 0) {
        localPriceType.value = 'free'
      } else if (newRange[0] > 0 || newRange[1] < maxPriceValue.value) {
        localPriceType.value = 'paid'
      } else {
        localPriceType.value = ''
      }
    }
)

watch(
    () => props.sortBy,
    (newSortBy) => {
      localSortBy.value = newSortBy
    }
)

watch(
    () => props.sortOrder,
    (newSortOrder) => {
      localSortOrder.value = newSortOrder
    }
)

// =========================> 方法定义 <=========================
/**
 * 格式化价格显示
 */
const formatPrice = (value: number): string => {
  return `¥${value}`
}

/**
 * 检查标签是否被选中
 */
const isTagSelected = (tagId: string): boolean => {
  return props.tags.includes(tagId)
}

/**
 * 切换标签选择
 */
const toggleTag = (tagId: string) => {
  const newTags = [...props.tags]
  const index = newTags.indexOf(tagId)

  if (index > -1) {
    newTags.splice(index, 1)
  } else {
    newTags.push(tagId)
  }

  emit('update:tags', newTags)
  emit('filterChange')
}

/**
 * 工具类型变化处理
 */
const handleToolTypeChange = (value: string) => {
  localToolType.value = value
  emit('update:toolType', value)
  emit('filterChange')
}

/**
 * 价格类型变化处理
 */
const handlePriceTypeChange = (value: string) => {
  localPriceType.value = value

  if (value === 'free') {
    localPriceRange.value = [0, 0]
  } else if (value === 'paid') {
    localPriceRange.value = [1, maxPriceValue.value]
  } else {
    localPriceRange.value = [0, maxPriceValue.value]
  }

  emit('update:priceRange', localPriceRange.value)
  emit('filterChange')
}

/**
 * 价格范围变化处理
 */
const handlePriceRangeChange = (range: [number, number]) => {
  // 如果价格范围不是全范围，设置价格类型为付费
  if (range[0] > 0 || range[1] < maxPriceValue.value) {
    localPriceType.value = 'paid'
  } else {
    localPriceType.value = ''
  }

  emit('update:priceRange', range)
  emit('filterChange')
}

/**
 * 排序字段变化处理
 */
const handleSortByChange = (value: string) => {
  localSortBy.value = value
  emit('update:sortBy', value)
  emit('filterChange')
}

/**
 * 排序顺序变化处理
 */
const handleSortOrderChange = (value: 'asc' | 'desc') => {
  localSortOrder.value = value
  emit('update:sortOrder', value)
  emit('filterChange')
}
</script>

<style scoped>
.tool-market-sidebar {
  width: 260px;
  padding: 12px 10px;
  background-color: var(--el-bg-color-overlay);
  border-right: 1px solid var(--el-border-color-light);
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

.sidebar-section {
  margin-bottom: 12px;
  position: relative;
}

.sidebar-section:not(:last-child)::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  right: 0;
  height: 1px;
  background-color: var(--el-border-color-lighter);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  position: relative;
  font-size: 13px;
}

.section-header .el-icon {
  color: var(--el-color-primary);
  font-size: 14px;
}

.toggle-tags-btn {
  margin-left: auto;
  padding: 0;
  font-size: 11px;
  color: var(--el-color-primary);
  height: auto;
  min-height: auto;
}

.section-content {
  padding-left: 20px;
}

.section-content.compact {
  padding-left: 18px;
}

/* 工具类型选择 */
.type-radio-group.compact {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

:deep(.type-radio-group.compact .el-radio) {
  margin-right: 0;
  margin-bottom: 0;
  height: 22px;
  display: flex;
  align-items: center;
}

:deep(.type-radio-group.compact .el-radio__label) {
  font-size: 12px;
  line-height: 1.2;
}

/* 价格筛选 */
.price-radio-group.compact {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 6px;
}

:deep(.price-radio-group.compact .el-radio) {
  margin-right: 0;
  margin-bottom: 0;
  height: 22px;
}

:deep(.price-radio-group.compact .el-radio__label) {
  font-size: 12px;
  line-height: 1.2;
}

.price-range {
  margin-top: 8px;
}

.compact-slider {
  margin: 4px 0;
}

.price-display {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

/* 标签云 */
.tags-cloud.compact {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.2s ease;
  margin: 0;
  font-size: 11px;
  padding: 2px 5px;
  height: 20px;
  line-height: 16px;
}

.tag-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 排序选择 */
.sort-container {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sort-select {
  width: 100%;
}

:deep(.sort-select .el-input__inner) {
  height: 26px;
  line-height: 26px;
  font-size: 12px;
}

.sort-order {
  width: 100%;
}

:deep(.sort-order .el-radio-group) {
  display: flex;
  width: 100%;
}

:deep(.sort-order .el-radio-button) {
  flex: 1;
  text-align: center;
}

:deep(.sort-order .el-radio-button__inner) {
  padding: 5px 8px;
  font-size: 11px;
  height: 24px;
}

/* 统计信息 */
.stats-info.compact {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.stat-item {
  text-align: center;
  padding: 6px 3px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 6px;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background-color: var(--el-fill-color-light);
  transform: translateY(-1px);
}

.stat-value {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 1px;
  line-height: 1.2;
}

.stat-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  line-height: 1.2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tool-market-sidebar {
    width: 100%;
    padding: 12px;
    border-right: none;
    border-bottom: 1px solid var(--el-border-color-light);
  }

  .sidebar-section {
    margin-bottom: 16px;
  }

  .sidebar-section:not(:last-child)::after {
    bottom: -8px;
  }

  .section-content {
    padding-left: 20px;
  }

  .stats-info.compact {
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }

  .stat-item {
    padding: 6px 3px;
  }
}

/* 滚动条样式 */
.tool-market-sidebar::-webkit-scrollbar {
  width: 4px;
}

.tool-market-sidebar::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 2px;
}

.tool-market-sidebar::-webkit-scrollbar-thumb {
  background: var(--el-border-color-darker);
  border-radius: 2px;
}

.tool-market-sidebar::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-dark);
}

/* 深色主题适配 */
.dark .tool-market-sidebar {
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color);
}

.dark .stat-item {
  background-color: var(--el-fill-color-darker);
}

.dark .stat-item:hover {
  background-color: var(--el-fill-color-dark);
}

.dark .sidebar-section:not(:last-child)::after {
  background-color: var(--el-border-color-light);
}
</style>