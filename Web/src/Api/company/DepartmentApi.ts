// DepartmentApi.ts
import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    DepartmentInfo,
    DepartmentAddRequest,
    DepartmentUpdateRequest,
    DepartmentDeleteRequest,
    DepartmentStatisticsRequest,
    DepartmentStatistics,
    DepartmentChangeParentRequest
} from "@/Entity/company/DepartmentEntity.ts";

const PREFIX = "/department";

export class DepartmentApi {
    /**
     * 添加部门
     */
    public static async addDepartment(request: DepartmentAddRequest): Promise<DepartmentInfo> {
        return await AxiosUtil.post(PREFIX + "/add", request);
    }

    /**
     * 删除部门
     */
    public static async deleteDepartment(request: DepartmentDeleteRequest): Promise<void> {
        await AxiosUtil.post(PREFIX + "/delete", request);
    }

    /**
     * 更新部门
     */
    public static async updateDepartment(request: DepartmentUpdateRequest): Promise<DepartmentInfo> {
        return await AxiosUtil.put(PREFIX + "/update", request);
    }

    /**
     * 更换部门的父级
     */
    public static async changeDepartmentParent(request: DepartmentChangeParentRequest): Promise<void> {
        await AxiosUtil.post(PREFIX + "/change/parent", request);
    }

    /**
     * 获取部门统计信息
     */
    public static async getDepartmentStatistics(request: DepartmentStatisticsRequest): Promise<DepartmentStatistics> {
        return await AxiosUtil.post(PREFIX + "/statistics", request);
    }
}
