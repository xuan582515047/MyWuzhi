// EmployeeApi.ts
import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    EmployeeInfo,
    PermissionInfo,
    EmployeeAddRequest,
    EmployeeUpdateRequest,
    EmployeeDeleteRequest,
    EmployeeQueryRequest,
    EmployeePageResponse,
    EmployeeStatistics,
    EmployeeStatisticsRequest,
    EmployeeChangeParentRequest, EmployeeDetailRequest, EmployeeDetail
} from "@/Entity/company/EmployeeEntity.ts";

const PREFIX = "/employee";

export class EmployeeApi {
    /**
     * 获取员工的权限
     */
    public static async getEmployeePermission(employeeId: string): Promise<PermissionInfo[]> {
        return await AxiosUtil.get(PREFIX + "/permission/" + employeeId);
    }

    /**
     * 添加员工
     */
    public static async addEmployee(request: EmployeeAddRequest): Promise<EmployeeInfo> {
        return await AxiosUtil.post(PREFIX + "/add", request);
    }

    /**
     * 删除员工
     */
    public static async deleteEmployee(request: EmployeeDeleteRequest): Promise<void> {
        await AxiosUtil.post(PREFIX + "/delete", request);
    }

    /**
     * 更新员工
     */
    public static async updateEmployee(request: EmployeeUpdateRequest): Promise<EmployeeInfo> {
        return await AxiosUtil.put(PREFIX + "/update", request);
    }

    /**
     * 更换员工的父级部门
     */
    public static async changeEmployeeParent(request: EmployeeChangeParentRequest): Promise<void> {
        await AxiosUtil.post(PREFIX + "/change/parent", request);
    }

    /**
     * 获取员工列表
     */
    public static async queryEmployeeList(request: EmployeeQueryRequest): Promise<EmployeePageResponse> {
        return await AxiosUtil.post(PREFIX + "/list", request);
    }

    /**
     * 获取员工统计信息
     */
    public static async getEmployeeStatistics(request: EmployeeStatisticsRequest): Promise<EmployeeStatistics> {
        return await AxiosUtil.post(PREFIX + "/statistics", request);
    }

    /**
     * 获取员工详情
     */
    public static async getEmployeeDetail(request: EmployeeDetailRequest): Promise<EmployeeDetail> {
        return await AxiosUtil.post(PREFIX + "/detail", request);
    }
}
