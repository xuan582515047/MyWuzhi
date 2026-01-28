package com.wuzhi.server.service_tool.pojo.dto.toolRecord

import jakarta.validation.constraints.NotBlank

class ToolRecordEditDTO(

    @NotBlank(message = "工具id不能为空")
    val toolId: String,

    val companyId: String? = null,
    val detail: String? = null,
) {
}