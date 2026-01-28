import {AxiosUtil} from "@/Util/AxiosUtil.ts";
import type {ConversationData, MessageData, SendMessageRequest, ModelInfo} from "@/Entity/ChatEntity.ts";


const PREFIX = "/chat"
export class ChatApi{

    // 发送消息
    public static async sendMessage(messageRequest: SendMessageRequest): Promise<string>{
        const formData = new FormData();
        formData.append("conversationId", messageRequest.conversationId);
        formData.append("message", messageRequest.message);
        formData.append("model", messageRequest.model);
        formData.append("databaseIds", JSON.stringify(messageRequest.databaseIds));
        if (messageRequest.files && messageRequest.files.length > 0){
            for(const file of messageRequest.files){
                formData.append("files", file);
            }
        }
        return await AxiosUtil.postFormData(PREFIX+"/message/call", formData);
    }

    // 查询对话列表
    public static async getConversationList(): Promise<ConversationData[]>{
        return await AxiosUtil.get(PREFIX+"/conversations");
    }

    // 删除对话
    static async deleteConversation(id: string) {
        return await AxiosUtil.delete(PREFIX+"/conversations/"+id);
    }

    // 重命名对话
    static async updateConversation(conversation: ConversationData) {
        return await AxiosUtil.put(PREFIX+"/conversations", conversation);
    }

    // 查询对话消息
    public static async getMessageList(conversationId: string): Promise<MessageData[]>{
        return await AxiosUtil.get(PREFIX+"/messages/"+conversationId);
    }

    // 获取模型列表
    public static async getModelList(): Promise<ModelInfo[]>{
        return await AxiosUtil.get(PREFIX+"/models");
    }
}