<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, CopyDocument } from '@element-plus/icons-vue'
import type { DatabaseNode, DatabaseDetailResponse } from '@/Entity/DatabaseEntity.ts'
import { DatabaseApi } from '@/Api/DatabaseApi.ts'
import { FILE_BASE_URL } from '@/Config/ApiConfig.ts'
import FilePreview from './FilePreview.vue'

// =========================> Props <=========================
interface Props {
  node?: DatabaseNode | null
  companyId?: string
}

const props = withDefaults(defineProps<Props>(), {
  node: null,
  companyId: undefined
})

// =========================> 响应式数据 <=========================
/** 数据库详情 */
const databaseDetail = ref<DatabaseDetailResponse | null>(null)
/** 加载状态 */
const loading = ref(false)
/** 编辑对话框可见 */
const editDialogVisible = ref(false)
/** 节点编辑对话框可见 */
const nodeEditDialogVisible = ref(false)
/** 编辑表单 */
const editForm = ref({
  name: '',
  description: '',
  type: 'local',
  url: '',
  text: ''
})
/** 节点编辑表单 */
const nodeEditForm = ref({
  name: ''
})

// =========================> 计算属性 <=========================
/** 是否为叶子节点（数据库/知识库，有数据） */
const isLeafNode = computed(() => {
  // 根据后端返回的 type 字段判断
  return props.node?.type === 'leaf'
})

/** 是否为中间节点（文件夹，无数据） */
const isMiddleNode = computed(() => {
  // 根据后端返回的 type 字段判断
  return props.node?.type === 'middle'
})

/** 节点名称 */
const nodeName = computed(() => props.node?.name || '')

/** 预览文件名（从 URL 中提取） */
const previewFileName = computed(() => {
  if (databaseDetail.value?.url) {
    const urlParts = databaseDetail.value.url.split('/')
    return urlParts[urlParts.length - 1] || databaseDetail.value.name
  }
  return databaseDetail.value?.name || ''
})

/** 完整的文件URL */
const fullFileUrl = computed(() => {
  if (databaseDetail.value?.url?.startsWith('/')) {
    return FILE_BASE_URL + databaseDetail.value.url
  }
  return databaseDetail.value?.url || ''
})


// =========================> 方法定义 <=========================
/**
 * 加载数据库详情
 * 只有叶子节点（数据库/知识库）才需要加载详情
 */
const loadDetail = async () => {
  if (!props.node?.id) {
    databaseDetail.value = null
    return
  }

  // 只有叶子节点才加载详情数据
  if (!isLeafNode.value) {
    databaseDetail.value = null
    return
  }

  loading.value = true
  try {
    databaseDetail.value = await DatabaseApi.getDatabaseDetail(props.node.id)
  } finally {
    loading.value = false
  }
}

/**
 * 打开编辑对话框
 */
const openEditDialog = () => {
  if (!props.node) {
    ElMessage.warning('请先选择节点')
    return
  }

  if (isLeafNode.value) {
    // 编辑叶子节点（数据库/知识库）
    if (!databaseDetail.value) {
      ElMessage.warning('数据库详情未加载')
      return
    }
    editForm.value = {
      name: databaseDetail.value.name,
      description: databaseDetail.value.description,
      type: databaseDetail.value.type,
      url: databaseDetail.value.url,
      text: databaseDetail.value.text || ''
    }
    editDialogVisible.value = true
  } else {
    // 编辑中间节点（文件夹）
    nodeEditForm.value = {
      name: nodeName.value
    }
    nodeEditDialogVisible.value = true
  }
}

/**
 * 保存数据库编辑
 */
const handleSaveDatabaseEdit = async () => {
  if (!props.node?.id) {
    ElMessage.warning('节点ID不存在')
    return
  }

  await DatabaseApi.editDatabase({
    id: props.node.id,
    name: editForm.value.name || undefined,
    description: editForm.value.description || undefined,
    type: editForm.value.type || undefined,
    url: editForm.value.url || undefined,
    text: editForm.value.text || undefined
  })

  ElMessage.success('保存成功')
  editDialogVisible.value = false
  await loadDetail()
}

