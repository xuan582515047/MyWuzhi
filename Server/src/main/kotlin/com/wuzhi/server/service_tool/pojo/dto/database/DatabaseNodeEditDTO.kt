package com.wuzhi.server.service_tool.pojo.dto.database

import jakarta.validation.constraints.NotBlank

class DatabaseNodeEditDTO(

    @NotBlank(message = "id不能为空")
    val id: String,

    val name: String? = null,
) {
}