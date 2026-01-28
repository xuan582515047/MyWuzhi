<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElTree, ElMessage, ElMessageBox, ElIcon } from 'element-plus'
import { Refresh, Folder, Document, MoreFilled, Edit, Switch, Delete } from '@element-plus/icons-vue'
import type { DatabaseTreeResponse, DatabaseNode } from '@/Entity/DatabaseEntity.ts'
import { DatabaseApi } from '@/Api/DatabaseApi.ts'

interface TreeNode extends DatabaseNode {
  label: string
  parentDisabled: boolean // 父节点是否被禁用，用于样式控制
  enable: boolean // 是否启用，用于 TreeLayout 组件
}

// =========================> Props <=========================
interface Props {
  companyId?: string
  onNodeClick?: (node: DatabaseNode) => void
  onAddNode?: (parentId: string, parentName: string) => void
  onLoaded?: (data: Array<any>) => void
}

const props = withDefaults(defineProps<Props>(), {
  companyId: undefined
})

// =========================> 响应式数据 <=========================
/** 树形数据 */
const treeData = ref<TreeNode[]>([])
/** 当前选中的节点 */
const currentNode = ref<DatabaseNode | null>(null)
/** 树组件引用 */
const treeRef = ref<InstanceType<typeof ElTree>>()
/** 加载状态 */
const loading = ref(false)
/** 默认展开的节点 */
const defaultExpandedKeys = ref<string[]>([])

// =========================> 方法定义 <=========================
/**
 * 加载数据库树数据
 */
const loadTreeData = async () => {
  loading.value = true
  try {
    const data = await DatabaseApi.getCompanyDatabaseTree(props.companyId!)
    if (data) {
      treeData.value = data.map(item => transformToTreeNode(item))
      defaultExpandedKeys.value = data.map(item => item.id)
      // 通知父组件树数据已加载
      props.onLoaded?.(treeData.value)
    } else {
      treeData.value = []
      defaultExpandedKeys.value = []
      props.onLoaded?.([])
    }
  } finally {
    loading.value = false
  }
}

/**
 * 递归转换树节点格式
 */
const transformToTreeNode = (item: DatabaseTreeResponse, parentDisabled: boolean = false): TreeNode => {
  return {
    id: item.id,
    name: item.name,
    status: item.status,
    type: item.type, // 保留 type 字段，用于判断节点类型
    label: item.name,
    parentDisabled, // 保存父节点禁用状态
    enable: item.status, // 用于 TreeLayout 组件的启用状态
    children: item.children && item.children.length > 0
      ? item.children.map(child => transformDatabaseNodeToTreeNode(child, parentDisabled || !item.status))
      : undefined
  }
}

/**
 * 将 DatabaseNode 转换为 TreeNode
 */
const transformDatabaseNodeToTreeNode = (node: DatabaseNode, parentDisabled: boolean = false): TreeNode => {
  return {
    id: node.id,
    name: node.name,
    status: node.status,
    type: node.type, // 保留 type 字段
    label: node.name,
    parentDisabled, // 保存父节点禁用状态
    enable: node.status, // 用于 TreeLayout 组件的启用状态
    children: node.children && node.children.length > 0
      ? node.children.map(child => transformDatabaseNodeToTreeNode(child, parentDisabled || !node.status))
      : undefined
  }
}

/**
 * 节点点击事件
 */
const handleNodeClick = (data: DatabaseNode) => {
  currentNode.value = data
  props.onNodeClick?.(data)
}

/**
 * 添加子节点
 */
const addChildNode = (node: TreeNode) => {
  props.onAddNode?.(node.id, node.label)
}

/**
 * 重命名节点
 */
const renameNode = (node: TreeNode) => {
  ElMessageBox.prompt('请输入新名称', '重命名', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '名称不能为空'
  }).then(async ({ value }) => {
    // 使用 editDatabaseNode 编辑节点名称
    await DatabaseApi.editDatabaseNode({ id: node.id, name: value })
    ElMessage.success('重命名成功')
    await loadTreeData()
  }).catch(() => {})
}

/**
 * 删除节点
 */
const deleteNode = (node: TreeNode) => {
  ElMessageBox.confirm('确定要删除该节点吗？删除后不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 使用 deleteDatabaseNode 删除节点
    await DatabaseApi.deleteDatabaseNode(node.id)
    ElMessage.success('删除成功')
    await loadTreeData()
  }).catch(() => {})
}

/**
 * 切换节点状态
 */
