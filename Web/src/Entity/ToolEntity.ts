
/**
 * 工具基本信息
 */
export interface ToolInfo {
    id: string;
    name: string;
    description: string;
    price: string;
    url: string;
    version: string;
    type: string;
    tagIds: string[];      // 字符串数组
    keywords: string[];    // 字符串数组
    viewCount: number;
    useCount: number;
    rating: number;
    createTime?: string;
    updateTime?: string;
}

/**
 * 工具使用记录信息
 */
export interface ToolUseRecord {
    id: string;
    toolId: string;
    companyId?: string;
    detail?: string;
    useTime: string;
    userId?: string;
}

/**
 * 工具使用节点信息
 */
export interface ToolUseNode {
    id: string;
    name: string;
    url?: string;
    type: 'middle' | 'leaf'
    status: boolean;
    children?: ToolUseNode[];
}

/**
 * 添加工具请求参数
 */
export interface AddToolRequest {
    name: string;
    description: string;
    price: string;
    url: string;
    version: string;
    type: string;
    tagIds: string[];      // 字符串数组
    keywords: string[];    // 字符串数组
}

/**
 * 编辑工具请求参数
 */
export interface EditToolRequest {
    id: string;
    name?: string;
    description?: string;
    price?: string;
    url?: string;
    version?: string;
    type?: string;
    tagIds?: string[];      // 字符串数组
    keywords?: string[];    // 字符串数组
    updateTime?: string;
}

/**
 * 工具查询请求参数
 */
export interface ToolQueryRequest {
    query?: string;
    type?: string;
    tagIds?: string[];
    minPrice?: string;
    maxPrice?: string;
    sortBy?: string;
    isAsc: boolean;
    page: number;
    pageSize: number;
}

/**
 * 工具查询响应
 */
export interface ToolQueryResponse {
    data: ToolInfo[];
    total: number;
}

/**
 * 工具使用请求参数
 */
export interface UseToolRequest {
    toolId: string;
    companyId?: string;
    name?: string;
    parentId?: string;
}

/**
 * 添加工具使用记录请求参数
 */
export interface AddToolUseRecordRequest {
    toolId: string;
    companyId: string;
    detail?: string;
}

/**
 * 编辑工具使用记录请求参数
 */
export interface EditToolUseRecordRequest {
    toolId: string;
    companyId?: string;
    detail?: string;
}

/**
 * 工具使用记录查询请求参数
 */
export interface ToolUseRecordQueryRequest {
    toolId: string;
    companyId?: string;
    startTime: string;
    endTime: string;
    isAsc: boolean;
    pageNum: number;
    pageSize: number;
}

/**
 * 工具使用记录查询响应
 */
export interface ToolUseRecordQueryResponse {
    list: ToolUseRecord[];
    total: number;
    pageNum: number;
    pageSize: number;
}

/**
 * 工具节点状态更新请求
 */
export interface ToolNodeStatusRequest {
    id: string;
    status: boolean;
}

/**
 * 常量类型定义
 */
export interface ConstantItem {
    name: string;
    value: string;
}

/**
 * 标签信息
 */
export interface TagInfo {
    id: string;
    name: string;
    description?: string;
}

export interface AddToolNodeRequest{
    companyId?: string;
    name: string;
    parentId?: string;
}

export interface EditToolNodeRequest{
    id: string;
    name: string;
}

export interface ChangeNodeParentRequest{
    id: string;
    parentId: string | null;
}