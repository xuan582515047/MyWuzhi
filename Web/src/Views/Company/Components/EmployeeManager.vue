<!-- EmployeeManager.vue -->
<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {CompanyApi} from '@/Api/company/CompanyApi.ts'
import {DepartmentApi} from '@/Api/company/DepartmentApi.ts'
import {EmployeeApi} from '@/Api/company/EmployeeApi.ts'
import {MissionApi} from '@/Api/company/MissionApi.ts'
import {TimeFormatUtil} from '@/Util/TimeFormatUtil.ts'
import {type CompanyNode, NodeType} from '@/Entity/company/CompanyEntity.ts'
import {type EmployeeDetail, type EmployeeInfo, EmployeeStatus,} from '@/Entity/company/EmployeeEntity.ts'
import type {MissionInfo, MissionPageResponse, MissionQueryRequest, MissionStatus, AddMissionRequest, UpdateMissionRequest} from '@/Entity/company/MissionEntity.ts'
import {Delete, Document, Edit, Folder, Plus, Refresh, Search, Timer, User} from '@element-plus/icons-vue'
import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElSpace,
  ElTable,
  ElTableColumn,
  ElTag
} from 'element-plus'
import TreeLayout from "@/Views/PublicComponents/TreeLayout.vue";

// =========================> 类型定义 <=========================
interface TreeNode {
  id: string
  name: string
  enable: boolean
  type: string
  children: TreeNode[]
}

// =========================> Props 定义 <=========================
const props = defineProps<{
  companyId: string
  departmentTree: any
  permissionTree: any
}>()

// =========================> 事件定义 <=========================
const emit = defineEmits<{
  'tree-updated': []
  'employee-selected': [employee: EmployeeInfo]
}>()

// =========================> 状态变量 <=========================
const companyId = ref(props.companyId)
const treeData = ref<TreeNode[]>([])
const loading = ref(false)
const currentNode = ref<TreeNode>({
  id: '',
  name: '请选择员工或部门',
  enable: true,
  type: '',
  children: []
})

// 员工详情相关
const employeeDetail = ref<EmployeeDetail | null>(null)
const employeeDetailLoading = ref(false)

// 员工列表相关
const employeeList = ref<EmployeeInfo[]>([])
const employeeLoading = ref(false)
const employeeTotal = ref(0)
const employeeQueryParams = ref({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: '' as EmployeeStatus | '',
  departmentId: ''
})

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref<'add' | 'edit'>('add')
const employeeForm = ref({
  userId: '',
  name: '',
  departmentId: '',
  departmentName: '',
  status: 'active' as EmployeeStatus
})

// 员工统计信息
const employeeStatistics = ref<any>(null)
const statsLoading = ref(false)

// 任务列表相关
const missionList = ref<MissionInfo[]>([])
const missionLoading = ref(false)
const missionTotal = ref(0)
const missionQueryParams = ref({
  page: 1,
  pageSize: 10,
  status: '' as MissionStatus | '',
  employeeId: '' as string
})

// 任务对话框状态
const missionDialogVisible = ref(false)
const missionDialogTitle = ref('')
const missionDialogMode = ref<'add' | 'edit'>('add')
const missionForm = ref({
  id: '',
  name: '',
  description: '',
  startTime: '',
  endTime: '',
  status: 'not_started' as MissionStatus,
  departmentId: '',
  employeeId: ''
})

// =========================> 常量定义 <=========================
const iconType = {
  middleNode: Folder,
  leafNode: User
}

const typeNotice = {
  middle: 'department',
  leaf: 'employee'
}

const typeName = {
  middle: '部门',
  leaf: '员工'
}

// =========================> 生命周期 <=========================
onMounted(() => {
  fetchCompanyTree()
  fetchEmployeeList()
})

watch(() => props.companyId, (newVal) => {
  if (newVal) {
    companyId.value = newVal
    fetchCompanyTree()
    fetchEmployeeList()
  }
})

// =========================> 方法定义 <=========================

/**
 * 获取公司部门树
 */
const fetchCompanyTree = async () => {
  if (!companyId.value) return

  loading.value = true
  const tree = await CompanyApi.getCompanyTree(companyId.value)
  treeData.value = [convertCompanyNodeToTreeNode(tree)]
  loading.value = false
}

