<!--
  工具市场页面
  功能描述：提供工具展示、搜索、筛选、购买等功能的市场界面
-->
<template>
  <Sidebar :show-exit-btn="true">
    <template #left-side>
      <!-- 侧边栏内容 -->
      <div class="market-sidebar-content">
        <!-- 搜索栏 -->
        <div class="search-section">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索工具名称、描述或关键词..."
              size="default"
              class="search-input"
              @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch" size="small">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button @click="showAdvancedFilter = !showAdvancedFilter" size="small">
            <el-icon><Filter /></el-icon>
            {{ showAdvancedFilter ? '隐藏筛选' : '高级筛选' }}
          </el-button>
          <el-button type="primary" @click="showAddToolDialog" size="small">
            <el-icon><Plus /></el-icon>
            发布工具
          </el-button>
        </div>

        <!-- 侧边筛选 -->
        <ToolMarketSidebar
            v-model:tool-type="selectedToolType"
            v-model:tags="selectedTags"
            v-model:price-range="priceRange"
            v-model:sort-by="sortBy"
            v-model:sort-order="sortOrder"
            :tool-types="toolTypes"
            :popular-tags="tagsList"
            :stats="stats"
            @filter-change="handleFilterChange"
        />
      </div>
    </template>

    <template #content>
      <!-- 主内容区域 -->
      <div class="market-main-content">
        <!-- 高级筛选面板 -->
        <ToolFilterPanel
            v-show="showAdvancedFilter"
            v-model:filters="advancedFilters"
            v-model:sort-by="sortBy"
            v-model:sort-order="sortOrder"
            @apply="handleAdvancedFilter"
            @reset="resetFilters"
        />

        <!-- 工具统计信息 -->
        <div class="stats-bar">
          <div class="stats-info">
            <span>共找到 {{ total }} 个工具</span>
            <el-tag v-if="searchKeyword" type="info" closable @close="clearSearch">
              搜索: {{ searchKeyword }}
            </el-tag>
            <el-tag v-if="selectedToolType" type="primary" closable @close="clearToolType">
              类型: {{ getToolTypeName(selectedToolType) }}
            </el-tag>
            <el-tag
                v-for="tagId in selectedTags"
                :key="tagId"
                type="success"
                closable
                @close="removeTag(tagId)"
            >
              {{ getTagName(tagId) }}
            </el-tag>
          </div>

          <!-- 视图切换 -->
          <div class="view-controls">
            <el-radio-group v-model="viewMode" @change="handleViewModeChange">
              <el-radio-button label="grid">
                <el-icon><Grid /></el-icon>
              </el-radio-button>
              <el-radio-button label="list">
                <el-icon><List /></el-icon>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 工具列表 -->
        <div v-loading="loading" class="tools-container" :class="viewMode">
          <template v-if="toolList && toolList.length > 0">
            <!-- 网格视图 -->
            <div v-if="viewMode === 'grid'" class="tools-grid">
              <ToolCard
                  v-for="tool in paginatedToolList"
                  :key="tool.id"
                  :tool="tool"
                  :tags-list="tagsList"
                  :tool-types="toolTypes"
                  @view-detail="showToolDetail"
                  @purchase="showPurchaseDialog"
                  @share="shareTool"
                  @use="useTool"
              />
            </div>

            <!-- 列表视图 -->
            <div v-else class="tools-list">
              <ToolCard
                  v-for="tool in paginatedToolList"
                  :key="tool.id"
                  :tool="tool"
                  :list-view="true"
                  :tags-list="tagsList"
                  :tool-types="toolTypes"
                  @view-detail="showToolDetail"
                  @purchase="showPurchaseDialog"
                  @share="shareTool"
                  @use="useTool"
              />
            </div>
          </template>

          <!-- 空状态 -->
          <el-empty
              v-else
              description="暂无工具"
              :image-size="200"
          >
            <el-button type="primary" @click="showAddToolDialog">
              发布第一个工具
            </el-button>
          </el-empty>
        </div>

        <!-- 分页 -->
        <div v-if="toolList && toolList.length > 0" class="pagination-container">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[12, 24, 36, 48]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </template>
  </Sidebar>

  <!-- 工具详情弹窗 -->
  <ToolDetailDialog
      v-model:visible="toolDetailVisible"
      :tool="selectedTool"
      :tags-list="tagsList"
      :tool-types="toolTypes"
      @purchase="showPurchaseDialog"
      @share="handleShareTool"
      @use="useTool"
  />

  <!-- 工具购买弹窗 -->
  <ToolPurchaseDialog
      v-model:visible="purchaseVisible"
      :tool="selectedTool"
      :tags-list="tagsList"
      @purchase-success="handlePurchaseSuccess"
  />

  <!-- 添加/编辑工具弹窗 -->
  <el-dialog
      v-model="addToolVisible"
      :title="editingTool ? '编辑工具' : '发布新工具'"
      width="800px"
      destroy-on-close
  >
    <ToolEditForm
        :tool="editingTool"
        :tool-types="toolTypes"
        :tag-list="tagsList"
        @submit="handleToolSubmit"
        @cancel="addToolVisible = false"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Filter,
  Plus,
  Grid,
  List
} from '@element-plus/icons-vue'
import Sidebar from '@/Views/PublicComponents/Sidebar.vue'  // 导入Sidebar组件
import { ToolApi } from '@/Api/ToolApi'

