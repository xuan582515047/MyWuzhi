// EmployeeEntity.ts


/**
 * 员工详情信息
 */
export interface EmployeeDetail {
    id?: string;
    name: string;
    departmentId?: string;
    departmentName: string;
    status: EmployeeStatus;
    createTime?: string;
    updateTime?: string;
}

/**
 * 员工详情查询请求
 */
export interface EmployeeDetailRequest {
    nodeId: string;
    companyId: string;
}

/**
 * 员工状态枚举
 */
export enum EmployeeStatus {
    ACTIVE = 'active',
    LEAVE = 'leave',
    BANNED = 'banned'
}

/**
 * 员工基本信息
 */
export interface EmployeeInfo {
    id: string;
    userId: string;
    name: string;
    companyId: string;
    departmentId?: string;
    status: EmployeeStatus;
    createTime?: string;
    updateTime?: string;
    operatorEmpId: string;
}

/**
 * 员工VO信息（用于操作记录）
 */
export interface EmployeeVO {
    id: string;
    userId: string;
    name: string;
    companyId: string;
    departmentId?: string;
    status: EmployeeStatus;
    createTime?: string;
    updateTime?: string;
    operatorEmpId: string;
}

/**
 * 权限信息
 */
export interface PermissionInfo {
    id: string;
    code: string;
    name: string;
    description?: string;
    parentId?: string;
}

/**
 * 添加员工请求参数
 */
export interface EmployeeAddRequest {
    userId: string;
    name: string;
    companyId: string;
    departmentNodeId: string;
    status: EmployeeStatus;
    operatorEmpId: string;
}

/**
 * 更新员工请求参数
 */
export interface EmployeeUpdateRequest {
    companyId: string;
    employeeId: string;
    name?: string;
    status?: EmployeeStatus;
}

/**
 * 删除员工请求参数
 */
export interface EmployeeDeleteRequest {
    companyId: string;
    employeeId: string;
}

/**
 * 更换员工父级部门请求参数
 */
export interface EmployeeChangeParentRequest {
    companyId: string;
    nodeId: string;
    parentId: string | null;
}

/**
 * 员工查询请求参数
 */
export interface EmployeeQueryRequest {
    companyId: string;
    name?: string;
    status?: EmployeeStatus;
    departmentId?: string;
    pageNum?: number;
    pageSize?: number;
}

/**
 * 员工统计信息查询请求
 */
export interface EmployeeStatisticsRequest {
    employeeId: string;
}

/**
 * 员工分页响应
 */
export interface EmployeePageResponse {
    total: number;
    data: EmployeeInfo[];
}

/**
 * 员工统计信息
 */
export interface EmployeeStatistics {
    employeeId: string;
    employeeName: string;
    departmentId: string;
    departmentName: string;
    totalMissions: number;
    missionStatusCount: Record<string, number>;
    completedMissionsThisMonth: number;
    inProgressMissions: number;
    overdueMissions: number;
    missionCompletionRate: number;
}
