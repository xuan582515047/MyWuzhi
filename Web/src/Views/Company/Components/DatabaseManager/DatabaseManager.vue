<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElButton, ElMessage, ElMessageBox } from 'element-plus'
import DatabaseTree from './components/DatabaseTree.vue'
import DatabaseDetail from './components/DatabaseDetail.vue'
import TreeLayout from '@/Views/PublicComponents/TreeLayout.vue'
import type { DatabaseNode, AddDatabaseRequest, AddDatabaseNodeRequest } from '@/Entity/DatabaseEntity.ts'
import { DatabaseApi } from '@/Api/DatabaseApi.ts'
import { Plus, Refresh, Folder, Document, UploadFilled } from '@element-plus/icons-vue'

// =========================> Props <=========================
const props = defineProps({
  companyId: { type: String, required: true },
})

// =========================> 响应式数据 <=========================
/** 当前选中的节点 */
const currentNode = ref<any>({
  id: '',
  name: '',
  enable: true,
  type: 'leaf',
  status: true,
  children: []
})

/** 文件夹弹窗状态 */
const showFolderDialog = ref(false)
const folderParentId = ref<string>('')
const folderParentName = ref<string>('')

/** 数据库弹窗状态 */
const showDatabaseDialog = ref(false)
const databaseParentId = ref<string>('')
const databaseParentName = ref<string>('')

/** 文件夹表单 */
const folderForm = ref({
  name: ''
})

/** 数据库表单 */
const databaseForm = ref<AddDatabaseRequest>({
  name: '',
  type: 'local',
  description: '',
  url: '',
  parentId: '',
  companyId: props.companyId
})

/** 文本内容 */
const textContent = ref('')

/** 文件列表 */
const fileList = ref<File[]>([])

/** 加载状态 */
const loading = ref(false)

/** 树组件引用 */
const treeRef = ref<InstanceType<typeof DatabaseTree>>()

/** 树形数据 */
const treeData = ref<Array<any>>([])

// =========================> 计算属性 <=========================
/** 图标类型配置 */
const iconType = computed(() => ({
  middleNode: Folder,
  leafNode: Document
}))

// =========================> 方法定义 <=========================
/**
 * 节点点击事件处理
 */
const handleNodeClick = (node: any) => {
  currentNode.value = node
}

/**
 * 打开根文件夹弹窗
 */
const handleAddRootFolder = () => {
  folderParentId.value = ''
  folderParentName.value = ''
  showFolderDialog.value = true
}

/**
 * 打开根数据库弹窗
 */
const handleAddRootDatabase = () => {
  databaseParentId.value = ''
  databaseParentName.value = ''
  showDatabaseDialog.value = true
}

/**
 * 打开添加子文件夹弹窗
 */
const handleAddMiddleNode = (parentId: string, parentName: string) => {
  folderParentId.value = parentId
  folderParentName.value = parentName
  showFolderDialog.value = true
}

/**
 * 处理添加叶子节点（从 TreeLayout 触发）
 */
const handleAddLeaf = (parentId: string | null) => {
  // 找到父节点名称
  const parentNode = parentId ? findNodeById(treeData.value, parentId) : null
  const parentName = parentNode ? parentNode.name : ''
  
  databaseParentId.value = parentId || ''
  databaseParentName.value = parentName
  showDatabaseDialog.value = true
}

/**
 * 文件上传前的处理
 */
const handleBeforeUpload = (file: File) => {
  fileList.value = [file]
  return false
}

/**
 * 文件选择变化处理
 */
const handleFileChange = (file: any) => {
  fileList.value = [file.raw]
}

/**
 * 文件移除处理
 */
const handleFileRemove = () => {
  fileList.value = []
}

/**
 * 文件数量超出限制时的处理
 */
const handleFileExceed = () => {
  ElMessage.warning('只能上传一个文件，将替换之前的选择')
}

/**
 * 提交文件夹表单
 */
