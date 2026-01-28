import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import {useRouter} from "vue-router";

export const useAuthStore = defineStore('auth', () => {
  const isLogin = ref(false)
  const accessToken = ref('')
  const refreshToken = ref('')
  const rememberLogin = ref(false)

  /**
   * 保存登录数据
   * 
   * @param accessToken1 访问令牌
   * @param refreshToken1 刷新令牌
   * @param remember 是否记住登录状态
   */
  function saveLoginData(accessToken1: string, refreshToken1: string, remember: boolean = false) {
    accessToken.value = accessToken1
    refreshToken.value = refreshToken1
    rememberLogin.value = remember
    isLogin.value = true
    // 注意：不再直接操作 localStorage，由 persist 插件自动处理
  }

  /**
   * 清除登录状态
   * 
   * 重置所有认证相关状态，持久化插件会自动同步到存储
   */
  function clear() {
    accessToken.value = ''
    refreshToken.value = ''
    isLogin.value = false
    rememberLogin.value = false
    // 注意：不再直接操作 localStorage，由 persist 插件自动处理
  }

  return { 
    isLogin, 
    accessToken, 
    refreshToken, 
    rememberLogin, 
    saveLoginData, 
    clear
  }
},{
  persist: {
    key: 'auth-store',
    storage: localStorage,
  }
})
