package com.wuzhi.server.service_tool.pojo.dto.database

import jakarta.validation.constraints.NotBlank

class DatabaseEditDTO(

    @NotBlank(message = "数据库id不能为空")
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val text: String? = null
) {
}