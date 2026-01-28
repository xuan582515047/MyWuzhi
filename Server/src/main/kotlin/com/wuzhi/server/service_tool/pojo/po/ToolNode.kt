package com.wuzhi.server.service_tool.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("node_tool")
class ToolNode(

    @TableId("id")
    var id: String? = null,

    @TableField("name")
    val name: String,

    @TableField("type")
    val type: String,

    @TableField("user_id")
    val userId: String? = null,

    @TableField("company_id")
    val companyId: String? = null,

    @TableField("operator_user_id")
    var operatorUserId: String,

    @TableField("parent_id")
    val parentId: String? = null,

    @TableField("tool_id")
    val toolId: String? = null,

    @TableField("create_time")
    var createTime: LocalDateTime,

    @TableField("update_time")
    var updateTime: LocalDateTime,

    @TableField("status")
    var status: Boolean,

    @TableField("url")
    var url: String? = null

    ) {
    companion object {
        const val TYPE_MIDDLE = "middle"
        const val TYPE_LEAF = "leaf"

        const val STATUS_ENABLE = true
        const val STATUS_DISABLE = false
    }
}