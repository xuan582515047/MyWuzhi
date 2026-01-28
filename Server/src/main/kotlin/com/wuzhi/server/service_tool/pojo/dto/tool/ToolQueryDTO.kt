package com.wuzhi.server.service_tool.pojo.dto.tool

class ToolQueryDTO(
    var query: String? = null,
    var type: String? = null,
    var tagIds: String? = null,    // 改为字符串类型
    var minPrice: String? = null,
    var maxPrice: String? = null,
    val sortBy: String? = null,
    val isAsc: Boolean,
    val page: Long,
    val pageSize: Long
) {
}