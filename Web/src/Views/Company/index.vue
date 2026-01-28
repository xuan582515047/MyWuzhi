<script setup lang="ts">

//==========================> 导入依赖 <==========================
import Sidebar from "@/Views/PublicComponents/Sidebar.vue";
import {ref} from "vue";
import {useRouter} from "vue-router";
import {ElMessage, ElMessageBox} from "element-plus";
import CompanyMenuList from "@/Views/Company/BasicLayout/CompanyMenuList.vue";
import CompanyContent from "@/Views/Company/BasicLayout/CompanyContent.vue";
import{type CompanyDetail,  type CompanyInfo, type CompanyAddRequest, type CompanyAuthRequest} from "@/Entity/company/CompanyEntity.ts";
import CompanySelect from "@/Views/Company/BasicLayout/CompanySelect.vue";
import Menu from "@/Views/New/Menu.vue";
import {HOME_MENU_LIST} from "@/Config/DataConfig.ts";
import type {HomeMenu} from "@/Views/New/index.vue";
import CompanyAddDialog from "@/Views/Company/BasicLayout/CompanyAddDialog.vue";
import {CompanyApi} from "@/Api/company/CompanyApi.ts";


//==========================> 类型定义 <==========================
export interface CompanyMenuData {
  id: string
  name: string
}

//==========================> 变量定义 <==========================
const router = useRouter()
const currentCompany = ref<CompanyInfo | null>(null)
const companyDetail = ref<CompanyDetail | null>(null)
const showAddDialog = ref(false)
const companySelectRef = ref<InstanceType<typeof CompanySelect> | null>(null)
const menuList: HomeMenu[] = HOME_MENU_LIST
const baseMenuList: CompanyMenuData[] = [{
  id: 'company',
  name: '公司信息',
},{
  id: 'employee',
  name: '部门和员工管理',
},{
  id: 'database-personal',
  name: '个人知识库',
},{
  id: 'database-company',
  name: '企业知识库',
},{
  id: 'toolUse',
  name: '工具管理',
}]
const activeMenuId = ref('company')
const companyStats = ref({
  totalEmployees: 0,
  totalDepartments: 0,
  totalMissions: 0,
  activeTools: 0
})

//==========================> 方法定义 <==========================

/**
 * 选择菜单
 * @param menuId
 */
const selectMenu = (menuId: string) => {
  activeMenuId.value = menuId
}

/**
 * 选择菜单
 * @param index
 */
const selectMainMenu = (index: number) => {
  if (menuList[index]!.label !== 'company'){
    router.push({name: menuList[index]!.label})
  }
}

/**
 * 选择企业
 */
const selectCompany = async (company: CompanyInfo) => {
  try {
    currentCompany.value = company
    // 并行加载详情和统计数据
    const [detail, stats] = await Promise.all([
      CompanyApi.getCompanyDetail(company.id),
      CompanyApi.getCompanyStatistics({ companyId: company.id })
    ])
    companyDetail.value = detail
    companyStats.value = {
      totalEmployees: stats.totalEmployees || 0,
      totalDepartments: stats.totalDepartments || 0,
      totalMissions: stats.totalMissions || 0,
      activeTools: (stats as any).activeTools || 0
    }
  } catch (error) {
    console.error('加载企业数据失败:', error)
    ElMessage.error('加载企业数据失败')
  }
}

/**
 * 添加企业
 */
const addCompany = () =>{
  showAddDialog.value = true
}

/**
 * 创建企业
 * @param data
 */
const createCompany = async (data: CompanyAddRequest) => {
  await CompanyApi.addCompany(data)
  showAddDialog.value = false
  // 刷新企业列表
  if (companySelectRef.value) {
    await companySelectRef.value.loadCompanyList()
  }
}

const returnBtn = () => {
  currentCompany.value = null
}

const addTool = () => {
  router.push('/tools')
}

</script>


<template>
  <div class="app-container">
    <div v-if="currentCompany === null || companyDetail === null">
      <Sidebar :show-exit-btn="false">
        <template #left-side>
          <Menu :menu-list="menuList"
                :first-index="2"
                @select="selectMainMenu" />
        </template>
        <template #content>
          <CompanySelect ref="companySelectRef"
                         @select="selectCompany"
                         @add="addCompany"/>
        </template>
      </Sidebar>
    </div>
    <div v-else>
      <Sidebar :show-exit-btn="true"
               :return-function="returnBtn">
        <template #left-side>
                    <company-menu-list :base-menu-list="baseMenuList"
                             :active-menu-id="activeMenuId"
                             :current-company="currentCompany"
                             :tool-menu-tree="[]"
                             @add="addTool"
                             @select="selectMenu"/>
        </template>
        <template #content>
          <div class="company-content-container">
            <CompanyContent :base-menu-list="baseMenuList"
                            :current-menu-id="activeMenuId"
                            :company-detail="companyDetail"
                            :current-company="currentCompany"
                            :tool-menu-tree="[]"/>
          </div>
        </template>
      </Sidebar>
    </div>

    <!-- 创建企业对话框 -->
    <CompanyAddDialog v-model:show="showAddDialog"
                      @create="createCompany"/>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--gradient-start);
  transition: all 0.5s ease;
  animation: fadeIn 0.6s ease-out;
}

.company-content-container {
  flex: 1;
  overflow: auto;
  background: var(--content-bg);
  user-select: none;
  position: relative;
  padding: 20px;
  animation: slideIn 0.5s ease-out;
}

.company-list {
  overflow: hidden;
  height: 100%;
  padding: 10px;
}

/* 添加全局动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(10px); }
  to { opacity: 1; transform: translateX(0); }
}

/* 添加微妙的背景动画 */
.app-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse at center, var(--bg-glow) 0%, transparent 70%);
  pointer-events: none;
  transition: all 0.5s ease;
  z-index: 0;
}

/* 为内容容器添加边距 */
.company-content-container {
  position: relative;
  z-index: 1;
}

/* 添加分割线的动画效果 */
:deep(.el-divider) {
  transition: all 0.3s ease;
  animation: fadeSlide 0.8s ease-out;
}

@keyframes fadeSlide {
  0% { opacity: 0; transform: scaleX(0); }
  100% { opacity: 1; transform: scaleX(1); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    flex-direction: column;
  }
  
  .company-content-container {
    padding: 10px;
  }
}
</style>