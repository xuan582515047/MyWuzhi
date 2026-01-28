package com.wuzhi.server.service_chat.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_conversation")
data class Conversation(
    @TableId("id")
    val id: String,

    @TableField("title")
    var title: String?,

    @TableField("user_id")
    val userId: String,

    @TableField("deleted")
    var deleted: Boolean,

    @TableField("create_time")
    val createTime: LocalDateTime,

    @TableField("update_time")
    var updateTime: LocalDateTime,
) {}