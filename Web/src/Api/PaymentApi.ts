import type { RechargeRecord, PaymentMethod, BalanceInfo, RechargeRequest, RechargeResponse } from "@/Entity/PaymentEntity";
import { AxiosUtil } from "@/Util/AxiosUtil";

/**
 * 获取账户余额
 */
export async function GetBalance(): Promise<BalanceInfo | null> {
  try {
    const response = await AxiosUtil.get<BalanceInfo>("/payment/balance");
    return response;
  } catch (error) {
    console.error("获取账户余额失败:", error);
    return null;
  }
}

/**
 * 发起充值
 * @param request 充值请求
 */
export async function Recharge(request: RechargeRequest): Promise<RechargeResponse | null> {
  try {
    const response = await AxiosUtil.post<RechargeResponse>("/payment/recharge", request);
    return response;
  } catch (error) {
    console.error("充值失败:", error);
    return null;
  }
}

/**
 * 获取充值记录（向后兼容）
 * @deprecated 建议使用带分页的版本
 */
export async function GetRecords(): Promise<RechargeRecord[]> {
  try {
    const response = await AxiosUtil.get<RechargeRecord[]>("/payment/records");

    if (response && Array.isArray(response)) {
      return response;
    }
    return [];
  } catch (error) {
    console.error("获取充值记录失败:", error);
    return [];
  }
}
