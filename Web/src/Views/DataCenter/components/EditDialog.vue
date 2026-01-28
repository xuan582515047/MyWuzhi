<!--
  通用编辑弹窗组件
  功能描述：提供一个通用的表单编辑弹窗，支持动态字段配置和验证
  作者：AI Assistant
  创建时间：2025-01-20
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    :width="width"
    :before-close="handleClose"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-width="labelWidth"
      :label-position="labelPosition"
    >
      <!-- 动态渲染表单字段 -->
      <template v-for="field in fields" :key="field.key">
        <!-- 输入框 -->
        <el-form-item
          v-if="field.type === 'input'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-input
            v-model="formData[field.key]"
            :placeholder="field.placeholder"
            :maxlength="field.maxLength"
            :show-word-limit="field.showWordLimit"
            :disabled="field.disabled"
            :clearable="field.clearable"
            @input="handleFieldChange(field.key, $event)"
          />
        </el-form-item>

        <!-- 文本域 -->
        <el-form-item
          v-else-if="field.type === 'textarea'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-input
            v-model="formData[field.key]"
            type="textarea"
            :rows="field.rows || 4"
            :placeholder="field.placeholder"
            :maxlength="field.maxLength"
            :show-word-limit="field.showWordLimit"
            :disabled="field.disabled"
            :resize="field.resize"
            @input="handleFieldChange(field.key, $event)"
          />
        </el-form-item>

        <!-- 数字输入框 -->
        <el-form-item
          v-else-if="field.type === 'number'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-input-number
            v-model="formData[field.key]"
            :min="field.min"
            :max="field.max"
            :precision="field.precision"
            :step="field.step"
            :disabled="field.disabled"
            :placeholder="field.placeholder"
            style="width: 100%"
            @change="handleFieldChange(field.key, $event)"
          />
        </el-form-item>

        <!-- 选择框 -->
        <el-form-item
          v-else-if="field.type === 'select'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-select
            v-model="formData[field.key]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :clearable="field.clearable"
            :filterable="field.filterable"
            :multiple="field.multiple"
            style="width: 100%"
            @change="handleFieldChange(field.key, $event)"
          >
            <el-option
              v-for="option in field.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
              :disabled="option.disabled"
            >
              <div v-if="option.customRender" v-html="option.customRender" />
              <span v-else>{{ option.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 单选框组 -->
        <el-form-item
          v-else-if="field.type === 'radio'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-radio-group
            v-model="formData[field.key]"
            :disabled="field.disabled"
            @change="handleFieldChange(field.key, $event)"
          >
            <el-radio
              v-for="option in field.options"
              :key="option.value"
              :label="option.value"
              :disabled="option.disabled"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 多选框组 -->
        <el-form-item
          v-else-if="field.type === 'checkbox'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-checkbox-group
            v-model="formData[field.key]"
            :disabled="field.disabled"
            @change="handleFieldChange(field.key, $event)"
          >
            <el-checkbox
              v-for="option in field.options"
              :key="option.value"
              :label="option.value"
              :disabled="option.disabled"
            >
              {{ option.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 开关 -->
        <el-form-item
          v-else-if="field.type === 'switch'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-switch
            v-model="formData[field.key]"
            :disabled="field.disabled"
            :active-text="field.activeText"
            :inactive-text="field.inactiveText"
            @change="handleFieldChange(field.key, $event)"
          />
        </el-form-item>

        <!-- 日期选择器 -->
        <el-form-item
          v-else-if="field.type === 'date'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-date-picker
            v-model="formData[field.key]"
            :type="field.dateType || 'date'"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :clearable="field.clearable"
            style="width: 100%"
            @change="handleFieldChange(field.key, $event)"
          />
        </el-form-item>

        <!-- 文件上传 -->
        <el-form-item
          v-else-if="field.type === 'upload'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <el-upload
            :action="field.uploadUrl"
            :headers="field.uploadHeaders"
            :data="field.uploadData"
            :before-upload="field.beforeUpload"
            :on-success="(response: any) => handleUploadSuccess(field.key, response)"
            :on-error="field.onError"
            :show-file-list="field.showFileList !== false"
            :accept="field.accept"
            :multiple="field.multiple"
            :limit="field.limit"
            :disabled="field.disabled"
            style="width: 100%"
          >
            <el-button type="primary" :disabled="field.disabled">
              <el-icon><Upload /></el-icon>
              {{ field.uploadText || '点击上传' }}
            </el-button>
            <template #tip v-if="field.tip">
              <div class="el-upload__tip">{{ field.tip }}</div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 富文本编辑器 -->
        <el-form-item
          v-else-if="field.type === 'editor'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <div class="editor-container">
            <div
              ref="editorRef"
              class="editor-content"
              :contenteditable="!field.disabled"
              @input="handleEditorChange(field.key, $event)"
            ></div>
          </div>
        </el-form-item>

        <!-- 自定义插槽 -->
        <el-form-item
          v-else-if="field.type === 'slot'"
          :label="field.label"
          :prop="field.key"
          :required="field.required"
        >
          <slot
            :name="field.slotName || field.key"
            :field="field"
            :value="formData[field.key]"
            :onChange="(value: any) => handleFieldChange(field.key, value)"
          />
        </el-form-item>
      </template>
    </el-form>

    <!-- 自定义底部内容 -->
    <div v-if="$slots.footer" class="custom-footer">
      <slot name="footer" :formData="formData" :validate="validateForm" />
    </div>

    <template #footer>
      <div v-if="!$slots.footer" class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="loading"
          @click="handleSubmit"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

// 字段配置接口
interface FieldOption {
  label: string
  value: any
  disabled?: boolean
  customRender?: string
}

interface FieldConfig {
  key: string
  label: string
  type: 'input' | 'textarea' | 'number' | 'select' | 'radio' | 'checkbox' | 'switch' | 'date' | 'upload' | 'editor' | 'slot'
  required?: boolean
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  
  // 输入框相关
  maxLength?: number
  showWordLimit?: boolean
  resize?: 'none' | 'both' | 'horizontal' | 'vertical'
  
  // 数字输入框相关
  min?: number
  max?: number
  precision?: number
  step?: number
  
  // 选择框相关
  options?: FieldOption[]
  filterable?: boolean
  multiple?: boolean
  
  // 开关相关
  activeText?: string
  inactiveText?: string
  
  // 日期选择器相关
  dateType?: 'date' | 'datetime' | 'daterange' | 'datetimerange'
  
  // 文件上传相关
  uploadUrl?: string
  uploadHeaders?: Record<string, string>
  uploadData?: Record<string, any>
  beforeUpload?: (file: File) => boolean
  onError?: (error: any) => void
  showFileList?: boolean
  accept?: string
  limit?: number
  uploadText?: string
  tip?: string
  
  // 文本域相关
  rows?: number
  
  // 插槽相关
  slotName?: string
  
  // 验证规则
  rules?: any[]
}

// Props
interface Props {
  visible: boolean
  title?: string
  width?: string
  fields: FieldConfig[]
  formData?: Record<string, any>
  labelWidth?: string
  labelPosition?: 'left' | 'right' | 'top'
  confirmText?: string
  loading?: boolean
  beforeClose?: () => boolean | Promise<boolean>
}

const props = withDefaults(defineProps<Props>(), {
  width: '600px',
  labelWidth: '100px',
  labelPosition: 'right',
  confirmText: '确定'
})

// Emits
const emit = defineEmits<{
  'update:visible': [visible: boolean]
  'submit': [formData: Record<string, any>]
  'cancel': []
  'fieldChange': [field: string, value: any]
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const editorRef = ref<HTMLElement[]>([])
const internalFormData = ref<Record<string, any>>({})

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogTitle = computed(() => {
  return props.title || '编辑'
})

const formData = computed({
  get: () => ({ ...internalFormData.value, ...(props.formData || {}) }),
  set: (value) => {
    internalFormData.value = value
  }
})

// 生成表单验证规则
const formRules = computed(() => {
  const rules: FormRules = {}
  
  props.fields.forEach(field => {
    if (field.required || field.rules) {
      rules[field.key] = [
        ...(field.required ? [{ required: true, message: `请输入${field.label}`, trigger: 'blur' }] : []),
        ...(field.rules || [])
      ]
    }
  })
  
  return rules
})

/**
 * 初始化表单数据
 */
const initFormData = () => {
  const data: Record<string, any> = {}
  
  props.fields.forEach(field => {
    if (field.type === 'checkbox' || field.type === 'switch') {
      data[field.key] = field.multiple ? [] : false
    } else if (field.type === 'number') {
      data[field.key] = field.min || 0
    } else {
      data[field.key] = ''
    }
  })
  
  // 合并传入的数据
  Object.assign(data, props.formData || {})
  internalFormData.value = data
}

/**
 * 字段变化处理
 */
const handleFieldChange = (field: string, value: any) => {
  formData.value[field] = value
  emit('fieldChange', field, value)
}

/**
 * 富文本编辑器变化
 */
const handleEditorChange = (field: string, event: Event) => {
  const target = event.target as HTMLElement
  formData.value[field] = target.innerHTML
  emit('fieldChange', field, target.innerHTML)
}

/**
 * 文件上传成功
 */
const handleUploadSuccess = (field: string, response: any) => {
  if (response.code === 200) {
    formData.value[field] = response.data.url
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

/**
 * 表单验证
 */
const validateForm = async (): Promise<boolean> => {
  if (!formRef.value) return false
  
  try {
    await formRef.value.validate()
    return true
  } catch (error) {
    return false
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const isValid = await validateForm()
  if (!isValid) return
  
  emit('submit', { ...formData.value })
}

/**
 * 关闭弹窗
 */
const handleClose = async () => {
  if (props.beforeClose) {
    const canClose = await props.beforeClose()
    if (!canClose) return
  }
  
  dialogVisible.value = false
  emit('cancel')
}

/**
 * 初始化富文本编辑器
 */
const initEditor = async () => {
  await nextTick()
  
  const editorFields = props.fields.filter(field => field.type === 'editor')
  if (editorFields.length === 0) return
  
  // 这里可以集成富文本编辑器库，如 Quill、TinyMCE 等
  editorFields.forEach((field, index) => {
    const editor = editorRef.value[index]
    if (editor && formData.value[field.key]) {
      editor.innerHTML = formData.value[field.key]
    }
  })
}

// 监听弹窗显示状态
watch(() => props.visible, (visible) => {
  if (visible) {
    initFormData()
    initEditor()
  }
})

// 监听外部数据变化
watch(() => props.formData, (data) => {
  if (data) {
    Object.assign(internalFormData.value, data)
  }
}, { deep: true })
</script>

<style scoped>
.custom-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.editor-container {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.editor-container .editor-content {
  min-height: 200px;
  padding: 12px;
  outline: none;
}

.editor-container .editor-content:focus {
  border-color: var(--el-color-primary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 深色主题适配 */
.dark .editor-container {
  border-color: var(--el-border-color);
}
</style>