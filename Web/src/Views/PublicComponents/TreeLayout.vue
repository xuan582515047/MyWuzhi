<script setup lang="ts">
import { ref, computed, defineEmits, defineProps } from 'vue'
import { ElTree, ElIcon, ElInput, ElButton, ElDropdown, ElDropdownMenu, ElDropdownItem, ElMessage, ElMessageBox } from 'element-plus'
import { Search, MoreFilled, Plus, Delete, Edit } from '@element-plus/icons-vue'

// =========================> 类型定义 <=========================
/**
 * 树节点接口
 */
export interface TreeNode {
  id: string
  name: string
  enable: boolean
  type: string
  children: TreeNode[]
}

/**
 * 图标类型接口
 */
export interface IconType {
  middleNode: any
  leafNode: any
}

/**
 * 类型标识
 */
export interface TypeNotice {
  middle: string
  leaf: string
}

/**
 * 类型名称
 */
export interface TypeName {
  middle: string
  leaf: string
}


// =========================> 通信定义 <=========================

/**
 * 树形布局组件
 * 
 * 提供左侧树形结构展示和右侧自定义内容插槽的通用布局组件。
 * 支持拖动分割线调整左右面板宽度。
 * 支持节点查询、移动、删除、添加子节点功能。
 */
const props = defineProps({
  /** 树形数据 */
  treeData: {type: [Object, Array] as unknown as () => TreeNode | TreeNode[], required: true},
  /** 图标类型配置 */
  iconType: {type: Object as () => IconType, required: true},
  /** 当前选中的节点（必填） */
  currentNode: {type: Object as () => TreeNode, required: true},
  /** 类型标识 */
  typeNotice: {type: Object as () => TypeNotice, default: {middle: 'middle', leaf: 'leaf'}},
  /** 类型名称 */
  typeName: {type: Object as () => TypeName, default: {middle: '中间节点', leaf: '叶子节点'}},
  /** 是否显示状态标签 */
  showStatus: {type: Boolean, default: false},
  /** 是否显示添加根节点按钮 */
  showRootAddBtn: {type: Boolean, default: false},
  /** 是否显示添加叶子节点按钮 */
  showAddLeafBtn: {type: Boolean, default: true},
  /** 是否允许移动根节点 */
  rootNodeMove: {type: Boolean, default: true}
})

/**
 * 定义事件
 */
const emit = defineEmits<{
  /** 节点选中事件 */
  select: [node: TreeNode]
  /** 节点位置移动事件 */
  changePosition: [fromNode: TreeNode, toParentId: string | null]
  /** 节点删除事件 */
  delete: [node: TreeNode]
  /** 添加中间节点事件 */
  addMiddle: [parentId: string | null]
  /** 添加叶子节点事件 */
  addLeaf: [parentId: string | null]
  /** 节点修改事件 */
  update: [node: TreeNode]
  /** 添加根节点事件 */
  addRoot: []
}>()

/**
 * 判断节点是否为中间节点
 */
const isMiddleNode = (node: TreeNode): boolean => {
  return node.type === props.typeNotice?.middle
}

// =========================> 变量定义 <=========================
/** 查询关键词 */
const searchKeyword = ref('')

/** 分割线位置（左侧宽度百分比） */
const splitPosition = ref(30)

/** 是否正在拖动分割线 */
const isDragging = ref(false)

/** 树组件引用 */
const treeRef = ref<InstanceType<typeof ElTree>>()

// =========================> 计算属性 <=========================
/** 标准化树形数据（将单个节点转换为数组） */
const normalizedTreeData = computed(() => {
  return Array.isArray(props.treeData) ? props.treeData : [props.treeData]
})

/** 过滤后的树形数据 */
const filteredTreeData = computed(() => {
  if (!searchKeyword.value.trim()) {
    return normalizedTreeData.value
  }
  return filterTreeData(normalizedTreeData.value, searchKeyword.value.trim().toLowerCase())
})

/**
 * 递归过滤树形数据
 */
const filterTreeData = (nodes: TreeNode[], keyword: string): TreeNode[] => {
  return nodes.reduce<TreeNode[]>((acc, node) => {
    const matchName = node.name.toLowerCase().includes(keyword)
    const filteredChildren = node.children && node.children.length > 0
      ? filterTreeData(node.children, keyword)
      : []

    // 如果节点名称匹配，或者子节点匹配，则保留该节点
    if (matchName || filteredChildren.length > 0) {
      acc.push({
        ...node,
        children: filteredChildren.length > 0 ? filteredChildren : node.children
      })
    }
    return acc
  }, [])
}

