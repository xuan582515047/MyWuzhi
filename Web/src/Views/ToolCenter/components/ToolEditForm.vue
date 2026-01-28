<template>
  <div class="tool-edit-form">
    <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        size="default"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <h3>基本信息</h3>
        <el-form-item label="工具名称" prop="name">
          <el-input
              v-model="formData.name"
              placeholder="请输入工具名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="工具类型" prop="type">
          <el-select
              v-model="formData.type"
              placeholder="请选择工具类型"
              style="width: 100%"
          >
            <el-option
                v-for="type in toolTypes"
                :key="type.value"
                :label="type.name"
                :value="type.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="工具描述" prop="description">
          <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入工具描述"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>
      </div>

      <!-- 价格信息 -->
      <div class="form-section">
        <h3>价格信息</h3>
        <el-form-item label="价格" prop="price">
          <el-input
              v-model="formData.price"
              placeholder="请输入价格，免费工具请输入 0"
              type="number"
              min="0"
              step="0.01"
          >
            <template #prefix>¥</template>
          </el-input>
          <div class="form-tip">
            免费工具请输入 0，付费工具请输入具体金额（如：99.99）
          </div>
        </el-form-item>
      </div>

      <!-- 工具配置 -->
      <div class="form-section">
        <h3>工具配置</h3>
        <el-form-item label="访问URL" prop="url">
          <el-input
              v-model="formData.url"
              placeholder="请输入工具访问URL，例如：https://example.com/tool"
          />
        </el-form-item>

        <el-form-item label="版本号" prop="version">
          <el-input
              v-model="formData.version"
              placeholder="请输入版本号，如 1.0.0"
          />
        </el-form-item>

        <el-form-item label="标签" prop="tagIds">
          <el-select
              v-model="formData.tagIds"
              multiple
              placeholder="请选择标签"
              style="width: 100%"
          >
            <el-option
                v-for="tag in tagList"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词" prop="keywords">
          <el-input
              v-model="keywordsInput"
              placeholder="请输入关键词，多个关键词用逗号分隔"
              @blur="handleKeywordsBlur"
          />
          <div class="form-tip">
            多个关键词用英文逗号分隔，如：AI,数据处理,自动化
          </div>
        </el-form-item>
      </div>
    </el-form>

    <!-- 表单操作按钮 -->
    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '更新工具' : '创建工具' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { ToolInfo, TagInfo, ConstantItem } from '@/Entity/ToolEntity'

// Props
interface Props {
  tool?: ToolInfo
  toolTypes?: ConstantItem[]
  tagList?: TagInfo[]
}

const props = withDefaults(defineProps<Props>(), {
  tool: undefined,
  toolTypes: () => [],
  tagList: () => []
})

// Emits
const emit = defineEmits<{
  submit: [formData: any]
  cancel: []
}>()

// =========================> 响应式数据 <=========================
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 是否为编辑模式 */
const isEdit = computed(() => !!props.tool?.id)

/** 提交状态 */
const submitting = ref(false)

/** 工具类型选项 */
const toolTypes = computed(() => {
  return props.toolTypes.length > 0
      ? props.toolTypes
      : [
        { name: '外部链接', value: 'external_link', description: '纯粹的iframe工具，必须免费' },
        { name: '内部链接', value: 'internal_link', description: 'iframe工具，会与平台页面交互' },
        { name: 'MCP服务器', value: 'mcp_server', description: 'MCP协议工具' }
      ]
})

/** 表单数据 */
const formData = reactive({
  id: '',
  name: '',
  description: '',
  price: '0',
  url: '',
  version: '',
  type: '',
  tagIds: [] as string[],
  keywords: [] as string[]
})

/** 关键词输入字符串 */
const keywordsInput = ref('')

/** 表单验证规则 */
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入工具名称', trigger: 'blur' },
    { min: 2, max: 50, message: '工具名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入工具描述', trigger: 'blur' },
    { min: 10, max: 500, message: '工具描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择工具类型', trigger: 'change' }
  ],
  url: [
    { required: true, message: '请输入工具URL', trigger: 'blur' },
    {
      pattern: /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/,
      message: '请输入有效的URL地址',
      trigger: 'blur'
    }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    {
      pattern: /^\d+(\.\d{1,2})?$/,
      message: '价格格式不正确，最多支持两位小数',
      trigger: 'blur'
    }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
    {
      pattern: /^\d+(\.\d+)*$/,
      message: '版本号格式不正确，如：1.0.0',
      trigger: 'blur'
    }
  ]
}

// =========================> 方法定义 <=========================
/**
 * 初始化表单数据
 */
const initFormData = () => {
  if (props.tool) {
    // 编辑模式 - 后端返回的是数组
    Object.assign(formData, {
      id: props.tool.id,
      name: props.tool.name,
      description: props.tool.description,
      price: props.tool.price,
      url: props.tool.url,
      version: props.tool.version,
      type: props.tool.type,
      tagIds: props.tool.tagIds || [],
      keywords: props.tool.keywords || []
    })
    // 将数组转换为输入框显示的逗号分隔格式
    keywordsInput.value = props.tool.keywords ? props.tool.keywords.join(', ') : ''
  } else {
    // 新增模式
    Object.assign(formData, {
      id: '',
      name: '',
      description: '',
      price: '0',
      url: '',
      version: '1.0.0',
      type: '',
      tagIds: [],
      keywords: []
    })
    keywordsInput.value = ''
  }
}

/**
 * 处理关键词输入变化
 */
const handleKeywordsBlur = () => {
  const keywords = keywordsInput.value
      .split(',')
      .map(keyword => keyword.trim())
      .filter(keyword => keyword !== '')
  formData.keywords = keywords  // 直接赋值数组
}

/**
 * 处理表单提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 处理关键词
    handleKeywordsBlur()

    // 构建提交数据
    const submitData = {
      ...formData,
      // 确保价格是字符串
      price: String(formData.price)
    }

    // 如果是编辑模式，确保包含id
    if (isEdit.value) {
      submitData.id = formData.id
    }

    emit('submit', submitData)
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('请检查表单输入')
  } finally {
    submitting.value = false
  }
}

/**
 * 处理取消
 */
const handleCancel = () => {
  emit('cancel')
}

// =========================> 监听器 <=========================
watch(
    () => props.tool,
    (newTool) => {
      if (newTool) {
        initFormData()
      }
    },
    { immediate: true }
)

watch(
    () => props.tagList,
    () => {
      // 重新初始化时可能需要更新标签列表
    }
)
</script>

<style scoped>
.tool-edit-form {
  padding: 0;
}

.form-section {
  margin-bottom: 32px;
}

.form-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  padding-bottom: 8px;
  border-bottom: 2px solid var(--el-color-primary);
}

.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
  margin-top: 32px;
}

/* 表单项样式调整 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--el-text-color-regular);
}

:deep(.el-textarea__inner) {
  resize: vertical;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-actions {
    justify-content: center;
  }
}
</style>