package com.wuzhi.server.service_chat

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.service_chat.pojo.po.Conversation
import com.wuzhi.server.service_chat.pojo.po.Chat
import com.wuzhi.server.service_chat.service.ConversationService
import com.wuzhi.server.service_chat.service.ChatService
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.Message
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DatabaseChatMemory(
    private val chatService: ChatService,
    private val normalChatService: ChatService,
    private val conversationService: ConversationService
): ChatMemory {
    
    override fun add(conversationId: String, messages: List<Message?>) {
        val conversation = conversationService.getById(conversationId)
        // conversation 不存在则新建，存在则更新
        if (conversation == null) {
            val title = "新建对话" + System.currentTimeMillis()
            val newConversation = Conversation(
                id = conversationId,
                title = title,
                userId = UserContext.getUserId(),
                deleted = false,
                createTime = LocalDateTime.now(),
                updateTime = LocalDateTime.now()
            )
            conversationService.save(newConversation)
        }else{
            conversation.updateTime = LocalDateTime.now()
            conversationService.updateById(conversation)
        }
        // 只保留用户的和AI的，其他直接删掉
        val chatLists = messages
            .map { Chat(
                conversationId = conversationId,
                type = it!!.messageType.toString(),
                content = it.text,
                createTime = LocalDateTime.now(),
                deleted = false
            ) }
            .filter {
                it.type == Chat.TYPE_AI || it.type == Chat.TYPE_USER
            }
        normalChatService.saveBatch(chatLists)
    }

    override fun get(conversationId: String): List<Message?> {
        val wrapper = KtQueryWrapper(Chat::class.java)
        // 查询的同时排序，保证 最新的数据 下标为0
        wrapper.eq(Chat::conversationId, conversationId)
            .eq(Chat::deleted, false)
            .orderBy(true, false, Chat::createTime)
        return normalChatService.list(wrapper)
            .map(Chat::toMessage)
    }

    override fun clear(conversationId: String) {
        // 仅逻辑删除 Conversation 和 NormalChat
        val wrapper = KtQueryWrapper(Chat::class.java)
        wrapper.eq(Chat::conversationId, conversationId)
        val messageList = normalChatService.list(wrapper)
        val conversation = conversationService.getById(conversationId)
        conversation.deleted = true
        conversationService.updateById(conversation)
        messageList.forEach {
            it.deleted = true
            normalChatService.updateById(it)
        }
    }
}