// =========================> 方法定义 <=========================
/**
 * 节点点击事件处理
 */
const handleNodeClick = (data: TreeNode) => {
  // 只在点击叶子节点时触发详情展示
  if (data.type !== props.typeNotice?.middle) {
    emit('select', data)
  }
}

/**
 * 处理节点修改
 * 直接将节点数据传给父组件，由父组件决定如何处理
 */
const handleUpdateNode = (node: TreeNode) => {
  emit('update', node)
}

/**
 * 处理添加根节点
 */
const handleAddRootNode = () => {
  emit('addRoot')
}

/**
 * 开始拖动分割线
 */
const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

/**
 * 拖动中
 */
const handleDrag = (e: MouseEvent) => {
  if (!isDragging.value) return

  const container = document.querySelector('.tree-layout-content')
  if (!container) return

  const containerRect = container.getBoundingClientRect()
  const newPosition = ((e.clientX - containerRect.left) / containerRect.width) * 100

  // 限制在 15% - 70% 之间
  if (newPosition >= 15 && newPosition <= 70) {
    splitPosition.value = newPosition
  }
}

/**
 * 停止拖动
 */
const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

/**
 * 检查节点是否为根节点
 */
const isRootNode = (node: TreeNode): boolean => {
  return normalizedTreeData.value.some(root => root.id === node.id)
}

/**
 * 检查是否允许拖拽节点
 */
const allowDrag = (draggingNode: any): boolean => {
  // 如果不允许移动根节点，检查并阻止
  if (!props.rootNodeMove && isRootNode(draggingNode.data)) {
    return false
  }
  return true
}

/**
 * 检查是否允许拖拽（叶子节点不能作为拖拽目标）
 */
const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  // 如果目标节点是中间节点，明确允许拖拽到任何位置（before/after/inner）
  if (isMiddleNode(dropNode.data)) {
    // 检查是否将父节点拖到自己的子节点下
    if (isDescendant(draggingNode.data, dropNode.data)) {
      return false
    }
    return true
  }
  
  // 如果目标节点是叶子节点，只允许拖拽到 before/after 位置，不允许 inner
  if (type === 'inner') {
    return false
  }
  
  // 检查是否将父节点拖到自己的子节点下
  if (isDescendant(draggingNode.data, dropNode.data)) {
    return false
  }
  
  return true
}

/**
 * 检查节点 A 是否是节点 B 的后代节点
 */
const isDescendant = (parent: TreeNode, child: TreeNode): boolean => {
  if (!parent.children || parent.children.length === 0) {
    return false
  }
  
  for (const node of parent.children) {
    if (node.id === child.id) {
      return true
    }
    if (isDescendant(node, child)) {
      return true
    }
  }
  
  return false
}

/**
 * 处理节点拖拽完成
 */
const handleNodeDrop = (draggingNode: any, dropNode: any, dropType: string) => {
  // 获取拖拽的节点
  const fromNode = draggingNode.data as TreeNode
  
  // 确定目标父节点ID
  let toParentId: string | null
  
  if (dropType === 'inner') {
    // 拖拽到节点内部，目标节点即为父节点
    toParentId = dropNode.data.id
  } else {
    // 拖拽到节点之前或之后
    // ✅ 如果目标节点是中间节点，直接作为其父节点（用户体验更友好）
    if (isMiddleNode(dropNode.data)) {
      toParentId = dropNode.data.id
    } else {
      // 否则使用目标节点的父节点
      const parentNode = findParentNode(normalizedTreeData.value, dropNode.data.id)
      toParentId = parentNode ? parentNode.id : null
    }
  }
  
  // 如果目标父节点ID与原父节点相同，则不需要移动
  if (toParentId === fromNode.id) {
    return
  }
  
  // 触发移动事件
  emit('changePosition', fromNode, toParentId)
}

/**
 * 查找指定ID的父节点
 */
const findParentNode = (nodes: TreeNode[], targetId: string): TreeNode | null => {
  for (const node of nodes) {
    if (node.children && node.children.length > 0) {
      for (const child of node.children) {
        if (child.id === targetId) {
          return node
        }
        const found = findParentNode([child], targetId)
        if (found) {
          return found
        }
      }
    }
  }
  return null
}

