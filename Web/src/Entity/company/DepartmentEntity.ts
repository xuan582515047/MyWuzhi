// DepartmentEntity.ts

import type { EmployeeVO } from "./EmployeeEntity.ts";

/**
 * 部门信息
 */
export interface DepartmentInfo {
    id: string;
    name: string;
    description?: string;
    companyId: string;
    createTime?: string;
    updateTime?: string;
    operatorEmpId: string;
}

/**
 * 添加部门请求参数
 */
export interface DepartmentAddRequest {
    name: string;
    description?: string;
    companyId: string;
    parentId: string;
}

/**
 * 更新部门请求参数
 */
export interface DepartmentUpdateRequest {
    companyId: string;
    depId: string;
    name?: string;
    description?: string;
}

/**
 * 更换部门父级请求参数
 */
export interface DepartmentChangeParentRequest {
    companyId: string;
    nodeId: string;
    parentId: string | null;
}

/**
 * 删除部门请求参数
 */
export interface DepartmentDeleteRequest {
    companyId: string;
    departmentId: string;
}

/**
 * 部门统计信息查询请求
 */
export interface DepartmentStatisticsRequest {
    departmentId: string;
}

/**
 * 部门统计信息
 */
export interface DepartmentStatistics {
    departmentId: string;
    departmentName: string;
    totalEmployees: number;
    totalSubDepartments: number;
    totalMissions: number;
    missionStatusCount: Record<string, number>;
    completedMissionsThisMonth: number;
    inProgressMissions: number;
    overdueMissions: number;
}
