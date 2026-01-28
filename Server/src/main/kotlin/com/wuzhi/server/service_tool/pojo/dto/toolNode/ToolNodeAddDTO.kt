package com.wuzhi.server.service_tool.pojo.dto.toolNode

import jakarta.validation.constraints.NotBlank

class ToolNodeAddDTO(
    val companyId: String? = null,

    @NotBlank(message = "工具节点名称不能为空")
    val name: String,

    val parentId: String? = null
) {
}