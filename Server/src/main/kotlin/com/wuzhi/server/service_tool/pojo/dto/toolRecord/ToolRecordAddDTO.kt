package com.wuzhi.server.service_tool.pojo.dto.toolRecord

class ToolRecordAddDTO(
    val toolId: String,
    val companyId: String?,  // 改为可空类型，支持不绑定公司
    val detail: String? = null,
) {
}