// CompanyEntity.ts

import type { EmployeeVO } from "./EmployeeEntity.ts";

/**
 * 公司状态枚举
 */
export enum CompanyStatus {
    ACTIVE = 'active',
    INACTIVE = 'inactive'
}

/**
 * 公司认证状态枚举
 */
export enum CompanyApproveStatus {
    APPROVED = 'approved',
    NOT_APPROVED = 'not_approved'
}

/**
 * 操作记录类型枚举
 */
export enum RecordType {
    ROLE = 'role',
    DEPARTMENT = 'department',
    EMPLOYEE = 'employee',
    MISSION = 'mission',
    COMPANY = 'company'
}

/**
 * 操作类型枚举
 */
export enum OperateType {
    ADD = 'add',
    DELETE = 'delete',
    UPDATE = 'update'
}

/**
 * 公司基本信息
 */
export interface CompanyDetail {
    id: string;
    shortName?: string;
    name: string;
    description?: string;
    creditCode?: string;
    legalRepresentative?: string;
    phone: string;
    email: string;
    industry: string;
    status: CompanyStatus;
    approveStatus: CompanyApproveStatus;
    createTime?: string;
}

/**
 * 添加公司请求参数
 */
export interface CompanyAddRequest {
    shortName?: string;
    description?: string;
    name: string;
    phone: string;
    email: string;
    industry: string;
}

/**
 * 更新公司请求参数
 */
export interface CompanyUpdateRequest {
    companyId: string;
    shortName?: string;
    name?: string;
    description?: string;
    phone?: string;
    email?: string;
    industry?: string;
    status?: CompanyStatus;
}

/**
 * 删除公司请求参数
 */
export interface CompanyDeleteRequest {
    companyId: string;
}

/**
 * 公司认证请求参数
 */
export interface CompanyAuthRequest {
    companyId: string;
    name: string;
    creditCode: string;
    legalRepresentative: string;
    industry: string;
}

/**
 * 公司操作记录查询请求参数
 */
export interface CompanyRecordQueryRequest {
    companyId: string;
    type?: RecordType;
    operateType?: OperateType;
    operatorEmpId?: string;
    startTime?: string;
    endTime?: string;
    pageNum: number;
    pageSize: number;
}

/**
 * 公司统计信息查询请求参数
 */
export interface CompanyStatisticsRequest {
    companyId: string;
}

/**
 * 操作记录信息
 */
export interface OperationRecord {
    id: string;
    companyId: string;
    type: RecordType;
    operateType: OperateType;
    targetId: string;
    message: string;
    operatorUserId: string;
    operateTime: string;
}

/**
 * 操作记录分页响应
 */
export interface OperationRecordPageResponse {
    records: OperationRecord[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

/**
 * 权限信息
 */
export interface PermissionNode {
    id: string;
    name: string;
    code: string;
    description?: string;
    parentId?: string;
}

/**
 * 公司节点（用于树形结构）
 */
export interface CompanyNode {
    id: string,
    name: string,
    type: NodeType,
    children: CompanyNode[]
}

/**
 * 节点类型枚举
 */
export enum NodeType {
    DEPARTMENT = 'department',
    EMPLOYEE = 'employee'
}

interface ComMissionInfo {
    description: string;
    status: string;
}

/**
 * 公司列表项
 */
export interface CompanyInfo {
    id: string;
    name: string;
    status: CompanyStatus;
    department: string;
    missionList: ComMissionInfo[];
}

/**
 * 公司统计信息
 */
export interface CompanyStatistics {
    companyId: string;
    companyName: string;
    totalEmployees: number;
    totalDepartments: number;
    totalMissions: number;
    missionStatusCount: Record<string, number>;
    activeEmployees: number;
    newEmployeesThisMonth: number;
    newMissionsThisMonth: number;
    overdueMissions: number;
}
