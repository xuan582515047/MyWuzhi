package com.wuzhi.server.service_tool.pojo.dto.toolNode

import jakarta.validation.constraints.NotBlank

class ToolNodeEditDTO(

    @NotBlank(message = "工具节点ID不能为空")
    val id: String,

    @NotBlank(message = "工具节点名称不能为空")
    val name: String,

) {
}