/**
 * 处理节点删除
 */
const handleDeleteNode = (node: TreeNode) => {
  ElMessageBox.confirm(`确定要删除节点 "${node.name}" 吗？删除后不可恢复`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    emit('delete', node)
  }).catch(() => {})
}

/**
 * 处理添加中间节点
 */
const handleAddMiddleNode = (node: TreeNode) => {
  // 如果是中间节点，添加子级；如果是叶子节点，添加同级
  if (isMiddleNode(node)) {
    emit('addMiddle', node.id)
  } else {
    // 查找父节点ID
    const parentNode = findParentNode(normalizedTreeData.value, node.id)
    emit('addMiddle', parentNode ? parentNode.id : null)
  }
}

/**
 * 处理添加叶子节点
 */
const handleAddLeafNode = (node: TreeNode) => {
  emit('addLeaf', node.id)
}

/**
 * 清空查询
 */
const clearSearch = () => {
  searchKeyword.value = ''
}

/**
 * 阻止事件冒泡，防止触发节点的展开/收缩
 */
const stopPropagation = (e: Event) => {
  e.stopPropagation()
}
</script>

<template>
  <div class="tree-layout">
    <div class="tree-layout-content">
      <!-- 左侧树形结构 -->
      <div class="tree-panel" :style="{ width: `${splitPosition}%` }">
        <!-- 查询输入框 -->
        <div class="tree-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索节点..."
            :prefix-icon="Search"
            clearable
            @clear="clearSearch"
          />
        </div>

        <!-- 添加根节点按钮 -->
        <div v-if="props.showRootAddBtn" class="tree-root-add">
          <el-button
            type="primary"
            :icon="Plus"
            size="small"
            @click="handleAddRootNode"
          >
            添加根{{props.typeName.middle}}
          </el-button>
        </div>

        <el-tree
          ref="treeRef"
          :data="filteredTreeData"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          highlight-current
          draggable
          :allow-drag="allowDrag"
          :allow-drop="allowDrop"
          @node-click="handleNodeClick"
          @node-drop="handleNodeDrop"
        >
          <template #default="{ node, data }">
            <div class="tree-node-content">
              <div class="node-info">
                <el-icon :class="['node-icon', { 'node-icon-disabled': !data.enable }]">
                  <component :is="data.type === props.typeNotice?.middle ? iconType.middleNode : iconType.leafNode" />
                </el-icon>
                <span :class="['node-label', { 'node-label-disabled': !data.enable }]">
                  {{ node.label }}
                </span>
                <!-- 状态标签 -->
                <el-tag
                  v-if="props.showStatus"
                  :type="data.enable ? 'success' : 'info'"
                  size="small"
                  class="node-status"
                >
                  {{ data.enable ? '启用' : '禁用' }}
                </el-tag>
              </div>

              <!-- 节点操作按钮 -->
              <div class="node-actions" @click="stopPropagation" @mousedown="stopPropagation">
                <el-dropdown @command="(cmd: string) => {
                  if (cmd === 'delete') handleDeleteNode(data)
                  else if (cmd === 'update') handleUpdateNode(data)
                  else if (cmd === 'addMiddle') handleAddMiddleNode(data)
                  else if (cmd === 'addLeaf') handleAddLeafNode(data)
                }">
                  <el-button size="small" text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="update">
                        <el-icon><Edit /></el-icon>
                        修改
                      </el-dropdown-item>
                      <el-dropdown-item command="delete">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.type === props.typeNotice?.middle" command="addMiddle">
                        <el-icon><Plus /></el-icon>
                        添加{{props.typeName.middle}}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.type === props.typeNotice?.middle && props.showAddLeafBtn" command="addLeaf">
                        <el-icon><Plus /></el-icon>
                        添加{{props.typeName.leaf}}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.type !== props.typeNotice?.middle" command="addMiddle">
                        <el-icon><Plus /></el-icon>
                        添加同级{{props.typeName.middle}}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.type !== props.typeNotice?.middle && props.showAddLeafBtn" command="addLeaf">
                        <el-icon><Plus /></el-icon>
                        添加同级{{props.typeName.leaf}}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </template>
        </el-tree>
      </div>

      <!-- 可拖动分割线 -->
      <div
        class="splitter"
        :class="{ dragging: isDragging }"
        @mousedown="startDrag"
      >
        <div class="splitter-handle"></div>
      </div>

      <!-- 右侧自定义内容 -->
      <div class="content-panel" :style="{ width: `${100 - splitPosition}%` }">
        <div v-if="props.currentNode.type !== props.typeNotice?.middle" :key="props.currentNode.id" class="content-wrapper">
          <slot name="content" :node="props.currentNode"></slot>
        </div>
        <div v-else class="empty-content">
          <el-empty description="请选择叶子节点查看详情" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tree-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 95vh;
  background: var(--bg-primary);
  overflow: hidden;
}

