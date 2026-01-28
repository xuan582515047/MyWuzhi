package com.wuzhi.server.service_chat.pojo.vo

import com.wuzhi.server.service_chat.pojo.po.Conversation

data class ConversationVO(
    val id: String,
    val title: String?,
    val createTime: String?,
    var updateTime: String?,
) {
    companion object {
        fun fromPO(po: Conversation): ConversationVO {
            return ConversationVO(
                id = po.id,
                title = po.title,
                createTime = po.createTime.toString(),
                updateTime = po.updateTime.toString(),
            )
        }
    }
}