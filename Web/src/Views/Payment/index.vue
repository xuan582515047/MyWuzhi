<template>
  <div class="payment-container">
    <!-- 账户信息卡片 -->
    <el-card class="account-card">
      <template #header>
        <div class="card-header">
          <span>账户信息</span>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">
          <el-tag type="info" size="large">{{ userStore.name || '未登录' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账户余额">
          <el-tag type="success" size="large" effect="dark">
            ¥ {{ formatCurrency(userStore.balance) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="recharge-button-container">
        <el-button 
          type="primary" 
          size="large" 
          @click="openRechargeDialog"
          :disabled="!userStore.name"
        >
          <el-icon><Wallet /></el-icon>
          立即充值
        </el-button>
      </div>
    </el-card>

    <!-- 充值记录卡片 -->
    <el-card class="records-card">
      <template #header>
        <div class="card-header">
          <span>充值记录</span>
        </div>
      </template>
      
      <el-table :data="rechargeRecords" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="交易单号" width="180" />
        <el-table-column label="充值金额" width="120">
          <template #default="scope">
            <span style="color: #67c23a; font-weight: bold">
              +¥ {{ formatCurrency(scope.row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="120">
          <template #default="scope">
            <el-tag :type="getPaymentMethodType(scope.row.paymentMethod)">
              {{ getPaymentMethodText(scope.row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="充值时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog
      v-model="rechargeDialogVisible"
      title="个人充值"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="rechargeFormRef"
        :model="rechargeForm"
        :rules="rechargeRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="充值金额" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :max="10000"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%"
            placeholder="请输入充值金额"
          />
          <div class="amount-tips">
            <el-text type="info" size="small">最低充值 1 元，最高 10,000 元</el-text>
          </div>
        </el-form-item>
        
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="rechargeForm.paymentMethod" size="large">
            <el-radio-button label="bank">
              <el-icon><CreditCard /></el-icon>
              银行卡
            </el-radio-button>
            <el-radio-button label="wechat">
              <el-icon><ChatDotRound /></el-icon>
              微信
            </el-radio-button>
            <el-radio-button label="alipay">
              <el-icon><Wallet /></el-icon>
              支付宝
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleRecharge"
            :loading="rechargeLoading"
          >
            确认充值
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'  // 添加 nextTick 导入
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/Stores/UserStore'
import { GetBalance, Recharge, GetRecords } from '@/Api/PaymentApi'
import type { RechargeRecord, RechargeRequest } from '@/Entity/PaymentEntity'
import { Wallet, CreditCard, ChatDotRound } from '@element-plus/icons-vue'

const userStore = useUserStore()

// 充值记录
const rechargeRecords = ref<RechargeRecord[]>([])
const loading = ref(false)

// 充值对话框
const rechargeDialogVisible = ref(false)
const rechargeLoading = ref(false)
const rechargeFormRef = ref()

// 充值表单
const rechargeForm = reactive<RechargeRequest>({
  amount: 100,
  paymentMethod: 'bank'
})

// 表单验证规则
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 1, message: '最低充值金额为 1 元', trigger: 'blur' },
    { type: 'number', max: 10000, message: '最高充值金额为 10,000 元', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 格式化金额
const formatCurrency = (amount: number): string => {
  return amount.toFixed(2)
}

// 获取支付方式文本
const getPaymentMethodText = (method: string): string => {
  const map: Record<string, string> = {
    bank: '银行卡',
    wechat: '微信',
    alipay: '支付宝'
  }
  return map[method] || method
}

// 获取支付方式标签类型
const getPaymentMethodType = (method: string): string => {
  const map: Record<string, string> = {
    bank: 'warning',
    wechat: 'success',
    alipay: 'primary'
  }
  return map[method] || 'info'
}

// 获取状态文本
const getStatusText = (status: string): string => {
  const map: Record<string, string> = {
    pending: '处理中',
    completed: '已完成',
    failed: '失败'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusType = (status: string): string => {
  const map: Record<string, string> = {
    pending: 'warning',
    completed: 'success',
    failed: 'danger'
  }
  return map[status] || 'info'
}

// 打开充值对话框
const openRechargeDialog = () => {
  rechargeForm.amount = 100
  rechargeForm.paymentMethod = 'bank'
  rechargeDialogVisible.value = true

  // 重置表单验证 - 使用已导入的 nextTick
  nextTick(() => {
    rechargeFormRef.value?.clearValidate()
  })
}

// 处理充值
const handleRecharge = async () => {
  if (!rechargeFormRef.value) return

  try {
    await rechargeFormRef.value.validate()

    // 确认充值
    await ElMessageBox.confirm(
        `确定要充值 ¥${formatCurrency(rechargeForm.amount)} 吗？`,
        '确认充值',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    rechargeLoading.value = true

    // 调用充值接口
    const response = await Recharge(rechargeForm)

    if (response && response.success) {
      ElMessage.success(`充值成功！当前余额：¥${formatCurrency(response.newBalance)}`)

      // 更新用户余额
      userStore.updateBalance(response.newBalance)

      // 关闭对话框
      rechargeDialogVisible.value = false

      // 刷新充值记录
      await loadRechargeRecords()
    } else {
      ElMessage.error(response?.message || '充值失败，请稍后重试')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('充值失败:', error)
      ElMessage.error('充值失败，请稍后重试')
    }
  } finally {
    rechargeLoading.value = false
  }
}

// 加载账户余额
const loadBalance = async () => {
  try {
    const balanceInfo = await GetBalance()
    if (balanceInfo) {
      userStore.updateBalance(balanceInfo.balance)
    }
  } catch (error) {
    console.error('加载余额失败:', error)
  }
}

// 加载充值记录
const loadRechargeRecords = async () => {
  loading.value = true
  try {
    const records = await GetRecords()
    rechargeRecords.value = records
  } catch (error) {
    console.error('加载充值记录失败:', error)
    ElMessage.error('加载充值记录失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化数据
onMounted(async () => {
  await Promise.all([
    loadBalance(),
    loadRechargeRecords()
  ])
})
</script>

<!-- 只保留 name 导出 -->
<script lang="ts">
export default {
  name: 'PaymentPage'
}
</script>

<style scoped>
.payment-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.account-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.recharge-button-container {
  margin-top: 20px;
  text-align: center;
}

.amount-tips {
  margin-top: 8px;
}

:deep(.el-input-number) .el-input__wrapper {
  padding: 0;
}

:deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .payment-container {
    padding: 10px;
  }
  
  :deep(.el-descriptions) {
    font-size: 14px;
  }
  
  :deep(.el-radio-group) {
    display: flex;
    flex-direction: column;
    width: 100%;
  }
  
  :deep(.el-radio-button) {
    width: 100%;
    margin-bottom: 8px;
  }
  
  :deep(.el-radio-button__inner) {
    width: 100%;
    justify-content: center;
  }
}

/* 暗色主题适配 */
:global(.dark) .account-card,
:global(.dark) .records-card {
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color-darker);
}
</style>