/**
 * 转换 CompanyNode 为 TreeNode
 */
const convertCompanyNodeToTreeNode = (node: CompanyNode): TreeNode => {
  const treeNode: TreeNode = {
    id: node.id as string,
    name: node.name as string,
    enable: true,
    type: node.type === NodeType.DEPARTMENT ? 'department' : 'employee',
    children: []
  }

  if (node.children && node.children.length > 0) {
    treeNode.children = node.children.map(child => convertCompanyNodeToTreeNode(child))
  }

  return treeNode
}

/**
 * 获取员工详情
 */
const fetchEmployeeDetail = async (nodeId: string) => {
  employeeDetailLoading.value = true
  employeeDetail.value = null

  const detail = await EmployeeApi.getEmployeeDetail({
    nodeId,
    companyId: companyId.value
  })
  employeeDetail.value = detail
  employeeDetailLoading.value = false
}

/**
 * 获取员工列表
 */
const fetchEmployeeList = async () => {
  employeeLoading.value = true
  const response = await EmployeeApi.queryEmployeeList({
    companyId: companyId.value,
    name: employeeQueryParams.value.name || undefined,
    status: employeeQueryParams.value.status || undefined,
    departmentId: employeeQueryParams.value.departmentId || undefined,
    pageNum: employeeQueryParams.value.pageNum,
    pageSize: employeeQueryParams.value.pageSize
  })
  employeeList.value = response.data
  employeeTotal.value = response.total
  employeeLoading.value = false
}

/**
 * 获取员工统计信息
 */
const fetchEmployeeStatistics = async (employeeId: string) => {
  statsLoading.value = true
  const stats = await EmployeeApi.getEmployeeStatistics({ employeeId })
  employeeStatistics.value = stats
  statsLoading.value = false
}

/**
 * 获取员工任务列表
 */
const fetchEmployeeMissions = async (employeeId: string) => {
  missionLoading.value = true
  // 设置 employeeId 到查询参数
  missionQueryParams.value.employeeId = employeeId
  
  const queryParams: any = {
    companyId: companyId.value,
    page: missionQueryParams.value.page,
    pageSize: missionQueryParams.value.pageSize,
    employeeId: missionQueryParams.value.employeeId
  }
  
  // 添加状态筛选
  if (missionQueryParams.value.status) {
    queryParams.status = missionQueryParams.value.status
  }
  
  const response: MissionPageResponse = await MissionApi.getMissionList(queryParams)
  missionList.value = response.data
  missionTotal.value = response.total
  missionLoading.value = false
}

/**
 * 处理树节点选择
 */
const handleNodeSelect = async (node: TreeNode) => {
  currentNode.value = node

  if (node.type === 'employee') {
    // 获取员工详情
    await fetchEmployeeDetail(node.id)
    // 获取员工统计信息（使用员工ID）
    if (employeeDetail.value?.id) {
      await fetchEmployeeStatistics(employeeDetail.value.id)
      // 获取员工任务列表
      await fetchEmployeeMissions(employeeDetail.value.id)
    }

    // 查找员工列表中的对应员工信息
    const employee = employeeList.value.find(emp => emp.id === employeeDetail.value?.id)
    if (employee) {
      emit('employee-selected', employee)
    }
  } else if (node.type === 'department') {
    // 如果是部门，更新查询参数并加载该部门的员工
    employeeQueryParams.value.departmentId = node.id
    fetchEmployeeList()
    // 清空任务列表
    missionList.value = []
    missionTotal.value = 0
  } else {
    // 清空任务列表
    missionList.value = []
    missionTotal.value = 0
  }
}

/**
 * 处理添加中间节点（部门）
 */
