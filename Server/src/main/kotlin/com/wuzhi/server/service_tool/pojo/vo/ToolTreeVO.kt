package com.wuzhi.server.service_tool.pojo.vo

class ToolTreeVO(
    val id: String,
    val name: String,
    val url: String? = null,
    val type: String,
    val status: Boolean,
    val children: List<ToolTreeVO>? = null
) {
}