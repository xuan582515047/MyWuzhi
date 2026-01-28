package com.wuzhi.server.service_tool.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_tool_recommendation")
class ToolRecommendation(

    @TableId("id")
    var userId: String,

    @TableField("company_id")
    var companyId: String,

    @TableField("tool_ids")
    var toolIds: List<String>,

    @TableField("update_time")
    var updateTime: LocalDateTime,
) {
}