<template>
  <div class="filter-panel">
    <el-card class="filter-card">
      <template #header>
        <div class="panel-header">
          <span>高级筛选</span>
          <el-button type="text" @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </template>

      <div class="filter-content">
        <!-- 价格筛选 -->
        <div class="filter-group">
          <label class="filter-label">价格区间</label>
          <div class="price-filter">
            <el-input-number
                v-model="localFilters.minPrice"
                :min="0"
                :precision="2"
                placeholder="最低价格"
                size="small"
                style="width: 120px"
            />
            <span class="separator">-</span>
            <el-input-number
                v-model="localFilters.maxPrice"
                :min="0"
                :precision="2"
                placeholder="最高价格"
                size="small"
                style="width: 120px"
            />
          </div>
        </div>

        <!-- 评分筛选 -->
        <div class="filter-group">
          <label class="filter-label">评分区间</label>
          <div class="rating-filter">
            <el-rate
                v-model="localFilters.minRating"
                :max="5"
                :allow-half="true"
                show-text
                :texts="ratingTexts"
                @change="handleRatingChange"
            />
          </div>
        </div>

        <!-- 创建时间筛选 -->
        <div class="filter-group">
          <label class="filter-label">创建时间</label>
          <div class="date-filter">
            <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="handleDateChange"
                style="width: 100%"
            />
          </div>
        </div>

        <!-- 排序选项 -->
        <div class="filter-group">
          <label class="filter-label">排序方式</label>
          <div class="sort-filter">
            <el-select v-model="localSort.sortBy" placeholder="排序字段" size="small" style="width: 100%">
              <el-option label="创建时间" value="createTime" />
              <el-option label="评分" value="rating" />
              <el-option label="使用量" value="useCount" />
              <el-option label="浏览量" value="viewCount" />
              <el-option label="价格" value="price" />
              <el-option label="更新时间" value="updateTime" />
            </el-select>
            <el-radio-group v-model="localSort.isAsc" size="small" style="margin-top: 8px">
              <el-radio-button :label="false">降序</el-radio-button>
              <el-radio-button :label="true">升序</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="filter-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleApply">应用筛选</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { RefreshLeft } from '@element-plus/icons-vue'

// Props
interface Filters {
  minPrice: string
  maxPrice: string
  minRating: number
  maxRating: number
  startTime?: string
  endTime?: string
}

interface SortOptions {
  sortBy: string
  isAsc: boolean
}

interface Props {
  filters: Filters
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

const props = withDefaults(defineProps<Props>(), {
  filters: () => ({
    minPrice: '',
    maxPrice: '',
    minRating: 0,
    maxRating: 5,
    startTime: '',
    endTime: ''
  }),
  sortBy: 'createTime',
  sortOrder: 'desc'
})

// Emits
const emit = defineEmits<{
  'update:filters': [filters: Filters]
  'update:sortBy': [sortBy: string]
  'update:sortOrder': [sortOrder: 'asc' | 'desc']
  apply: []
  reset: []
}>()

// =========================> 响应式数据 <=========================
/** 本地筛选条件 */
const localFilters = reactive<Filters>({
  minPrice: '',
  maxPrice: '',
  minRating: 0,
  maxRating: 5,
  startTime: '',
  endTime: ''
})

/** 本地排序选项 */
const localSort = reactive<SortOptions>({
  sortBy: props.sortBy,
  isAsc: props.sortOrder === 'asc'
})

/** 日期范围 */
const dateRange = ref<[string, string] | null>(null)

/** 评分文本 */
const ratingTexts = ['不限', '1分以上', '2分以上', '3分以上', '4分以上', '5分']

// =========================> 监听器 <=========================
watch(
    () => props.filters,
    (newFilters) => {
      Object.assign(localFilters, newFilters)
      // 设置日期范围
      if (newFilters.startTime && newFilters.endTime) {
        dateRange.value = [newFilters.startTime, newFilters.endTime]
      } else {
        dateRange.value = null
      }
    },
    { deep: true, immediate: true }
)

watch(
    () => props.sortBy,
    (newSortBy) => {
      localSort.sortBy = newSortBy
    }
)

watch(
    () => props.sortOrder,
    (newSortOrder) => {
      localSort.isAsc = newSortOrder === 'asc'
    }
)

// =========================> 方法定义 <=========================
/**
 * 处理评分变化
 */
const handleRatingChange = (value: number) => {
  localFilters.minRating = value
  localFilters.maxRating = 5
}

/**
 * 处理日期变化
 */
const handleDateChange = (dates: [string, string] | null) => {
  if (dates && dates[0] && dates[1]) {
    localFilters.startTime = dates[0]
    localFilters.endTime = dates[1]
  } else {
    localFilters.startTime = ''
    localFilters.endTime = ''
  }
}

/**
 * 应用筛选
 */
const handleApply = () => {
  // 验证价格区间
  if (localFilters.minPrice && localFilters.maxPrice) {
    const min = parseFloat(localFilters.minPrice)
    const max = parseFloat(localFilters.maxPrice)
    if (min > max) {
      // 如果最小值大于最大值，交换它们
      const temp = localFilters.minPrice
      localFilters.minPrice = localFilters.maxPrice
      localFilters.maxPrice = temp
    }
  }

  // 更新父组件的筛选条件
  emit('update:filters', { ...localFilters })
  emit('update:sortBy', localSort.sortBy)
  emit('update:sortOrder', localSort.isAsc ? 'asc' : 'desc')
  emit('apply')
}

/**
 * 重置筛选条件
 */
const handleReset = () => {
  // 重置本地数据
  Object.assign(localFilters, {
    minPrice: '',
    maxPrice: '',
    minRating: 0,
    maxRating: 5,
    startTime: '',
    endTime: ''
  })

  Object.assign(localSort, {
    sortBy: 'createTime',
    isAsc: false
  })

  dateRange.value = null

  // 通知父组件重置
  emit('reset')
}
</script>

<style scoped>
.filter-panel {
  margin-bottom: 24px;
}

.filter-card {
  border: 1px solid var(--el-border-color-light);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header span {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-filter .el-input-number {
  flex: 1;
}

.separator {
  color: var(--el-text-color-secondary);
  font-weight: 500;
  padding: 0 4px;
}

.rating-filter {
  display: flex;
  align-items: center;
}

.date-filter {
  display: flex;
  align-items: center;
}

.sort-filter {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.filter-actions .el-button {
  min-width: 80px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .price-filter {
    flex-direction: column;
    align-items: stretch;
  }

  .separator {
    text-align: center;
  }

  .filter-actions {
    justify-content: center;
  }
}

/* 深色主题适配 */
.dark .filter-card {
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color);
}

.dark .filter-actions {
  border-color: var(--el-border-color);
}
</style>