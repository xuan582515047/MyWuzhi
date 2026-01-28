package com.wuzhi.server.service_company.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_schedule")
class Schedule(
    @TableId("id")
    var id: String? = null,

    @TableField("company_id")
    val companyId: String,

    @TableField("user_id")
    val userId: String,

    @TableField("title")
    val title: String? = null,

    @TableField("description")
    val description: String? = null,

    @TableField("time")
    val time: LocalDateTime
) {


}