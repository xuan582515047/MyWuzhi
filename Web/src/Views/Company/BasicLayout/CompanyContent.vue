<script setup lang="ts">
import {defineProps, onMounted, ref} from 'vue';
import BaseInfo from "@/Views/Company/Components/BaseInfo/BaseInfo.vue";
import Schedule from "@/Views/Company/Components/BaseInfo/Schedule.vue";
import type {CompanyMenuData} from "@/Views/Company/index.vue";
import type {CompanyDetail, CompanyInfo, CompanyNode, PermissionNode} from "@/Entity/company/CompanyEntity.ts";
import EmployeeManager from "@/Views/Company/Components/EmployeeManager.vue";
import {DepartmentApi} from "@/Api/company/DepartmentApi.ts";
import {CompanyApi} from "@/Api/company/CompanyApi.ts";
import DatabaseManager from "@/Views/Company/Components/DatabaseManager/DatabaseManager.vue";
import ToolUse from "@/Views/Company/Components/ToolUse.vue";
import type {ToolUseNode} from "@/Entity/ToolEntity.ts";
import Statistics from "@/Views/Company/Components/BaseInfo/Statistics.vue";


//==========================> 通信定义 <==========================
// currentCompany 必须存在，因为企业选择已在登录页完成
const props = defineProps({
  baseMenuList: { type: Array<CompanyMenuData>, required: true },
  toolMenuTree: { type: Array<ToolUseNode>, required: true },
  currentMenuId: { type: String, required: true },
  currentCompany: { type: Object as () => CompanyInfo, required: true },
  companyDetail: { type: Object as () => CompanyDetail, required: true }
})

//==========================> 变量定义 <==========================
const permissionList = ref<PermissionNode[]>([])
const companyTree = ref<CompanyNode | null>(null)

onMounted(async () => {
  companyTree.value = await CompanyApi.getCompanyTree(props.currentCompany.id)
  permissionList.value = await CompanyApi.getCompanyPermission(props.currentCompany.id)
})
</script>
<template>
  <div style="height: auto;">
    <div v-if="currentMenuId === 'company'" >
      <BaseInfo :company-detail="props.companyDetail"/>
      <el-divider/>
      <Statistics :company-id="props.currentCompany.id"/>
      <el-divider/>
      <Schedule :company-id="props.currentCompany.id"/>
    </div>
    <div v-else-if="currentMenuId === 'employee'" >
      <el-skeleton :loading="!companyTree" animated>
        <template #template>
          <div style="padding: 20px;">
            <el-skeleton-item variant="h3" style="width: 40%; margin-bottom: 20px;" />
            <el-skeleton-item variant="rect" style="height: 400px;" />
          </div>
        </template>
        <template #default>
          <EmployeeManager v-if="companyTree"
                          :department-tree="companyTree"
                          :permission-tree="permissionList"
                          :company-id="props.currentCompany.id"/>
        </template>
      </el-skeleton>
    </div>
    <div v-else-if="currentMenuId === 'database-personal'" >
      <DatabaseManager :company-id="props.currentCompany.id"/>
    </div>
    <div v-else-if="currentMenuId === 'database-company'" >
      <DatabaseManager :company-id="props.currentCompany.id"/>
    </div>
    <div v-else-if="currentMenuId === 'toolUse'" >
      <ToolUse :company-id="props.currentCompany.id"/>
    </div>
    <div v-else>
      <div v-for="toolMenu in props.toolMenuTree" :key="toolMenu.id">
        <iframe :src="toolMenu.url" width="100%" height="100%"
                v-if="props.currentMenuId === toolMenu.id"/>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>