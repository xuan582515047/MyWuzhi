// MissionApi.ts
import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    MissionInfo,
    AddMissionRequest,
    AddMissionAIRequest,
    UpdateMissionRequest,
    UpdateMissionStatusRequest,
    DeleteMissionRequest,
    MissionQueryRequest,
    MissionPageResponse,
    MissionStatistics, MissionInfoCallback
} from "@/Entity/company/MissionEntity.ts";

const PREFIX = "/mission";

export class MissionApi {
    /**
     * 为部门分配任务
     */
    public static async addMission(request: AddMissionRequest): Promise<MissionInfo> {
        return await AxiosUtil.post(PREFIX + "/add", request);
    }

    /**
     * AI为部门自动分配任务
     */
    public static async addMissionAI(request: AddMissionAIRequest): Promise<MissionInfoCallback> {
        return await AxiosUtil.post(PREFIX + "/add/ai", request);
    }

    /**
     * 删除任务
     */
    public static async deleteMission(request: DeleteMissionRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/delete", request);
    }

    /**
     * 修改任务
     */
    public static async updateMission(request: UpdateMissionRequest): Promise<MissionInfo> {
        return await AxiosUtil.put(PREFIX + "/update", request);
    }

    /**
     * 员工或部门更新任务状态
     */
    public static async updateMissionStatus(request: UpdateMissionStatusRequest): Promise<MissionInfo> {
        return await AxiosUtil.put(PREFIX + "/update/status", request);
    }

    /**
     * 获取任务列表
     */
    public static async getMissionList(request: MissionQueryRequest): Promise<MissionPageResponse> {
        return await AxiosUtil.post(PREFIX + "/list", request);
    }

    /**
     * 获取任务统计信息
     */
    public static async getMissionStatistics(companyId: string): Promise<MissionStatistics> {
        return await AxiosUtil.post(PREFIX + "/statistics", { companyId });
    }
}