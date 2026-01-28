<script setup lang="ts">
import {ref, defineProps, defineEmits, onMounted} from "vue";
import type {HomeMenu} from "@/Views/New/index.vue";

//==========================> 变量定义 <==========================
const activeBtn = ref<string>('')
const props = defineProps({
  menuList: {type: Array<HomeMenu>, required: true},
  firstIndex: {type: Number, default: 0}
})
const emits = defineEmits(['select'])
const btnList = props.menuList;

onMounted(async () => {
  activeBtn.value = btnList[props.firstIndex]!.label;
})

//==========================> 方法定义 <==========================
const handleButtonClick = (index: number, btn: string) => {
  activeBtn.value = btn
  emits('select', index);
}
</script>

<template>
  <br/>
  <div v-for="(btn, index) in btnList" :key="btn.label"
       style="width: 300px">
    <el-button
        round
        class="sidebar-btn"
        size="large"
        :class="{ active: activeBtn === btn.label }"
        @click="handleButtonClick(index, btn.label)"
        :icon="btn.icon">
      <span>{{ btn.name }}</span>
    </el-button>
  </div>
</template>

<style scoped>
/* 添加整体动画效果 */
div {
  display: flex;
  justify-content: center;
  animation: slideInMenu 0.6s ease-out;
}

@keyframes slideInMenu {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.sidebar-btn{
  margin: 5px 0;
  justify-content: flex-start;
  height: 60px;
  /* 修改为紫色-白（偏蓝）的渐变背景 */
  background: linear-gradient(135deg, var(--primary-gradient-end) 0%, var(--primary-gradient-mid) 100%);
  color: var(--text-primary);
  font-size: 16px;
  border-radius: 20px;
  transition: all 0.3s ease, color 0s;
  width: 90% !important;
  border: 1px solid var(--border-primary);
  box-shadow: var(--shadow-light);
  position: relative;
  overflow: hidden;
  transform-origin: center;
}

.sidebar-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.sidebar-btn:hover::before {
  left: 100%;
}

.sidebar-btn:hover {
  /* 悬停时的渐变效果 */
  background: linear-gradient(135deg, var(--primary-gradient-mid) 0%, var(--primary-gradient-start) 100%);
  color: var(--text-primary);
  transform: translateY(-3px) scale(1.02);
  box-shadow: var(--shadow-medium);
  border-color: var(--border-secondary);
  transition: all 0.3s ease, color 0s;
}

/* 浅色模式下的hover效果增强 */
[data-theme="light"] .sidebar-btn:hover {
  background: linear-gradient(135deg, color-mix(in srgb, var(--primary-purple) 80%, black) 0%, color-mix(in srgb, var(--primary-blue) 80%, black) 100%);
  color: white;
  box-shadow: 0 6px 20px color-mix(in srgb, var(--primary-purple) 60%, transparent);
  border-color: color-mix(in srgb, var(--primary-purple) 80%, black);
}

.sidebar-btn:active{
  background: linear-gradient(135deg, var(--primary-gradient-start) 0%, var(--primary-gradient-mid) 100%);
  transition: 0.1s, color 0s;
  color: var(--text-primary);
  transform: translateY(0) scale(0.98);
}

.sidebar-btn.active {
  /* 激活状态的紫色-白（偏蓝）渐变 */
  background: linear-gradient(135deg, var(--primary-purple) 0%, var(--primary-blue) 100%);
  color: white;
  font-weight: 600;
  box-shadow: var(--shadow-heavy);
  border: 1px solid var(--primary-purple);
  position: relative;
  animation: pulse 2s infinite;
  transition: all 0.3s ease, color 0s;
}

@keyframes pulse {
  0% { box-shadow: var(--shadow-medium); }
  50% { box-shadow: 0 6px 30px var(--primary-purple); }
  100% { box-shadow: var(--shadow-medium); }
}

.sidebar-btn.active::after {
  content: '';
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
}

.sidebar-btn .el-icon {
  margin-right: 8px;
  transition: all 0.3s ease, color 0s;
  position: relative;
  z-index: 1;
}

.sidebar-btn.active .el-icon {
  color: white;
  transform: scale(1.2);
}

.sidebar-btn:hover .el-icon {
  transform: scale(1.1);
}

/* 添加按钮按下效果 */
.sidebar-btn:active {
  transition: all 0.1s ease;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .sidebar-btn {
    height: 50px;
    font-size: 14px;
    border-radius: 15px;
  }
}

/* 深色模式支持 */
.dark .sidebar-btn {
  background: linear-gradient(135deg, var(--dark-bg-mid) 0%, var(--dark-bg-start) 100%);
  color: var(--text-primary);
  border: 1px solid var(--border-primary);
  box-shadow: var(--shadow-light);
}

.dark .sidebar-btn:hover {
  background: linear-gradient(135deg, var(--dark-accent-blue) 0%, var(--dark-accent-purple) 100%);
  color: var(--text-primary);
  box-shadow: var(--shadow-medium);
  border-color: var(--primary-blue);
}

.dark .sidebar-btn:active {
  background: linear-gradient(135deg, var(--gradient-dark-start) 0%, var(--gradient-start) 100%);
  color: var(--text-on-primary);
}

.dark .sidebar-btn.active {
  background: linear-gradient(135deg, #3498db 0%, #9b59b6 100%);
  box-shadow: 0 6px 25px rgba(52, 152, 219, 0.5);
  border: 1px solid #3498db;
}

.dark .sidebar-btn.active::after {
  background: #ecf0f1;
  box-shadow: 0 0 10px rgba(236, 240, 241, 0.8);
}

.dark .sidebar-btn.active .el-icon {
  color: var(--text-on-primary);
}

.dark .sidebar-btn .el-icon {
  color: var(--primary-blue);
}
</style>