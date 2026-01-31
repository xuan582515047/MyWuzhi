import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type {UserData} from "@/Views/History/index.vue";

export const useUserStore = defineStore('user', () => {
  const name = ref('')
  const avatar = ref('')
  const balance = ref<number>(0)
  
  /**
   * 保存用户数据
   * 
   * @param name1 用户名
   * @param avatar1 用户头像
   * @param balance1 用户余额（可选）
   */
  function saveUserData(name1: string, avatar1: string, balance1?: number) {
    name.value = name1
    avatar.value = avatar1
    if (balance1 !== undefined) {
      balance.value = balance1
    }
    // 注意：不再直接操作 localStorage，由 persist 插件自动处理
  }
  
  /**
   * 更新用户余额
   * 
   * @param newBalance 新的余额
   */
  function updateBalance(newBalance: number) {
    balance.value = newBalance
  }

  /**
   * 清除用户数据
   */
  function clear() {
    name.value = ''
    avatar.value = ''
  }

  return { 
    name, 
    avatar,
    balance,
    saveUserData,
    updateBalance,
    clear
  }
},{
  persist: {
    key: 'user-store',
    storage: localStorage,
  }
})
