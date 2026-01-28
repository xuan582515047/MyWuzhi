import { AxiosUtil } from "@/Util/AxiosUtil.ts";
import type {
    DatabaseInfo,
    DatabaseNode,
    AddDatabaseRequest,
    AddDatabaseNodeRequest,
    EditDatabaseRequest,
    EditDatabaseNodeRequest,
    DatabaseNodeStatusRequest,
    DatabaseTreeResponse,
    DatabaseDetailResponse
} from "@/Entity/DatabaseEntity.ts";

const PREFIX = "/database";

export class DatabaseApi {
    /**
     * 添加数据库
     */
    public static async addDatabase(request: AddDatabaseRequest): Promise<void> {
        // 如果有文件，使用FormData上传
        if (request.file && request.file.length > 0) {
            const formData = new FormData();
            formData.append("name", request.name);
            formData.append("parentId", request.parentId);
            formData.append("type", request.type);

            if (request.description) formData.append("description", request.description);
            if (request.companyId) formData.append("companyId", request.companyId);
            if (request.url) formData.append("url", request.url);

            // 添加文件
            request.file.forEach(file => {
                formData.append("file", file);
            });

            return await AxiosUtil.postFormData(PREFIX + "/add", formData);
        } else {
            return await AxiosUtil.post(PREFIX + "/add", request);
        }
    }

    /**
     * 添加数据库节点
     */
    public static async addDatabaseNode(request: AddDatabaseNodeRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/add/node", request);
    }

    /**
     * 删除数据库
     */
    public static async deleteDatabase(id: string): Promise<void> {
        return await AxiosUtil.delete(PREFIX + "/delete/" + id);
    }

    /**
     * 删除数据库节点
     */
    public static async deleteDatabaseNode(id: string): Promise<void> {
        return await AxiosUtil.delete(PREFIX + "/delete/node/" + id);
    }

    /**
     * 编辑数据库
     */
    public static async editDatabase(request: EditDatabaseRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/edit", request);
    }

    /**
     * 编辑数据库节点
     */
    public static async editDatabaseNode(request: EditDatabaseNodeRequest): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/edit/node", request);
    }

    /**
     * 获取数据库详情
     */
    public static async getDatabaseDetail(id: string): Promise<DatabaseDetailResponse> {
        return await AxiosUtil.get(PREFIX + "/detail/" + id);
    }

    /**
     * 获取数据库树
     */
    public static async getDatabaseTree(): Promise<DatabaseTreeResponse[]> {
        return await AxiosUtil.get(PREFIX + "/tree");
    }

    /**
     * 获取指定公司的数据库树
     */
    public static async getCompanyDatabaseTree(companyId: string): Promise<DatabaseTreeResponse[]> {
        return await AxiosUtil.get(PREFIX + "/tree/" + companyId);
    }

    /**
     * 更新数据库节点状态
     */
    public static async updateDatabaseNodeStatus(requests: DatabaseNodeStatusRequest[]): Promise<void> {
        return await AxiosUtil.post(PREFIX + "/status", requests);
    }

    /**
     * 修改数据库节点的父级
     */
    public static async changeDatabaseParent(request: {
        nodeId: string;
        parentId: string | null;
        companyId: string;
    }): Promise<void> {
        await AxiosUtil.post(PREFIX + "/change/parent", request);
    }
}