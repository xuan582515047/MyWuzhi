<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import TreeLayout from '@/Views/PublicComponents/TreeLayout.vue'
import { ToolApi } from '@/Api/ToolApi.ts'
import { ElMessageBox } from 'element-plus'
import { Folder, Link } from '@element-plus/icons-vue'
import type { TreeNode } from '@/Views/PublicComponents/TreeLayout.vue'
import type { ToolUseNode } from '@/Entity/ToolEntity.ts'

interface ToolTreeNode extends TreeNode {
  url?: string
}

// =========================> 通信定义 <=========================
const props = defineProps({
  companyId: { type: String, required: true }
})

// =========================> 变量定义 <=========================
/** 工具使用树数据 */
const toolUseTree = ref<ToolTreeNode[]>([])

/** 当前选中的节点 */
const currentNode = ref<ToolTreeNode>({
  id: '',
  name: '',
  enable: true,
  type: 'leaf',
  children: []
})

/** 图标类型配置 */
const iconType = {
  middleNode: Folder,
  leafNode: Link
}

/** 加载状态 */
const loading = ref(false)

// =========================> 计算属性 <=========================
/** 当前选中工具的URL */
const currentToolUrl = computed(() => {
  if (currentNode.value.type === 'leaf' && currentNode.value.id) {
    return currentNode.value.url || ''
  }
  return ''
})

// =========================> 方法定义 <=========================

/**
 * 加载工具使用树
 */
const loadToolUseTree = async () => {
  if (!props.companyId) return
  
  loading.value = true
  const nodes = await ToolApi.getToolUseTree(props.companyId)
  toolUseTree.value = convertToTreeNodes(nodes)
  loading.value = false
}

/**
 * 转换 ToolUseNode 为 TreeNode
 */
const convertToTreeNodes = (nodes: ToolUseNode[]): ToolTreeNode[] => {
  return nodes.map(node => ({
    id: node.id,
    name: node.name,
    enable: true,  // 工具节点全部启用
    type: node.type,
    url: node.url || '',
    children: node.children ? convertToTreeNodes(node.children) : []
  }))
}

/**
 * 查找第一个叶子节点
 */
const findFirstLeafNode = (nodes: ToolTreeNode[]): ToolTreeNode | null => {
  for (const node of nodes) {
    if (node.type === 'leaf') {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findFirstLeafNode(node.children)
      if (found) return found
    }
  }
  return null
}

/**
 * 处理节点选中事件
 */
const handleNodeSelect = async (node: ToolTreeNode) => {
  currentNode.value = node
  
  //TODO 记录工具使用
  if (node.type === 'leaf') {

  }
}

/**
 * 处理节点移动事件
 */
const handleNodeMove = async (fromNode: ToolTreeNode, toParentId: string | null) => {
  try {
    await ToolApi.changeNodeParent({
      id: fromNode.id,
      parentId: toParentId
    })
    await loadToolUseTree()
  } catch (error) {
    // 错误已在AxiosUtil中处理
  }
}

/**
 * 处理节点删除事件
 */
const handleNodeDelete = async (node: ToolTreeNode) => {
  try {
    await ToolApi.deleteToolNode(node.id)
    await loadToolUseTree()
  } catch (error) {
    // 错误已在AxiosUtil中处理
  }
}

/**
 * 处理添加中间节点事件
 */
const handleAddMiddleNode = async (parentId: string | null) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入节点名称', '添加中间节点', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\S.{0,48}\S$/,
      inputErrorMessage: '请输入2-50个字符的节点名称'
    })
    
    await ToolApi.addToolNode({
      parentId: parentId || undefined,
      name: value,
      companyId: props.companyId
    })
    
    await loadToolUseTree()
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 处理添加根节点事件
 */
const handleAddRootNode = async () => {
  await handleAddMiddleNode(null)
}

/**
 * 处理节点修改事件
 */
const handleNodeUpdate = async (node: ToolTreeNode) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的名称', '修改节点', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: node.name,
      inputPattern: /^\S.{0,48}\S$/,
      inputErrorMessage: '请输入2-50个字符的节点名称'
    })
    
    await ToolApi.editToolNode({
      id: node.id,
      name: value
    })
    
    await loadToolUseTree()
  } catch (error) {
    // 用户取消操作
  }
}

// =========================> 生命周期 <=========================
onMounted(() => {
  loadToolUseTree()
})
</script>

<template>
  <div class="tool-use-container">
    <TreeLayout
      :tree-data="toolUseTree"
      :icon-type="iconType"
      :current-node="currentNode"
      :show-status="false"
      :show-root-add-btn="true"
      :show-add-leaf-btn="false"
      :root-node-move="true"
      :type-name="{middle: '文件夹', leaf: '工具'}"
      @select="handleNodeSelect"
      @change-position="handleNodeMove"
      @delete="handleNodeDelete"
      @add-middle="handleAddMiddleNode"
      @update="handleNodeUpdate"
      @add-root="handleAddRootNode"
    >
      <template #content="{ node }">
        <div class="tool-iframe-container">
          <div v-if="currentToolUrl" class="iframe-wrapper">
            <iframe
              :src="currentToolUrl"
              class="tool-iframe"
              frameborder="0"
              @load="loading = false"
            ></iframe>
          </div>
          <div v-else class="empty-tip">
            <el-empty description="请选择工具查看内容" />
          </div>
        </div>
      </template>
    </TreeLayout>
  </div>
</template>

<style scoped>
.tool-use-container {
  height: 100%;
  overflow: hidden;
}

.tool-iframe-container {
  height: 100%;
  overflow: hidden;
}

.iframe-wrapper {
  height: 100%;
  overflow: hidden;
}

.tool-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

.empty-tip {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>