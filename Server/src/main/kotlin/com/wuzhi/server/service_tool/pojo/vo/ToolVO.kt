package com.wuzhi.server.service_tool.pojo.vo

import com.wuzhi.server.service_tool.pojo.po.Tool
import java.math.BigDecimal
import java.time.LocalDateTime

class ToolVO(
    val id: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val url: String,
    val version: String,
    val viewCount: Int,
    val useCount: Int,
    val rating: Int,
    val type: String,
    val authorUserId: String,
    val tagIds: List<String>,
    val keywords: List<String>,
    val createTime: LocalDateTime,
    val updateTime: LocalDateTime,
){
    companion object{
        fun fromTool(tool: Tool): ToolVO{
            return ToolVO(
                id = tool.id!!,
                name = tool.name,
                description = tool.description,
                price = tool.price,
                url = tool.url,
                version = tool.version,
                viewCount = tool.viewCount,
                useCount = tool.useCount,
                rating = tool.rating,
                type = tool.type,
                authorUserId = tool.authorUserId,
                tagIds = tool.tagIds.split(",").filter { it.isNotBlank() },
                keywords = tool.keywords.split(",").filter { it.isNotBlank() },
                createTime = tool.createTime,
                updateTime = tool.updateTime,
            )
        }
    }
}