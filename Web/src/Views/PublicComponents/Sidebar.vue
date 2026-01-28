<script setup lang="ts">
import {ref, defineProps, defineEmits, onMounted} from 'vue'
import {useUserStore} from "@/Stores/UserStore.ts";
import {useAuthStore} from "@/Stores/AuthStore.ts";
import {useThemeStore} from '@/Stores/ThemeStore.ts';
import LogoBox from "@/Views/PublicComponents/LogoBox.vue";
import {More, User as UserIcon, Setting, SwitchButton, ArrowLeftBold, Expand, Fold, Sunny, Moon} from '@element-plus/icons-vue'
import {useRouter} from "vue-router";
import ProfileDialog from "@/Views/PublicComponents/ProfileDialog.vue";

// 类型定义
export interface ProfileData {
  name: string;
  avatar: string;
  username?: string;
  email?: string;
  phone?: string;
  wechatName?: string;
  qqName?: string;
}

// 通信定义
const props = defineProps({
  showExitBtn: {type: Boolean, default: false},
  title: {type: String, default: ""},
  returnFunction: {type: Function, required: false},
})
const emits = defineEmits(['return'])

/**
 * 处理个人资料
 */
const handleProfile = () => {
  openProfile()
}

/**
 * 处理设置
 */
const handleSetting = () => {

}

/**
 * 处理登出
 */
const handleLogout = () => {
  // 获取store实例
  const authStore = useAuthStore()
  const userStore = useUserStore()
  
  // 清除登录状态
  authStore.clear()
  userStore.clear()
  
  // 跳转到登录页面
  router.push('/login')
}

// 变量定义
const router = useRouter();
const userData = useUserStore();
const authData = useAuthStore();
const dropdownList = [
  { name: '个人资料', command: 'profile', divided: false, icon: UserIcon, function: handleProfile},
  { name: '设置', command: 'settings', divided: false, icon: Setting, function: handleSetting },
  { name: '退出登录', command: 'logout', divided: true, icon: SwitchButton, function: handleLogout },
]
const user = {
  name: userData.name,
  avatar: userData.avatar
}
const showProfileDialog = ref(false)
// 侧边栏收缩状态
const isCollapsed = ref(false)
const isCollapsing = ref(false)
const isExpanding = ref(false)

// 使用主题Store
const themeStore = useThemeStore();

// 主题切换冷却时间相关
const isThemeCooldown = ref(false);
const themeCooldownTime = ref(0);
const THEME_COOLDOWN_DURATION = 3000; // 3秒冷却时间

// 在组件挂载时初始化主题
onMounted(() => {
  themeStore.initTheme();
})

/**
 * 打开个人资料弹窗
 */
const openProfile = () => {
  showProfileDialog.value = true
}

/**
 * 处理资料保存
 * @param profile
 */
const handleProfileSave = (profile: ProfileData) => {
  console.log('保存的资料数据:', profile)
}

/**
 * 处理弹窗关闭
 */
const handleProfileClose = () => {
  console.log('个人资料弹窗已关闭')
};

/**
 * 返回
 */
const returnAction = () => {
  emits('return')
  if(props.returnFunction){
    props.returnFunction()
  }else{
    router.back()
  }
};

/**
 * 切换主题（带冷却时间）
 */
const handleThemeToggle = () => {
  if (isThemeCooldown.value) return;
  
  themeStore.toggleTheme();
  
  // 开始冷却
  isThemeCooldown.value = true;
  themeCooldownTime.value = THEME_COOLDOWN_DURATION;
  
  // 更新倒计时
  const startTime = Date.now();
  const timer = setInterval(() => {
    const elapsed = Date.now() - startTime;
    const remaining = THEME_COOLDOWN_DURATION - elapsed;
    
    if (remaining <= 0) {
      isThemeCooldown.value = false;
      themeCooldownTime.value = 0;
      clearInterval(timer);
    } else {
      themeCooldownTime.value = remaining;
    }
  }, 100);
};

/**
 * 切换侧边栏收缩状态
 */
