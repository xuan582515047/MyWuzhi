<script setup lang="ts">
import { computed, ref } from 'vue';
import { useThemeStore } from '@/Stores/ThemeStore.ts';
import { Sunny, Moon } from '@element-plus/icons-vue';
import {useRoute} from "vue-router";

//==========================> 变量定义 <==========================
const themeStore = useThemeStore();

// 冷却时间相关
const isCooldown = ref(false);
const cooldownTime = ref(0);
const COOLDOWN_DURATION = 3000; // 3秒冷却时间

// 计算属性，根据当前主题显示不同的图标
const themeIcon = computed(() => themeStore.isDarkMode ? Sunny : Moon);

//==========================> 方法定义 <==========================
// 切换主题
const toggleTheme = () => {
  if (isCooldown.value) return;
  themeStore.toggleTheme();
  // 开始冷却
  isCooldown.value = true;
  cooldownTime.value = COOLDOWN_DURATION;
  // 更新倒计时
  const startTime = Date.now();
  const timer = setInterval(() => {
    const elapsed = Date.now() - startTime;
    const remaining = COOLDOWN_DURATION - elapsed;
    if (remaining <= 0) {
      isCooldown.value = false;
      cooldownTime.value = 0;
      clearInterval(timer);
    } else {
      cooldownTime.value = remaining;
    }
  }, 100);
};
</script>

<template>
  <div class="theme-toggle"
    :class="{ 'cooldown': isCooldown }"
    @click="toggleTheme"
  >
    <el-icon :size="20" class="theme-icon">
      <component :is="themeIcon" />
    </el-icon>
    <div v-if="isCooldown" class="cooldown-overlay">
      <span class="cooldown-text">{{ Math.ceil(cooldownTime/1000) }}s</span>
    </div>
  </div>
</template>

<style scoped>
.theme-toggle {
  user-select: none;
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-purple) 0%, var(--primary-blue) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px var(--shadow-medium);
  z-index: 1000;
  transition: all 0.3s ease;
  overflow: hidden;
}

/* 冷却状态样式 */
.theme-toggle.cooldown {
  cursor: not-allowed;
  opacity: 0.7;
}

.cooldown-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(2px);
}

.cooldown-text {
  font-size: 12px;
  font-weight: bold;
  color: #dad6dd !important;
}

.theme-toggle:hover:not(.cooldown) {
  transform: scale(1.1) rotate(10deg);
  box-shadow: 0 6px 16px var(--shadow-heavy);
  background: linear-gradient(135deg, var(--primary-blue-light) 0%, var(--primary-purple) 100%);
}

.theme-toggle:active {
  transform: scale(0.95);
}

.theme-icon {
  transition: all 0.3s ease;
}

.theme-toggle:hover .theme-icon {
  transform: rotate(180deg);
}

/* 添加脉冲动画效果 */
.theme-toggle {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(94, 53, 177, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(94, 53, 177, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(94, 53, 177, 0);
  }
}

/* 添加旋转光环效果 */
.theme-toggle::before {
  content: '';
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent, var(--primary-blue), transparent, var(--primary-purple), transparent);
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: rotate 4s linear infinite;
}

.theme-toggle:hover::before {
  opacity: 0.7;
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 暗色模式样式 */
.dark .theme-toggle {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.dark .theme-toggle:hover {
  background: linear-gradient(135deg, var(--dark-accent-purple) 0%, var(--dark-accent-blue) 100%);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
}

.dark .theme-toggle::before {
  background: conic-gradient(from 0deg, transparent, var(--dark-accent-blue), transparent, var(--dark-accent-purple), transparent);
}
</style>