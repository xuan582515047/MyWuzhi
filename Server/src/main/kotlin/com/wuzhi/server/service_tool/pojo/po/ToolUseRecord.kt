package com.wuzhi.server.service_tool.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("record_tool")
class ToolUseRecord(

    @TableField("user_id")
    val userId: String,

    @TableField("tool_id")
    val toolId: String,

    @TableField("company_id")
    val companyId: String?,  // 改为可空类型，支持不绑定公司

    @TableField("detail")
    val detail: String,

    @TableField("create_time")
    val createTime: LocalDateTime,
) {
    companion object{
        const val NONE_COMPANY_ID = "NONE_COMPANY_ID"
    }
}