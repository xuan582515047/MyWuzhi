<script setup lang="ts">
import { computed } from 'vue';
import { useThemeStore } from '@/Stores/ThemeStore.ts';

// 使用主题Store
const themeStore = useThemeStore();

// 计算属性，根据当前主题显示不同的样式
const logoClass = computed(() => ({
  'logo': true,
  'dark-logo': themeStore.isDarkMode,
}));
</script>

<template>
  <div :class="logoClass" style="margin-left: 10px">
    <img src="../../Assets/logo.png" alt="logo" height="50" class="logo-image">
    <div class="logo-text">
      无知
    </div>
  </div>
</template>

<style scoped>
.logo {
  user-select: none;
  width: auto;
  font-size: 20px;
  text-align: center;
  font-weight: bold;
  display: flex;
  align-items: center;
  animation: logoFloat 3s ease-in-out infinite;
}

.logo-image {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.logo-text {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 5px;
  font-weight: 700;
  letter-spacing: 1px;
  /* 亮色模式下的字体颜色 */
  color: var(--primary-purple);
}

/* 暗色模式下的字体颜色 */
.dark-logo .logo-text {
  color: var(--primary-blue-light);
}

@keyframes logoFloat {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-3px);
  }
  100% {
    transform: translateY(0px);
  }
}
</style>