// 导入类型
import type {
  ToolInfo,
  ToolQueryRequest,
  TagInfo,
  ConstantItem,
  AddToolRequest,
  EditToolRequest,
  UseToolRequest
} from '@/Entity/ToolEntity'
import ToolMarketSidebar from "@/Views/ToolCenter/components/ToolMarketSidebar.vue";
import ToolFilterPanel from "@/Views/ToolCenter/components/ToolFilterPanel.vue";
import ToolCard from "@/Views/ToolCenter/components/ToolCard.vue";
import ToolDetailDialog from "@/Views/ToolCenter/components/ToolDetailDialog.vue";
import ToolPurchaseDialog from "@/Views/ToolCenter/components/ToolPurchaseDialog.vue";
import ToolEditForm from "@/Views/ToolCenter/components/ToolEditForm.vue";

// =========================> 响应式数据 <=========================
/** 搜索关键词 */
const searchKeyword = ref('')

/** 工具列表 */
const toolList = ref<ToolInfo[]>([])

/** 分页数据 */
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

/** 加载状态 */
const loading = ref(false)

/** 视图模式 */
const viewMode = ref<'grid' | 'list'>('grid')

/** 高级筛选显示状态 */
const showAdvancedFilter = ref(false)

/** 筛选条件 */
const selectedToolType = ref('')
const selectedTags = ref<string[]>([])
const priceRange = ref<[number, number]>([0, 9999])
const sortBy = ref('createTime')
const sortOrder = ref<'asc' | 'desc'>('desc')

/** 高级筛选条件 */
const advancedFilters = ref({
  minPrice: '',
  maxPrice: '',
  minRating: 0,
  maxRating: 5,
  startTime: '',
  endTime: ''
})

/** 工具类型列表 */
const toolTypes = ref<ConstantItem[]>([])

/** 标签列表 */
const tagsList = ref<TagInfo[]>([])

/** 统计信息 */
const stats = ref({
  totalTools: 0,
  freeTools: 0,
  paidTools: 0
})

/** 弹窗状态 */
const toolDetailVisible = ref(false)
const purchaseVisible = ref(false)
const addToolVisible = ref(false)

/** 选中的工具 */
const selectedTool = ref<ToolInfo | undefined>()

/** 编辑中的工具 */
const editingTool = ref<ToolInfo | undefined>()

// =========================> 计算属性 <=========================
/**
 * 获取标签名称
 */
const getTagName = (tagId: string): string => {
  const tag = tagsList.value.find(t => t.id === tagId)
  return tag ? tag.name : tagId
}

/**
 * 获取工具类型名称
 */
const getToolTypeName = (typeValue: string): string => {
  const type = toolTypes.value.find(t => t.value === typeValue)
  return type ? type.name : typeValue
}

/**
 * 工具列表（直接使用后端返回的数据）
 */
const paginatedToolList = computed(() => {
  if (!toolList.value || !Array.isArray(toolList.value)) {
    return []
  }
  
  // 后端已经分页，直接返回完整列表
  return toolList.value
})

// =========================> 方法定义 <=========================
/**
 * 加载工具列表
 */
