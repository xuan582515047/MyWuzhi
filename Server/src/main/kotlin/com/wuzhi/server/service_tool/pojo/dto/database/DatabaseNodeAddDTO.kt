package com.wuzhi.server.service_tool.pojo.dto.database

import jakarta.validation.constraints.NotBlank

class DatabaseNodeAddDTO(
    val companyId: String? = null,

    @NotBlank(message = "名称不能为空")
    var name: String,

    var parentId: String? = null,
) {

}