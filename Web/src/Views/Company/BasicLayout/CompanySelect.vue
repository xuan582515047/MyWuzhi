<script setup lang="ts">
import { ref, onMounted, defineEmits, defineExpose} from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { CompanyApi } from '@/Api/company/CompanyApi.ts';
import type {CompanyInfo} from "@/Entity/company/CompanyEntity.ts";

//=======================> 通信定义 <=======================
const emits = defineEmits(['select','add'])

//=======================> 变量定义 <=======================
const companyList = ref<CompanyInfo[]>([]);
const loading = ref(false);

//=======================> 方法定义 <=======================

/**
 * 选中公司
 */
const selectCompany = (company: CompanyInfo) => {
  emits('select', company)
}

/**
 * 添加公司
 */
const addCompany = () => {
  emits('add')
};

/**
 * 加载企业列表
 */
const loadCompanyList = async () => {
  loading.value = true;
  companyList.value = await CompanyApi.getCompanyList();
  loading.value = false;
};

// 生命周期钩子
onMounted(() => {
  loadCompanyList();
});

// 暴露方法给父组件调用
defineExpose({
  loadCompanyList
});
</script>

<template>
  <div class="company-select-container">
    <div class="company-select-layout">
      <!-- 企业选择卡片区域 -->
      <div class="company-select-card">
        <div class="company-select-form">
          <div class="form-header">
            <h3>请选择您的组织或企业</h3>
            <p>选择一个企业开始使用，或者创建新的企业</p>
          </div>

          <!-- 企业列表 -->
          <div class="company-list-section" v-loading="loading">
            <div class="company-list">
              <!-- 企业卡片 -->
              <el-card 
                class="company-card"
                :class="{ 'active-card': false }"
                @click="selectCompany(company)"
                shadow="hover"
                v-for="company in companyList"
                :key="company.id"
              >
                <div class="card-top">
                  <el-text class="company-name" line-clamp="2">{{ company.name }}</el-text>
                  <el-tag
                    :class="[
                      company.status === 'active' ? 'status-tag-active' : 'status-tag-inactive',
                      { 'active-status-tag': false }
                    ]"
                    size="small"
                    class="status-tag"
                  >
                    {{ company.status === 'active'? '运营中' : '未运营' }}
                  </el-tag>
                </div>

                <div class="info-item">
                  <span class="label">所属部门：</span>
                  <span class="value">{{ company.department }}</span>
                </div>

                <div class="info-row">
                  <span class="label">任务列表：</span>
                  <div class="content-text">
                    <div v-if="company.missionList.length > 0" v-for="mission in company.missionList">
                      <div>{{ mission.description }}</div>
                      <div>{{ mission.status }}</div>
                    </div>
                    <div v-else>暂无任务</div>
                  </div>
                </div>
              </el-card>

              <!-- 添加企业卡片 -->
              <el-card 
                class="company-card add-card"
                @click="addCompany"
                shadow="hover"
              >
                <div class="add-card-content">
                  <el-icon class="add-icon" :size="32">
                    <Plus />
                  </el-icon>
                  <div class="add-text">添加企业</div>
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.company-select-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--gradient-bg);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 整体布局 */
.company-select-layout {
  display: flex;
  width: 100%;
  max-width: 900px;
  height: 700px;
  background: var(--bg-secondary);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-heavy);
  border: 1px solid var(--border-light);
  overflow: hidden;
  position: relative;
}

/* 暗色模式下的卡片边缘发光效果 */
.dark .company-select-layout {
  box-shadow: var(--shadow-heavy), var(--card-edge-glow);
}

/* 企业选择卡片区域 */
.company-select-card {
  width: 100%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
  height: 100%;
  overflow: hidden;
}

.company-select-form {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.back-section {
  text-align: left;
  margin-bottom: 16px;
}

.back-btn {
  color: var(--text-secondary);
  padding: 4px 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  color: var(--primary-color);
}

.form-header h3 {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.form-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.company-list-section {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

.company-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 28px;
  align-items: start;
}

.company-card {
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  background: var(--card-bg);
  border: 1px solid var(--border-light);
  height: fit-content;
}

.company-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  border-color: var(--primary-color);
}

.company-card.active-card {
  background: var(--gradient-primary);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-primary);
}

.company-card.active-card .company-name,
.company-card.active-card .label,
.company-card.active-card .value,
.company-card.active-card .content-text {
  color: white;
}

.company-card.active-card .status-tag-active {
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary-color);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
}

.company-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
  line-height: 1.4;
  flex: 1;
  margin-right: 12px;
}

.status-tag {
  font-size: 11px;
  color: white;
  border-radius: 10px;
  font-weight: 500;
  border: none;
  padding: 2px 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

.status-tag-active {
  background: #67c23a;
}

.status-tag-inactive {
  background: var(--danger-color);
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  align-items: flex-start;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  align-items: flex-start;
}

.label {
  font-weight: 500;
  color: var(--text-secondary);
  min-width: 50px;
  flex-shrink: 0;
  font-size: 14px;
}

.value, .content-text {
  color: var(--text-primary);
  flex: 1;
  line-height: 1.5;
  font-size: 14px;
}

.add-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 140px;
  border: 2px dashed var(--border-primary);
  background: var(--bg-secondary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: fit-content;
}

.add-card:hover {
  border-color: var(--primary-color);
  background: var(--bg-primary);
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.08);
}

.add-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--text-secondary);
}

.add-icon {
  margin-bottom: 8px;
  color: var(--primary-color);
}

.add-text {
  font-weight: 500;
  font-size: 14px;
}

/* 暗色模式适配 */
.dark .company-card {
  background: var(--card-bg);
  border-color: var(--border-light);
}

.dark .add-card {
  background: var(--bg-secondary);
  border-color: var(--border-primary);
}

/* 滚动条样式 */
.company-list-section::-webkit-scrollbar {
  width: 6px;
}

.company-list-section::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 3px;
}

.company-list-section::-webkit-scrollbar-thumb {
  background: var(--border-primary);
  border-radius: 3px;
}

.company-list-section::-webkit-scrollbar-thumb:hover {
  background: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .company-select-layout {
    max-width: 100%;
    margin: 0;
    border-radius: 0;
    height: 100vh;
  }
  
  .company-select-card {
    padding: 24px 20px;
    max-height: calc(100vh - 40px);
  }
  
  .company-list {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .form-header h3 {
    font-size: 20px;
  }
  
  .card-top {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .company-name {
    margin-right: 0;
  }
}

@media (max-width: 480px) {
  .company-select-container {
    padding: 0;
  }
  
  .company-select-card {
    padding: 20px 16px;
  }
  
  .company-list {
    gap: 12px;
  }
}
</style>