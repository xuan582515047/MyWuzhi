<template>
  <el-dialog
      v-model="dialogVisible"
      title="确认购买"
      width="600px"
      destroy-on-close
      class="purchase-dialog"
      :close-on-click-modal="false"
  >
    <div v-if="tool" class="purchase-content">
      <!-- 购买步骤指示器 -->
      <div class="purchase-steps">
        <el-steps :active="currentStep" finish-status="success">
          <el-step title="确认信息" />
          <el-step title="选择支付方式" />
          <el-step title="完成支付" />
        </el-steps>
      </div>

      <!-- 步骤1: 确认购买信息 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="purchase-confirm">
          <!-- 工具信息 -->
          <div class="tool-purchase-info">
            <div class="tool-header">
              <div class="tool-cover">
                <div class="default-icon">
                  <el-icon><Tools /></el-icon>
                </div>
              </div>

              <div class="tool-details">
                <h3 class="tool-name">{{ tool.name }}</h3>
                <p class="tool-description">{{ tool.description }}</p>

                <!-- 工具标签 -->
                <div class="tool-tags" v-if="tool.tagIds">
                  <el-tag
                      v-for="(tagId, index) in parseTagIds(tool.tagIds).slice(0, 3)"
                      :key="tagId"
                      size="small"
                      type="info"
                      effect="plain"
                  >
                    {{ getTagName(tagId) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>

          <!-- 价格明细 -->
          <div class="price-details">
            <h4>价格明细</h4>
            <div class="price-breakdown">
              <div class="price-row">
                <span>商品价格</span>
                <span>¥{{ Number(tool.price).toFixed(2) }}</span>
              </div>

              <div v-if="couponDiscount > 0" class="price-row discount">
                <span>优惠券</span>
                <span class="discount-amount">-¥{{ couponDiscount.toFixed(2) }}</span>
              </div>

              <div class="price-row total">
                <span>应付金额</span>
                <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
              </div>
            </div>

            <!-- 优惠券选择 -->
            <div class="coupon-section">
              <el-button
                  text
                  type="primary"
                  @click="showCouponSelector = !showCouponSelector"
              >
                <el-icon><Ticket /></el-icon>
                {{ selectedCoupon ? '更换优惠券' : '选择优惠券' }}
              </el-button>

              <div v-if="showCouponSelector" class="coupon-selector">
                <div class="coupon-list">
                  <div
                      v-for="coupon in availableCoupons"
                      :key="coupon.id"
                      class="coupon-item"
                      :class="{ selected: selectedCoupon?.id === coupon.id }"
                      @click="selectCoupon(coupon)"
                  >
                    <div class="coupon-info">
                      <div class="coupon-name">{{ coupon.name }}</div>
                      <div class="coupon-desc">{{ coupon.description }}</div>
                    </div>
                    <div class="coupon-value">
                      <span v-if="coupon.type === 'percentage'">-{{ coupon.value }}%</span>
                      <span v-else>-¥{{ coupon.value }}</span>
                    </div>
                  </div>
                </div>
                <el-button
                    v-if="selectedCoupon"
                    text
                    type="danger"
                    @click="clearCoupon"
                >
                  取消使用优惠券
                </el-button>
              </div>
            </div>
          </div>

          <!-- 购买须知 -->
          <div class="purchase-notice">
            <el-alert
                title="购买须知"
                type="info"
                :closable="false"
            >
              <ul>
                <li>购买后即可永久使用该工具的所有功能</li>
                <li>支持7天无理由退款</li>
                <li>购买成功后将在您的工具中心显示</li>
                <li>如有问题请联系客服</li>
              </ul>
            </el-alert>
          </div>
        </div>
      </div>

      <!-- 步骤2: 选择支付方式 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="payment-methods">
          <h4>选择支付方式</h4>

          <div class="payment-options">
            <div
                v-for="method in paymentMethods"
                :key="method.code"
                class="payment-option"
                :class="{ selected: selectedPayment === method.code }"
                @click="selectPayment(method.code)"
            >
              <div class="payment-icon">
                <img v-if="method.icon" :src="method.icon" :alt="method.name" />
                <el-icon v-else><CreditCard /></el-icon>
              </div>
              <div class="payment-info">
                <div class="payment-name">{{ method.name }}</div>
                <div class="payment-desc">{{ method.description }}</div>
              </div>
              <div class="payment-radio">
                <el-radio v-model="selectedPayment" :label="method.code" />
              </div>
            </div>
          </div>

          <!-- 支付信息表单 -->
          <div v-if="selectedPayment === 'bank'" class="payment-form">
            <h5>银行卡信息</h5>
            <el-form :model="paymentForm" label-width="100px">
              <el-form-item label="持卡人">
                <el-input v-model="paymentForm.cardholder" placeholder="请输入持卡人姓名" />
              </el-form-item>
              <el-form-item label="卡号">
                <el-input v-model="paymentForm.cardNumber" placeholder="请输入银行卡号" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 账户余额 -->
          <div class="balance-info">
            <div class="balance-row">
              <span>账户余额</span>
              <span class="balance-amount">¥{{ userBalance.toFixed(2) }}</span>
            </div>
            <div v-if="userBalance >= totalAmount" class="balance-usage">
              <el-checkbox v-model="useBalance">
                使用余额支付 (可抵扣 ¥{{ Math.min(userBalance, totalAmount).toFixed(2) }})
              </el-checkbox>
            </div>
          </div>
        </div>
      </div>

      <!-- 步骤3: 完成支付 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="payment-processing">
          <div v-if="paymentStatus === 'processing'" class="processing-content">
            <el-icon class="processing-icon"><Loading /></el-icon>
            <h4>正在处理支付...</h4>
            <p>请稍候，正在确认您的支付信息</p>
          </div>

          <div v-else-if="paymentStatus === 'success'" class="success-content">
            <el-icon class="success-icon"><SuccessFilled /></el-icon>
            <h4>购买成功！</h4>
            <p>您已成功购买工具：{{ tool.name }}</p>
            <div class="order-info">
              <p>订单号：{{ orderInfo.orderNumber }}</p>
              <p>支付时间：{{ orderInfo.paymentTime }}</p>
            </div>
          </div>

          <div v-else-if="paymentStatus === 'failed'" class="failed-content">
            <el-icon class="failed-icon"><CircleCloseFilled /></el-icon>
            <h4>支付失败</h4>
            <p>{{ errorMessage || '支付过程中出现错误，请重试' }}</p>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <template v-if="currentStep === 0">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="nextStep">下一步</el-button>
        </template>

        <template v-else-if="currentStep === 1">
          <el-button @click="prevStep">上一步</el-button>
          <el-button
              type="primary"
              :loading="paymentLoading"
              @click="processPayment"
          >
            确认支付 ¥{{ totalAmount.toFixed(2) }}
          </el-button>
        </template>

        <template v-else-if="currentStep === 2">
          <el-button
              v-if="paymentStatus === 'success'"
              type="primary"
              @click="handlePurchaseSuccess"
          >
            完成
          </el-button>
          <el-button
              v-else-if="paymentStatus === 'failed'"
              @click="prevStep"
          >
            重新支付
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Tools, Ticket, CreditCard, Loading,
  SuccessFilled, CircleCloseFilled
} from '@element-plus/icons-vue'
import { type ToolInfo, type TagInfo } from '@/Entity/ToolEntity'

// Props
interface Props {
  visible: boolean
  tool?: ToolInfo
  tagsList?: TagInfo[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  tool: undefined,
  tagsList: () => []
})

// Emits
const emit = defineEmits<{
  'update:visible': [visible: boolean]
  'purchaseSuccess': [tool: ToolInfo]
}>()

// 响应式数据
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const currentStep = ref(0)
const selectedPayment = ref('alipay')
const useBalance = ref(false)
const selectedCoupon = ref<any>(null)
const showCouponSelector = ref(false)
const paymentLoading = ref(false)
const paymentStatus = ref<'processing' | 'success' | 'failed'>('processing')
const errorMessage = ref('')

// 用户余额
const userBalance = ref(156.88)

// 支付方式
const paymentMethods = ref([
  {
    code: 'alipay',
    name: '支付宝',
    description: '使用支付宝安全快捷支付',
    icon: '/icons/alipay.png'
  },
  {
    code: 'wechat',
    name: '微信支付',
    description: '使用微信扫码支付',
    icon: '/icons/wechat.png'
  },
  {
    code: 'bank',
    name: '银行卡',
    description: '使用银行卡支付'
  }
])

// 支付表单
const paymentForm = ref({
  cardholder: '',
  cardNumber: ''
})

// 可用优惠券
const availableCoupons = ref([
  {
    id: '1',
    name: '新用户专享',
    description: '首次购买可使用',
    type: 'percentage',
    value: 10,
    minAmount: 50
  },
  {
    id: '2',
    name: '满100减20',
    description: '满100元可用',
    type: 'fixed',
    value: 20,
    minAmount: 100
  }
])

// 订单信息
const orderInfo = ref({
  orderNumber: '',
  paymentTime: ''
})

// 计算属性
const couponDiscount = computed(() => {
  if (!selectedCoupon.value || !props.tool) return 0

  const coupon = selectedCoupon.value
  const price = Number(props.tool.price) || 0

  if (coupon.minAmount && price < coupon.minAmount) return 0

  if (coupon.type === 'percentage') {
    return price * (coupon.value / 100)
  } else {
    return Math.min(coupon.value, price)
  }
})

const totalAmount = computed(() => {
  if (!props.tool) return 0

  const price = Number(props.tool.price) || 0
  const discount = couponDiscount.value
  let finalAmount = Math.max(0, price - discount)

  if (useBalance.value) {
    finalAmount = Math.max(0, finalAmount - userBalance.value)
  }

  return finalAmount
})

/**
 * 根据标签ID获取标签名称
 */
const getTagName = (tagId: string): string => {
  const tag = props.tagsList.find(t => t.id === tagId)
  return tag ? tag.name : tagId
}

/**
 * 将tagIds转换为数组（后端已返回数组，此方法备用）
 */
const parseTagIds = (tagIds: string | string[]): string[] => {
  if (!tagIds) return []
  if (Array.isArray(tagIds)) return tagIds
  return tagIds.split(',').filter(id => id.trim().length > 0)
}

/**
 * 选择优惠券
 */
const selectCoupon = (coupon: any) => {
  const price = Number(props.tool?.price) || 0
  if (coupon.minAmount && price < coupon.minAmount) {
    ElMessage.warning(`该优惠券需满${coupon.minAmount}元才可使用`)
    return
  }
  selectedCoupon.value = coupon
  showCouponSelector.value = false
}

/**
 * 清除优惠券
 */
const clearCoupon = () => {
  selectedCoupon.value = null
  showCouponSelector.value = false
}

/**
 * 选择支付方式
 */
const selectPayment = (method: string) => {
  selectedPayment.value = method
}

/**
 * 下一步
 */
const nextStep = () => {
  if (currentStep.value < 2) {
    currentStep.value++
  }
}

/**
 * 上一步
 */
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    paymentStatus.value = 'processing'
    errorMessage.value = ''
  }
}

