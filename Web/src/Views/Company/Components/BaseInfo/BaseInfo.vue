<script lang="ts" setup>
import {ElTag, ElSkeleton, ElIcon } from 'element-plus'
import {Phone, Document, Check} from '@element-plus/icons-vue'
import type {CompanyDetail} from "@/Entity/company/CompanyEntity.ts";
import { CompanyStatus, CompanyApproveStatus } from "@/Entity/company/CompanyEntity.ts";
import { TimeFormatUtil } from "@/Util/TimeFormatUtil.ts";

//==========================> 变量定义 <==========================
// 通信变量
const props = defineProps({
  companyDetail: { type: Object as () => CompanyDetail, required: true },
  loading: { type: Boolean, default: false }
})

//==========================> 方法定义 <==========================
/**
 * 获取公司状态文本
 */
const getStatusText = (status: CompanyStatus): string => {
  switch (status) {
    case CompanyStatus.ACTIVE:
      return '运营中';
    case CompanyStatus.INACTIVE:
      return '未运营';
    default:
      return '未知';
  }
};

/**
 * 获取认证状态文本
 */
const getApproveStatusText = (status: CompanyApproveStatus): string => {
  switch (status) {
    case CompanyApproveStatus.APPROVED:
      return '已认证';
    case CompanyApproveStatus.NOT_APPROVED:
      return '未认证';
    default:
      return '未知';
  }
};

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime: string): string => {
  return TimeFormatUtil.getTimeView(new Date(dateTime).toISOString());
};
</script>

<template>
  <div class="company-info-container">
    <!-- 加载状态 -->
    <el-skeleton :loading="loading" animated>
      <template #default>
        <!-- 公司头部信息卡片 -->
        <div class="company-header-card">
          <div class="company-visual">
            <div class="company-logo">
              <span class="logo-text">{{ props.companyDetail.name.charAt(0).toUpperCase() }}</span>
            </div>
            <div class="company-title-info">
              <h1 class="company-name">{{ props.companyDetail.name }}</h1>
              <div class="company-short-name" v-if="props.companyDetail.shortName">
                {{ props.companyDetail.shortName }}
              </div>
            </div>
          </div>
          
          <div class="company-status-tags">
            <el-tag 
              :type="props.companyDetail.status === CompanyStatus.ACTIVE ? 'success' : 'danger'"
              size="large"
              effect="dark"
              class="status-tag"
            >
              {{ getStatusText(props.companyDetail.status) }}
            </el-tag>
            <el-tag 
              :type="props.companyDetail.approveStatus === CompanyApproveStatus.APPROVED ? 'success' : 'warning'"
              size="large"
              effect="dark"
              class="status-tag"
            >
              {{ getApproveStatusText(props.companyDetail.approveStatus) }}
            </el-tag>
          </div>
        </div>

        <!-- 信息卡片网格 -->
        <div class="info-grid">
          <!-- 基本信息卡片 -->
          <div class="info-card">
            <div class="card-header">
              <div class="card-icon">
                <el-icon><Document /></el-icon>
              </div>
              <h3 class="card-title">基本信息</h3>
            </div>
            <div class="card-content">
              <div class="info-item">
                <div class="info-label">公司全名</div>
                <div class="info-value">{{ props.companyDetail.name }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">所属行业</div>
                <div class="info-value">{{ props.companyDetail.industry }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">创建时间</div>
                <div class="info-value" v-if="props.companyDetail.createTime">
                  {{ formatDateTime(props.companyDetail.createTime) }}
                </div>
              </div>
            </div>
          </div>

          <!-- 联系方式卡片 -->
          <div class="info-card">
            <div class="card-header">
              <div class="card-icon">
                <el-icon><Phone /></el-icon>
              </div>
              <h3 class="card-title">联系方式</h3>
            </div>
            <div class="card-content">
              <div class="info-item">
                <div class="info-label">联系电话</div>
                <div class="info-value">{{ props.companyDetail.phone }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">电子邮箱</div>
                <div class="info-value">{{ props.companyDetail.email }}</div>
              </div>
            </div>
          </div>

          <!-- 企业认证卡片 -->
          <div class="info-card" v-if="props.companyDetail.creditCode || props.companyDetail.legalRepresentative">
            <div class="card-header">
              <div class="card-icon">
                <el-icon><Check /></el-icon>
              </div>
              <h3 class="card-title">企业认证</h3>
            </div>
            <div class="card-content">
              <div class="info-item" v-if="props.companyDetail.creditCode">
                <div class="info-label">统一社会信用代码</div>
                <div class="info-value">{{ props.companyDetail.creditCode }}</div>
              </div>
              <div class="info-item" v-if="props.companyDetail.legalRepresentative">
                <div class="info-label">法定代表人</div>
                <div class="info-value">{{ props.companyDetail.legalRepresentative }}</div>
              </div>
            </div>
          </div>

          <!-- 公司简介卡片 -->
          <div class="info-card full-width" v-if="props.companyDetail.description">
            <div class="card-header">
              <div class="card-icon">
                <el-icon><Document /></el-icon>
              </div>
              <h3 class="card-title">公司简介</h3>
            </div>
            <div class="card-content">
              <div class="description-content">
                {{ props.companyDetail.description }}
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.company-info-container {
  padding: 24px;
  background-color: var(--bg-primary);
}

/* 公司头部信息卡片 */
.company-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  margin-bottom: 24px;
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-primary);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.company-header-card:hover {
  box-shadow: var(--shadow-medium);
}

.company-visual {
  display: flex;
  align-items: center;
  gap: 16px;
}

.company-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: var(--gradient-primary);
  border-radius: 16px;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: white;
}

.company-title-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.company-name {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.company-short-name {
  color: var(--text-tertiary);
  font-size: 14px;
  font-weight: 500;
}

.company-status-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.status-tag {
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 30px;
  box-shadow: var(--shadow-light);
  transition: all 0.2s ease;
}

.status-tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

/* 信息卡片网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.info-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--border-primary);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-medium);
}

.info-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--gradient-primary);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
}

.card-icon:hover {
  transform: scale(1.05);
}

.card-icon :deep(.el-icon) {
  font-size: 20px;
  color: white;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-tertiary);
}

.info-value {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  word-break: break-word;
}

.description-content {
  font-size: 16px;
  line-height: 1.6;
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-skeleton) {
  background-color: var(--card-bg);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-light);
}

:deep(.el-skeleton__item) {
  background: var(--gradient-overlay);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .company-header-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .company-status-tags {
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
