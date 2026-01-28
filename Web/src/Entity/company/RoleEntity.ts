// RoleEntity.ts

/**
 * 角色信息
 */
export interface RoleInfo {
    id: string;
    name: string;
    description?: string;
    companyId: string;
    createTime?: string;
    updateTime?: string;
    operatorEmpId: string;
}

/**
 * 角色列表项
 */
export interface RoleListItem {
    name: string;
    description?: string;
    createTime: string;
    updateTime: string;
}

/**
 * 给角色分配用户请求参数
 */
export interface AssignEmployeesToRoleRequest {
    companyId: string;
    roleId: string;
    employeeIds: string[];
}

/**
 * 给用户分配角色请求参数
 */
export interface AssignRolesToEmployeeRequest {
    companyId: string;
    employeeId: string;
    roleIds: string[];
}

/**
 * 给角色设置权限请求参数
 */
export interface SetPermissionsToRoleRequest {
    companyId: string;
    roleId: string;
    authIds: string[];
}

/**
 * 添加角色请求参数
 */
export interface AddRoleRequest {
    operatorEmpId: string;
    companyId: string;
    name: string;
    description?: string;
}

/**
 * 更新角色请求参数
 */
export interface UpdateRoleRequest {
    roleId: string;
    companyId: string;
    name?: string;
    description?: string;
}

/**
 * 删除角色请求参数
 */
export interface DeleteRoleRequest {
    companyId: string;
    roleId: string;
}