/**
 * 保存节点编辑
 */
const handleSaveNodeEdit = async () => {
  if (!props.node?.id) {
    ElMessage.warning('节点ID不存在')
    return
  }

  if (!nodeEditForm.value.name.trim()) {
    ElMessage.warning('节点名称不能为空')
    return
  }

  await DatabaseApi.editDatabaseNode({
    id: props.node.id,
    name: nodeEditForm.value.name
  })

  ElMessage.success('保存成功')
  nodeEditDialogVisible.value = false
  // 触发父组件刷新树
  props.node.name = nodeEditForm.value.name
}

/**
 * 复制URL
 */
const copyUrl = () => {
  if (databaseDetail.value?.url) {
    navigator.clipboard.writeText(databaseDetail.value.url)
    ElMessage.success('URL已复制到剪贴板')
  }
}

/**
 * 复制ID
 */
const copyId = () => {
  if (props.node?.id) {
    navigator.clipboard.writeText(props.node.id)
    ElMessage.success('ID已复制到剪贴板')
  }
}

/**
 * 复制文本内容
 */
const copyText = () => {
  if (databaseDetail.value?.text) {
    navigator.clipboard.writeText(databaseDetail.value.text)
    ElMessage.success('文本已复制到剪贴板')
  }
}

// =========================> 监听器 <=========================
watch(() => props.node?.id, () => {
  loadDetail()
}, { immediate: true })
</script>