const toggleCollapse = () => {
  if (isCollapsed.value) {
    // 展开操作
    isExpanding.value = true;
    isCollapsed.value = false;
    
    // 动画结束后清除状态
    setTimeout(() => {
      isExpanding.value = false;
    }, 300);
  } else {
    // 收缩操作
    isCollapsed.value = true;
    isCollapsing.value = true;
    
    // 动画结束后更新状态
    setTimeout(() => {
      isCollapsing.value = false;
    }, 300);
  }
};

</script>

<template>
  <div style="display: flex;width: 100vw">
    <div :class="['sidebar',
                isCollapsed ? 'sidebar-collapsed' : '',
                isCollapsing ? 'sidebar-collapsing' : '',
                isExpanding ? 'sidebar-expanding' : '']">
      <!-- 顶部内容区域 -->
      <div class="sidebar-top">
        <div class="sidebar-header">
          <LogoBox v-if="!isCollapsed && !isCollapsing"/>
          <div class="header-controls">
            <el-button
                type="danger"
                plain
                @click="returnAction"
                v-if="props.showExitBtn && !isCollapsed && !isCollapsing">
              <el-icon><ArrowLeftBold /></el-icon>返回
            </el-button>
            <el-button
                class="collapse-btn"
                type="primary"
                @click="toggleCollapse"
                :icon="isCollapsed && !isExpanding ? Expand : Fold"
                circle />
          </div>
        </div>
        <div class="sidebar-title" v-if="props.title && props.title !== '' && !isCollapsed && !isCollapsing">{{ props.title }}</div>
        <div class="sidebar-content" v-if="!isCollapsed && !isCollapsing">
          <slot name="left-side"/>
        </div>
      </div>
      <!-- 底部用户信息 -->
      <div class="sidebar-bottom" v-if="!isCollapsed && !isCollapsing">
        <el-dropdown trigger="click">
          <div class="user-info">
            <el-avatar :size="32" :src="user.avatar" />
            <div class="user-details">
              <el-text truncated style="flex: 1">
                <span class="username">{{ user.name }}</span>
              </el-text>
            </div>
            <el-icon :size="20"><More /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="dropdown-list custom-dropdown-popper">
              <div v-for="dItem in dropdownList" :key="dItem.name">
                <el-dropdown-item
                    :icon="dItem.icon"
                    @click="dItem.function"
                    :command="dItem.command"
                    :divided="dItem.divided">
                  <div class="dropdown-item">{{ dItem.name }}</div>
                </el-dropdown-item>
              </div>
              <el-dropdown-item
                  :icon="themeStore.isDarkMode ? Sunny : Moon"
                  @click="handleThemeToggle"
                  :disabled="isThemeCooldown"
                  :class="{ 'cooldown-item': isThemeCooldown }"
                  command="theme"
                  :divided="true">
                <div class="dropdown-item">
                  <span v-if="!isThemeCooldown">{{ themeStore.isDarkMode ? '浅色模式' : '深色模式' }}</span>
                  <span v-else class="cooldown-text">冷却中 ({{ Math.ceil(themeCooldownTime/1000) }}s)</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 收缩状态下的底部用户信息 -->
      <div class="sidebar-bottom-collapsed" v-else>
        <el-dropdown trigger="click">
          <el-avatar
              :size="32"
              :src="user.avatar"
              style="margin: 0 auto; display: block; cursor: pointer;" />
          <template #dropdown>
            <el-dropdown-menu class="dropdown-list custom-dropdown-popper">
              <div v-for="dItem in dropdownList" :key="dItem.name">
                <el-dropdown-item
                    :icon="dItem.icon"
                    @click="dItem.function"
                    :command="dItem.command"
                    :divided="dItem.divided">
                  <div class="dropdown-item">{{ dItem.name }}</div>
                </el-dropdown-item>
              </div>
              <el-dropdown-item
                  :icon="themeStore.isDarkMode ? Sunny : Moon"
                  @click="handleThemeToggle"
                  :disabled="isThemeCooldown"
                  :class="{ 'cooldown-item': isThemeCooldown }"
                  command="theme"
                  :divided="true">
                <div class="dropdown-item">
                  <span v-if="!isThemeCooldown">{{ themeStore.isDarkMode ? '浅色模式' : '深色模式' }}</span>
                  <span v-else class="cooldown-text">冷却中 ({{ Math.ceil(themeCooldownTime/1000) }}s)</span>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <ProfileDialog
          v-model:visible="showProfileDialog"
          :profile="{ name: '', avatar: '' }"
          @save="handleProfileSave"
          @close="handleProfileClose" />
    </div>
    <div class="sidebar-mid-content">
      <slot name="content"/>
    </div>
  </div>
