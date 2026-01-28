<script setup lang="ts">
import Sidebar from "@/Views/PublicComponents/Sidebar.vue";
import ChatArea, {type UploadedFile} from "@/Views/PublicComponents/ChatArea.vue";
import {onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import HistoryList from "@/Views/History/HistoryList.vue";
import {ChatApi} from "@/Api/ChatApi.ts";
import type {ConversationData, MessageData, SendMessageRequest} from "@/Entity/ChatEntity.ts";
import {useUserStore} from "@/Stores/UserStore.ts";

//==========================> 类型定义 <==========================
interface ChatAreaREF {
  aiMessageCallback: () => void;
}

export interface UserData {
  name: string;
  avatar: string;
}
//==========================> 变量定义 <==========================
const userData = ref({} as UserData)
const conversationList = ref<ConversationData[]>([] as ConversationData[])
const currentConversation = ref<ConversationData>();
const messageList = ref<MessageData[]>([] as MessageData[])
const chatAreaRef = ref<ChatAreaREF>();

onMounted(async () => {
  const userStore = useUserStore()
  userData.value = {
    name: userStore.name,
    avatar: userStore.avatar
  }
  conversationList.value = await ChatApi.getConversationList()
  currentConversation.value = conversationList.value[0]
  messageList.value = await ChatApi.getMessageList(currentConversation.value?.id!)
})

//==========================> 方法定义 <==========================
/**
 * 用户发送消息
 * @param message
 */
const sendMessage = async (message: string, model: string, files: File[]) => {
  // 添加用户信息
  messageList.value.push({
    content: message,
    type: 'USER',
    createTime: new Date().toISOString()
  })
  // 发送消息，获取AI回复
  const sendMessageRequest: SendMessageRequest = {
    conversationId: currentConversation.value?.id!,
    message: message,
    model: model,
    databaseIds: null,
    files: files
  }
  const content = await ChatApi.sendMessage(sendMessageRequest)
  messageList.value.push({
    content: content,
    type: 'ASSISTANT',
    createTime: new Date().toISOString()
  })
  chatAreaRef.value!.aiMessageCallback()
}

/**
 * 用户上传文件
 * @param files
 */
const uploadFile = async (files: UploadedFile[]) => {
  //TODO(上传文件)
}

/**
 * 选中对话
 * @param conversation
 */
const selectConversation = async (conversation: ConversationData) => {
  // 在现有列表中查找
  currentConversation.value = conversation
  messageList.value = await ChatApi.getMessageList(conversation.id)
}

/**
 * 删除对话
 * @param conversation
 */
const deleteConversation = async (conversation: ConversationData) => {
  conversationList.value = conversationList.value.filter(item => item.id !== conversation.id)
  await ChatApi.deleteConversation(conversation.id)
}

/**
 * 重命名对话
 * @param conversation
 */
const renameConversation = async (conversation: ConversationData) => {
  await ChatApi.updateConversation(conversation)
}
</script>

<template>
  <div class="app-container">
    <Sidebar :user-data="userData"
             :show-exit-btn="true">
      <template #left-side>
        <HistoryList :conversation-list="conversationList"
                     :current-id="currentConversation?.id"
                     @delete="deleteConversation"
                     @rename="renameConversation"
                     @click="selectConversation"/>
      </template>
      <template #content>
        <ChatArea @upload-file="uploadFile"
                  @send-message="sendMessage"
                  ref="chatAreaRef"
                  :message-list="messageList"
                  :title="currentConversation == undefined? '' : currentConversation.title"/>
      </template>
    </Sidebar>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, var(--gradient-start) 0%, var(--gradient-end) 100%);
  transition: all 0.5s ease;
  animation: fadeIn 0.6s ease-out;
  position: relative;
}

/* 深色模式支持 */
.dark .app-container {
  background: linear-gradient(135deg, var(--gradient-dark-start) 0%, var(--gradient-dark-end) 100%);
  color: var(--text-primary);
}

/* 添加微妙的背景动画 */
.app-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse at center, rgba(167, 139, 250, 0.05) 0%, rgba(255, 255, 255, 0) 70%);
  pointer-events: none;
  transition: all 0.5s ease;
  z-index: 0;
}

.dark .app-container::before {
  background: radial-gradient(ellipse at center, rgba(76, 110, 245, 0.05) 0%, rgba(0, 0, 0, 0) 70%);
}

/* 动画定义 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    flex-direction: column;
  }
}
</style>