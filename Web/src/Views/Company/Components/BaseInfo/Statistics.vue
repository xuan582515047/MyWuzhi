<script setup lang="ts">
import { defineProps, ref, onMounted } from 'vue'
import { CompanyApi } from '@/Api/company/CompanyApi.ts'
import { ElMessage } from 'element-plus'
import type { CompanyStatistics } from '@/Entity/company/CompanyEntity.ts'
import {Loading} from "@element-plus/icons-vue";

//==========================> 通信定义 <==========================
const props = defineProps({
  companyId: { type: String, required: true }
})

//==========================> 变量定义 <==========================
const isLoading = ref(false)
const statistics = ref<CompanyStatistics>({
  companyId: '',
  companyName: '',
  totalEmployees: 0,
  totalDepartments: 0,
  totalMissions: 0,
  missionStatusCount: {},
  activeEmployees: 0,
  newEmployeesThisMonth: 0,
  newMissionsThisMonth: 0,
  overdueMissions: 0
})

//==========================> 方法定义 <==========================

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  isLoading.value = true
  try {
    const data = await CompanyApi.getCompanyStatistics({ companyId: props.companyId })
    statistics.value = data
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    isLoading.value = false
  }
}

//==========================> 生命周期 <==========================
onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div class="statistics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title-info">
        <h2 class="page-title">数据统计</h2>
        <span v-if="statistics.companyName" class="company-name">{{ statistics.companyName }}</span>
      </div>
      <el-button type="primary" :icon="'Refresh'" @click="loadStatistics" :loading="isLoading">
        刷新数据
      </el-button>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon employees">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.totalEmployees }}</div>
            <div class="stat-label">员工总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon departments">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2l-5.5 9h11z M12 5.84L13.93 9h-3.87z M17.5 13c-2.49 0-4.5 2.01-4.5 4.5s2.01 4.5 4.5 4.5 4.5-2.01 4.5-4.5-2.01-4.5-4.5-4.5z M5 21.5h8v-8H5v8z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.totalDepartments }}</div>
            <div class="stat-label">部门数量</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon missions">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.totalMissions }}</div>
            <div class="stat-label">任务总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon active-employees">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.activeEmployees }}</div>
            <div class="stat-label">活跃员工</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon new-employees">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.newEmployeesThisMonth }}</div>
            <div class="stat-label">本月新员工</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon new-missions">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.newMissionsThisMonth }}</div>
            <div class="stat-label">本月新任务</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon overdue-missions">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.overdueMissions }}</div>
            <div class="stat-label">逾期任务</div>
          </div>
        </div>
      </el-card>


    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-container">
      <el-icon class="is-loading" :size="40">
        <Loading />
      </el-icon>
      <p>正在加载统计数据...</p>
    </div>
  </div>
</template>

<style scoped>
.statistics-container {
  padding: 20px;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-primary);
}

.title-info {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
}

.company-name {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  background: var(--bg-card);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
  border-color: var(--primary-color);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 30px;
  height: 30px;
  color: white;
}

.stat-icon.employees {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.departments {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.missions {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.active-employees {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon.new-employees {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-icon.new-missions {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.stat-icon.overdue-missions {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}



.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.loading-container p {
  margin-top: 16px;
  color: var(--text-secondary);
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-container {
    padding: 16px;
  }
  
  .title-info {
    flex-direction: column;
    gap: 4px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .company-name {
    font-size: 14px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .stat-content {
    padding: 16px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    margin-right: 16px;
  }
  
  .stat-icon svg {
    width: 24px;
    height: 24px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .page-title {
    color: var(--text-primary);
  }
  
  .stat-value {
    color: var(--text-primary);
  }
  
  .stat-label {
    color: var(--text-secondary);
  }
}
</style>