</template>

<style scoped>
.sidebar-mid-content{
  overflow-x: hidden;
  overflow-y: auto;
  border-radius: 20px;
  flex: 1;
  /* 即使是flex: 1，也一定要有一个宽度 */
  width: 100px;
  height: 100vh;
}
.sidebar {
  max-width: 350px;
  height: 100vh;
  max-height: 100vh;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border-primary);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px 0;
  box-shadow: 2px 0 15px var(--shadow-light);
  transition: width 0.3s ease;
  position: relative;
  overflow: hidden;
}

.sidebar-top {
  display: flex;
  height: 91vh;
  flex-direction: column;
  gap: 1%;
  transition: all 0.3s ease;
}

.sidebar-bottom {
  height: 5vh;
  padding: 0 16px;
  user-select: none;
  margin-bottom: 10px;
  transition: all 0.3s ease;
  opacity: 1;
}

.sidebar-collapsed .sidebar-bottom {
  opacity: 0;
  transition: opacity 0.1s ease;
}

.sidebar-bottom-collapsed {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 5vh;
  padding: 10px 0;
  margin-bottom: 10px;
  user-select: none;
  opacity: 1;
}

.sidebar-collapsed .sidebar-bottom-collapsed {
  opacity: 1;
  transition: opacity 0.3s ease 0.15s;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  height: 5%;
  align-items: center;
  padding-right: 10px;
  transition: all 0.3s ease;
}

.sidebar-collapsing .sidebar-header {
  justify-content: center;
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: hidden;
  transition: opacity 0.15s ease;
  opacity: 1;
}

.sidebar-collapsing .sidebar-content {
  opacity: 0;
  transition: opacity 0.15s ease;
}

.sidebar-title {
  margin: 0 5%;
  text-align: center;
  border-radius: 20px;
  box-shadow: 0 4px 8px var(--shadow-light);
  padding: 10px 0;
  border: 1px dashed var(--border-primary);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  opacity: 1;
}

.sidebar-collapsing .sidebar-title {
  opacity: 0;
  transition: opacity 0.15s ease;
}

.collapse-btn {
  background: var(--accent-primary);
  border: none;
  color: white;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: var(--accent-primary);
  opacity: 0.8;
  transform: scale(1.05);
}

/* 修复下拉框白边和hover样式问题 */
:deep(.el-dropdown-menu) {
  border-radius: 16px !important;
  border: 1px solid var(--border-primary) !important;
  background: var(--card-bg) !important;
  padding: 8px 0 !important;
  box-shadow: 0 4px 20px var(--shadow-medium) !important;
  overflow: hidden;
}

/* 浅色模式下的阴影效果 */
[data-theme="light"] :deep(.el-dropdown-menu) {
  box-shadow: 0 4px 20px rgba(147, 51, 234, 0.15) !important;
}

/* 深色模式下的发光效果 */
[data-theme="dark"] :deep(.el-dropdown-menu) {
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3) !important;
}

/* 修复下拉项hover背景不完整的问题 */
:deep(.el-dropdown-menu__item) {
  margin: 2px 8px !important;
  padding: 12px 16px !important;
  transition: all 0.3s ease !important;
  color: var(--text-primary) !important;
  font-weight: 500;
}

/* 确保hover效果覆盖整个选项 */
:deep(.el-dropdown-menu__item:hover) {
  background: var(--hover-bg) !important;
  color: var(--text-primary) !important;
  border-radius: 12px !important;
  transform: translateX(4px);
}

