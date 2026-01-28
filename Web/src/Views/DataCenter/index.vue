<!--
  工具与知识库管理页面
  功能描述：提供工具使用管理、知识库管理等功能的管理界面
  作者：AI Assistant
  创建时间：2025-01-20
-->
<template>
  <div class="data-center-container">
    <!-- 顶部导航栏 -->
    <div class="center-header">
      <div class="header-content">
        <div class="center-title">
          <el-icon><Management /></el-icon>
          <h1>管理中心</h1>
        </div>
        
        <div class="header-actions">
          <el-button type="primary" @click="handleAddAction">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
          <el-button @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </div>

    <div class="center-content">
      <!-- 侧边栏 -->
      <Sidebar>
        <!-- 工具管理 -->
        <div class="sidebar-section">
          <div class="section-header" @click="toggleToolSection">
            <el-icon><Tools /></el-icon>
            <span>工具管理</span>
            <el-icon class="arrow" :class="{ expanded: toolSectionExpanded }">
              <ArrowRight />
            </el-icon>
          </div>
          <div v-show="toolSectionExpanded" class="section-content">
            <div class="tool-stats">
              <div class="stat-item">
                <span class="stat-value">{{ toolStats.total }}</span>
                <span class="stat-label">总工具数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ toolStats.active }}</span>
                <span class="stat-label">已启用</span>
              </div>
            </div>
            <div class="quick-actions">
              <el-button size="small" @click="activeTab = 'tools'">
                查看详情
              </el-button>
              <el-button size="small" type="primary" @click="showAddToolDialog">
                添加工具节点
              </el-button>
            </div>
          </div>
        </div>

        <!-- 知识库管理 -->
        <div class="sidebar-section">
          <div class="section-header" @click="toggleKnowledgeSection">
            <el-icon><Collection /></el-icon>
            <span>知识库管理</span>
            <el-icon class="arrow" :class="{ expanded: knowledgeSectionExpanded }">
              <ArrowRight />
            </el-icon>
          </div>
          <div v-show="knowledgeSectionExpanded" class="section-content">
            <div class="knowledge-stats">
              <div class="stat-item">
                <span class="stat-value">{{ knowledgeStats.total }}</span>
                <span class="stat-label">总节点数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ knowledgeStats.enabled }}</span>
                <span class="stat-label">已启用</span>
              </div>
            </div>
            <div class="quick-actions">
              <el-button size="small" @click="activeTab = 'knowledge'">
                查看详情
              </el-button>
              <el-button size="small" type="primary" @click="showAddKnowledgeDialog">
                添加知识库节点
              </el-button>
            </div>
          </div>
        </div>
      </Sidebar>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 标签页 -->
        <el-tabs v-model="activeTab" class="content-tabs">
          <!-- 工具管理标签页 -->
          <el-tab-pane label="工具管理" name="tools">
            <ToolUsageRecord
              :tool-tree="toolUseTree"
              :usage-records="toolUsageRecords"
              :loading="toolLoading"
              @add-node="handleAddToolNode"
              @edit-node="handleEditToolNode"
              @delete-node="handleDeleteToolNode"
              @toggle-status="handleToggleToolStatus"
              @add-record="handleAddUsageRecord"
              @edit-record="handleEditUsageRecord"
              @delete-record="handleDeleteUsageRecord"
              @refresh="loadToolData"
            />
          </el-tab-pane>

          <!-- 知识库管理标签页 -->
          <el-tab-pane label="知识库管理" name="knowledge">
            <KnowledgeTree
              :tree-data="databaseTree"
              :loading="knowledgeLoading"
              @add="showAddKnowledgeDialog"
              @edit="showEditKnowledgeDialog"
              @delete="handleDeleteKnowledge"
              @toggle-status="handleToggleKnowledgeStatus"
              @refresh="loadKnowledgeTree"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 工具编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑' : '新增'"
      width="800px"
      destroy-on-close
    >
      <EditDialog
        v-if="editDialogVisible"
        :visible="editDialogVisible"
        :type="editType"
        :data="editData"
        :fields="getEditFields()"
        @submit="handleEditSubmit"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Management,
  Tools,
  Collection,
  Plus,
  Refresh,
  ArrowRight
} from '@element-plus/icons-vue'

