// RoleApi.ts
import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    RoleInfo,
    RoleListItem,
    AssignEmployeesToRoleRequest,
    AssignRolesToEmployeeRequest,
    SetPermissionsToRoleRequest,
    AddRoleRequest,
    UpdateRoleRequest,
    DeleteRoleRequest
} from "@/Entity/company/RoleEntity.ts";

const PREFIX = "/role";

export class RoleApi {
    /**
     * 给角色分配用户
     */
    public static async assignEmployeesToRole(request: AssignEmployeesToRoleRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/employee/from", request);
    }

    /**
     * 给用户分配角色
     */
    public static async assignRolesToEmployee(request: AssignRolesToEmployeeRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/employee/to", request);
    }

    /**
     * 给角色设置权限
     */
    public static async setPermissionsToRole(request: SetPermissionsToRoleRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/permission", request);
    }

    /**
     * 添加角色
     */
    public static async addRole(request: AddRoleRequest): Promise<RoleInfo> {
        return await AxiosUtil.post(PREFIX + "/add", request);
    }

    /**
     * 删除角色
     */
    public static async deleteRole(request: DeleteRoleRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/delete", request);
    }

    /**
     * 更新角色
     */
    public static async updateRole(request: UpdateRoleRequest): Promise<RoleInfo> {
        return await AxiosUtil.put(PREFIX + "/update", request);
    }

    /**
     * 获取角色列表
     */
    public static async getRoleList(companyId: string): Promise<RoleListItem[]> {
        return await AxiosUtil.get(PREFIX + "/list/" + companyId);
    }
}