/**
 * 处理支付
 */
const processPayment = async () => {
  paymentLoading.value = true
  paymentStatus.value = 'processing'

  try {
    // 模拟支付处理
    await new Promise(resolve => setTimeout(resolve, 2000))

    // 模拟支付结果
    const isSuccess = Math.random() > 0.1 // 90% 成功率

    if (isSuccess) {
      paymentStatus.value = 'success'
      orderInfo.value = {
        orderNumber: 'ORD' + Date.now(),
        paymentTime: new Date().toLocaleString('zh-CN')
      }
    } else {
      paymentStatus.value = 'failed'
      errorMessage.value = '支付超时，请重试'
    }
  } catch (error) {
    paymentStatus.value = 'failed'
    errorMessage.value = '支付过程中出现错误'
  } finally {
    paymentLoading.value = false
  }
}

/**
 * 取消购买
 */
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消购买吗？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '继续购买',
    type: 'warning'
  }).then(() => {
    dialogVisible.value = false
    resetPurchase()
  }).catch(() => {
    // 用户选择继续购买，不做任何操作
  })
}

/**
 * 购买成功处理
 */
const handlePurchaseSuccess = () => {
  if (props.tool) {
    emit('purchaseSuccess', props.tool)
  }
  dialogVisible.value = false
  resetPurchase()
}

