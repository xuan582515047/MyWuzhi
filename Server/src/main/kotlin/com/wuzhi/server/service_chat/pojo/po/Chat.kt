package com.wuzhi.server.service_chat.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.messages.UserMessage
import java.time.LocalDateTime

@TableName("data_chat")
data class Chat(
    @TableId("id")
    var id: String? = null,

    @TableField("conversation_id")
    var conversationId: String,

    @TableField("type")
    var type: String,

    @TableField("content")
    var content: String,

    @TableField("create_time")
    var createTime: LocalDateTime,

    @TableField("deleted")
    var deleted: Boolean

) {
    fun toMessage(): Message?{
        return when(type){
            TYPE_USER -> UserMessage(content)
            TYPE_AI -> AssistantMessage(content)
            else -> return null
        }
    }
    companion object{
        val TYPE_USER = MessageType.USER.toString()
        val TYPE_AI = MessageType.ASSISTANT.toString()
    }
}