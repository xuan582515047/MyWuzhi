<!--
  工具使用记录组件
  功能描述：管理工具使用记录的组件，包括工具树和使用记录列表
  作者：AI Assistant
  创建时间：2025-01-20
-->
<template>
  <div class="tool-usage-record">
    <!-- 工具树 -->
    <div class="tree-section">
      <div class="section-header">
        <h3>工具节点</h3>
        <el-button type="primary" size="small" @click="handleAddNode">
          <el-icon><Plus /></el-icon>
          添加节点
        </el-button>
      </div>
      
      <div class="tree-container">
        <el-tree
          ref="treeRef"
          :data="toolTree"
          :props="treeProps"
          :default-expand-all="true"
          :expand-on-click-node="false"
          node-key="id"
          class="tool-tree"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-info">
                <el-icon class="node-icon">
                  <component :is="getNodeIcon(data)" />
                </el-icon>
                <span class="node-name">{{ data.name }}</span>
                <el-tag
                  :type="data.status ? 'success' : 'danger'"
                  size="small"
                >
                  {{ data.status ? '已启用' : '已禁用' }}
                </el-tag>
              </div>
              
              <div class="node-actions">
                <el-button
                  size="small"
                  type="primary"
                  @click.stop="handleToggleStatus(data)"
                >
                  {{ data.status ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" @click.stop="handleEditNode(data)">
                  编辑
                </el-button>
                <el-button size="small" @click.stop="handleAddChildNode(data)">
                  添加子节点
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  @click.stop="handleDeleteNode(data)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 使用记录列表 -->
    <div class="records-section">
      <div class="section-header">
        <h3>使用记录</h3>
        <div class="header-actions">
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
          />
          <el-button type="primary" size="small" @click="handleAddRecord">
            <el-icon><Plus /></el-icon>
            添加记录
          </el-button>
        </div>
      </div>

      <div class="records-table">
        <el-table
          v-loading="loading"
          :data="usageRecords"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="toolId" label="工具ID" width="150" />
          <el-table-column prop="companyId" label="公司ID" width="150" />
          <el-table-column prop="detail" label="使用详情" show-overflow-tooltip />
          <el-table-column prop="useTime" label="使用时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.useTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="120" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleEditRecord(row)">
                编辑
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDeleteRecord(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Folder,
  Document,
  Tools
} from '@element-plus/icons-vue'

// 导入类型
import type { 
  ToolUseNode, 
  ToolUseRecord,
  AddToolUseRecordRequest,
  EditToolUseRecordRequest
} from '@/Entity/ToolEntity'

// Props
interface Props {
  toolTree: ToolUseNode[]
  usageRecords: ToolUseRecord[]
  loading: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  addNode: [parentNode?: ToolUseNode]
  editNode: [node: ToolUseNode]
  deleteNode: [nodeId: string]
  toggleStatus: [node: ToolUseNode]
  addRecord: [record: AddToolUseRecordRequest]
  editRecord: [record: EditToolUseRecordRequest]
  deleteRecord: [recordId: string]
  refresh: []
}>()

// =========================> 响应式数据 <=========================
/** 树组件引用 */
const treeRef = ref()

/** 分页数据 */
const currentPage = ref(1)
const pageSize = ref(20)
const total = computed(() => props.usageRecords.length)

/** 日期范围 */
const dateRange = ref<[string, string] | null>(null)

/** 树节点配置 */
const treeProps = {
  children: 'children',
  label: 'name'
}

// =========================> 方法定义 <=========================
/**
 * 获取节点图标
 */
const getNodeIcon = (data: ToolUseNode) => {
  if (data.children && data.children.length > 0) {
    return Folder
  } else if (data.url) {
    return Tools
  } else {
    return Document
  }
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime: string): string => {
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return dateTime
  }
}

/**
 * 添加节点
 */
const handleAddNode = () => {
  emit('addNode')
}

/**
 * 添加子节点
 */
const handleAddChildNode = (parentNode: ToolUseNode) => {
  emit('addNode', parentNode)
}

/**
 * 编辑节点
 */
const handleEditNode = (node: ToolUseNode) => {
  emit('editNode', node)
}

/**
 * 删除节点
 */
const handleDeleteNode = async (node: ToolUseNode) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除节点"${node.name}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    emit('deleteNode', node.id)
  } catch (error) {
    // 用户取消删除
  }
}

/**
 * 切换节点状态
 */
const handleToggleStatus = (node: ToolUseNode) => {
  emit('toggleStatus', node)
}

/**
 * 日期变化处理
 */
const handleDateChange = (dates: [string, string] | null) => {
  // 这里可以根据日期范围筛选记录
  console.log('日期范围:', dates)
  emit('refresh')
}

/**
 * 添加使用记录
 */
const handleAddRecord = () => {
  const record: AddToolUseRecordRequest = {
    toolId: '',
    companyId: '',
    detail: ''
  }
  emit('addRecord', record)
}

/**
 * 编辑使用记录
 */
const handleEditRecord = (record: ToolUseRecord) => {
  const editRecord: EditToolUseRecordRequest = {
    toolId: record.toolId,
    companyId: record.companyId,
    detail: record.detail
  }
  emit('editRecord', editRecord)
}

/**
 * 删除使用记录
 */
const handleDeleteRecord = async (record: ToolUseRecord) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条使用记录吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    emit('deleteRecord', record.id)
  } catch (error) {
    // 用户取消删除
  }
}

/**
 * 分页大小变化
 */
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
}

/**
 * 当前页变化
 */
const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
}
</script>

<style scoped>
.tool-usage-record {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 24px;
  height: 100%;
}

.tree-section,
.records-section {
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: var(--el-fill-color-lighter);
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.tree-container {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}

.tool-tree :deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 0;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.node-icon {
  color: var(--el-color-primary);
  font-size: 16px;
}

.node-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.node-actions {
  display: flex;
  gap: 4px;
}

.node-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
}

.records-table {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.records-table :deep(.el-table) {
  flex: 1;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .tool-usage-record {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .tree-node {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .node-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
}

/* 滚动条样式 */
.tree-container::-webkit-scrollbar {
  width: 6px;
}

.tree-container::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
}

.tree-container::-webkit-scrollbar-thumb {
  background: var(--el-border-color-darker);
  border-radius: 3px;
}

.tree-container::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-dark);
}

/* 深色主题适配 */
.dark .tree-section,
.dark .records-section {
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color);
}

.dark .section-header {
  background-color: var(--el-fill-color-darker);
  border-color: var(--el-border-color);
}

.dark .pagination {
  border-color: var(--el-border-color);
}
</style>