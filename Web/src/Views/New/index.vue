<script setup lang="ts">

import Sidebar from "@/Views/PublicComponents/Sidebar.vue";
import ChatArea, {type UploadedFile} from "@/Views/PublicComponents/ChatArea.vue";
import Menu from "@/Views/New/Menu.vue";
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import { HOME_MENU_LIST } from "@/Config/DataConfig.ts";
import {ChatApi} from "@/Api/ChatApi.ts";
import {UuidUtil} from "@/Util/UuidUtil.ts";
import type {MessageData, SendMessageRequest} from "@/Entity/ChatEntity.ts";

//==========================> 类型定义 <==========================
interface ChatAreaREF {
  aiMessageCallback: () => void;
}

export interface HomeMenu {
  name: string;
  label: string;
  icon: any;
}

//==========================> 变量定义 <==========================
const router = useRouter()
const menuList: HomeMenu[] = HOME_MENU_LIST
const messageList = ref<MessageData[]>([])
const chatAreaRef = ref<ChatAreaREF>();
const currentConversationId = ref<string>('')

onMounted(async () => {

})

//==========================> 方法定义 <==========================

/**
 * 选择主菜单
 * @param index
 */
const selectMenu = (index: number) => {
  if (menuList[index]!.name === "new") {
    // 点击"新建对话"，清空当前对话数据和对话ID
    messageList.value = [];
    currentConversationId.value = '';
  } else {
    router.push({name: menuList[index]!.label})
  }
}

/**
 * 用户发送消息
 * @param message
 * @param model
 * @param files
 */
const sendMessage = async (message: string, model: string, files: File[]) => {
  // 添加用户信息
  messageList.value.push({
    content: message,
    type: 'USER',
    createTime: new Date().toISOString()
  })

  // 如果当前对话id为空，则生成一个新的对话id
  if (currentConversationId.value === '') {
    currentConversationId.value = UuidUtil.randomUUID32()
  }
  const sendMessageRequest: SendMessageRequest = {
    model: model,
    conversationId: currentConversationId.value,
    message: message,
    databaseIds: null,
    files: files
  }

  // 发送消息，等待回复
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
const uploadFile = (files: UploadedFile[]) => {
  //TODO(上传文件)
  console.log('上传文件')
}
</script>

<template>
  <div class="app-container">
    <Sidebar :show-exit-btn="false">
      <template #left-side>
        <Menu :menu-list="menuList" @select="selectMenu" />
      </template>
      <template #content>
        <ChatArea
            ref="chatAreaRef"
            :message-list="messageList"
            title="新建对话"
            @upload-file="uploadFile"
            @send-message="sendMessage"/>
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
  position: relative;
  animation: fadeIn 0.6s ease-out;
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

/* 深色模式支持 */
.dark .app-container {
  background: linear-gradient(135deg, var(--gradient-dark-start) 0%, var(--gradient-dark-end) 100%);
  color: var(--text-primary);
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