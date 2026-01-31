export type PaymentStatus = 'pending' | 'completed' | 'failed';
export type PaymentMethod = 'bank' | 'wechat' | 'alipay';

/**
 * 账户余额信息
 */
export interface BalanceInfo {
  userId: string;
  balance: number;
  updatedAt: string;
}

/**
 * 充值记录
 */
export interface RechargeRecord {
  id: string;
  amount: number;
  paymentMethod: PaymentMethod;
  createTime: string;
  status: PaymentStatus;
  transactionId?: string;
}

/**
 * 充值请求
 */
export interface RechargeRequest {
  amount: number;
  paymentMethod: PaymentMethod;
}

/**
 * 充值响应
 */
export interface RechargeResponse {
  success: boolean;
  newBalance: number;
  transactionId: string;
  message?: string;
}