<template>
  <div class="database-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 显示数据库详情 -->
    <div v-else-if="databaseDetail" class="detail-content">
      <div class="detail-header">
        <h2 class="detail-title">{{ databaseDetail.name }}</h2>
        <el-button type="primary" @click="openEditDialog">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
      </div>

      <el-divider />

      <div class="detail-info">
        <div class="info-item">
          <span class="info-label">数据库ID：</span>
          <span class="info-value">{{ databaseDetail.id }}</span>
          <el-button size="small" text @click="copyId" v-if="databaseDetail.id">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </div>

        <div class="info-item">
          <span class="info-label">数据库类型：</span>
          <el-tag>{{ databaseDetail.type }}</el-tag>
        </div>

        <div class="info-item" v-if="databaseDetail.type === 'url' && databaseDetail.url">
          <span class="info-label">连接地址：</span>
          <span class="info-value url-value">{{ databaseDetail.url }}</span>
          <el-button size="small" text @click="copyUrl">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </div>

        <div class="info-item" v-if="databaseDetail.type === 'local' && databaseDetail.url">
          <span class="info-label">文件地址：</span>
          <span class="info-value url-value">{{ databaseDetail.url }}</span>
          <el-button size="small" text @click="copyUrl">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </div>

        <div class="info-item">
          <span class="info-label">描述：</span>
          <p class="info-description">{{ databaseDetail.description || '暂无描述' }}</p>
        </div>
      </div>

      <!-- 文件/内容预览区 -->
      <div v-if="databaseDetail.type === 'local' && databaseDetail.url" class="preview-section">
        <el-divider />
        <div class="preview-header">
          <h3 class="preview-title">文件预览</h3>
        </div>
        <div class="file-preview-wrapper">
          <FilePreview :file-url="fullFileUrl" :file-name="previewFileName" />
        </div>
      </div>

      <div v-if="databaseDetail.type === 'text' && databaseDetail.text" class="preview-section">
        <el-divider />
        <div class="preview-header">
          <h3 class="preview-title">文本预览</h3>
          <el-button size="small" @click="copyText">
            <el-icon><CopyDocument /></el-icon>
            复制文本
          </el-button>
        </div>
        <div class="text-preview-container">
          <pre class="text-content">{{ databaseDetail.text }}</pre>
        </div>
      </div>
    </div>

    <!-- 显示中间节点（文件夹）信息 -->
    <div v-else-if="props.node && isMiddleNode" class="detail-content">
      <div class="detail-header">
        <h2 class="detail-title">{{ props.node.name }}</h2>
        <el-button type="primary" @click="openEditDialog">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
      </div>

      <el-divider />

      <div class="detail-info">
        <div class="info-item">
          <span class="info-label">节点ID：</span>
          <span class="info-value">{{ props.node.id }}</span>
          <el-button size="small" text @click="copyId" v-if="props.node.id">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </div>

        <div class="info-item">
          <span class="info-label">类型：</span>
          <el-tag type="info">文件夹</el-tag>
        </div>

        <div class="info-item">
          <span class="info-label">子节点数：</span>
          <span class="info-value">{{ props.node.children?.length || 0 }} 个</span>
        </div>

        <div class="info-item" v-if="props.node.status !== undefined">
          <span class="info-label">状态：</span>
          <el-tag :type="props.node.status ? 'success' : 'info'">
            {{ props.node.status ? '启用' : '禁用' }}
          </el-tag>
        </div>

        <div class="info-item" v-if="props.node.parentId">
          <span class="info-label">父节点ID：</span>
          <span class="info-value">{{ props.node.parentId }}</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="请从左侧选择数据库节点" />
    </div>

    <!-- 数据库编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑数据库"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="数据库名称">
          <el-input v-model="editForm.name" placeholder="请输入数据库名称" />
        </el-form-item>

        <el-form-item label="数据库类型">
          <el-select v-model="editForm.type" placeholder="请选择数据库类型" style="width: 100%">
            <el-option label="本地文件" value="local" />
            <el-option label="远程URL" value="url" />
            <el-option label="文本内容" value="text" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="editForm.type === 'url'" label="连接地址">
          <el-input v-model="editForm.url" placeholder="请输入数据库连接地址" />
        </el-form-item>

        <el-form-item v-if="editForm.type === 'text'" label="文本内容">
          <el-input
            v-model="editForm.text"
            type="textarea"
            :rows="8"
            placeholder="请输入数据库内容"
          />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入描述信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDatabaseEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 中间节点（文件夹）编辑对话框 -->
    <el-dialog
      v-model="nodeEditDialogVisible"
      title="编辑文件夹"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="节点名称">
          <el-input v-model="nodeEditForm.name" placeholder="请输入节点名称" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="nodeEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveNodeEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.database-detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
  overflow: hidden;
}

.loading-container {
  padding: 24px;
}

.detail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow: hidden;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.detail-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.detail-info {
  flex: none;
  overflow: visible;
  padding-bottom: 0;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.info-label {
  width: 80px;
  flex-shrink: 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.info-value {
  flex: 1;
  color: var(--text-primary);
  font-size: 13px;
  word-break: break-all;
}

.url-value {
  font-family: 'Courier New', monospace;
  color: var(--accent-primary);
}

.info-description {
  flex: 1;
  margin: 0;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.info-text-content {
  flex: 1;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
}

/* ========== 预览区域样式 ========== */
.preview-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  min-height: 300px;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.preview-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.file-preview-wrapper {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  background-color: var(--card-bg);
}

.preview-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

/* ========== 文本预览样式 ========== */
.text-preview-container {
  flex: 1;
  overflow: auto;
  background-color: #282c34;
  border-radius: 4px;
  padding: 16px;
  min-height: 0;
  border: 1px solid var(--border-primary);
}

.text-content {
  margin: 0;
  padding: 0;
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #abb2bf;
  white-space: pre-wrap;
  word-break: break-all;
}

.text-preview-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.text-preview-container::-webkit-scrollbar-track {
  background: var(--border-primary);
}

.text-preview-container::-webkit-scrollbar-thumb {
  background: #909399;
  border-radius: 4px;
}

.text-preview-container::-webkit-scrollbar-thumb:hover {
  background: #606266;
}
</style>
