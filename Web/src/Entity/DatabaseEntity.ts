/**
 * 数据库基本信息
 */
export interface DatabaseInfo {
    id: string;
    name: string;
    description?: string;
    type: string;
    url?: string;
    companyId?: string;
    parentId: string;
    createTime?: string;
    updateTime?: string;
}

/**
 * 数据库节点信息
 */
export interface DatabaseNode {
    id: string;
    name: string;
    parentId?: string;
    companyId?: string;
    status: boolean;
    type?: string; // 节点类型：middle-中间节点（文件夹），其他-叶子节点（文件）
    parentDisabled?: boolean; // 父节点是否被禁用
    children?: DatabaseNode[];
}

/**
 * 添加数据库请求参数
 */
export interface AddDatabaseRequest {
    name: string;
    parentId: string;
    description?: string;
    companyId?: string;
    type: string;
    url?: string;
    file?: File[];
    text?: string;
}

/**
 * 添加数据库节点请求参数
 */
export interface AddDatabaseNodeRequest {
    companyId?: string;
    name: string;
    parentId?: string;
}

/**
 * 编辑数据库请求参数
 */
export interface EditDatabaseRequest {
    id: string;
    name?: string;
    description?: string;
    type?: string;
    text?: string;
}

/**
 * 编辑数据库节点请求参数
 */
export interface EditDatabaseNodeRequest {
    id: string;
    name?: string;
}

/**
 * 数据库节点状态更新请求
 */
export interface DatabaseNodeStatusRequest {
    id: string;
    status: boolean;
}

/**
 * 数据库树查询响应
 */
export interface DatabaseTreeResponse {
    id: string;
    name: string;
    status: boolean;
    type: string;
    children: DatabaseNode[];
}

/**
 * 数据库详情响应
 */
export interface DatabaseDetailResponse {
    id: string;
    name: string;
    description: string;
    type: string;
    url: string;
    text?: string;
}