/* 修复下拉项之间的分隔线 */
:deep(.el-dropdown-menu__item--divided) {
  border-top: 1px solid var(--border-primary) !important;
  margin-top: 8px !important;
}

/* 调整下拉项图标颜色 */
:deep(.el-dropdown-menu__item .el-icon) {
  color: inherit !important;
  margin-right: 8px;
}

.el-button:focus,
.el-dropdown:focus {
  outline: none !important;
  box-shadow: none !important;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  box-shadow: 0 2px 8px var(--shadow-light);
}

.user-info:hover {
  background: var(--hover-bg);
  box-shadow: var(--shadow-medium);
  transform: translateY(-2px);
}

.user-details {
  flex: 1;
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  opacity: 1;
}

.sidebar-collapsing .user-details {
  opacity: 0;
  transition: opacity 0.1s ease;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 调整返回按钮样式以匹配新主题 */
.el-button--danger {
  background: var(--accent-primary);
  border: none;
  color: white;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.el-button--danger:hover {
  background: var(--accent-primary);
  opacity: 0.8;
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

/* 滚动条样式调整为蓝紫色 */
.sidebar-content::-webkit-scrollbar {
  width: 6px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 3px;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: var(--border-primary);
  border-radius: 3px;
}

.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: var(--border-secondary);
}

/* 为侧边栏整体添加微妙的渐变边框效果 */
.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--gradient-start) 0%, var(--gradient-end) 100%);
  opacity: 0.1;
  pointer-events: none;
  border-radius: 0 20px 20px 0;
  transition: opacity 0.3s ease;
}

.sidebar:hover::before {
  opacity: 0.1;
}

