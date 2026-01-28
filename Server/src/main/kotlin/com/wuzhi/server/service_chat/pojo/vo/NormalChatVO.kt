package com.wuzhi.server.service_chat.pojo.vo

import com.wuzhi.server.service_chat.pojo.po.Chat

class NormalChatVO(
    var type: String?,
    var content: String?,
    var createTime: String?
) {
    companion object{
        fun fromPO(chat: Chat): NormalChatVO {
            return NormalChatVO(
                type = chat.type,
                content = chat.content,
                createTime = chat.createTime.toString()
            )
        }
    }
}