const handleSubmitFolder = async () => {
  if (!folderForm.value.name || folderForm.value.name.trim() === '') {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  loading.value = true
  try {
    const request: AddDatabaseNodeRequest = {
      name: folderForm.value.name,
      parentId: folderParentId.value || '',
      companyId: props.companyId
    }

    await DatabaseApi.addDatabaseNode(request)
    showFolderDialog.value = false
    resetFolderForm()
    treeRef.value?.refreshTree()
  } finally {
    loading.value = false
  }
}

/**
 * 提交数据库表单
 */
const handleSubmitDatabase = async () => {
  if (!databaseForm.value.name || databaseForm.value.name.trim() === '') {
    ElMessage.warning('请输入数据库名称')
    return
  }

  if (!databaseForm.value.type) {
    ElMessage.warning('请选择数据库类型')
    return
  }
  
  if (databaseForm.value.type === 'local' && fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  
  if (databaseForm.value.type === 'url' && !databaseForm.value.url) {
    ElMessage.warning('请输入数据库连接地址')
    return
  }
  
  if (databaseForm.value.type === 'text' && !textContent.value) {
    ElMessage.warning('请输入文本内容')
    return
  }

  loading.value = true
  try {
    const request: AddDatabaseRequest = {
      name: databaseForm.value.name,
      parentId: databaseParentId.value || '',
      type: databaseForm.value.type,
      companyId: props.companyId,
      description: databaseForm.value.description,
      url: databaseForm.value.type === 'url' ? databaseForm.value.url : undefined,
      file: databaseForm.value.type === 'local' && fileList.value.length > 0 ? fileList.value : undefined,
      text: databaseForm.value.type === 'text' ? textContent.value : undefined
    }

    await DatabaseApi.addDatabase(request)
    showDatabaseDialog.value = false
    resetDatabaseForm()
    treeRef.value?.refreshTree()
  } finally {
    loading.value = false
  }
}

/**
 * 重置文件夹表单
 */
const resetFolderForm = () => {
  folderForm.value = { name: '' }
}

/**
 * 重置数据库表单
 */
const resetDatabaseForm = () => {
  databaseForm.value = {
    name: '',
    type: 'local',
    description: '',
    url: '',
    parentId: '',
    companyId: props.companyId
  }
  fileList.value = []
  textContent.value = ''
}

/**
 * 根据ID查找节点
 */
const findNodeById = (nodes: any[], id: string): any => {
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
 * 刷新树
 */
const handleRefresh = () => {
  treeRef.value?.refreshTree()
}

/**
 * 树数据加载完成回调
 */
const handleTreeLoaded = (data: Array<any>) => {
  treeData.value = data
}

/**
 * 处理节点修改
 * 文件夹只重命名，叶子节点可以修改完整信息
 */
const handleUpdateNode = async (node: any) => {
  if (node.type === 'middle') {
    // 文件夹只重命名，使用简单的 prompt 对话框
    try {
      const { value } = await ElMessageBox.prompt('请输入新的文件夹名称', '重命名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: node.name,
        inputPattern: /^.{1,50}$/,
        inputErrorMessage: '名称长度必须在1-50个字符之间'
      })
      
      // 调用重命名 API
      await DatabaseApi.editDatabaseNode({ id: node.id, name: value })
      ElMessage.success('重命名成功')
      // 刷新树
      treeRef.value?.refreshTree()
    } catch (error) {
      // 用户取消操作
    }
  } else {
    // 叶子节点修改完整信息
    databaseParentId.value = node.parentId || ''
    databaseParentName.value = node.parentName || ''
    showDatabaseDialog.value = true
  }
}

/**
 * 处理节点位置变更（拖拽移动）
 */
const handleChangePosition = async (fromNode: any, toParentId: string | null) => {
  try {
    loading.value = true
    
    await DatabaseApi.changeDatabaseParent({
      nodeId: fromNode.id,
      parentId: toParentId,
      companyId: props.companyId
    })
    
    treeRef.value?.refreshTree()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="database-manager">
    <TreeLayout
      :tree-data="treeData"
      :icon-type="iconType"
      :current-node="currentNode"
      :show-status="true"
      :show-root-add-btn="true"
      :root-node-move="true"
      :type-name="{ middle: '文件夹', leaf: '数据' }"
      @select="handleNodeClick"
      @update="handleUpdateNode"
      @add-root="handleAddRootFolder"
      @add-leaf="handleAddLeaf"
      @change-position="handleChangePosition"
      class="full-layout"
    >
      <template #content="{ node }">
        <DatabaseDetail
          :node="node as unknown as DatabaseNode"
          :company-id="props.companyId"
        />
      </template>
    </TreeLayout>

    <!-- 保留 DatabaseTree 组件用于数据加载和管理 -->
    <DatabaseTree
      ref="treeRef"
      :company-id="props.companyId"
      :on-node-click="handleNodeClick"
      :on-add-node="handleAddMiddleNode"
      :on-loaded="handleTreeLoaded"
      class="hidden"
    />

    <!-- 添加文件夹弹窗 -->
    <el-dialog
      v-model="showFolderDialog"
      :title="folderParentName ? `在 ${folderParentName} 下添加文件夹` : '添加文件夹'"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetFolderForm"
    >
      <el-form label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="folderForm.name" placeholder="请输入文件夹名称" />
        </el-form-item>
        <el-form-item label="父节点">
          <el-input v-model="folderParentName" placeholder="无" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFolderDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmitFolder">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加数据库弹窗 -->
    <el-dialog
      v-model="showDatabaseDialog"
      :title="databaseParentName ? `在 ${databaseParentName} 下添加数据库` : '添加数据库'"
      width="600px"
      :close-on-click-modal="false"
      @closed="resetDatabaseForm"
    >
      <el-form label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="databaseForm.name" placeholder="请输入数据库名称" />
        </el-form-item>

        <el-form-item label="父节点">
          <el-input v-model="databaseParentName" placeholder="无" disabled />
        </el-form-item>

        <el-form-item label="数据库类型">
          <el-select v-model="databaseForm.type" placeholder="请选择数据库类型" style="width: 100%">
            <el-option label="本地文件" value="local" />
            <el-option label="远程URL" value="url" />
            <el-option label="文本内容" value="text" />
          </el-select>
        </el-form-item>

        <!-- 本地文件：上传文件 -->
        <el-form-item v-if="databaseForm.type === 'local'" label="上传文件" required>
          <el-upload
            drag
            :auto-upload="false"
            :file-list="fileList.map(f => ({ name: f.name, raw: f }))"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="1"
            :on-exceed="handleFileExceed"
            :before-upload="handleBeforeUpload"
            accept="*/*"
            class="upload-dragger"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">只能上传一个文件</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 远程URL：输入URL -->
        <el-form-item v-if="databaseForm.type === 'url'" label="连接地址" required>
          <el-input v-model="databaseForm.url" placeholder="请输入数据库连接地址" />
        </el-form-item>

        <!-- 文本内容：输入文本 -->
        <el-form-item v-if="databaseForm.type === 'text'" label="文本内容" required>
          <el-input
            v-model="textContent"
            type="textarea"
            :rows="8"
            placeholder="请输入数据库内容"
          />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="databaseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入描述信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDatabaseDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmitDatabase">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.database-manager {
  height: 95vh;
  overflow: hidden;
}

.full-layout {
  height: 100% !important;
}

.hidden {
  display: none;
}

.el-upload__tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>
