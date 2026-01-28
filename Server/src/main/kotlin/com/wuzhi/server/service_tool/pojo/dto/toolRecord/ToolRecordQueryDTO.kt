package com.wuzhi.server.service_tool.pojo.dto.toolRecord

class ToolRecordQueryDTO(
    val toolId: String,
    val companyId: String? = null,
    val startTime: String,
    val endTime: String,
    val isAsc: Boolean,
    val pageNum: Long,
    val pageSize: Long
) {

}