// 导入API
import { ToolApi } from '@/Api/ToolApi'
import { DatabaseApi } from '@/Api/DatabaseApi'

// 导入组件
import Sidebar from '@/Views/PublicComponents/Sidebar.vue'
import ToolUsageRecord from './components/ToolUsageRecord.vue'
import KnowledgeTree from './components/KnowledgeTree.vue'
import EditDialog from './components/EditDialog.vue'

// 导入类型
import type { 
  ToolInfo, 
  ToolUseNode, 
  ToolUseRecord,
  AddToolUseRecordRequest,
  EditToolUseRecordRequest,
  ToolUseRecordQueryRequest,
  ToolNodeStatusRequest
} from '@/Entity/ToolEntity'
import type { 
  DatabaseTreeResponse,
  DatabaseNode,
  AddDatabaseNodeRequest,
  DatabaseNodeStatusRequest
} from '@/Entity/DatabaseEntity'

// =========================> 响应式数据 <=========================
/** 当前激活的标签页 */
const activeTab = ref('tools')

/** 工具相关状态 */
const toolUseTree = ref<ToolUseNode[]>([])
const toolUsageRecords = ref<ToolUseRecord[]>([])
const toolLoading = ref(false)
const toolStats = reactive({
  total: 0,
  active: 0
})

/** 知识库相关状态 */
const databaseTree = ref<DatabaseTreeResponse[]>([])
const knowledgeLoading = ref(false)
const knowledgeStats = reactive({
  total: 0,
  enabled: 0
})

/** 侧边栏展开状态 */
const toolSectionExpanded = ref(true)
const knowledgeSectionExpanded = ref(true)

/** 编辑弹窗状态 */
const editDialogVisible = ref(false)
const isEdit = ref(false)
const editType = ref<'tool-node' | 'usage-record' | 'knowledge-node'>('tool-node')
const editData = ref<any>(null)

/** 当前公司ID（假设从用户信息或路由参数获取） */
const currentCompanyId = ref('company-001')

// =========================> 计算属性 <=========================
/** 获取工具统计数据 */
const toolStatsData = computed(() => {
  const total = toolUseTree.value.length
  const active = toolUseTree.value.filter(node => node.status).length
  return { total, active }
})

/** 获取知识库统计数据 */
const knowledgeStatsData = computed(() => {
  const countNodes = (nodes: DatabaseNode[]): { total: number; enabled: number } => {
    let total = 0
    let enabled = 0
    
    const traverse = (nodeList: DatabaseNode[]) => {
      nodeList.forEach(node => {
        total++
        if (node.status) enabled++
        if (node.children && node.children.length > 0) {
          traverse(node.children)
        }
      })
    }
    
    traverse(nodes)
    return { total, enabled }
  }
  
  let total = 0
  let enabled = 0
  
  databaseTree.value.forEach(tree => {
    const stats = countNodes(tree.children || [])
    total += stats.total
    enabled += stats.enabled
  })
  
  return { total, enabled }
})

// =========================> 方法定义 <=========================
/**
 * 切换工具管理区域展开状态
 */
const toggleToolSection = () => {
  toolSectionExpanded.value = !toolSectionExpanded.value
}

/**
 * 切换知识库管理区域展开状态
 */
const toggleKnowledgeSection = () => {
  knowledgeSectionExpanded.value = !knowledgeSectionExpanded.value
}

/**
 * 加载工具数据
 */
const loadToolData = async () => {
  toolLoading.value = true
  try {
    // 加载工具使用树
    const useTree = await ToolApi.getToolUseTree(currentCompanyId.value)
    toolUseTree.value = useTree

    // 加载工具使用记录
    const now = new Date()
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
    
    const startDate = thirtyDaysAgo.toISOString().split('T')[0]
    const endDate = now.toISOString().split('T')[0]
    
    const recordRequest: ToolUseRecordQueryRequest = {
      toolId: '', // 空字符串表示查询所有工具
      companyId: currentCompanyId.value,
      startTime: startDate || '',
      endTime: endDate || '',
      isAsc: false,
      pageNum: 1,
      pageSize: 100
    }
    
    const records = await ToolApi.getToolUseRecordList(recordRequest)
    toolUsageRecords.value = records.list

    // 更新统计数据
    const stats = toolStatsData.value
    toolStats.total = stats.total
    toolStats.active = stats.active
  } catch (error) {
    console.error('加载工具数据失败:', error)
    ElMessage.error('加载工具数据失败')
  } finally {
    toolLoading.value = false
  }
}