/**
 * 重置购买状态
 */
const resetPurchase = () => {
  currentStep.value = 0
  selectedPayment.value = 'alipay'
  useBalance.value = false
  selectedCoupon.value = null
  showCouponSelector.value = false
  paymentStatus.value = 'processing'
  errorMessage.value = ''
  paymentForm.value = {
    cardholder: '',
    cardNumber: ''
  }
}

// 监听可见性变化，重置状态
watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        resetPurchase()
      }
    }
)
</script>

<style scoped>
.purchase-dialog .el-dialog__body {
  padding: 24px;
}

.purchase-content .purchase-steps {
  margin-bottom: 32px;
}

.step-content {
  min-height: 400px;
}

.purchase-confirm .tool-purchase-info {
  margin-bottom: 24px;
}

.purchase-confirm .tool-purchase-info .tool-header {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.purchase-confirm .tool-purchase-info .tool-header .tool-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-fill-color-lighter);
}

.purchase-confirm .tool-purchase-info .tool-header .tool-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.purchase-confirm .tool-purchase-info .tool-header .tool-cover .default-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: var(--el-text-color-placeholder);
}

.purchase-confirm .tool-purchase-info .tool-header .tool-details {
  flex: 1;
}

.purchase-confirm .tool-purchase-info .tool-header .tool-details .tool-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.purchase-confirm .tool-purchase-info .tool-header .tool-details .tool-description {
  margin: 0 0 12px 0;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

.purchase-confirm .tool-purchase-info .tool-header .tool-details .tool-tags {
  display: flex;
  gap: 6px;
}

.purchase-confirm .price-details h4 {
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
}

.purchase-confirm .price-details .price-breakdown {
  background: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.purchase-confirm .price-details .price-breakdown .price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.purchase-confirm .price-details .price-breakdown .price-row:last-child {
  margin-bottom: 0;
}

.purchase-confirm .price-details .price-breakdown .price-row.total {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.purchase-confirm .price-details .price-breakdown .price-row.discount {
  color: var(--el-color-success);
}

.purchase-confirm .price-details .price-breakdown .price-row.discount .discount-amount {
  font-weight: 600;
}

.purchase-confirm .price-details .coupon-section .coupon-selector {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}

.purchase-confirm .price-details .coupon-section .coupon-selector .coupon-list {
  margin-bottom: 12px;
}

.purchase-confirm .price-details .coupon-section .coupon-selector .coupon-list .coupon-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.purchase-confirm .price-details .coupon-section .coupon-selector .coupon-list .coupon-item:hover {
  border-color: var(--el-color-primary);
}

.purchase-confirm .price-details .coupon-section .coupon-selector .coupon-list .coupon-item.selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.purchase-confirm .price-details .coupon-section .coupon-selector .coupon-list .coupon-item .coupon-info .coupon-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.purchase-confirm .price-details .coupon-section .coupon-selector .coupon-list .coupon-item .coupon-info .coupon-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.purchase-confirm .price-details .coupon-section .coupon-selector .coupon-list .coupon-item .coupon-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-color-danger);
}

.purchase-confirm .purchase-notice {
  margin-top: 24px;
}

.purchase-confirm .purchase-notice ul {
  margin: 8px 0 0 16px;
  padding: 0;
}

.purchase-confirm .purchase-notice ul li {
  margin-bottom: 4px;
  color: var(--el-text-color-regular);
}

.payment-methods h4 {
  margin-bottom: 24px;
  color: var(--el-text-color-primary);
}

.payment-methods .payment-options {
  margin-bottom: 24px;
}

.payment-methods .payment-options .payment-option {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-methods .payment-options .payment-option:hover {
  border-color: var(--el-color-primary);
}

.payment-methods .payment-options .payment-option.selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.payment-methods .payment-options .payment-option .payment-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.payment-methods .payment-options .payment-option .payment-icon img {
  max-width: 100%;
  max-height: 100%;
}

.payment-methods .payment-options .payment-option .payment-icon .el-icon {
  font-size: 24px;
  color: var(--el-color-primary);
}

.payment-methods .payment-options .payment-option .payment-info {
  flex: 1;
}

.payment-methods .payment-options .payment-option .payment-info .payment-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.payment-methods .payment-options .payment-option .payment-info .payment-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.payment-methods .payment-form {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.payment-methods .payment-form h5 {
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
}

.payment-methods .balance-info {
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.payment-methods .balance-info .balance-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.payment-methods .balance-info .balance-row .balance-amount {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-success);
}

.payment-methods .balance-info .balance-usage {
  margin-top: 8px;
}

.payment-processing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 300px;
}

.payment-processing .processing-icon {
  font-size: 48px;
  color: var(--el-color-primary);
  animation: spin 1s linear infinite;
  margin-bottom: 24px;
}

.payment-processing .success-icon {
  font-size: 48px;
  color: var(--el-color-success);
  margin-bottom: 24px;
}

.payment-processing .failed-icon {
  font-size: 48px;
  color: var(--el-color-danger);
  margin-bottom: 24px;
}

.payment-processing h4 {
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.payment-processing p {
  color: var(--el-text-color-regular);
  margin-bottom: 24px;
}

.payment-processing .order-info {
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  text-align: left;
  max-width: 80%;
  margin: 0 auto;
}

.payment-processing .order-info p {
  margin-bottom: 8px;
  color: var(--el-text-color-regular);
}

.payment-processing .order-info p:last-child {
  margin-bottom: 0;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 深色主题适配 */
.dark .payment-processing .order-info {
  background: var(--el-fill-color-darker);
}
</style>