.header-controls {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-left: auto;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.sidebar-collapsing .header-controls {
  margin-left: 0;
  gap: 0;
}

/* 主题切换按钮样式 */
.theme-toggle-container {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

.theme-toggle-btn {
  background: var(--accent-primary);
  border: none;
  color: white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
}

.theme-toggle-btn:hover {
  background: var(--accent-primary);
  opacity: 0.8;
  transform: scale(1.1);
  box-shadow: var(--shadow-medium);
}

.theme-toggle-btn-collapsed {
  background: var(--accent-primary);
  border: none;
  color: white;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  padding: 6px;
  margin: 5px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
}

.theme-toggle-btn-collapsed:hover {
  background: var(--accent-primary);
  opacity: 0.8;
  transform: scale(1.1);
  box-shadow: var(--shadow-medium);
}

/* 移除硬编码的深色模式样式，使用全局CSS变量 */

/* 添加更多动画效果 */
.sidebar {
  animation: slideInLeft 0.5s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sidebar-top {
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.sidebar-bottom {
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 添加悬停时的波纹效果 */
.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, var(--accent-primary) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  z-index: -1;
}

.sidebar:hover::before {
  opacity: 0.1;
}

/* 添加菜单项的动画效果 */
.sidebar-content {
  animation: menuSlideIn 0.7s ease-out;
}

@keyframes menuSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 添加用户信息区域的动画效果 */
.user-info {
  animation: userSlideIn 0.9s ease-out;
}

@keyframes userSlideIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 添加按钮点击时的涟漪效果 */
.collapse-btn {
  position: relative;
  overflow: hidden;
}

.collapse-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.collapse-btn:active::after {
  width: 100px;
  height: 100px;
}

/* 添加下拉菜单的动画效果 */
:deep(.el-dropdown-menu) {
  animation: dropdownFadeIn 0.3s ease-out;
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========================= 侧边栏右侧简单发光效果 ========================= */
.sidebar::after {
  content: '';
  position: absolute;
  top: 0;
  right: -1px;
  width: 2px;
  height: 100%;
  background: var(--primary-blue);
  opacity: 0.3;
  box-shadow: 0 0 8px var(--primary-blue);
  z-index: 10;
  transition: all 0.3s ease;
}

/* 浅色模式下使用投影效果 */
[data-theme="light"] .sidebar::after {
  background: transparent;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  opacity: 0.6;
}
</style>

<!-- 如果需要全局覆盖，可以在App.vue或main.ts中添加以下全局样式 -->
<style>
/* 全局修复Element Plus下拉框样式 */
.custom-dropdown-popper {
  border-radius: 16px !important;
  border: 1px solid var(--border-primary) !important;
  background: var(--card-bg) !important;
  padding: 8px 0 !important;
  box-shadow: var(--shadow-medium) !important;
  overflow: hidden;
}

/* 浅色模式下的阴影效果 */
[data-theme="light"] .custom-dropdown-popper {
  box-shadow: 0 4px 20px rgba(147, 51, 234, 0.15) !important;
}

/* 深色模式下的发光效果 */
[data-theme="dark"] .custom-dropdown-popper {
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3) !important;
}

.custom-dropdown-popper .el-dropdown-menu__item {
  border-radius: 12px !important;
  margin: 2px 8px !important;
  padding: 12px 16px !important;
  transition: all 0.3s ease !important;
  color: var(--text-primary) !important;
  font-weight: 500;
}

.custom-dropdown-popper .el-dropdown-menu__item:hover {
  background: var(--hover-bg) !important;
  color: var(--text-primary) !important;
  transform: translateX(4px);
}

/* 修复侧边栏下拉菜单的亮暗模式适配 */
.el-popper[data-popper-placement] {
  background: var(--card-bg) !important;
  border: 1px solid var(--border-primary) !important;
  box-shadow: var(--shadow-medium) !important;
  border-radius: 16px !important;
  overflow: hidden;
}

.el-dropdown-menu {
  background: var(--card-bg) !important;
  border: 1px solid var(--border-primary) !important;
  box-shadow: var(--shadow-medium) !important;
  border-radius: 16px !important;
  overflow: hidden;
}

/* 浅色模式下的阴影效果 */
[data-theme="light"] .el-popper[data-popper-placement],
[data-theme="light"] .el-dropdown-menu {
  box-shadow: 0 4px 20px rgba(147, 51, 234, 0.15) !important;
}

/* 深色模式下的发光效果 */
[data-theme="dark"] .el-popper[data-popper-placement],
[data-theme="dark"] .el-dropdown-menu {
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3) !important;
}

.el-dropdown-menu__item {
  color: var(--text-primary) !important;
  background: transparent !important;
}

.el-dropdown-menu__item:hover {
  background: var(--hover-bg) !important;
  color: var(--text-primary) !important;
}

/* 深色模式下的下拉菜单样式 */
.dark .el-popper[data-popper-placement] {
  background: var(--card-bg) !important;
  border: 1px solid var(--border-primary) !important;
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3) !important;
  border-radius: 16px !important;
  overflow: hidden;
}

.dark .el-dropdown-menu {
  background: var(--card-bg) !important;
  border: 1px solid var(--border-primary) !important;
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3) !important;
  border-radius: 16px !important;
  overflow: hidden;
}

/* 浅色模式下的下拉菜单样式 */
[data-theme="light"] .el-popper[data-popper-placement],
[data-theme="light"] .el-dropdown-menu {
  box-shadow: 0 4px 20px rgba(147, 51, 234, 0.15) !important;
}

.dark .el-dropdown-menu__item {
  color: var(--text-primary) !important;
  background: transparent !important;
}

.dark .el-dropdown-menu__item:hover {
  background: var(--hover-bg) !important;
  color: var(--text-primary) !important;
}

/* 主题切换冷却状态样式 */
.cooldown-item {
  opacity: 0.6 !important;
  cursor: not-allowed !important;
  background: var(--bg-tertiary) !important;
}

.cooldown-item:hover {
  background: var(--bg-tertiary) !important;
  transform: none !important;
}

.cooldown-text {
  color: var(--text-tertiary) !important;
  font-size: 12px;
  font-weight: 500;
}
.el-dropdown {
  width: 100% !important;
  display: block !important;
}
</style>