.tree-layout-content {
  display: flex;
  height: 100%;
  min-height: 95vh;
  overflow: hidden;
  background: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow-card);
}

.tree-panel {
  flex-shrink: 0;
  overflow: hidden;
  min-width: 200px;
  border-right: 1px solid var(--border-primary);
  display: flex;
  flex-direction: column;
}

.tree-search {
  padding: 12px;
  border-bottom: 1px solid var(--border-primary);
  flex-shrink: 0;
}

.tree-root-add {
  padding: 12px;
  border-bottom: 1px solid var(--border-primary);
  flex-shrink: 0;
}

:deep(.el-tree) {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  height: 100%;
  background: transparent;
}

:deep(.el-tree-node__content) {
  height: 40px;
  padding-right: 8px;
  transition: all 0.3s ease;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--hover-bg);
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--primary-color-light);
}

:deep(.el-tree-node__expand-icon) {
  color: var(--text-tertiary);
  transition: all 0.3s ease;
}

:deep(.el-tree-node__expand-icon:hover) {
  color: var(--accent-primary);
}

.tree-node-content {
  display: flex;
  align-items: center;
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
  color: var(--accent-primary);
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.node-icon-disabled {
  color: var(--text-tertiary);
  opacity: 0.6;
}

.node-label {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  transition: all 0.3s ease;
}

.node-label-disabled {
  color: var(--text-tertiary);
}

.node-status {
  margin-left: 8px;
  flex-shrink: 0;
}

:deep(.el-tag) {
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.node-actions {
  opacity: 0.3;
  transition: opacity 0.3s ease;
}

:deep(.el-tree-node__content:hover) .node-actions {
  opacity: 1;
}

:deep(.el-button--text) {
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

:deep(.el-button--text:hover) {
  color: var(--accent-primary);
  transform: scale(1.1);
}

.splitter {
  width: 6px;
  flex-shrink: 0;
  background: var(--bg-secondary);
  border-left: 1px solid var(--border-primary);
  border-right: 1px solid var(--border-primary);
  cursor: col-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  z-index: 10;
}

.splitter:hover {
  background: var(--border-primary);
}

.splitter.dragging {
  background: var(--accent-primary);
}

.splitter-handle {
  width: 2px;
  height: 40px;
  background: var(--border-secondary);
  border-radius: 1px;
  transition: background-color 0.3s ease;
}

.splitter:hover .splitter-handle {
  background: var(--border-tertiary);
}

.splitter.dragging .splitter-handle {
  background: var(--text-on-primary);
}

.content-panel {
  flex-shrink: 0;
  overflow: hidden;
  min-width: 300px;
}

.content-wrapper {
  height: 100%;
  animation: fadeIn 0.3s ease-in-out;
}

.empty-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 覆盖 Element Plus 内置样式 */
:deep(.el-input__wrapper) {
  background: var(--input-bg);
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--border-secondary);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 1px var(--accent-primary);
}

:deep(.el-input__inner) {
  color: var(--text-primary);
  font-weight: 500;
}

:deep(.el-input__prefix) {
  color: var(--text-tertiary);
}

:deep(.el-button--primary) {
  background: var(--primary-gradient);
  border: none;
  color: var(--text-on-primary);
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background: var(--btn-primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

:deep(.el-dropdown-menu) {
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  background: var(--card-bg);
  box-shadow: var(--shadow-medium);
  padding: 8px 0;
}

:deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  margin: 2px 8px;
  padding: 10px 16px;
  color: var(--text-primary);
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-dropdown-menu__item:hover) {
  background: var(--hover-bg);
  color: var(--accent-primary);
  transform: translateX(4px);
}

:deep(.el-dropdown-menu__item .el-icon) {
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: var(--accent-primary);
}

/* 暗色模式适配 */
:deep(.el-empty__description) {
  color: var(--text-secondary);
}

:deep(.el-empty__description p) {
  font-weight: 500;
}
</style>
