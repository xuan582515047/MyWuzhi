// MissionEntity.ts

/**
 * 任务状态枚举
 */
export enum MissionStatus {
    COMPLETED = 'completed',
    IN_PROGRESS = 'in_progress',
    NOT_STARTED = 'not_started',
    PAUSED = 'paused',
    CANCELED = 'canceled',
    OVERDUE = 'overdue'
}

export interface MissionInfoCallback {
    name: string,
    description: string,
    startTime: string,
    endTime: string,
    status: MissionStatus,
}

/**
 * 任务基本信息
 */
export interface MissionInfo {
    id: string;
    name: string;
    description?: string;
    startTime: string;
    endTime: string;
    status: MissionStatus;
    departmentId: string;
    departmentName?: string;
    employeeName?: string;
    operatorEmpName?: string;
    publisherEmpName?: string;
    marked: boolean;
    createTime?: string;
    updateTime?: string;
}

/**
 * 添加任务请求参数
 */
export interface AddMissionRequest {
    name: string;
    description?: string;
    startTime: string;
    endTime: string;
    status: MissionStatus;
    companyId: string;
    departmentId: string;
    employeeId?: string;
    publisherEmpId: string;
}

/**
 * AI添加任务请求参数
 */
export interface AddMissionAIRequest {
    companyId: string;
    departmentId: string;
    employeeId: string;
}

/**
 * 更新任务请求参数
 */
export interface UpdateMissionRequest {
    companyId: string;
    missionId: string;
    marked?: boolean;
    name?: string;
    description?: string;
    startTime?: string;
    endTime?: string;
    status?: MissionStatus;
    departmentId?: string;
    employeeId?: string;
    publisherEmpId?: string;
}

/**
 * 更新任务状态请求参数
 */
export interface UpdateMissionStatusRequest {
    companyId: string;
    missionId: string;
    status: MissionStatus;
    operatorEmpId: string;
}

/**
 * 删除任务请求参数
 */
export interface DeleteMissionRequest {
    companyId: string;
    missionIds: string[];
}

/**
 * 任务查询请求参数
 */
export interface MissionQueryRequest {
    companyId: string;
    name?: string;
    status?: MissionStatus;
    publisherEmpId?: string;
    startTime?: string;
    endTime?: string;
    marked?: boolean;
    departmentId?: string;
    employeeId?: string;
    page?: number;
    pageSize?: number;
}

/**
 * 任务分页响应
 */
export interface MissionPageResponse {
    data: MissionInfo[];
    total: number;
}

/**
 * 任务统计信息
 */
export interface MissionStatistics {
    companyId: string;
    totalMissions: number;
    missionStatusCount: Record<string, number>;
    newMissionsThisMonth: number;
    completedMissionsThisMonth: number;
    overdueMissions: number;
    highPriorityMissions: number;
    departmentMissionCount: Record<string, number>;
}