package com.wuzhi.server.service_tool.pojo.dto.tool

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

class ToolEditDTO(

    @NotBlank(message = "工具id不能为空")
    val id: String,

    val name: String? = null,

    val description: String? = null,

    val price: String? = null,

    val url: String? = null,

    val version: String? = null,

    val type: String? = null,

    val tagIds: List<String>? = null,

    val keywords: List<String>? = null,

    val updateTime: LocalDateTime? = null,
) {
}