const toggleNodeStatus = async (node: TreeNode) => {
  try {
    await DatabaseApi.updateDatabaseNodeStatus([{ id: node.id, status: !node.status }])
    node.status = !node.status
    // 递归更新所有子节点的 parentDisabled
    updateChildrenParentDisabled(node)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

/**
 * 递归更新子节点的 parentDisabled 状态
 */
const updateChildrenParentDisabled = (node: any, parentDisabled?: boolean) => {
  const newParentDisabled = parentDisabled !== undefined ? parentDisabled : !node.status

  if (node.children && node.children.length > 0) {
    node.children.forEach((child: any) => {
      child.parentDisabled = newParentDisabled
      updateChildrenParentDisabled(child, newParentDisabled)
    })
  }
}

/**
 * 刷新树
 */
const refreshTree = () => {
  loadTreeData()
}

// =========================> 监听器 <=========================
watch(() => props.companyId, () => {
  if (props.companyId) {
    loadTreeData()
  }
}, { immediate: true })

// =========================> 暴露方法 <=========================
defineExpose({
  refreshTree,
  getCurrentNode: () => currentNode.value
})
</script>

<template>
  <div class="database-tree-container">
    <div class="tree-header">
      <h3>数据库结构</h3>
      <el-button
        size="small"
        type="primary"
        :loading="loading"
        @click="refreshTree"
      >
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    
    <el-tree
      ref="treeRef"
      v-loading="loading"
      :data="treeData"
      :props="{ label: 'label', children: 'children' }"
      :default-expanded-keys="defaultExpandedKeys"
      node-key="id"
      highlight-current
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="tree-node-content" :class="{ 'parent-disabled': data.parentDisabled }">
          <div class="node-info">
            <el-icon :class="['node-icon', { 'node-icon-disabled': !data.status }]">
              <Folder v-if="data.type === 'middle'" />
              <Document v-else />
            </el-icon>
            <span :class="['node-label', { 'node-label-disabled': !data.status }]">
              {{ node.label }}
              <!-- 所有节点都显示状态标签 -->
              <el-tag
                :type="data.status ? 'success' : 'info'"
                size="small"
                style="margin-left: 8px;"
              >
                {{ data.status ? '启用' : (data.parentDisabled ? '启用（但父级节点被禁用）' : '禁用') }}
              </el-tag>
            </span>
          </div>
          
          <div class="node-actions">
            <el-dropdown @command="(cmd: string) => {
              if (cmd === 'rename') renameNode(data)
              else if (cmd === 'delete') deleteNode(data)
              else if (cmd === 'toggleStatus') toggleNodeStatus(data)
              else if (cmd === 'addNode') addChildNode(data)
            }">
              <el-button size="small" text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 只有中间节点（文件夹）才能添加子节点 -->
                  <el-dropdown-item command="addNode" v-if="data.type === 'middle'">
                    <el-icon><Folder /></el-icon>
                    添加子节点
                  </el-dropdown-item>
                  <el-dropdown-item command="rename">
                    <el-icon><Edit /></el-icon>
                    重命名
                  </el-dropdown-item>
                  <el-dropdown-item command="toggleStatus">
                    <el-icon><Switch /></el-icon>
                    {{ data.status ? '禁用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
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
    
    <div v-if="!loading && treeData.length === 0" class="empty-state">
      <el-empty description="暂无数据库" />
    </div>
  </div>
</template>

<style scoped>
.database-tree-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
  border-right: 1px solid var(--border-primary);
  overflow: hidden;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border-primary);
  flex-shrink: 0;
}

.tree-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

:deep(.el-tree) {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  height: 100%;
}

:deep(.el-tree-node__content) {
  height: 36px;
  padding-right: 8px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f5f5;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #e6f7ff;
}

.tree-node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding-right: 8px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.node-icon {
  font-size: 16px;
  color: #1890ff;
  flex-shrink: 0;
}

.node-icon-disabled {
  color: #d9d9d9;
}

.node-label {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-label-disabled {
  color: #999;
}

/* 父节点被禁用时，子节点显示灰色 */
:deep(.el-tree-node) {
  transition: opacity 0.2s;
}

.tree-node-content.parent-disabled {
  opacity: 0.5;
}

.parent-disabled .node-icon {
  color: #d9d9d9 !important;
}

.parent-disabled .node-label {
  color: #999 !important;
}

.node-actions {
  opacity: 0.3;
  transition: opacity 0.2s;
}

:deep(.el-tree-node__content:hover) .node-actions {
  opacity: 1;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}
</style>
