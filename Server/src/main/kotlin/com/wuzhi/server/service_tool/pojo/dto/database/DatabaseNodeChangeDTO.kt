package com.wuzhi.server.service_tool.pojo.dto.database

import jakarta.validation.constraints.NotBlank

class DatabaseNodeChangeDTO(
    @NotBlank(message = "节点ID不能为空")
    val nodeId: String,

    val parentId: String?, // null 表示移动到根节点

    @NotBlank(message = "公司ID不能为空")
    val companyId: String
)