const handleAddMiddle = async (parentId: string | null) => {
  try {
    const { value: name } = await ElMessageBox.prompt('请输入部门名称', '添加部门', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '部门名称不能为空'
    })

    await DepartmentApi.addDepartment({
      name,
      companyId: companyId.value,
      parentId: parentId || ''
    })

    emit('tree-updated')
    fetchCompanyTree() // 刷新树
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 处理添加叶子节点（员工）
 */
const handleAddLeaf = async (parentId: string | null) => {
  dialogMode.value = 'add'
  dialogTitle.value = '添加员工'

  // 设置部门ID和部门名称
  let targetNode: TreeNode | null = null
  
  if (parentId) {
    // 从parentId查找节点
    const findNodeById = (nodes: TreeNode[], id: string): TreeNode | null => {
      for (const node of nodes) {
        if (node.id === id) return node
        if (node.children.length > 0) {
          const found = findNodeById(node.children, id)
          if (found) return found
        }
      }
      return null
    }
    targetNode = findNodeById(treeData.value, parentId)
  } else if (currentNode.value.type === 'department') {
    // 使用当前选中的部门节点
    targetNode = currentNode.value
  }
  
  if (targetNode) {
    employeeForm.value.departmentId = targetNode.id
    employeeForm.value.departmentName = targetNode.name
  }

  dialogVisible.value = true
}

/**
 * 处理节点删除
 */
const handleDeleteNode = async (node: TreeNode) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除${node.type === 'department' ? '部门' : '员工'} "${node.name}" 吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    if (node.type === 'department') {
      await DepartmentApi.deleteDepartment({
        companyId: companyId.value,
        departmentId: node.id
      })
    } else {
      await EmployeeApi.deleteEmployee({
        companyId: companyId.value,
        employeeId: node.id
      })
    }

    emit('tree-updated')
    fetchCompanyTree() // 刷新树
    fetchEmployeeList() // 刷新员工列表
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 根据ID查找节点
 */
const findNodeById = (nodes: TreeNode[], id: string): TreeNode | null => {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children && node.children.length > 0) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

/**
 * 处理节点位置变更（带确认对话框）
 */
const handleChangePosition = async (fromNode: TreeNode, toParentId: string | null) => {
  try {
    // 查找目标父节点
    const toParentNode = toParentId ? findNodeById(treeData.value, toParentId) : null
    const toParentName = toParentNode ? toParentNode.name : '根节点'
    
    // 显示确认对话框
    await ElMessageBox.confirm(
      `确定要将 "${fromNode.name}" 移动到 "${toParentName}" 下吗？`,
      '移动确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    
    if (fromNode.type === 'department') {
      await DepartmentApi.changeDepartmentParent({
        companyId: companyId.value,
        nodeId: fromNode.id,
        parentId: toParentId
      })
    } else {
      await EmployeeApi.changeEmployeeParent({
        companyId: companyId.value,
        nodeId: fromNode.id,
        parentId: toParentId
      })
    }

    emit('tree-updated')
    fetchCompanyTree() // 刷新树
  } catch (error) {
    // 用户取消操作
  } finally {
    loading.value = false
  }
}

/**
 * 处理节点更新
 */
const handleUpdateNode = async (node: TreeNode) => {
  if (node.type === 'employee') {
    // 打开编辑对话框前先获取员工详情
    await fetchEmployeeDetail(node.id)

    if (employeeDetail.value) {
      // 从employeeList中获取userId（EmployeeDetail不包含userId）
      const employeeInfo = employeeList.value.find(emp => emp.id === node.id)
      
      employeeForm.value = {
        userId: employeeInfo?.userId || '',
        name: employeeDetail.value.name,
        departmentId: employeeDetail.value.departmentId || '',
        departmentName: employeeDetail.value.departmentName || '',
        status: employeeDetail.value.status as EmployeeStatus
      }
      dialogMode.value = 'edit'
      dialogTitle.value = '编辑员工信息'
      dialogVisible.value = true
    }
  } else {
    // 如果是部门，弹出编辑对话框
    try {
      const { value: name } = await ElMessageBox.prompt(
          '请输入部门名称',
          '编辑部门',
          {
            inputValue: node.name,
            confirmButtonText: '确定',
            cancelButtonText: '取消'
          }
      )

      await DepartmentApi.updateDepartment({
        companyId: companyId.value,
        depId: node.id,
        name
      })

      emit('tree-updated')
      fetchCompanyTree() // 刷新树
    } catch (error) {
      // 用户取消操作
    }
  }
}

/**
 * 处理添加/编辑员工提交
 */
const handleSubmitEmployee = async () => {
  if (dialogMode.value === 'add') {
    // 添加员工
    await EmployeeApi.addEmployee({
      userId: employeeForm.value.userId,
      name: employeeForm.value.name,
      companyId: companyId.value,
      departmentNodeId: employeeForm.value.departmentId,
      status: employeeForm.value.status,
      operatorEmpId: 'current_user_id' // 实际项目中应从store获取
    })
  } else {
    // 更新员工
    await EmployeeApi.updateEmployee({
      companyId: companyId.value,
      employeeId: currentNode.value.id,
      name: employeeForm.value.name,
      status: employeeForm.value.status
    })
  }

  dialogVisible.value = false
  resetEmployeeForm()

  // 刷新数据
  emit('tree-updated')
  fetchCompanyTree()
  fetchEmployeeList()
}

/**
 * 重置员工表单
 */
const resetEmployeeForm = () => {
  employeeForm.value = {
    userId: '',
    name: '',
    departmentId: '',
    departmentName: '',
    status: EmployeeStatus.ACTIVE
  }
}

/**
 * 处理查询参数变更
 */
const handleQueryChange = () => {
  employeeQueryParams.value.pageNum = 1
  fetchEmployeeList()
}

/**
 * 处理页码变更
 */
const handlePageChange = (page: number) => {
  employeeQueryParams.value.pageNum = page
  fetchEmployeeList()
}

/**
 * 处理每页数量变更
 */
const handleSizeChange = (size: number) => {
  employeeQueryParams.value.pageSize = size
  employeeQueryParams.value.pageNum = 1
  fetchEmployeeList()
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status: EmployeeStatus) => {
  switch (status) {
    case 'active': return 'success'
    case 'leave': return 'warning'
    case 'banned': return 'danger'
    default: return 'info'
  }
}

/**
 * 获取状态显示文本
 */
const getStatusText = (status: EmployeeStatus) => {
  switch (status) {
    case 'active': return '在职'
    case 'leave': return '休假'
    case 'banned': return '停职'
    default: return status
  }
}

/**
 * 清空部门筛选
 */
const clearDepartmentFilter = () => {
  employeeQueryParams.value.departmentId = ''
  fetchEmployeeList()
}



/**
 * 处理添加任务
 */
const handleAddMission = () => {
  missionDialogMode.value = 'add'
  missionDialogTitle.value = '添加任务'
  
  // 重置表单
  missionForm.value = {
    id: '',
    name: '',
    description: '',
    startTime: '',
    endTime: '',
    status: 'not_started' as MissionStatus,
    departmentId: employeeDetail.value?.departmentId || '',
    employeeId: employeeDetail.value?.id || ''
  }
  
  missionDialogVisible.value = true
}

/**
 * 处理AI分配任务
 */
const handleAddMissionByAI = async () => {
  const aiRequest = {
    companyId: companyId.value,
    departmentId: employeeDetail.value?.departmentId || '',
    employeeId: employeeDetail.value?.id || ''
  }
  
  const aiResponse = await MissionApi.addMissionAI(aiRequest)
  
  // 填充AI生成的数据到表单
  missionDialogMode.value = 'add'
  missionDialogTitle.value = 'AI分配任务'
  
  missionForm.value = {
    id: '',
    name: aiResponse.name || '',
    description: aiResponse.description || '',
    startTime: aiResponse.startTime || '',
    endTime: aiResponse.endTime || '',
    status: (aiResponse.status || 'not_started') as MissionStatus,
    departmentId: employeeDetail.value?.departmentId || '',
    employeeId: employeeDetail.value?.id || ''
  }
  
  missionDialogVisible.value = true
}

/**
 * 处理编辑任务
 */
const handleEditMission = (mission: MissionInfo) => {
  missionDialogMode.value = 'edit'
  missionDialogTitle.value = '编辑任务'
  
  // 填充表单
  missionForm.value = {
    id: mission.id,
    name: mission.name,
    description: mission.description || '',
    startTime: mission.startTime,
    endTime: mission.endTime,
    status: mission.status,
    departmentId: mission.departmentId || '',
    employeeId: employeeDetail.value?.id || ''
  }
  
  missionDialogVisible.value = true
}

/**
 * 处理删除任务
 */
const handleDeleteMission = async (mission: MissionInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务 "${mission.name}" 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (!employeeDetail.value?.id) return
    
    await MissionApi.deleteMission({
      companyId: companyId.value,
      missionIds: [mission.id]
    })
    
    await fetchEmployeeMissions(employeeDetail.value.id)
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 处理提交任务表单
 */
const handleSubmitMission = async () => {
  if (!employeeDetail.value?.id) return
  
  if (missionDialogMode.value === 'add') {
    // 添加任务
    const request: AddMissionRequest = {
      name: missionForm.value.name,
      description: missionForm.value.description || undefined,
      startTime: missionForm.value.startTime,
      endTime: missionForm.value.endTime,
      status: missionForm.value.status,
      companyId: companyId.value,
      departmentId: missionForm.value.departmentId || employeeDetail.value.departmentId || '',
      employeeId: employeeDetail.value.id,
      publisherEmpId: 'current_user_id' // 实际项目中应从store获取
    }
    
    await MissionApi.addMission(request)
  } else {
    // 更新任务
    const request: UpdateMissionRequest = {
      companyId: companyId.value,
      missionId: missionForm.value.id,
      name: missionForm.value.name,
      description: missionForm.value.description || undefined,
      startTime: missionForm.value.startTime,
      endTime: missionForm.value.endTime,
      status: missionForm.value.status,
      departmentId: missionForm.value.departmentId || employeeDetail.value.departmentId,
      employeeId: employeeDetail.value.id,
      publisherEmpId: 'current_user_id'
    }
    
    await MissionApi.updateMission(request)
  }
  
  missionDialogVisible.value = false
  await fetchEmployeeMissions(employeeDetail.value.id)
  // 刷新统计信息
  await fetchEmployeeStatistics(employeeDetail.value.id)
}

/**
 * 获取任务状态标签类型
 */
const getMissionStatusTagType = (status: MissionStatus) => {
  switch (status) {
    case 'completed': return 'success'
    case 'in_progress': return 'primary'
    case 'not_started': return 'info'
    case 'paused': return 'warning'
    case 'canceled': return 'info'
    case 'overdue': return 'danger'
    default: return 'info'
  }
}

/**
 * 获取任务状态显示文本
 */
const getMissionStatusText = (status: MissionStatus) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'in_progress': return '进行中'
    case 'not_started': return '未开始'
    case 'paused': return '已暂停'
    case 'canceled': return '已取消'
    case 'overdue': return '已逾期'
    default: return status
  }
}

/**
 * 处理任务查询参数变更
 */
const handleMissionQueryChange = () => {
  missionQueryParams.value.page = 1
  if (employeeDetail.value?.id) {
    fetchEmployeeMissions(employeeDetail.value.id)
  }
}

/**
 * 处理任务页码变更
 */
const handleMissionPageChange = (page: number) => {
  missionQueryParams.value.page = page
  if (employeeDetail.value?.id) {
    fetchEmployeeMissions(employeeDetail.value.id)
  }
}

/**
 * 处理任务每页数量变更
 */
const handleMissionSizeChange = (size: number) => {
  missionQueryParams.value.pageSize = size
  missionQueryParams.value.page = 1
  if (employeeDetail.value?.id) {
    fetchEmployeeMissions(employeeDetail.value.id)
  }
}

// =========================> 时间格式化方法 <=========================

/**
 * 格式化日期时间（完整格式，用于title属性）
 */
const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return ''
  return TimeFormatUtil.getTimeView(new Date(dateTime).toISOString())
}

