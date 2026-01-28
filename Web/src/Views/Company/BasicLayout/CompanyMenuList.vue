<script setup lang="ts">
//==========================> 导入依赖 <==========================
import {defineProps, defineEmits, computed, ref, onMounted} from 'vue';
import type {CompanyMenuData} from "@/Views/Company/index.vue";
import type {CompanyDetail, CompanyInfo} from "@/Entity/company/CompanyEntity.ts";
import {ToolApi} from "@/Api/ToolApi.ts";
import {CompanyApi} from "@/Api/company/CompanyApi.ts";
import type {ToolUseNode} from "@/Entity/ToolEntity.ts";


//==========================> 变量定义 <==========================
const props = defineProps({
  currentCompany: { type: Object as () => CompanyInfo, required: true },
  baseMenuList: { type: Array<CompanyMenuData>, required: true },
  toolMenuTree: { type: Array<ToolUseNode>, required: true },
  activeMenuId: { type: String, required: true }
})

const emits = defineEmits(['select', 'add'])

// 添加悬停状态
const hoveredItem = ref<string | null>(null);
//==========================> 方法定义 <==========================

/**
 * 选中菜单项
 * @param menuId
 */
const selectMenu = async (menuId: string) => {
  emits('select', menuId);
}

/**
 * 添加操作
 */
const handleAdd = () => {
  emits('add');
}

/**
 * 判断菜单项是否被选中
 * @param menuId
 */
const isMenuActive = (menuId: string) => {
  return props.activeMenuId === menuId;
}

/**
 * 设置悬停状态
 */
const setHovered = (menuId: string | null) => {
  hoveredItem.value = menuId;
}

const baseMenuList = computed(() => {
  return props.baseMenuList.filter(menu => menu.id !== 'company');
});

</script>

<template>
  <div class="left-sidebar">
    <!-- 公司卡片 - 使用ElementPlus Card组件 -->
    <el-card class="sidebar-item company-item"
             :class="{ active: isMenuActive('company'), hover: hoveredItem === 'company' }"
             @click="selectMenu('company')"
             @mouseenter="setHovered('company')"
             @mouseleave="setHovered(null)"
             shadow="hover">

      <div class="company-card-content">
        <!-- 公司头部信息 -->
        <div class="card-top">
          <el-text class="company-name" line-clamp="2">{{ props.currentCompany.name }}</el-text>
          <el-tag
              :class="[
              props.currentCompany.status === 'active' ? 'status-tag-active' : 'status-tag-inactive',
              { 'active-status-tag': isMenuActive('company') }
            ]"
              size="small"
              class="status-tag"
          >
            {{ props.currentCompany.status === 'active'? '运营中' : '未运营' }}
          </el-tag>
        </div>

        <!-- 职位信息 -->
        <div class="info-item">
          <span class="label">所属部门：</span>
          <span class="value">{{ props.currentCompany?.department }}</span>
        </div>
      </div>
    </el-card>

    <!-- 基础菜单项 -->
    <div v-for="menu in baseMenuList"
         :key="menu.id"
         class="sidebar-item menu-item"
         :class="{ active: isMenuActive(menu.id), hover: hoveredItem === menu.id }"
         @click="selectMenu(menu.id)"
         @mouseenter="setHovered(menu.id)"
         @mouseleave="setHovered(null)">

      <div class="menu-content">
        <div class="menu-icon"></div>
        <span class="menu-label">{{ menu.name }}</span>
        <div class="menu-arrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8.59 16.59L13.17 12L8.59 7.41L10 6L16 12L10 18L8.59 16.59Z" fill="currentColor"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 添加按钮（单独处理） -->
    <div class="sidebar-item add-item"
         :class="{ hover: hoveredItem === 'add' }"
         @click="handleAdd"
         @mouseenter="setHovered('add')"
         @mouseleave="setHovered(null)">
      <div class="menu-content">
        <div class="add-icon">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 13H13V19H11V13H5V11H11V5H13V11H19V13Z" fill="currentColor"/>
          </svg>
        </div>
        <span class="add-label">添加工具</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.left-sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 15px;
  min-height: 100%;
  user-select: none;
  position: relative;
  overflow: hidden;
}

.left-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(121, 134, 203, 0.3), transparent);
}

.sidebar-item {
  border-radius: 16px;
  background: var(--bg-card);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-primary);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.sidebar-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.sidebar-item:hover::before {
  left: 100%;
}

