import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type {UserData} from "@/Views/History/index.vue";

export const useUserStore = defineStore('user', () => {
  const name = ref('')
  const avatar = ref('')
  
  /**
   * 保存用户数据
   * 
   * @param name1 用户名
   * @param avatar1 用户头像
   */
  function saveUserData(name1: string, avatar1: string) {
    name.value = name1
    avatar.value = avatar1
    // 注意：不再直接操作 localStorage，由 persist 插件自动处理
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
    saveUserData,
    clear
  }
},{
  persist: {
    key: 'user-store',
    storage: localStorage,
  }
})