/**
 * 格式化时间显示（简洁格式，用于显示）
 */
const formatTimeView = (dateTime?: string): string => {
  if (!dateTime) return ''
  return TimeFormatUtil.getTimeView(new Date(dateTime).toISOString())
}

</script>

<template>
  <div class="employee-manager">
    <!-- 树形布局 -->
    <TreeLayout
        :tree-data="treeData"
        :icon-type="iconType"
        :current-node="currentNode"
        :type-notice="typeNotice"
        :type-name="typeName"
        @select="handleNodeSelect"
        @change-position="handleChangePosition"
        @delete="handleDeleteNode"
        @add-middle="handleAddMiddle"
        @add-leaf="handleAddLeaf"
        @update="handleUpdateNode"
    >
      <!-- 右侧内容插槽 -->
      <template #content="{ node }">
        <div class="employee-detail">
          <!-- 员工详情 -->
          <div v-if="node.type === 'employee'" class="detail-card">
            <el-card v-loading="employeeDetailLoading">
              <template #header>
                <div class="card-header">
                  <h3>{{ employeeDetail?.name || node.name }}</h3>
                  <el-button
                      type="primary"
                      :icon="Edit"
                      @click="handleUpdateNode(node)"
                  >
                    编辑
                  </el-button>
                </div>
              </template>

              <div class="detail-content" v-if="employeeDetail">
                <!-- 员工基本信息 -->
                <el-descriptions title="基本信息" :column="2" border>
                  <el-descriptions-item label="员工ID">
                    {{ employeeDetail.id }}
                  </el-descriptions-item>
                  <el-descriptions-item label="姓名">
                    {{ employeeDetail.name }}
                  </el-descriptions-item>
                  <el-descriptions-item label="状态">
                    <el-tag :type="getStatusTagType(employeeDetail.status as EmployeeStatus)">
                      {{ getStatusText(employeeDetail.status as EmployeeStatus) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="所属部门">
                    {{ employeeDetail.departmentName || '未分配' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="创建时间">
                    <span :title="formatDateTime(employeeDetail.createTime)">
                      {{ formatTimeView(employeeDetail.createTime) }}
                    </span>
                  </el-descriptions-item>
                  <el-descriptions-item label="更新时间">
                    <span :title="formatDateTime(employeeDetail.updateTime)">
                      {{ formatTimeView(employeeDetail.updateTime) }}
                    </span>
                  </el-descriptions-item>
                </el-descriptions>

                <!-- 统计信息 -->
                <div v-if="employeeStatistics" class="stats-section">
                  <h4>工作统计</h4>
                  <div class="stats-grid">
                    <el-card class="stat-card">
                      <div class="stat-item">
                        <el-icon><Document /></el-icon>
                        <div class="stat-info">
                          <div class="stat-value">{{ employeeStatistics.totalMissions || 0 }}</div>
                          <div class="stat-label">总任务数</div>
                        </div>
                      </div>
                    </el-card>

                    <el-card class="stat-card">
                      <div class="stat-item">
                        <el-icon><Timer /></el-icon>
                        <div class="stat-info">
                          <div class="stat-value">{{ employeeStatistics.inProgressMissions || 0 }}</div>
                          <div class="stat-label">进行中</div>
                        </div>
                      </div>
                    </el-card>

                    <el-card class="stat-card">
                      <div class="stat-item">
                        <el-icon class="stat-icon-completed"><Timer /></el-icon>
                        <div class="stat-info">
                          <div class="stat-value">{{ employeeStatistics.completedMissionsThisMonth || 0 }}</div>
                          <div class="stat-label">本月完成</div>
                        </div>
                      </div>
                    </el-card>

                    <el-card class="stat-card">
                      <div class="stat-item">
                        <el-icon class="stat-icon-overdue"><Timer /></el-icon>
                        <div class="stat-info">
                          <div class="stat-value">{{ employeeStatistics.overdueMissions || 0 }}</div>
                          <div class="stat-label">逾期任务</div>
                        </div>
                      </div>
                    </el-card>
                  </div>
                </div>

                <!-- 员工任务列表 -->
                <div class="mission-section">
                  <div class="mission-header">
                    <h4>员工任务列表</h4>
                    <el-space>
                      <el-button
                          type="success"
                          :icon="Plus"
                          size="small"
                          @click="handleAddMissionByAI"
                      >
                        AI分配任务
                      </el-button>
                      <el-button
                          type="primary"
                          :icon="Plus"
                          size="small"
                          @click="handleAddMission"
                      >
                        添加任务
                      </el-button>
                    </el-space>
                  </div>
                  
                  <div class="mission-filters">
                    <el-select
                        v-model="missionQueryParams.status"
                        placeholder="任务状态"
                        clearable
                        size="small"
                        style="width: 120px"
                        @change="handleMissionQueryChange"
                    >
                      <el-option label="全部" value="" />
                      <el-option label="未开始" value="not_started" />
                      <el-option label="进行中" value="in_progress" />
                      <el-option label="已完成" value="completed" />
                      <el-option label="已暂停" value="paused" />
                      <el-option label="已取消" value="canceled" />
                      <el-option label="已逾期" value="overdue" />
                    </el-select>
                  </div>
                  
                  <el-table
                      :data="missionList"
                      v-loading="missionLoading"
                      style="width: 100%"
                      size="small"
                  >
                    <el-table-column prop="name" label="任务名称" min-width="150" show-overflow-tooltip />
                    <el-table-column prop="description" label="任务描述" min-width="200" show-overflow-tooltip />
                    <el-table-column prop="status" label="状态" width="100">
                      <template #default="{ row }">
                        <el-tag :type="getMissionStatusTagType(row.status)" size="small">
                          {{ getMissionStatusText(row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="startTime" label="开始时间" width="160">
                      <template #default="{ row }">
                        <span :title="formatDateTime(row.startTime)">
                          {{ formatTimeView(row.startTime) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="endTime" label="结束时间" width="160">
                      <template #default="{ row }">
                        <span :title="formatDateTime(row.endTime)">
                          {{ formatTimeView(row.endTime) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="departmentName" label="所属部门" width="120" />
                    <el-table-column label="操作" width="150" fixed="right">
                      <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            size="small"
                            :icon="Edit"
                            @click="handleEditMission(row)"
                        >
                          编辑
                        </el-button>
                        <el-button
                            type="danger"
                            link
                            size="small"
                            :icon="Delete"
                            @click="handleDeleteMission(row)"
                        >
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  
                  <div class="pagination" v-if="missionTotal > 0">
                    <el-pagination
                        v-model:current-page="missionQueryParams.page"
                        v-model:page-size="missionQueryParams.pageSize"
                        :page-sizes="[5, 10, 20, 50]"
                        :total="missionTotal"
                        layout="total, sizes, prev, pager, next, jumper"
                        size="small"
                        @size-change="handleMissionSizeChange"
                        @current-change="handleMissionPageChange"
                    />
                  </div>
                </div>
              </div>

              <div v-else-if="!employeeDetailLoading" class="empty-state">
                <el-empty description="获取员工详情失败" />
              </div>
            </el-card>
          </div>

          <!-- 部门详情 -->
          <div v-else-if="node.type === 'department'" class="detail-card">
            <el-card>
              <template #header>
                <div class="card-header">
                  <h3>{{ node.name }}</h3>
                  <div class="header-actions">
                    <el-button
                        type="primary"
                        :icon="Plus"
                        @click="handleAddLeaf(node.id)"
                    >
                      添加员工
                    </el-button>
                    <el-button
                        :icon="Edit"
                        @click="handleUpdateNode(node)"
                    >
                      编辑
                    </el-button>
                  </div>
                </div>
              </template>

              <div class="detail-content">
                <el-descriptions title="部门信息" :column="1" border>
                  <el-descriptions-item label="部门ID">
                    {{ node.id }}
                  </el-descriptions-item>
                  <el-descriptions-item label="部门名称">
                    {{ node.name }}
                  </el-descriptions-item>
                </el-descriptions>

                <div class="action-buttons">
                  <el-button
                      type="primary"
                      @click="employeeQueryParams.departmentId = node.id; fetchEmployeeList()"
                  >
                    查看部门员工
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 无选择状态 -->
          <div v-else class="empty-state">
            <el-empty description="请从左侧选择员工或部门查看详情" />
          </div>
        </div>
      </template>
    </TreeLayout>

    <!-- 员工列表区域 -->
    <div class="employee-list-section">
      <el-card>
        <template #header>
          <div class="list-header">
            <h3>员工列表</h3>
            <el-space>
              <el-input
                  v-model="employeeQueryParams.name"
                  placeholder="搜索员工姓名"
                  :prefix-icon="Search"
                  style="width: 200px"
                  @change="handleQueryChange"
                  @keyup.enter="handleQueryChange"
              />
              <el-select
                  v-model="employeeQueryParams.status"
                  placeholder="状态筛选"
                  clearable
                  @change="handleQueryChange"
              >
                <el-option label="全部" value="" />
                <el-option label="在职" value="active" />
                <el-option label="休假" value="leave" />
                <el-option label="停职" value="banned" />
              </el-select>

              <el-button
                  v-if="employeeQueryParams.departmentId"
                  type="info"
                  @click="clearDepartmentFilter"
              >
                清除部门筛选
              </el-button>

              <el-button
                  type="primary"
                  :icon="Plus"
                  @click="handleAddLeaf(null)"
              >
                添加员工
              </el-button>
              <el-button
                  :icon="Refresh"
                  @click="fetchEmployeeList"
              >
                刷新
              </el-button>
            </el-space>
          </div>
        </template>

        <el-table
            :data="employeeList"
            v-loading="employeeLoading"
            style="width: 100%"
        >
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="userId" label="用户ID" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="departmentId" label="部门ID" width="180" />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              <span :title="formatDateTime(row.createTime)">
                {{ formatTimeView(row.createTime) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="180">
            <template #default="{ row }">
              <span :title="formatDateTime(row.updateTime)">
                {{ formatTimeView(row.updateTime) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button
                  type="primary"
                  link
                  :icon="Edit"
                  @click="handleUpdateNode({
                  id: row.id,
                  name: row.name,
                  enable: true,
                  type: 'employee',
                  children: []
                })"
              >
                编辑
              </el-button>
              <el-button
                  type="danger"
                  link
                  :icon="Delete"
                  @click="handleDeleteNode({
                  id: row.id,
                  name: row.name,
                  enable: true,
                  type: 'employee',
                  children: []
                })"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
              v-model:current-page="employeeQueryParams.pageNum"
              v-model:page-size="employeeQueryParams.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="employeeTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 员工表单对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
        destroy-on-close
        @closed="resetEmployeeForm"
    >
      <el-form
          :model="employeeForm"
          label-width="80px"
          :rules="{
          name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
          userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
          departmentId: [{ required: true, message: '部门不能为空', trigger: 'blur' }]
        }"
          ref="formRef"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input
              v-model="employeeForm.userId"
              placeholder="请输入用户ID"
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
              v-model="employeeForm.name"
              placeholder="请输入员工姓名"
          />
        </el-form-item>

        <el-form-item label="部门" prop="departmentId">
          <el-input
              v-model="employeeForm.departmentName"
              placeholder="部门名称"
              style="width: 100%"
              readonly
          />
          <input type="hidden" v-model="employeeForm.departmentId" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select
              v-model="employeeForm.status"
              placeholder="请选择状态"
              style="width: 100%"
          >
            <el-option label="在职" value="active" />
            <el-option label="休假" value="leave" />
            <el-option label="停职" value="banned" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitEmployee">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 任务表单对话框 -->
    <el-dialog
        v-model="missionDialogVisible"
        :title="missionDialogTitle"
        width="600px"
        destroy-on-close
    >
      <el-form
          :model="missionForm"
          label-width="100px"
          :rules="{
          name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
          startTime: [{ required: true, message: '请选择开始时间', trigger: 'blur' }],
          endTime: [{ required: true, message: '请选择结束时间', trigger: 'blur' }]
        }"
          ref="missionFormRef"
      >
        <el-form-item label="任务名称" prop="name">
          <el-input
              v-model="missionForm.name"
              placeholder="请输入任务名称"
          />
        </el-form-item>

        <el-form-item label="任务描述" prop="description">
          <el-input
              v-model="missionForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入任务描述"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
              v-model="missionForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
              value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
              v-model="missionForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
              value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="任务状态" prop="status">
          <el-select
              v-model="missionForm.status"
              placeholder="请选择任务状态"
              style="width: 100%"
          >
            <el-option label="未开始" value="not_started" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已取消" value="canceled" />
            <el-option label="已逾期" value="overdue" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="missionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitMission">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.employee-manager {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.tree-layout) {
  flex: 1;
  margin-bottom: 20px;
}

.employee-list-section {
  margin-top: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.list-header h3 {
  margin: 0;
}

.detail-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.detail-content {
  flex: 1;
}

.stats-section {
  margin-top: 20px;
}

.stats-section h4 {
  margin-bottom: 15px;
  color: #303133;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.mission-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.mission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.mission-header h4 {
  margin: 0;
  color: #303133;
}

.mission-filters {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-item .el-icon {
  font-size: 24px;
  color: #409eff;
}

.stat-icon-completed {
  color: #67c23a;
}

.stat-icon-overdue {
  color: #f56c6c;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    align-items: stretch;
  }

  .list-header h3 {
    margin-bottom: 10px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .card-header h3 {
    margin-bottom: 10px;
  }
}
</style>