.sidebar-item:hover {
  background: var(--bg-card-hover);
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
  border-color: var(--primary-color);
}

.sidebar-item.hover {
  transform: translateY(-1px);
}

.sidebar-item.active {
  background: var(--gradient-primary);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-primary);
  animation: gentleGlow 3s ease-in-out infinite alternate;
}

@keyframes gentleGlow {
  0% {
    box-shadow: 0 6px 20px rgba(121, 134, 203, 0.4);
  }
  100% {
    box-shadow: 0 8px 30px rgba(121, 134, 203, 0.6);
  }
}

/* 公司卡片特殊样式 */
.company-item {
  padding: 20px;
  min-height: auto;
  display: flex;
  flex-direction: column;
  position: relative;
}

.company-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20px;
  right: 20px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(121, 134, 203, 0.3), transparent);
}

.company-item.active .company-name,
.company-item.active .label,
.company-item.active .value,
.company-item.active .content-text {
  color: white;
}

.company-item.active .content-links .el-link {
  color: white !important;
}

/* 公司卡片内部样式 */
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 8px;
}

.company-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
  transition: color 0.3s ease;
  flex: 1;
  line-height: 1.3;
}

.status-tag {
  font-size: 10px;
  color: white;
  border-radius: 10px;
  font-weight: 500;
  border: none;
  flex-shrink: 0;
  padding: 4px 8px;
  transition: all 0.3s ease;
}

.status-tag-active {
  background: #67c23a;
}

.status-tag-inactive {
  background: var(--danger-color);
}

.company-item.active .status-tag {
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
  line-height: 1.4;
  gap: 8px;
}

.label {
  color: var(--text-primary);
  font-size: 12px;
  min-width: 35px;
  flex-shrink: 0;
  font-weight: 500;
  transition: color 0.3s ease;
}

.value {
  color: var(--text-secondary);
  font-size: 12px;
  transition: color 0.3s ease;
}

.content-text {
  color: var(--text-secondary);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
  flex: 1;
}

.content-links {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  font-size: 12px;
  flex: 1;
}

.content-links .el-link {
  font-size: 12px;
  color: var(--primary-color);
  transition: color 0.3s ease;
}

.content-links .el-link:hover {
  transform: translateY(-1px);
}

/* 普通菜单项样式 */
.menu-item {
  padding: 16px 20px;
  min-height: 60px;
  display: flex;
  align-items: center;
}

.menu-item.active {
  background: var(--gradient-primary);
}

.menu-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 10px;
}

.menu-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(121, 134, 203, 0.1);
  transition: all 0.3s ease;
}

.menu-item.active .menu-icon {
  background: rgba(255, 255, 255, 0.3);
}

.menu-label {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
  transition: color 0.3s ease;
  flex: 1;
}

.menu-item.active .menu-label {
  color: white;
}

.menu-arrow {
  opacity: 0;
  transform: translateX(-5px);
  transition: all 0.3s ease;
}

.menu-item:hover .menu-arrow {
  opacity: 1;
  transform: translateX(0);
}

.menu-item.active .menu-arrow {
  opacity: 1;
  transform: translateX(0);
}

.menu-arrow svg {
  color: var(--primary-color);
  transition: color 0.3s ease;
}

.menu-item.active .menu-arrow svg {
  color: white;
}

/* 添加按钮特殊样式 */
.add-item {
  padding: 16px 20px;
  min-height: 60px;
  display: flex;
  align-items: center;
  background: var(--bg-secondary) !important;
  border: 1.5px dashed var(--border-primary) !important;
  transition: all 0.3s ease;
}

.add-item:hover {
  background: var(--bg-tertiary) !important;
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover) !important;
  border-color: var(--primary-color) !important;
  border-style: solid !important;
}

.add-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.add-icon svg {
  color: #5e35b1;
  transition: color 0.3s ease;
}

.add-item:hover .add-icon {
  transform: rotate(90deg);
}

.add-label {
  color: #5e35b1;
  font-weight: 500;
  font-size: 14px;
  transition: color 0.3s ease;
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .left-sidebar {
    padding: 16px 12px;
    gap: 12px;
  }

  .company-item {
    padding: 16px;
  }

  .menu-item, .add-item {
    padding: 14px 16px;
    min-height: 55px;
  }

  .company-name {
    font-size: 15px;
  }

  .menu-label, .add-label {
    font-size: 13px;
  }
}

/* 覆盖ElementPlus Card组件默认样式 */
:deep(.el-card__body) {
  padding: 0 !important;
}
</style>