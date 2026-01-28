<script setup lang="ts">

import {useRoute, useRouter} from "vue-router";

import {onMounted, watch, ref} from "vue";

import {useAuthStore} from "@/Stores/AuthStore.ts";

import {useThemeStore} from "@/Stores/ThemeStore.ts";
import ThemeToggle from "@/Views/PublicComponents/ThemeToggle.vue";
import {AxiosUtil} from "@/Util/AxiosUtil.ts";

//==========================> 前置方法定义 <==========================
/**
 * 检查用户登录状态，未登录则跳转到登录页
 */
const checkAuth = () => {
  // 检查当前路径是否为登录页，避免在登录页重复跳转
  if (route.path === '/login' || route.path === '/register') {
    return
  }

  if (!authStore.isLogin) {
    router.push('/login')
  }
}
//==========================> 变量定义 <==========================
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

onMounted( async () => {
  // 初始化主题
  themeStore.initTheme();
  checkAuth()
  //TODO 权限，记得改
  //authStore.isLogin = true
  if(!await AxiosUtil.tryRefreshToken()){
    authStore.clear()
    router.push('/login')
  }
})


watch(
    () => route.path,
    (newPath) => {
      checkAuth()
    },
    { immediate: true }
)

const html = document.documentElement

// ✅ 修复：使用 themeStore 替代 theme，避免重复创建 store 实例
watch(() => themeStore.isDarkMode, (isDark: boolean) => {
  // 创建波纹遮罩层
  const overlay = document.createElement('div')
  overlay.className = 'theme-transition-overlay'

  // 根据目标主题设置初始状态
  if (isDark) {
    overlay.classList.add('to-dark')
  } else {
    overlay.classList.add('to-light')
  }

  document.body.appendChild(overlay)

  // 触发重排以启动动画
  overlay.offsetHeight

  // 启动波纹动画
  overlay.classList.add('active')

  themeStore.updateThemeClass()

  // 波纹完成后清理遮罩层
  setTimeout(() => {
    document.body.removeChild(overlay)
  }, 1200)
})

//==========================> 方法定义 <==========================


</script>

<template>
  <div class="app-wrapper">
    <router-view />
    <ThemeToggle />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', SimSun, sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
  overflow-x: hidden;
}



/* 防止路由切换时的闪烁 - 确保在切换主题和路由时平滑过渡 */
body, #app {
  transition: background-color 0.3s ease, color 0.3s ease;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-gradient-start) 0%, var(--primary-gradient-end) 100%);
}



.dark body, .dark #app {
  background: linear-gradient(135deg, var(--dark-bg-start) 0%, var(--dark-bg-end) 100%);
  color: var(--dark-text);

}
/* 基础动画类 */
.fade-in {
  animation: fadeIn 0.5s ease-in;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.slide-in {
  animation: slideIn 0.6s ease-out;
}
@keyframes slideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}
/* 悬停动画效果 */
.hover-lift {
  transition: all 0.3s ease;
}
.hover-lift:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px var(--shadow-color) !important;
}
/* 渐变背景动画 */
.gradient-bg {
  background: linear-gradient(-45deg, #6e8efb, #a777e3, #7e57c2, #5c6bc0);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}
@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, var(--primary-gradient-start) 0%, var(--primary-gradient-end) 100%);
  transition: background-color 0.3s ease;
}
/* 深色模式全局样式 */
.dark {
  background: linear-gradient(135deg, var(--dark-bg-start) 0%, var(--dark-bg-mid) 30%, var(--dark-bg-end) 100%);
  color: var(--dark-text);
}
.dark .app-container {
  background: linear-gradient(135deg, var(--dark-bg-start) 0%, var(--dark-bg-mid) 30%, var(--dark-bg-end) 100%);
  color: var(--dark-text);
}
/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: rgba(232, 234, 246, 0.5);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--primary-blue) 0%, var(--primary-purple) 100%);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, var(--primary-purple) 0%, var(--primary-blue-light) 100%);
}
/* 深色模式滚动条 */
.dark ::-webkit-scrollbar-track {
  background: rgba(44, 62, 80, 0.5);
}
.dark ::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
}
.dark ::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, var(--dark-accent-purple) 0%, #8e44ad 100%);
}

/* 主题切换转场动画 */
.app-wrapper {
  position: relative;
  min-height: 100vh;
}

/* 主题切换波纹遮罩层 */
.theme-transition-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  z-index: 9999;
  pointer-events: none;
  transform: translate(-50%, -50%);
  transition: all 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 切换到深色模式的波纹 */
.theme-transition-overlay.to-dark {
  background: radial-gradient(circle at center, 
    var(--dark-bg-start) 0%, 
    var(--dark-bg-mid) 50%, 
    var(--dark-bg-end) 100%);
  box-shadow: 0 0 100px 50px rgba(0, 0, 0, 0.3);
}

/* 切换到浅色模式的波纹 */
.theme-transition-overlay.to-light {
  background: radial-gradient(circle at center, 
    var(--primary-gradient-start) 0%, 
    var(--primary-gradient-end) 100%);
  box-shadow: 0 0 100px 50px rgba(255, 255, 255, 0.3);
}

/* 激活波纹扩散动画 */
.theme-transition-overlay.active {
  width: 300vmax;
  height: 300vmax;
  opacity: 0;
}



/* 主题切换时的颜色过渡增强 - 排除弹窗相关元素 */
body, .app-container, .app-wrapper, 
.card, .button, .el-button, .el-card, .el-input,
.el-select, .el-tag, .el-menu-item, .el-tabs__item {
  transition: background-color 0.4s cubic-bezier(0.4, 0, 0.2, 1), 
              color 0.4s cubic-bezier(0.4, 0, 0.2, 1), 
              border-color 0.4s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 确保弹窗相关元素不受全局过渡影响 */
.el-dialog, .el-dialog__wrapper, .el-overlay, 
.el-dialog__header, .el-dialog__body, .el-dialog__footer,
.el-overlay-dialog, .v-modal {
  transition: none !important;
}

/* 弹窗内容使用快速过渡 */
.el-dialog__wrapper {
  transition: opacity 0.2s ease, transform 0.2s ease !important;
}

.el-dialog {
  transition: transform 0.2s ease, opacity 0.2s ease !important;
}

/* 波纹扩散时的光晕效果 */
.theme-transition-overlay::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.6s ease;
}

.theme-transition-overlay.to-dark::after {
  background: radial-gradient(circle, 
    rgba(59, 130, 246, 0.4) 0%, 
    rgba(147, 51, 234, 0.2) 50%, 
    transparent 100%);
}

.theme-transition-overlay.to-light::after {
  background: radial-gradient(circle, 
    rgba(147, 51, 234, 0.4) 0%, 
    rgba(59, 130, 246, 0.2) 50%, 
    transparent 100%);
}

.theme-transition-overlay.active::after {
  opacity: 1;
}
</style>
