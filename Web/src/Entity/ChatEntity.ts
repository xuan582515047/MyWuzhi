export interface MessageData {
    type: 'user' | 'assistant' | 'USER' | 'ASSISTANT';
    content: string;
    createTime: string;
}

export interface ConversationData {
    id: string;
    title: string;
    createTime: string;
    updateTime: string;
}

export interface SendMessageRequest {
    conversationId: string;
    message: string;
    model: string;
    databaseIds: string[] | null;
    files: File[] | null;
}

/**
 * 模型信息
 * 
 * 表示可用的AI模型信息
 */
export interface ModelInfo {
    /** 模型代码 */
    code: string;
    /** 模型名称 */
    name: string;
}