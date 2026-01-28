import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useThemeStore = defineStore('theme', () => {
  // State
  const isDarkMode = ref(false);

  // Actions
  /**
   * 切换主题模式
   */
  const toggleTheme = () => {
    isDarkMode.value = !isDarkMode.value;
    updateThemeClass();
    // 注意：不再调用 localStorage.setItem，由 persist 插件自动处理
  };

  /**
   * 设置主题模式
   * 
   * @param isDark 是否为深色模式
   */
  const setTheme = (isDark: boolean) => {
    isDarkMode.value = isDark;
    updateThemeClass();
    // 注意：不再调用 localStorage.setItem，由 persist 插件自动处理
  };

  /**
   * 初始化主题设置
   * 
   * 从持久化存储中恢复主题设置，如果无则使用系统默认设置
   */
  const initTheme = () => {
    // 状态已经由 persist 插件自动恢复，这里只需要应用主题
    const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    
    // 如果持久化的值 undefined 或 null（从未设置过），才使用系统偏好
    // 注意：persist 插件会在初始化时自动恢复值，所以这里只需要应用主题
    updateThemeClass();
  };

  /**
   * 更新文档主题类名
   */
  const updateThemeClass = () => {
    if (isDarkMode.value) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  };

  return {
    // State
    isDarkMode,
    // Actions
    toggleTheme,
    setTheme,
    initTheme,
    updateThemeClass
  };
}, {
  // 启用持久化，将主题设置保存在本地存储中
  persist: {
    key: 'theme-settings',
    storage: localStorage,
  },
});