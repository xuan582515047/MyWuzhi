package com.wuzhi.server.service_tool.pojo.dto.tool

import jakarta.validation.constraints.NotBlank

class ToolUseAddDTO(

    @NotBlank(message = "工具id不能为空")
    val toolId: String,
    val companyId: String? = null,
    val name: String? = null,
    val parentId: String? = null,
) {
}