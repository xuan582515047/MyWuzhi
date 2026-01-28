package com.wuzhi.server.service_user.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("record_user")
class UserRecord(

    @TableId(value = "id")
    val id: String? = null,

    @TableField(value = "user_id")
    val userId: String,

    @TableField(value = "record_type")
    val recordType: String,

    @TableField(value = "create_time")
    val createTime: LocalDateTime,
) {
    companion object{
        const val TYPE_LOGIN = "login"
        const val TYPE_REFRESH_TOKEN = "refresh_token"
    }
}