package com.wuzhi.server.service_tool.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler
import java.math.BigDecimal
import java.time.LocalDateTime

@TableName("data_tool")
class Tool(

    //================== 基础信息 ==================

    @TableId("id")
    val id: String? = null,

    @TableField("name")
    val name: String,

    @TableField("description")
    val description: String,

    @TableField("price")
    val price: BigDecimal,

    @TableField("url")
    val url: String,

    //================== 统计信息 ==================

    @TableField("view_count")
    val viewCount: Int,

    @TableField("use_count")
    val useCount: Int,

    @TableField("rating")
    val rating: Int,

    //================== 开发者信息 ==================

    @TableField("version")
    val version: String,

    @TableField("author_user_id")
    val authorUserId: String,

    @TableField("type")
    val type: String,

    @TableField("create_time")
    val createTime: LocalDateTime,

    @TableField("update_time")
    val updateTime: LocalDateTime,

    //================== 状态信息 ==================

    @TableField("status")
    val status: String,

    @TableField("audit_status")
    val auditStatus: String,

    //================== 标签信息 ==================
    //TODO 改成List<String>

    @TableField("tag_ids")
    val tagIds: String,

    @TableField("keywords")
    val keywords: String,

    ) {
    companion object{
        // 私有
        const val STATUS_PRIVATE = "private"
        // 公开
        const val STATUS_PUBLIC = "public"

        // 未审核
        const val AUDIT_STATUS_NONE = "none"
        // 审核中
        const val AUDIT_STATUS_AUDITING = "auditing"
        // 审核通过
        const val AUDIT_STATUS_PASSED = "audit_passed"
        // 审核失败
        const val AUDIT_STATUS_FAILED = "audit_failed"

    }
}