/**
 * 加载知识库树
 */
const loadKnowledgeTree = async () => {
  knowledgeLoading.value = true
  try {
    const tree = await DatabaseApi.getDatabaseTree()
    databaseTree.value = tree

    // 更新统计数据
    const stats = knowledgeStatsData.value
    knowledgeStats.total = stats.total
    knowledgeStats.enabled = stats.enabled
  } catch (error) {
    console.error('加载知识库树失败:', error)
    ElMessage.error('加载知识库树失败')
  } finally {
    knowledgeLoading.value = false
  }
}

/**
 * 处理新增操作
 */
const handleAddAction = () => {
  if (activeTab.value === 'tools') {
    showAddToolDialog()
  } else {
    showAddKnowledgeDialog()
  }
}

/**
 * 显示新增工具节点对话框
 */
const showAddToolDialog = (parentNode?: ToolUseNode) => {
  isEdit.value = false
  editType.value = 'tool-node'
  editData.value = parentNode ? { parentId: parentNode.id, companyId: currentCompanyId.value } : { companyId: currentCompanyId.value }
  editDialogVisible.value = true
}

/**
 * 显示新增知识库节点对话框
 */
const showAddKnowledgeDialog = (parentNode?: DatabaseNode) => {
  isEdit.value = false
  editType.value = 'knowledge-node'
  editData.value = parentNode ? { parentId: parentNode.id, companyId: currentCompanyId.value } : { companyId: currentCompanyId.value }
  editDialogVisible.value = true
}

/**
 * 显示编辑知识库节点对话框
 */
const showEditKnowledgeDialog = (node: DatabaseNode) => {
  isEdit.value = true
  editType.value = 'knowledge-node'
  editData.value = { ...node }
  editDialogVisible.value = true
}

/**
 * 处理工具节点操作
 */
const handleAddToolNode = (nodeData: any) => {
  // 这里应该调用添加工具节点的API
  console.log('添加工具节点:', nodeData)
  loadToolData()
}

const handleEditToolNode = (nodeData: any) => {
  // 这里应该调用编辑工具节点的API
  console.log('编辑工具节点:', nodeData)
  loadToolData()
}

const handleDeleteToolNode = (nodeId: string) => {
  // 这里应该调用删除工具节点的API
  console.log('删除工具节点:', nodeId)
  loadToolData()
}

const handleToggleToolStatus = async (node: ToolUseNode) => {
  try {
    const request: ToolNodeStatusRequest = {
      id: node.id,
      status: !node.status
    }
    await ToolApi.updateToolNodeStatus([request])
    ElMessage.success('工具节点状态更新成功')
    await loadToolData()
  } catch (error) {
    console.error('更新工具节点状态失败:', error)
    ElMessage.error('更新工具节点状态失败')
  }
}

/**
 * 处理使用记录操作
 */
const handleAddUsageRecord = (recordData: AddToolUseRecordRequest) => {
  console.log('添加使用记录:', recordData)
  loadToolData()
}

const handleEditUsageRecord = (recordData: EditToolUseRecordRequest) => {
  console.log('编辑使用记录:', recordData)
  loadToolData()
}

const handleDeleteUsageRecord = (recordId: string) => {
  console.log('删除使用记录:', recordId)
  loadToolData()
}

/**
 * 处理知识库操作
 */
const handleDeleteKnowledge = async (node: DatabaseNode) => {
  try {
    await DatabaseApi.deleteDatabaseNode(node.id)
    ElMessage.success('知识库节点删除成功')
    await loadKnowledgeTree()
  } catch (error) {
    console.error('删除知识库节点失败:', error)
    ElMessage.error('删除知识库节点失败')
  }
}

