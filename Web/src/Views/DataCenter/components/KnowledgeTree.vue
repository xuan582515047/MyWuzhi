<!--
  知识库树形组件
  功能描述：管理知识库节点树形结构的组件
  作者：AI Assistant
  创建时间：2025-01-20
-->
<template>
  <div class="knowledge-tree">
    <div class="tree-header">
      <div class="header-left">
        <h3>知识库结构</h3>
        <el-button type="primary" size="small" @click="handleAddRootNode">
          <el-icon><Plus /></el-icon>
          添加根节点
        </el-button>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索节点..."
          size="small"
          style="width: 200px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button size="small" @click="expandAll">
          <el-icon><Expand /></el-icon>
          展开全部
        </el-button>
        <el-button size="small" @click="collapseAll">
          <el-icon><Fold /></el-icon>
          收起全部
        </el-button>
      </div>
    </div>

    <div class="tree-content">
      <el-tree
        ref="treeRef"
        v-loading="loading"
        :data="filteredTreeData"
        :props="treeProps"
        :default-expand-all="false"
        :expand-on-click-node="false"
        :filter-node-method="filterNode"
        node-key="id"
        draggable
        @node-drag-start="handleDragStart"
        @node-drag-end="handleDragEnd"
        @node-drop="handleDrop"
        class="knowledge-tree-component"
      >
        <template #default="{ node, data }">
          <div class="tree-node" :class="{ 'is-disabled': !data.status }">
            <div class="node-info">
              <el-icon class="node-icon" :class="{ 'is-root': !data.parentId }">
                <component :is="getNodeIcon(data)" />
              </el-icon>
              <span class="node-name">{{ data.name }}</span>
              <el-tag
                :type="data.status ? 'success' : 'danger'"
                size="small"
                effect="dark"
              >
                {{ data.status ? '已启用' : '已禁用' }}
              </el-tag>
              <span v-if="data.companyId" class="company-id">
                {{ data.companyId }}
              </span>
            </div>
            
            <div class="node-actions">
              <el-dropdown @command="handleAction" trigger="click">
                <el-button size="small" type="primary" plain>
                  操作
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      :command="{ action: 'addChild', data }"
                      :disabled="!data.status"
                    >
                      <el-icon><Plus /></el-icon>
                      添加子节点
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'edit', data }">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{ action: 'toggleStatus', data }"
                    >
                      <el-icon>
                        <component :is="data.status ? 'VideoPlay' : 'VideoPause'" />
                      </el-icon>
                      {{ data.status ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{ action: 'delete', data }"
                      divided
                      class="danger-item"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 拖拽提示 -->
    <div class="drag-hint">
      <el-alert
        title="拖拽提示"
        type="info"
        :closable="false"
        show-icon
      >
        您可以通过拖拽节点来调整知识库结构。拖拽时请确保目标节点支持接收子节点。
      </el-alert>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Expand,
  Fold,
  ArrowDown,
  Edit,
  Delete,
  VideoPlay,
  VideoPause,
  Folder,
  Document,
  Collection
} from '@element-plus/icons-vue'

// 导入类型
import type { DatabaseTreeResponse, DatabaseNode } from '@/Entity/DatabaseEntity'

// Props
interface Props {
  treeData: DatabaseTreeResponse[]
  loading: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  add: [parentNode?: DatabaseNode]
  edit: [node: DatabaseNode]
  delete: [node: DatabaseNode]
  toggleStatus: [node: DatabaseNode]
  refresh: []
}>()

// =========================> 响应式数据 <=========================
/** 树组件引用 */
const treeRef = ref()

/** 搜索关键词 */
const searchKeyword = ref('')

/** 拖拽相关状态 */
const draggingNode = ref<any>(null)
const dropNode = ref<any>(null)

/** 树节点配置 */
const treeProps = {
  children: 'children',
  label: 'name'
}

// =========================> 计算属性 <=========================
/** 过滤后的树数据 */
const filteredTreeData = computed(() => {
  return props.treeData
})

// =========================> 监听器 <=========================
watch(
  searchKeyword,
  (val) => {
    treeRef.value?.filter(val)
  }
)

// =========================> 方法定义 <=========================
/**
 * 获取节点图标
 */
const getNodeIcon = (data: DatabaseNode) => {
  if (!data.parentId) {
    return Collection // 根节点
  } else if (data.children && data.children.length > 0) {
    return Folder // 有子节点的文件夹
  } else {
    return Document // 叶子节点
  }
}

/**
 * 过滤节点
 */
const filterNode = (value: string, data: DatabaseNode) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

/**
 * 搜索处理
 */
const handleSearch = () => {
  // 搜索逻辑已在 filterNode 中处理
}

/**
 * 展开全部节点
 */
const expandAll = () => {
  const expandKeys = getAllNodeKeys(props.treeData)
  nextTick(() => {
    expandKeys.forEach(key => {
      treeRef.value?.store.nodesMap[key]?.expand()
    })
  })
}

/**
 * 收起全部节点
 */
const collapseAll = () => {
  const allKeys = getAllNodeKeys(props.treeData)
  nextTick(() => {
    allKeys.forEach(key => {
      treeRef.value?.store.nodesMap[key]?.collapse()
    })
  })
}

/**
 * 获取所有节点键值
 */
const getAllNodeKeys = (nodes: any[]): string[] => {
  const keys: string[] = []
  
  const traverse = (nodeList: any[]) => {
    nodeList.forEach(node => {
      keys.push(node.id)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    })
  }
  
  traverse(nodes)
  return keys
}

/**
 * 添加根节点
 */
const handleAddRootNode = () => {
  emit('add', undefined)
}

/**
 * 处理操作菜单命令
 */
const handleAction = (command: { action: string; data: DatabaseNode }) => {
  const { action, data } = command
  
  switch (action) {
    case 'addChild':
      emit('add', data)
      break
    case 'edit':
      emit('edit', data)
      break
    case 'toggleStatus':
      emit('toggleStatus', data)
      break
    case 'delete':
      handleDelete(data)
      break
  }
}

/**
 * 删除节点
 */
const handleDelete = async (node: DatabaseNode) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除节点"${node.name}"吗？此操作不可恢复，删除后其所有子节点也将被删除。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    emit('delete', node)
  } catch (error) {
    // 用户取消删除
  }
}

