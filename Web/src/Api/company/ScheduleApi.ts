// ScheduleApi.ts
import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    ScheduleInfo,
    AddScheduleRequest,
    UpdateScheduleRequest,
    DeleteScheduleRequest,
    ScheduleListRequest,
    ScheduleListResponse
} from "@/Entity/company/ScheduleEntity.ts";

const PREFIX = "/schedule";

export class ScheduleApi {
    /**
     * 添加日程
     */
    public static async addSchedule(request: AddScheduleRequest): Promise<ScheduleInfo> {
        return await AxiosUtil.post(PREFIX + "/add", request);
    }

    /**
     * 删除日程
     */
    public static async deleteSchedule(request: DeleteScheduleRequest): Promise<void> {
        await AxiosUtil.delete(PREFIX + "/delete/" + request.id);
    }

    /**
     * 更新日程
     */
    public static async updateSchedule(request: UpdateScheduleRequest): Promise<ScheduleInfo> {
        return await AxiosUtil.put(PREFIX + "/update", request);
    }

    /**
     * 获取日程列表
     */
    public static async getScheduleList(request: ScheduleListRequest): Promise<ScheduleListResponse> {
        return await AxiosUtil.post(PREFIX + "/list", request);
    }
}
