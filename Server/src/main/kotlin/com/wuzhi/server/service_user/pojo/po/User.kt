package com.wuzhi.server.service_user.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_user")
data class User(
    @TableId("id")
    var id: String? = null,

    @TableField("phone")
    var phone: String,

    @TableField("password")
    var password: String,

    @TableField("name")
    var name: String,

    @TableField("avatar")
    var avatar: String? = null,

    @TableField("create_time")
    var createTime: LocalDateTime

) {}