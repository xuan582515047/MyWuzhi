<template>
  <el-dialog
      v-model="dialogVisible"
      title="创建企业"
      width="580px"
      :close-on-click-modal="false"
      @close="handleClose"
      class="modern-dialog"
  >
    <!-- 表单内容 -->
    <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        size="large"
        class="company-form"
    >
      <div class="form-content">
        <!-- 公司名称 -->
        <el-form-item label="公司名称" prop="name">
          <el-input
              v-model="formData.name"
              placeholder="请输入公司名称"
              clearable
          />
        </el-form-item>

        <!-- 公司简称 -->
        <el-form-item label="公司简称" prop="shortName">
          <el-input
              v-model="formData.shortName"
              placeholder="请输入公司简称（可选）"
              clearable
              maxlength="20"
              show-word-limit
          />
        </el-form-item>

        <!-- 联系电话 -->
        <el-form-item label="联系电话" prop="phone">
          <el-input
              v-model="formData.phone"
              placeholder="请输入11位手机号码"
              clearable
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="formData.email"
              placeholder="请输入邮箱地址"
              clearable
          />
        </el-form-item>

        <!-- 所属行业 -->
        <el-form-item label="所属行业" prop="industry">
          <el-select
              v-model="formData.industry"
              placeholder="请选择行业"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
          >
            <el-option label="互联网" value="互联网" />
            <el-option label="金融" value="金融" />
            <el-option label="制造业" value="制造业" />
            <el-option label="教育" value="教育" />
            <el-option label="医疗健康" value="医疗健康" />
            <el-option label="房地产" value="房地产" />
            <el-option label="物流运输" value="物流运输" />
            <el-option label="餐饮服务" value="餐饮服务" />
            <el-option label="文化娱乐" value="文化娱乐" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 公司描述 -->
        <el-form-item label="公司描述" prop="description">
          <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入公司描述（可选）"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>
      </div>
    </el-form>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" size="large">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit" size="large">
          创建企业
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, defineProps, defineEmits } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { CompanyAddRequest } from '@/Entity/company/CompanyEntity.ts'

//=======================> 通信定义 <=======================
const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  create: [data: CompanyAddRequest]
  'update:show': [value: boolean]
}>()

//=======================> 变量定义 <=======================
const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive<CompanyAddRequest>({
  name: '',
  shortName: '',
  phone: '',
  email: '',
  industry: '',
  description: ''
})
const formRules = reactive<FormRules<CompanyAddRequest>>({
  name: [
    { required: true, message: '请输入公司名称', trigger: 'blur' },
    { min: 2, max: 50, message: '公司名称长度在2到50个字符', trigger: 'blur' }
  ],
  shortName: [
    { max: 20, message: '公司简称不能超过20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码格式',
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'blur'
    }
  ],
  industry: [
    { required: true, message: '请选择行业', trigger: 'change' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
})

// 监听props.show变化
watch(() => props.show, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    resetForm()
  }
})

// 监听dialogVisible变化并同步到父组件
watch(dialogVisible, (newVal) => {
  emit('update:show', newVal)
})

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    shortName: '',
    phone: '',
    email: '',
    industry: '',
    description: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  loading.value = false
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true

    // 创建请求数据对象
    const requestData: CompanyAddRequest = {
      name: formData.name.trim(),
      phone: formData.phone.trim(),
      email: formData.email.trim(),
      industry: formData.industry.trim()
    }

    // 添加可选字段
    if (formData.shortName?.trim()) {
      requestData.shortName = formData.shortName.trim()
    }

    if (formData.description?.trim()) {
      requestData.description = formData.description.trim()
    }

    // 发送创建事件给父组件
    emit('create', requestData)

    // 重置表单
    resetForm()
    dialogVisible.value = false

  } catch (error) {
    console.error('表单验证失败:', error)
    loading.value = false
  }
}

// 处理取消
const handleCancel = () => {
  resetForm()
  dialogVisible.value = false
}

// 处理关闭
const handleClose = () => {
  handleCancel()
}
</script>

<style scoped>
.modern-dialog {
  max-height: 85vh;
  overflow-y: auto;
}

.form-content {
  padding: 10px 0;
}

.company-form {
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0 0 0;
}

/* 基础样式调整 */
:deep(.el-dialog) {
  background-color: var(--dialog-bg);
  border-radius: 8px;
  box-shadow: var(--dialog-shadow);
  overflow: visible;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  background-color: var(--dialog-header-bg);
  border-bottom: 1px solid var(--dialog-header-border);
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--dialog-title-color);
}

:deep(.el-dialog__body) {
  padding: 0 24px;
  background-color: var(--dialog-body-bg);
  color: var(--text-primary);
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  background-color: var(--dialog-footer-bg);
  border-top: 1px solid var(--dialog-footer-border);
}

/* 表单项样式 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  padding-right: 12px;
  line-height: 32px;
}

/* 输入框样式 */
:deep(.el-input__wrapper) {
  background-color: var(--input-bg);
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  box-shadow: none;
  transition: all 0.2s ease;
  padding: 8px 12px;
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--border-hover-color);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.1);
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
}

/* 文本域样式 */
:deep(.el-textarea__inner) {
  background-color: var(--input-bg);
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
  resize: vertical;
  transition: all 0.2s ease;
  box-shadow: none;
}

:deep(.el-textarea__inner:hover) {
  border-color: var(--border-hover-color);
}

:deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.1);
}

/* 选择器样式 */
:deep(.el-select__wrapper) {
  background-color: var(--input-bg);
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  box-shadow: none;
  transition: all 0.2s ease;
  padding: 8px 12px;
}

:deep(.el-select__wrapper:hover) {
  border-color: var(--border-hover-color);
}

:deep(.el-select__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(124, 58, 237, 0.1);
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
  padding: 8px 16px;
}

:deep(.el-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.el-button--primary:hover) {
  background-color: var(--accent-secondary);
  border-color: var(--accent-secondary);
}

/* 字数统计样式 */
:deep(.el-input__count) {
  background: none;
  color: var(--text-tertiary);
  font-size: 12px;
  right: 8px;
  bottom: 2px;
}

:deep(.el-textarea .el-input__count) {
  bottom: 5px;
  right: 10px;
}

/* 下拉框样式 */
:deep(.el-select-dropdown) {
  background-color: var(--card-bg);
  border: 1px solid var(--border-primary);
  border-radius: 6px;
  box-shadow: var(--shadow-medium);
  padding: 8px 0;
}

:deep(.el-select-dropdown__item) {
  color: var(--text-primary);
  padding: 0 12px;
  height: 36px;
  line-height: 36px;
}

:deep(.el-select-dropdown__item:hover) {
  background-color: var(--hover-bg);
}

:deep(.el-select-dropdown__item.selected) {
  color: var(--primary-color);
  font-weight: 500;
}
</style>