// ScheduleEntity.ts

/**
 * 日程基本信息
 */
export interface ScheduleInfo {
    id: string;
    title: string;
    description: string;
    time: string; // ISO 格式日期时间
}

/**
 * 添加日程请求参数
 */
export interface AddScheduleRequest {
    companyId: string;
    title: string;
    description: string;
    time: string; // ISO 格式日期时间
}

/**
 * 更新日程请求参数
 */
export interface UpdateScheduleRequest {
    id: string;
    title?: string;
    description?: string;
    time?: string; // ISO 格式日期时间
}

/**
 * 删除日程请求参数
 */
export interface DeleteScheduleRequest {
    id: string;
}

/**
 * 获取日程列表请求参数
 */
export interface ScheduleListRequest {
    companyId: string;
    startDate: string; // ISO 格式日期时间
    endDate: string; // ISO 格式日期时间
}

/**
 * 日程按天分组项
 */
export interface ScheduleDailyItem {
    id: string;
    title: string;
    description: string;
    time: string; // ISO 格式日期时间
}

/**
 * 日程按天分组响应
 */
export interface ScheduleDailyGroup {
    date: string; // ISO 格式日期 (YYYY-MM-DD)
    scheduleList: ScheduleDailyItem[];
}

/**
 * 日程列表响应
 */
export type ScheduleListResponse = ScheduleDailyGroup[];