const handleToggleKnowledgeStatus = async (node: DatabaseNode) => {
  try {
    const request: DatabaseNodeStatusRequest = {
      id: node.id,
      status: !node.status
    }
    await DatabaseApi.updateDatabaseNodeStatus([request])
    ElMessage.success('知识库节点状态更新成功')
    await loadKnowledgeTree()
  } catch (error) {
    console.error('更新知识库节点状态失败:', error)
    ElMessage.error('更新知识库节点状态失败')
  }
}

/**
 * 获取编辑表单字段
 */
const getEditFields = () => {
  if (editType.value === 'knowledge-node') {
    return [
      {
        key: 'name',
        label: '节点名称',
        type: 'input' as const,
        required: true,
        placeholder: '请输入节点名称',
        maxLength: 50
      }
    ]
  }
  
  // 工具节点和其他类型返回空数组，保持原有行为
  return []
}

/**
 * 处理编辑提交
 */
const handleEditSubmit = async (formData: any) => {
  try {
    if (editType.value === 'tool-node') {
      // 处理工具节点编辑/新增
      if (isEdit.value) {
        // 编辑模式
        await ToolApi.editToolUseRecord(formData)
        ElMessage.success('工具节点更新成功')
      } else {
        // 新增模式
        const request: AddToolUseRecordRequest = {
          toolId: formData.toolId || '',
          companyId: currentCompanyId.value,
          detail: formData.description || ''
        }
        await ToolApi.addToolUseRecord(request)
        ElMessage.success('工具节点创建成功')
      }
      await loadToolData()
    } else if (editType.value === 'usage-record') {
      // 处理使用记录编辑/新增
      if (isEdit.value) {
        await ToolApi.editToolUseRecord(formData)
        ElMessage.success('使用记录更新成功')
      } else {
        await ToolApi.addToolUseRecord(formData)
        ElMessage.success('使用记录创建成功')
      }
      await loadToolData()
    } else if (editType.value === 'knowledge-node') {
      // 处理知识库节点编辑/新增
      if (isEdit.value) {
        await DatabaseApi.editDatabaseNode(formData)
        ElMessage.success('知识库节点更新成功')
      } else {
        const request: AddDatabaseNodeRequest = {
          name: formData.name || '新节点',
          parentId: formData.parentId || null,
          companyId: currentCompanyId.value
        }
        await DatabaseApi.addDatabaseNode(request)
        ElMessage.success('知识库节点创建成功')
      }
      await loadKnowledgeTree()
    }
    
    editDialogVisible.value = false
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

/**
 * 刷新数据
 */
const refreshData = async () => {
  if (activeTab.value === 'tools') {
    await loadToolData()
  } else {
    await loadKnowledgeTree()
  }
  ElMessage.success('数据已刷新')
}

// =========================> 生命周期钩子 <=========================
onMounted(async () => {
  await Promise.all([
    loadToolData(),
    loadKnowledgeTree()
  ])
})
</script>

<style scoped>
.data-center-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);
}

.center-header {
  background-color: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color);
  padding: 16px 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.center-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.center-title h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.center-title .el-icon {
  font-size: 28px;
  color: var(--el-color-primary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.center-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow: hidden;
}

.content-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: auto;
}

.sidebar-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.section-header:hover {
  background-color: var(--el-fill-color-light);
}

.section-header .el-icon:first-child {
  color: var(--el-color-primary);
}

.section-header span {
  flex: 1;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.arrow {
  transition: transform 0.3s ease;
  color: var(--el-text-color-secondary);
}

.arrow.expanded {
  transform: rotate(90deg);
}

.section-content {
  padding: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.tool-stats,
.knowledge-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.quick-actions {
  display: flex;
  gap: 8px;
}

.quick-actions .el-button {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .center-content {
    flex-direction: column;
  }
  
  .main-content {
    padding: 16px;
  }
  
  .tool-stats,
  .knowledge-stats {
    flex-direction: column;
    gap: 8px;
  }
  
  .quick-actions {
    flex-direction: column;
  }
}

/* 深色主题适配 */
.dark .data-center-container {
  background-color: var(--el-bg-color);
}

.dark .center-header {
  background-color: var(--el-bg-color-page);
}

.dark .section-header:hover {
  background-color: var(--el-fill-color-dark);
}

.dark .stat-item {
  background-color: var(--el-fill-color-darker);
}
</style>