const loadToolList = async () => {
  loading.value = true
  try {
    const request: ToolQueryRequest = {
      query: searchKeyword.value,
      type: selectedToolType.value,
      tagIds: selectedTags.value.length > 0 ? selectedTags.value : undefined,  // 使用字符串数组
      sortBy: sortBy.value,
      isAsc: sortOrder.value === 'asc',
      page: currentPage.value,
      pageSize: pageSize.value
    }

    // 价格范围
    if (priceRange.value[0] > 0 || priceRange.value[1] < 9999) {
      request.minPrice = priceRange.value[0].toString()
      request.maxPrice = priceRange.value[1].toString()
    }

    // 高级筛选条件
    if (advancedFilters.value.minPrice) {
      request.minPrice = advancedFilters.value.minPrice
    }
    if (advancedFilters.value.maxPrice) {
      request.maxPrice = advancedFilters.value.maxPrice
    }

    const response = await ToolApi.getToolList(request)
    console.log('API Response:', response) // 调试用
    
    // 接口规定格式：{data: [...], total: number}
    toolList.value = response.data
    total.value = response.total
    
    console.log('toolList赋值后:', toolList.value, 'total:', total.value) // 调试用
    
    // 调试价格数据
    if (toolList.value && toolList.value.length > 0) {
      console.log('第一个工具的价格:', toolList.value[0]!.price, '类型:', typeof toolList.value[0]!.price)
    }

    // 更新统计信息
    updateStats()
  } catch (error) {
    console.error('加载工具列表失败:', error)
    ElMessage.error('加载工具列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 更新统计信息
 */
const updateStats = () => {
  if (!toolList.value || !Array.isArray(toolList.value)) {
    stats.value = {
      totalTools: 0,
      freeTools: 0,
      paidTools: 0
    }
    return
  }
  
  const totalTools = toolList.value.length
  const freeTools = toolList.value.filter(tool => tool.price === '0').length
  const paidTools = totalTools - freeTools

  stats.value = {
    totalTools,
    freeTools,
    paidTools
  }
}

/**
 * 加载工具类型列表
 */
const loadToolTypes = async () => {
  try {
    const types = await ToolApi.getToolTypeList()
    toolTypes.value = types
  } catch (error) {
    console.error('加载工具类型失败:', error)
  }
}

/**
 * 加载标签列表
 */
const loadTags = async () => {
  try {
    const tags = await ToolApi.getTagList()
    tagsList.value = tags
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

/**
 * 搜索处理
 */
const handleSearch = () => {
  currentPage.value = 1
  loadToolList()
}

/**
 * 筛选变化处理
 */
const handleFilterChange = () => {
  currentPage.value = 1
  loadToolList()
}

/**
 * 高级筛选应用
 */
const handleAdvancedFilter = () => {
  currentPage.value = 1
  loadToolList()
  showAdvancedFilter.value = false
}

/**
 * 重置筛选条件
 */
const resetFilters = () => {
  searchKeyword.value = ''
  selectedToolType.value = ''
  selectedTags.value = []
  // 动态计算价格范围
  const prices = toolList.value.map(tool => parseFloat(String(tool.price)) || 0).filter(price => price > 0)
  const maxPrice = prices.length > 0 ? Math.max(...prices) : 9999
  priceRange.value = [0, maxPrice]
  sortBy.value = 'createTime'
  sortOrder.value = 'desc'
  Object.assign(advancedFilters, {
    minPrice: '',
    maxPrice: '',
    minRating: 0,
    maxRating: 5,
    startTime: '',
    endTime: ''
  })
  currentPage.value = 1
  loadToolList()
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  handleSearch()
}

/**
 * 清除工具类型筛选
 */
const clearToolType = () => {
  selectedToolType.value = ''
  handleFilterChange()
}

/**
 * 移除标签
 */
const removeTag = (tagId: string) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
    handleFilterChange()
  }
}

/**
 * 视图模式变化处理
 */
const handleViewModeChange = () => {
  // 视图模式变化的处理逻辑
}

/**
 * 分页大小变化处理
 */
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadToolList()
}

/**
 * 当前页变化处理
 */
const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  loadToolList()
}

/**
 * 显示工具详情
 */
const showToolDetail = (tool: ToolInfo) => {
  selectedTool.value = tool
  toolDetailVisible.value = true
}

/**
 * 显示购买对话框
 */
const showPurchaseDialog = (tool: ToolInfo) => {
  selectedTool.value = tool
  purchaseVisible.value = true
}

/**
 * 使用工具
 */
const useTool = async (tool: ToolInfo) => {
  try {
    const request: UseToolRequest = {
      toolId: tool.id,
      companyId: undefined  // 不绑定公司，传 undefined
    }
    await ToolApi.useTool(request)
    // 增加使用次数
    tool.useCount++
    ElMessage.success(`工具"${tool.name}"使用成功！`)
  } catch (error) {
    console.error('使用工具失败:', error)
    ElMessage.error('使用工具失败，请重试')
  }
}

/**
 * 分享工具
 */
const shareTool = (tool: ToolInfo) => {
  handleShareTool(tool)
}

/**
 * 显示添加工具对话框
 */
const showAddToolDialog = () => {
  editingTool.value = undefined
  addToolVisible.value = true
}

/**
 * 分享工具
 */
const handleShareTool = (tool: ToolInfo) => {
  const shareUrl = `${window.location.origin}/tool/${tool.id}`
  
  // 创建分享选项
  const shareOptions = [
    {
      label: '复制链接',
      icon: 'Link',
      action: () => {
        navigator.clipboard.writeText(shareUrl).then(() => {
          ElMessage.success('工具链接已复制到剪贴板')
        }).catch(() => {
          ElMessage.error('复制链接失败')
        })
      }
    },
    {
      label: '分享到微信',
      icon: 'ChatDotRound',
      action: () => {
        ElMessage.info('请使用微信扫描二维码分享')
        // 这里可以生成二维码
      }
    },
    {
      label: '分享到微博',
      icon: 'Notification',
      action: () => {
        const weiboUrl = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(shareUrl)}&title=${encodeURIComponent(tool.name)}`
        window.open(weiboUrl, '_blank')
      }
    }
  ]
  
  // 显示分享菜单
  ElMessageBox.alert(
    `<div style="display: flex; flex-direction: column; gap: 10px;">
      ${shareOptions.map(option => 
        `<button class="el-button el-button--primary el-button--small" style="width: 100%; justify-content: flex-start;" onclick="this.closest('.el-message-box').__shareAction='${option.label}'">
          <i class="el-icon-${option.icon.toLowerCase()}"></i>
          ${option.label}
        </button>`
      ).join('')}
    </div>`,
    '分享工具',
    {
      dangerouslyUseHTMLString: true,
      showConfirmButton: false,
      callback: (action: string, instance: any) => {
        const selectedAction = instance?.props?.message?.__shareAction
        if (selectedAction) {
          const option = shareOptions.find(opt => opt.label === selectedAction)
          option?.action()
        }
      }
    }
  ).catch(() => {})
}

/**
 * 工具提交处理
 */
const handleToolSubmit = async (formData: any) => {
  try {
    if (editingTool.value) {
      // 编辑工具
      const request: EditToolRequest = {
        id: editingTool.value.id,
        ...formData
      }
      await ToolApi.editTool(request)
      ElMessage.success('工具更新成功')
    } else {
      // 添加工具
      const request: AddToolRequest = {
        name: formData.name,
        description: formData.description,
        price: formData.price,
        url: formData.url,
        version: formData.version || '1.0.0',
        type: formData.type,
        tagIds: formData.tagIds || [],
        keywords: formData.keywords || []
      }
      await ToolApi.addTool(request)
      ElMessage.success('工具创建成功')
    }

    addToolVisible.value = false
    loadToolList()
  } catch (error) {
    console.error('保存工具失败:', error)
    ElMessage.error('保存工具失败')
  }
}

/**
 * 购买成功处理
 */
const handlePurchaseSuccess = () => {
  ElMessage.success('购买成功')
  purchaseVisible.value = false
  loadToolList()
}

// =========================> 生命周期钩子 <=========================
onMounted(async () => {
  await Promise.all([
    loadToolList(),
    loadToolTypes(),
    loadTags()
  ])
})
</script>

<style scoped>
.market-sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
  overflow-y: auto;
  padding: 0 10px;
}

.search-section {
  width: 100%;
}

.search-input {
  width: 100%;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 120px;
}

.market-main-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow: hidden;
  background-color: var(--el-bg-color);
}

.stats-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tools-container {
  flex: 1;
  overflow-y: auto;
  min-height: 0; /* 防止flex容器溢出 */
}

.tools-container.grid .tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.tools-container.list .tools-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 侧边栏滚动条样式 */
.market-sidebar-content::-webkit-scrollbar {
  width: 4px;
}

.market-sidebar-content::-webkit-scrollbar-track {
  background: transparent;
}

.market-sidebar-content::-webkit-scrollbar-thumb {
  background: var(--border-primary);
  border-radius: 2px;
}

.market-sidebar-content::-webkit-scrollbar-thumb:hover {
  background: var(--border-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-buttons .el-button {
    min-width: 100%;
  }

  .market-main-content {
    padding: 16px;
  }

  .stats-bar {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}

/* 深色主题适配 */
.dark .market-main-content {
  background-color: var(--el-bg-color);
}

.dark .stats-bar {
  background-color: var(--el-fill-color-darker);
}
</style>