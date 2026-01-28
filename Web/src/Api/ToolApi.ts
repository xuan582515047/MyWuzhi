import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    ToolInfo,
    ToolUseRecord,
    ToolUseNode,
    AddToolRequest,
    EditToolRequest,
    ToolQueryRequest,
    ToolQueryResponse,
    UseToolRequest,
    AddToolUseRecordRequest,
    EditToolUseRecordRequest,
    ToolUseRecordQueryRequest,
    ToolUseRecordQueryResponse,
    ToolNodeStatusRequest,
    ConstantItem,
    TagInfo, AddToolNodeRequest, EditToolNodeRequest, ChangeNodeParentRequest
} from "@/Entity/ToolEntity.ts";

const PREFIX = "/tool";

export class ToolApi {
    // ==================== 工具管理 ====================

    /**
     * 添加工具
     */
    public static async addTool(request: AddToolRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/add", request);
    }

    /**
     * 删除工具
     */
    public static async deleteTool(id: string): Promise<void> {
        return await AxiosUtil.delete(PREFIX + "/delete/" + id);
    }

    /**
     * 编辑工具
     */
    public static async editTool(request: EditToolRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/edit", request);
    }

    /**
     * 查询工具列表
     */
    public static async getToolList(request: ToolQueryRequest): Promise<ToolQueryResponse> {
        return await AxiosUtil.post(PREFIX + "/list", request);
    }

    /**
     * 查询工具详情
     */
    public static async getToolDetail(id: string): Promise<ToolInfo> {
        return await AxiosUtil.get(PREFIX + "/detail/" + id);
    }

    // ==================== 工具使用管理 ====================

    /**
     * 修改工具节点状态
     */
    public static async updateToolNodeStatus(requests: ToolNodeStatusRequest[]): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/use/status", requests);
    }

    /**
     * 获取工具使用树
     */
    public static async getToolUseTree(companyId: string): Promise<ToolUseNode[]> {
        return await AxiosUtil.get(PREFIX + "/use/tree/" + companyId);
    }

    /**
     * 用户使用工具
     */
    public static async useTool(request: UseToolRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/use/add", request);
    }

    /**
     * 添加工具使用记录
     */
    public static async addToolUseRecord(request: AddToolUseRecordRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/use/record", request);
    }

    /**
     * 删除工具使用记录
     */
    public static async deleteToolUseRecord(id: string): Promise<void> {
        return await AxiosUtil.delete(PREFIX + "/use/delete/" + id);
    }

    /**
     * 编辑工具使用记录
     */
    public static async editToolUseRecord(request: EditToolUseRecordRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/use/edit", request);
    }

    /**
     * 查询工具使用记录列表
     */
    public static async getToolUseRecordList(request: ToolUseRecordQueryRequest): Promise<ToolUseRecordQueryResponse> {
        return await AxiosUtil.post(PREFIX + "/use/list", request);
    }
    // ==================== 工具节点 ====================

    public static async addToolNode(request: AddToolNodeRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/node/add", request);
    }

    public static async deleteToolNode(id: string): Promise<void> {
        return await AxiosUtil.delete(PREFIX + "/node/delete/" + id);
    }

    public static async editToolNode(request: EditToolNodeRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/node/edit", request);
    }

    public static async changeNodeParent(request: ChangeNodeParentRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/node/change", request);
    }

    // ==================== 常量列表 ====================

    /**
     * 获取标签列表
     */
    public static async getTagList(): Promise<TagInfo[]> {
        return await AxiosUtil.get(PREFIX + "/constant/tags");
    }

    /**
     * 获取工具类型列表
     */
    public static async getToolTypeList(): Promise<ConstantItem[]> {
        return await AxiosUtil.get(PREFIX + "/constant/types");
    }

    /**
     * 获取排序类型列表
     */
    public static async getSortTypeList(): Promise<ConstantItem[]> {
        return await AxiosUtil.get(PREFIX + "/constant/sortTypes");
    }
}