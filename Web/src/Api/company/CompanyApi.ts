// CompanyApi.ts
import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    CompanyDetail,
    CompanyAddRequest,
    CompanyUpdateRequest,
    CompanyDeleteRequest,
    CompanyAuthRequest,
    CompanyRecordQueryRequest,
    CompanyStatisticsRequest,
    OperationRecordPageResponse,
    PermissionNode,
    CompanyInfo,
    CompanyStatistics,
    CompanyNode
} from "@/Entity/company/CompanyEntity.ts";

const PREFIX = "/company";

export class CompanyApi {
    /**
     * 获取公司部门和员工树
     */
    public static async getCompanyTree(companyId: string): Promise<CompanyNode> {
        return await AxiosUtil.get(PREFIX + "/tree/" + companyId);
    }

    /**
     * 查询公司操作记录
     */
    public static async getCompanyRecord(request: CompanyRecordQueryRequest): Promise<OperationRecordPageResponse> {
        return await AxiosUtil.post(PREFIX + "/record", request);
    }

    /**
     * 获取公司权限列表
     */
    public static async getCompanyPermission(companyId: string): Promise<PermissionNode[]> {
        return await AxiosUtil.get(PREFIX + "/permission/" + companyId);
    }

    /**
     * 获取公司详情信息
     */
    public static async getCompanyDetail(companyId: string): Promise<CompanyDetail> {
        return await AxiosUtil.get(PREFIX + "/detail/" + companyId);
    }

    /**
     * 公司认证
     */
    public static async authCompany(request: CompanyAuthRequest): Promise<void> {
        await AxiosUtil.post(PREFIX + "/auth", request);
    }

    /**
     * 添加公司
     */
    public static async addCompany(request: CompanyAddRequest): Promise<CompanyDetail> {
        return await AxiosUtil.post(PREFIX + "/add", request);
    }

    /**
     * 删除公司
     */
    public static async deleteCompany(request: CompanyDeleteRequest): Promise<void> {
        await AxiosUtil.post(PREFIX + "/delete", request);
    }

    /**
     * 更新公司
     */
    public static async updateCompany(request: CompanyUpdateRequest): Promise<CompanyDetail> {
        return await AxiosUtil.put(PREFIX + "/update", request);
    }

    /**
     * 获取公司列表
     */
    public static async getCompanyList(): Promise<CompanyInfo[]> {
        return await AxiosUtil.get(PREFIX + "/list");
    }

    /**
     * 获取公司统计信息
     */
    public static async getCompanyStatistics(request: CompanyStatisticsRequest): Promise<CompanyStatistics> {
        return await AxiosUtil.post(PREFIX + "/statistics", request);
    }
}