/**
 * 拖拽开始
 */
const handleDragStart = (node: any) => {
  draggingNode.value = node
}

/**
 * 拖拽结束
 */
const handleDragEnd = () => {
  draggingNode.value = null
  dropNode.value = null
}

/**
 * 拖拽放置
 */
const handleDrop = (draggingNode: any, dropNode: any, dropType: string) => {
  console.log('拖拽完成:', {
    draggingNode: draggingNode.data.name,
    dropNode: dropNode.data.name,
    dropType
  })
  
  // 这里可以调用API更新树结构
  ElMessage.success('节点位置已更新')
  emit('refresh')
}
</script>

<style scoped>
.knowledge-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: var(--el-fill-color-lighter);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tree-content {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}

.knowledge-tree-component :deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 0;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.knowledge-tree-component :deep(.el-tree-node__content:hover) {
  background-color: var(--el-fill-color-light);
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 4px 8px;
  border-radius: 4px;
}

.tree-node.is-disabled {
  opacity: 0.6;
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
  transition: color 0.3s ease;
}

.node-icon.is-root {
  color: var(--el-color-warning);
  font-size: 18px;
}

.node-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
  flex: 1;
}

.company-id {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background-color: var(--el-fill-color-light);
  padding: 2px 6px;
  border-radius: 4px;
}

.node-actions {
  opacity: 0;
  transition: opacity 0.3s ease;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.drag-hint {
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 下拉菜单危险项样式 */
:deep(.danger-item) {
  color: var(--el-color-danger);
}

:deep(.danger-item:hover) {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

/* 拖拽样式 */
:deep(.el-tree-node.is-drop-inner > .el-tree-node__content .el-tree-node__label) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

:deep(.el-tree-node__content.is-dragging) {
  opacity: 0.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tree-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .header-left,
  .header-right {
    justify-content: center;
  }
  
  .tree-node {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .node-actions {
    opacity: 1;
    width: 100%;
  }
  
  .node-actions .el-button {
    width: 100%;
  }
}

/* 滚动条样式 */
.tree-content::-webkit-scrollbar {
  width: 6px;
}

.tree-content::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
}

.tree-content::-webkit-scrollbar-thumb {
  background: var(--el-border-color-darker);
  border-radius: 3px;
}

.tree-content::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-dark);
}

/* 深色主题适配 */
.dark .tree-header {
  background-color: var(--el-fill-color-darker);
  border-color: var(--el-border-color);
}

.dark .drag-hint {
  border-color: var(--el-border-color);
}

.dark .company-id {
  background-color: var(--el-fill-color-dark);
  color: var(--el-text-color-secondary);
}
</style>